## 前言

上文介绍了[Kubernetes副本机制](https://my.oschina.net/OutOfMemory/blog/3147325),正是因为副本机制你的部署能自动保待运行，并且保持健康，无须任何手动干预；本文继续介绍kubernetes的另一个强大的功能**服务**，在客户端和pod之间提供一个服务层，提供了单一的接入点，更加方便客户端使用pod。

## 服务

Kubernetes服务是一种为一组功能相同的pod提供单一不变的接入点的资源；当服务存在时，它的IP地址和端口不会改变，客户端通过IP地址和端口号建立连接，这些连接会被路由到提供该服务的任意一个pod上；

### 1.创建服务

服务的连接对所有的后端pod是负载均衡的，至于哪些pod被属于哪个服务，通过在定义服务的时候设置标签选择器；

```
[d:\k8s]$ kubectl create -f kubia-rc.yaml
replicationcontroller/kubia created

[d:\k8s]$ kubectl get pod
NAME          READY   STATUS              RESTARTS   AGE
kubia-6dxn7   0/1     ContainerCreating   0          4s
kubia-fhxht   0/1     ContainerCreating   0          4s
kubia-fpvc7   0/1     ContainerCreating   0          4s
```

使用之前的yaml文件创建pod，模版中设置的标签为**app: kubia**，所以创建服务的yaml（还有之前介绍的**kubectl expose**方式也可以创建服务）中也需要指定相同的标签：

```
apiVersion: v1
kind: Service
metadata:
  name: kubia
spec:
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: kubia
```

首先指定的资源类型为Service，然后指定了两个端口分别：port服务提供的端口，targetPort指定pod中进程监听的端口，最后指定标签选择器，相同标签的pod被当前服务管理；

```
[d:\k8s]$ kubectl create -f kubia-svc.yaml
service/kubia created

[d:\k8s]$ kubectl get svc
NAME         TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)   AGE
kubernetes   ClusterIP   10.96.0.1       <none>        443/TCP   6d15h
kubia        ClusterIP   10.96.191.193   <none>        80/TCP    4s

[d:\k8s]$ kubectl exec kubia-6dxn7 -- curl -s http://10.96.191.193
You've hit kubia-fhxht

[d:\k8s]$ kubectl exec kubia-6dxn7 -- curl -s http://10.96.191.193
You've hit kubia-fpvc7
```

创建完服务之后，可以发现给kubia分配了CLUSTER-IP，这是一个内部ip；至于如何测试可以使用kubectl exec命令远程地在一个已经存在的pod容器上执行任何命令；pod名称可以随意指定三个中的任何一个，接收到crul命令的pod，会转发给Service，由Service来决定将请求交给哪个pod处理，所以可以看到多次执行，发现每次处理的pod都不一样；如果希望特定客户端产生的所有请求每次都指向同一个pod, 可以设置服务的sessionAffinity属性为ClientIP；

#### 1.1配置会话黏性

```
apiVersion: v1
kind: Service
metadata:
  name: kubia
spec:
  sessionAffinity: ClientIP
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: kubia
```

除了添加了sessionAffinity: ClientIP，其他都一样

```
[d:\k8s]$ kubectl delete svc kubia
service "kubia" deleted

[d:\k8s]$ kubectl create -f kubia-svc-client-ip-session-affinity.yaml
service/kubia created

[d:\k8s]$ kubectl get svc
NAME         TYPE        CLUSTER-IP    EXTERNAL-IP   PORT(S)   AGE
kubernetes   ClusterIP   10.96.0.1     <none>        443/TCP   6d15h
kubia        ClusterIP   10.96.51.99   <none>        80/TCP    25s

[d:\k8s]$ kubectl exec kubia-6dxn7 -- curl -s http://10.96.51.99
You've hit kubia-fhxht

[d:\k8s]$ kubectl exec kubia-6dxn7 -- curl -s http://10.96.51.99
You've hit kubia-fhxht
```

#### 1.2 同一个服务暴露多个端口

如果pod监听了两个或者多个端口，那么服务同样可以暴露多个端口：

```
apiVersion: v1
kind: Service
metadata:
  name: kubia
spec:
  ports:
  - name: http
    port: 80
    targetPort: 8080
  - name: https
    port: 443
    targetPort: 8080
  selector:
    app: kubia
```

因为Node.js只监听了8080一个端口，所以这里在Service里面配置两个端口都指向同一个目标端口，看是否都能访问：

```
[d:\k8s]$ kubectl create -f kubia-svc-named-ports.yaml
service/kubia created

[d:\k8s]$ kubectl get svc
NAME         TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)          AGE
kubernetes   ClusterIP   10.96.0.1      <none>        443/TCP          6d18h
kubia        ClusterIP   10.96.13.178   <none>        80/TCP,443/TCP   7s

[d:\k8s]$ kubectl exec kubia-6dxn7 -- curl -s http://10.96.13.178
You've hit kubia-fpvc7

[d:\k8s]$ kubectl exec kubia-6dxn7 -- curl -s http://10.96.13.178:443
You've hit kubia-fpvc7
```

可以发现使用两个端口都可以访问；

#### 1.3 使用命名的端口

在Service中指定了端口为8080，如果目标端口变了这里也需要改变，可以在定义pod的模版中给端口命名，在Service中可以直接指定名称:

```
apiVersion: v1
kind: ReplicationController
metadata: 
   name: kubia
spec: 
   replicas: 3
   selector: 
      app: kubia
   template:
      metadata: 
         labels:
            app: kubia
      spec: 
         containers: 
         - name: kubia
           image: ksfzhaohui/kubia
           ports: 
           - name: http
             containerPort: 8080
```

在之前的ReplicationController中稍作修改，在port是中指定了名称，Service的yaml文件同样做修改，直接使用名称：

```
apiVersion: v1
kind: Service
metadata:
  name: kubia
spec:
  ports:
  - port: 80
    targetPort: http
  selector:
    app: kubia
```

targetPort直接使用了名称http：

```
[d:\k8s]$ kubectl create -f kubia-rc2.yaml
replicationcontroller/kubia created

[d:\k8s]$ kubectl get pod
NAME          READY   STATUS    RESTARTS   AGE
kubia-4m9nv   1/1     Running   0          66s
kubia-bm6rx   1/1     Running   0          66s
kubia-dh87r   1/1     Running   0          66s

[d:\k8s]$ kubectl create -f kubia-svc2.yaml
service/kubia created

[d:\k8s]$ kubectl get svc
NAME         TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)   AGE
kubernetes   ClusterIP   10.96.0.1      <none>        443/TCP   7d
kubia        ClusterIP   10.96.106.37   <none>        80/TCP    10s

[d:\k8s]$ kubectl exec kubia-4m9nv -- curl -s http://10.96.106.37
You've hit kubia-dh87r
```

### 2.服务发现

服务给我们提供了一个单一不变的ip去访问pod，那是否每次都要先创建服务，然后找到服务的CLUSTER-IP，再给其他pod去使用；这样就太麻烦了，Kubernets还提供了其他方式去访问服务；

#### 2.1 通过环境变量发现服务

在pod开始运行的时候，Kubernets会初始化一系列的环境变量指向现在存在的服务；如果创建的服务早于客户端pod的创建，pod上的进程可以根据环境变量获得服务的IP地址和端口号；

```
[d:\k8s]$ kubectl get svc
NAME         TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)   AGE
kubernetes   ClusterIP   10.96.0.1      <none>        443/TCP   7d14h
kubia        ClusterIP   10.96.106.37   <none>        80/TCP    14h

[d:\k8s]$ kubectl get pod
NAME          READY   STATUS    RESTARTS   AGE
kubia-4m9nv   1/1     Running   0          14h
kubia-bm6rx   1/1     Running   0          14h
kubia-dh87r   1/1     Running   0          14h

[d:\k8s]$ kubectl exec kubia-4m9nv env
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
HOSTNAME=kubia-4m9nv
KUBERNETES_SERVICE_PORT_HTTPS=443
KUBERNETES_PORT=tcp://10.96.0.1:443
KUBERNETES_PORT_443_TCP=tcp://10.96.0.1:443
KUBERNETES_PORT_443_TCP_PROTO=tcp
KUBERNETES_PORT_443_TCP_PORT=443
KUBERNETES_PORT_443_TCP_ADDR=10.96.0.1
KUBERNETES_SERVICE_HOST=10.96.0.1
KUBERNETES_SERVICE_PORT=443
NPM_CONFIG_LOGLEVEL=info
NODE_VERSION=7.10.1
YARN_VERSION=0.24.4
HOME=/root
```

因为这里的pod早于服务的创建，所有没有相关服务的相关信息：

```
[d:\k8s]$ kubectl delete po --all
pod "kubia-4m9nv" deleted
pod "kubia-bm6rx" deleted
pod "kubia-dh87r" deleted

[d:\k8s]$ kubectl get pod
NAME          READY   STATUS    RESTARTS   AGE
kubia-599v9   1/1     Running   0          48s
kubia-8s8j4   1/1     Running   0          48s
kubia-dm6kr   1/1     Running   0          48s

[d:\k8s]$ kubectl exec kubia-599v9 env
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
HOSTNAME=kubia-599v9
...
KUBIA_SERVICE_HOST=10.96.106.37
KUBIA_SERVICE_PORT=80
...
```

如果删除pod重新创建新的pod，这样服务就在创建pod之前了，再次获取环境变量可以发现有KUBIA\_SERVICE\_HOST和KUBIA\_SERVICE\_PORT，分别代表了kubia服务的IP地址和端口号；这样就可以通过环境变量去获取IP和端口了；

#### 2.2 通过DNS发现服务

命名空间kube-system下有一个默认的服务kube-dns，其后端是一个coredns的pod：

```
[d:\k8s]$ kubectl get svc --namespace kube-system
NAME       TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)                  AGE
kube-dns   ClusterIP   10.96.0.10   <none>        53/UDP,53/TCP,9153/TCP   9d

[d:\k8s]$ kubectl get po -o wide --namespace kube-system
NAME                               READY   STATUS    RESTARTS   AGE   IP               NODE       NOMINATED NODE   READINESS GATES
coredns-7f9c544f75-h2cwn           1/1     Running   0          9d    172.17.0.3       minikube   <none>           <none>
coredns-7f9c544f75-x2ttk           1/1     Running   0          9d    172.17.0.2       minikube   <none>           <none>
```

运行在pod上的进程DNS查询都会被Kubernets自身的DNS服务器响应，该服务器知道系统中运行的所有服务；客户端的pod在知道服务名称的情况下可以通过全限定域名(FQDN)来访问

```
[d:\k8s]$ kubectl exec kubia-599v9 -- curl -s http://kubia.default.svc.cluster.local
You've hit kubia-8s8j4
```

kubia对应服务名称，default为服务所在的命名空间，svc.cluster.local是在所有集群本地服务名称中使用的可配置集群域后缀；如果两个pod在同一个命名空间下，可以省略svc.cluster.local和default，使用服务名即可：

```
[d:\k8s]$ kubectl exec kubia-599v9 -- curl -s http://kubia.default
You've hit kubia-dm6kr

[d:\k8s]$ kubectl exec kubia-599v9 -- curl -s http://kubia
You've hit kubia-dm6kr
```

#### 2.3 在pod中运行shell

```
d:\k8s>winpty kubectl exec -it kubia-599v9 -- sh
# curl -s http://kubia
You've hit kubia-dm6kr
# exit
```

通过kubectl exec命令在一个pod容器上运行bash，这样就无须为每个要运行的命令执行kubectl exec命令；因为在windows环境下使用了winpty工具；

## 连接集群外部的服务

以上介绍的后端是集群中运行的一个或多个pod的服务；但也存在希望通过Kubernetes服务特性暴露外部服务的情况，可以通过Endpoint方式和外部服务别名的方式；

### 1.Endpoint

服务并不是和pod直接相连的；有一种资源介于两者之间：它就是Endpoint资源

```
[d:\k8s]$ kubectl describe svc kubia
Name:              kubia
Namespace:         default
Labels:            <none>
Annotations:       <none>
Selector:          app=kubia
Type:              ClusterIP
IP:                10.96.106.37
Port:              <unset>  80/TCP
TargetPort:        http/TCP
Endpoints:         172.17.0.10:8080,172.17.0.11:8080,172.17.0.9:8080
Session Affinity:  None
Events:            <none>

[d:\k8s]$ kubectl get pod -o wide
NAME          READY   STATUS    RESTARTS   AGE     IP            NODE       NOMINATED NODE   READINESS GATES
kubia-599v9   1/1     Running   0          3h51m   172.17.0.10   minikube   <none>           <none>
kubia-8s8j4   1/1     Running   0          3h51m   172.17.0.11   minikube   <none>           <none>
kubia-dm6kr   1/1     Running   0          3h51m   172.17.0.9    minikube   <none>           <none>
```

可以看到Endpoints对应其实就是pod的IP和端口；当客户端连接到服务时，服务代理选择这些IP和端口对中的一个，并将传入连接重定向到在该位置监听的服务器；

### 2.手动配置服务的endpoint（内部）

如果创建了不包含pod选择器的服务，Kubernetes将不会创建Endpoint资源；这样就需要创建Endpoint资源来指定该服务的Endpoint列表；

```
apiVersion: v1
kind: Service
metadata:
  name: external-service
spec:
  ports:
  - port: 80
```

如上定义没有指定selector选择器：

```
[d:\k8s]$ kubectl create -f external-service.yaml
service/external-service created

[d:\k8s]$ kubectl get svc external-service
NAME               TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)   AGE
external-service   ClusterIP   10.96.241.116   <none>        80/TCP    74s

[d:\k8s]$ kubectl describe svc external-service
Name:              external-service
Namespace:         default
Labels:            <none>
Annotations:       <none>
Selector:          <none>
Type:              ClusterIP
IP:                10.96.241.116
Port:              <unset>  80/TCP
TargetPort:        80/TCP
Endpoints:         <none>
Session Affinity:  None
Events:            <none>
```

可以发现因为没有指定selector选择器，external-service的Endpoints为none，这种情况可以手动配置服务的Endpoint；

```
apiVersion: v1
kind: Endpoints
metadata:
  name: external-service
subsets:
  - addresses:
    - ip: 172.17.0.9
    - ip: 172.17.0.10
    ports:
    - port: 8080
```

Endpoint对象需要与服务具有相同的名称，并包含该服务的目标IP地址和端口列表：

```
[d:\k8s]$ kubectl create -f external-service-endpoints.yaml
endpoints/external-service created

[d:\k8s]$ kubectl describe svc external-service
Name:              external-service
Namespace:         default
Labels:            <none>
Annotations:       <none>
Selector:          <none>
Type:              ClusterIP
IP:                10.96.241.116
Port:              <unset>  80/TCP
TargetPort:        80/TCP
Endpoints:         172.17.0.10:8080,172.17.0.9:8080
Session Affinity:  None
Events:            <none>

[d:\k8s]$ kubectl exec kubia-599v9 -- curl -s http://external-service
You've hit kubia-dm6kr
```

可以发现再创建完Endpoints之后，服务external-service的Endpoints中多了pod的ip地址和端口，同样也可以通过kubectl exec执行请求；

### 3.手动配置服务的endpoint（外部）

以上在endpoint配置的是kubernetes内部的ip端口，同样也可以配置外部的ip端口，在kubernetes外部启动一个服务：

```
apiVersion: v1
kind: Endpoints
metadata:
  name: external-service
subsets:
  - addresses:
    - ip: 10.13.82.21
    ports:
    - port: 8080
```

以上配置的10.13.82.21:8080就是一个普通的tomcat服务，在本机启动即可

```
[d:\k8s]$ kubectl create -f external-service-endpoints2.yaml
endpoints/external-service created

[d:\k8s]$ kubectl create -f external-service.yaml
service/external-service created

[d:\k8s]$ kubectl exec kubia-599v9 -- curl -s http://external-service
ok
```

经测试可以返回外部服务的响应

### 4.创建外部服务别名

除了手动配置服务的Endpoint来代替公开外部服务方法，还可以通过给外部服务指定一个别名，比如给10.13.82.21指定一个域名：api.ksfzhaohui.com

```
apiVersion: v1
kind: Service
metadata:
  name: external-service
spec:
  type: ExternalName
  externalName: api.ksfzhaohui.com
  ports:
  - port: 80
```

要创建一个具有别名的外部服务的服务时，要将创建服务资源的一个type字段设置为ExternalName；在externalName中指定外服服务的域名：

```
[d:\k8s]$ kubectl create -f external-service-externalname.yaml
service/external-service created

[d:\k8s]$ kubectl exec kubia-599v9 -- curl -s http://external-service:8080
ok
```

经测试可以返回外部服务的响应

## 将服务暴露给外部客户端

向外部公开某些服务，kubernetes提供了三种方式：NodePort服务，LoadBalance服务以及Ingress资源方式，下面分别介绍及实战；

### 1.NodePort类型的服务

创建一个服务并将其类型设置为NodePort，通过创建NodePort服务，可以让kubernetes在其所有节点上保留一个端口（所有节点上都使用相同的端口号），然后将传入的连接转发给pod；

```
apiVersion: v1
kind: Service
metadata:
  name: kubia-nodeport
spec:
  type: NodePort
  ports:
  - port: 80
    targetPort: 8080
    nodePort: 30123
  selector:
    app: kubia
```

指定服务类型为NodePort，节点端口为30123；

```
d:\k8s]$ kubectl create -f kubia-svc-nodeport.yaml
service/kubia-nodeport created

[d:\k8s]$ kubectl get svc
NAME             TYPE        CLUSTER-IP    EXTERNAL-IP   PORT(S)        AGE
kubernetes       ClusterIP   10.96.0.1     <none>        443/TCP        31d
kubia-nodeport   NodePort    10.96.59.16   <none>        80:30123/TCP   3s

[d:\k8s]$ kubectl exec kubia-7fs6m -- curl -s http://10.96.59.16
You've hit kubia-m487j
```

要外部可以访问内部pod服务，需要知道节点的IP，我们这里使用的节点为minikube，因为这里的minikube是安装在本地windows系统下，可以直接使用minikube的内部ip进行访问

```
d:\k8s]$ kubectl get nodes -o wide
NAME       STATUS   ROLES    AGE   VERSION   INTERNAL-IP      EXTERNAL-IP   OS-IMAGE              KERNEL-VERSION   CONTAINER-RUNTIME
minikube   Ready    master   34d   v1.17.0   192.168.99.108   <none>        Buildroot 2019.02.7   4.19.81          docker://19.3.5
```

![](https://oscimg.oschina.net/oscnet/up-5e85cb7ccfa15a14d2ce94c4bb6c6524e7d.png)

### 2.LoadBalance类型服务

相比NodePort方式可以通过任何节点的30312端口访问内部的pod，LoadBalance方式拥有自己独一无二的可公开访问的IP地址；LoadBalance其实是NodePort的一种扩展，使得服务可以通过一个专用的负载均衡器来访问；

```
apiVersion: v1
kind: Service
metadata:
  name: kubia-loadbalancer
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: kubia
```

指定服务类型为LoadBalancer，无需指定节点端口；

```
d:\k8s]$ kubectl create -f kubia-svc-loadbalancer.yaml
service/kubia-loadbalancer created

[d:\k8s]$ kubectl get svc
NAME                 TYPE           CLUSTER-IP      EXTERNAL-IP   PORT(S)        AGE
kubernetes           ClusterIP      10.96.0.1       <none>        443/TCP        31d
kubia-loadbalancer   LoadBalancer   10.96.207.113   <pending>     80:30038/TCP   7s
kubia-nodeport       NodePort       10.96.59.16     <none>        80:30123/TCP   32m
```

可以看到虽然我们没有指定节点端口，但是创建完之后自动启动了30038节点端口  
![](https://oscimg.oschina.net/oscnet/up-41ee73ade89baecd312ff568468182f56d2.png)  
所以可以发现同样能通过使用NodePort的方式来访问服务（节点IP+节点端口）；同时也可以通过EXTERNAL-IP来访问，但是使用Minikube，就不会有外部IP地址，外部IP地址将会一直是pending状态；

### 3.了解并防止不必要的网络跳数

当外部客户端通过节点端口连接到服务时，随机选择的pod并不一定在接收连接的同一节点上运行；可以通过将服务配置为仅将外部通信重定向到接收连接的节点上运行的pod来阻止此额外跳数；

```
apiVersion: v1
kind: Service
metadata:
  name: kubia-nodeport-onlylocal
spec:
  type: NodePort
  externalTrafficPolicy: Local
  ports:
  - port: 80
    targetPort: 8080
    nodePort: 30124
  selector:
    app: kubia
```

通过在服务的spec部分中设置externalTrafficPolicy字段来完成；

### 4.Ingress类型服务

每个LoadBalancer服务都需要自己的负载均衡器，以及独有的公有IP地址；而Ingress 只需要一个公网IP就能为许多服务提供访问；当客户端向Ingress发送HTTP请求时，Ingress会根据请求的主机名和路径转发到对应的服务；

#### 4.1 Ingress控制器

只有Ingress控制器在集群中运行，Ingress资源才能正常工作；不同的Kubernetes环境使用不同的控制器实现，但有些并不提供默认控制器；我这里使用的Minikube需要启用附加组件才可以使用控制器；

```
[d:\Program Files\Kubernetes\Minikube]$ minikube addons list
- addon-manager: enabled
- dashboard: enabled
- default-storageclass: enabled
- efk: disabled
- freshpod: disabled
- gvisor: disabled
- helm-tiller: disabled
- ingress: disabled
- ingress-dns: disabled
- logviewer: disabled
- metrics-server: disabled
- nvidia-driver-installer: disabled
- nvidia-gpu-device-plugin: disabled
- registry: disabled
- registry-creds: disabled
- storage-provisioner: enabled
- storage-provisioner-gluster: disabled
```

列出所有的附件组件，可以看到ingress是不可用的，所以需要开启

```
[d:\Program Files\Kubernetes\Minikube]$ minikube addons enable ingress
* ingress was successfully enabled
```

启动之后可以查看kube-system命名空间下的pod

```
[d:\k8s]$ kubectl get pods -n kube-system
NAME                                        READY   STATUS              RESTARTS   AGE
coredns-7f9c544f75-h2cwn                    1/1     Running             0          55d
coredns-7f9c544f75-x2ttk                    1/1     Running             0          55d
etcd-minikube                               1/1     Running             0          55d
kube-addon-manager-minikube                 1/1     Running             0          55d
kube-apiserver-minikube                     1/1     Running             0          55d
kube-controller-manager-minikube            1/1     Running             2          55d
kube-proxy-xtbc4                            1/1     Running             0          55d
kube-scheduler-minikube                     1/1     Running             2          55d
nginx-ingress-controller-6fc5bcc8c9-nvcb5   0/1     ContainerCreating   0          8s
storage-provisioner                         1/1     Running             0          55d
```

可以发现正在创建一个名称为nginx-ingress-controller的pod，会一直停留在拉取镜像状态，并显示如下错误：

```
Failed to pull image "quay.io/kubernetes-ingress-controller/nginx-ingress-controller:0.26.1": rpc error: code = Unknown desc = context canceled
```

这是因为国内无法下载quay.io下面的镜像，可以使用阿里云镜像：

```
image: registry.aliyuncs.com/google_containers/nginx-ingress-controller:0.26.1
```

可以从[ingress-nginx](https://github.com/kubernetes/ingress-nginx)下的deploy/static/mandatory.yaml文件修改其中的镜像为阿里云镜像，然后重新创建即可：

```
[d:\k8s]$ kubectl create -f mandatory.yaml
namespace/ingress-nginx created
configmap/nginx-configuration created
configmap/tcp-services created
configmap/udp-services created
serviceaccount/nginx-ingress-serviceaccount created
clusterrole.rbac.authorization.k8s.io/nginx-ingress-clusterrole created
role.rbac.authorization.k8s.io/nginx-ingress-role created
rolebinding.rbac.authorization.k8s.io/nginx-ingress-role-nisa-binding created
clusterrolebinding.rbac.authorization.k8s.io/nginx-ingress-clusterrole-nisa-binding created
deployment.apps/nginx-ingress-controller created
```

再次查看kube-system命名空间下的pod

```
[d:\k8s]$ kubectl get pods -n kube-system
NAME                                        READY   STATUS    RESTARTS   AGE
coredns-7f9c544f75-h2cwn                    1/1     Running   0          56d
coredns-7f9c544f75-x2ttk                    1/1     Running   0          56d
etcd-minikube                               1/1     Running   0          56d
kube-addon-manager-minikube                 1/1     Running   0          56d
kube-apiserver-minikube                     1/1     Running   0          56d
kube-controller-manager-minikube            1/1     Running   2          56d
kube-proxy-xtbc4                            1/1     Running   0          56d
kube-scheduler-minikube                     1/1     Running   2          56d
nginx-ingress-controller-6fc5bcc8c9-nvcb5   1/1     Running   0          10m
storage-provisioner                         1/1     Running   0          56d
```

nginx-ingress-controller已经为Running状态，下面就可以使用Ingress资源了；

#### 4.2 Ingress资源

Ingress控制器启动之后，就可以创建Ingress资源了

```
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: kubia
spec:
  rules:
  - host: kubia.example.com
    http:
      paths:
      - path: /
        backend:
          serviceName: kubia-nodeport
          servicePort: 80
```

指定资源类型为Ingress，定一个单一规则，所有发送kubia.example.com的请求都会被转发给端口为80的kubia-nodeport服务上；

```
[d:\k8s]$ kubectl get svc
NAME             TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)        AGE
kubernetes       ClusterIP   10.96.0.1       <none>        443/TCP        53d
kubia-nodeport   NodePort    10.96.204.104   <none>        80:30123/TCP   21h

[d:\k8s]$ kubectl create -f kubia-ingress.yaml
ingress.extensions/kubia created

[d:\k8s]$ kubectl get ingress
NAME    HOSTS               ADDRESS          PORTS   AGE
kubia   kubia.example.com   192.168.99.108   80      6m4s
```

需要把域名映射到ADDRESS:192.168.99.108，修改hosts文件即可，下面就可以直接用域名访问了，最终请求会被转发到kubia-nodeport服务  
![](https://oscimg.oschina.net/oscnet/up-713668ad1126027bc1e2f35070d8fcffb57.png)

**大致请求流程如下**：浏览器中请求域名首先会查询域名服务器，然后DNS返回了控制器的IP地址；客户端向控制器发送请求并在头部指定了kubia.example.com；然后控制器根据头部信息确定客户端需要访问哪个服务；然后通过服务关联的Endpoint对象查看pod IP，并将请求转发给其中一个；

#### 4.3 Ingress暴露多个服务

rules和paths是数组，可以配置多个

```
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: kubia2
spec:
  rules:
  - host: kubia.example.com
    http:
      paths:
      - path: /v1
        backend:
          serviceName: kubia-nodeport
          servicePort: 80
      - path: /v2
        backend:
          serviceName: kubia-nodeport
          servicePort: 80
  - host: kubia2.example.com
    http:
      paths:
      - path: /
        backend:
          serviceName: kubia-nodeport
          servicePort: 80
```

配置了多个host和path，这里为了方便映射了同样服务；

```
[d:\k8s]$ kubectl create -f kubia-ingress2.yaml
ingress.extensions/kubia2 created

[d:\k8s]$ kubectl get ingress
NAME     HOSTS                                  ADDRESS          PORTS   AGE
kubia    kubia.example.com                      192.168.99.108   80      41m
kubia2   kubia.example.com,kubia2.example.com   192.168.99.108   80      15m
```

同样需要配置host文件，测试如下：  
![](https://oscimg.oschina.net/oscnet/up-bd1e8363555ccd405089dce3bd3e1219825.png)

![](https://oscimg.oschina.net/oscnet/up-c4c402b64f608ecb39524294ec2b299c80a.png)

![](https://oscimg.oschina.net/oscnet/up-c6a29b9a53b2371c5832fb3aa6d4a6b470b.png)

#### 4.4 配置Ingress处理TLS传输

以上介绍的消息都是基于Http协议，Https协议需要配置相关证书；客户端创建到Ingress控制器的TLS连接时，控制器将终止TLS连接；客户端与Ingress控制器之间是加密的，而Ingress控制器和pod之间没有加密；要使控制器可以这样，需要将证书和私钥附加到Ingress中；

```
[root@localhost batck-job]# openssl genrsa -out tls.key 2048
Generating RSA private key, 2048 bit long modulus
..................................................................+++
........................+++
e is 65537 (0x10001)
[root@localhost batck-job]# openssl req -new -x509 -key tls.key -out tls.cert -days 360 -subj /CN=kubia.example.com

[root@localhost batck-job]# ll
-rw-r--r--. 1 root root 1115 Feb 11 01:20 tls.cert
-rw-r--r--. 1 root root 1679 Feb 11 01:20 tls.key
```

生成的两个文件创建secret

```
[d:\k8s]$ kubectl create secret tls tls-secret --cert=tls.cert --key=tls.key
secret/tls-secret created
```

现在可以更新Ingress对象，以便它也接收kubia.example.com的HTTPS请求；

```
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: kubia
spec:
  tls:
  - hosts: 
    - kubia.example.com
    secretName: tls-secret
  rules:
  - host: kubia.example.com
    http:
      paths:
      - path: /
        backend:
          serviceName: kubia-nodeport
          servicePort: 80
```

tls中指定相关证书

```
[d:\k8s]$ kubectl apply -f kubia-ingress-tls.yaml
Warning: kubectl apply should be used on resource created by either kubectl create --save-config or kubectl apply
ingress.extensions/kubia configured
```

通过浏览器访问https协议，如下图所示  
![](https://oscimg.oschina.net/oscnet/up-068adf47131466e28e303511c0a88e7a73a.png)

## Pod就绪信号

只要pod的标签和服务的pod选择器想匹配，pod就可以作为服务的后端，但是如果pod没有准备好，是不能处理请求的，这时候就需要就绪探针了，用来检查pod是否已经准备好了，如果检查成功就可以作为服务的后端处理消息了；

### 1.就绪探针类型

就绪探针有三种类型分别：

-   Exec探针：执行进程的地方，容器的状态由进程的退出状态代码确认；
-   Http get探针：向容器发送HTTP GET请求，通过响应的HTTP状态代码判断容器是否准备好；
-   Tcp socket探针：它打开一个TCP连接到容器的指定端口，如果连接己建立，则认为容器己准备就绪。

kubernetes会周期性地调用探针，并根据就绪探针的结果采取行动。如果某个pod报告它尚未准备就绪，则会从该服务中删除该pod。如果pod再次准备就绪，则重新添加pod；

### 2.向pod添加就行探针

编辑ReplicationController，修改pod模版添加就绪探针

```
[d:\k8s]$ kubectl edit rc kubia
libpng warning: iCCP: known incorrect sRGB profile
replicationcontroller/kubia edited

[d:\k8s]$ kubectl get pods
NAME          READY   STATUS    RESTARTS   AGE
kubia-7fs6m   1/1     Running   0          22d
kubia-m487j   1/1     Running   0          22d
kubia-q6z5w   1/1     Running   0          22d
```

编辑ReplicationController如下所示，添加readinessProbe

```
apiVersion: v1
kind: ReplicationController
metadata: 
   name: kubia
spec: 
   replicas: 3
   selector: 
      app: kubia
   template:
      metadata: 
         labels:
            app: kubia
      spec: 
         containers: 
         - name: kubia
           image: ksfzhaohui/kubia
           ports: 
           - containerPort: 8080
           readinessProbe:
           exec:
            command:
            - ls
            - /var/ready
```

就绪探针将定期在容器内执行ls/var/ready命令。如果文件存在，则ls命令返回退出码 0, 否则返回非零的退出码；如果文件存在，则就绪探针将成功，否则失败；  
我们编辑完ReplicationController还没有产生新的pod所以可以发现以上pod的READY都为1，表示已经准备好可以处理消息；

```
[d:\k8s]$ kubectl delete pod kubia-m487j
pod "kubia-m487j" deleted

[d:\k8s]$ kubectl get pods
NAME          READY   STATUS    RESTARTS   AGE
kubia-7fs6m   1/1     Running   0          22d
kubia-cxz5v   0/1     Running   0          114s
kubia-q6z5w   1/1     Running   0          22d
```

删除一个pod，马上会创建一个带有就绪探针的pod，可以发现长时间READY为0；

## 总结

本文首先介绍了服务的基本知识，如何创建服务发现服务；然后介绍了服务和pod直接的关联器endpoint；最后重点介绍了将服务暴露给外部客户端的三种方式。

## 参考

Kubernetes in Action

## 博客地址

[Github](https://github.com/ksfzhaohui/blog)