## 前言

在上文[Kubernetes入门篇](https://my.oschina.net/OutOfMemory/blog/3144118)中对kubernetes有一个初步的介绍，本文继续介绍kubernetes的核心组件Pod，当然还是以实际操作为主；另外说明一点的是本系列是本人的学习笔记。

## Pod介绍

Pod是Kubernetes中最为重要的核心概念，代表了Kubernetes中的基本构建模块；一个Pod可以包含一个容器或者多个容器，Pod本身是轻量级的，所以经常会创建很多Pod，紧密相关的容器进程经常放到同一个Pod中；一个Pod中的容器运行于相同的Network命名空间中，因此它们共享相同的IP地址和端口空间；Kubernetes集群中的所有Pod都在同一个共享网络地址空间中，每个Pod都可以通过其他Pod的IP地址来实现相互访问，就像局域网上的计算机一样。

### 1.创建Pod

Pod和其他Kubernetes资源通常是通过向Kubernetes REST API提供JSON或YAML描述文件来创建的，当然还有如kubectl run类似的简单命令，只不过配置属性有限；

#### 1.1 查询Pod描述文件

```
[d:\k8s]$ kubectl get po kubia-dms8n -o yaml
apiVersion: v1
kind: Pod
metadata:
  ......
spec:
  ......
status:
  ......
```

-o指定具体的格式，可以是yaml或者json格式；描述文件主要包括apiVersion，kind，metadata，spec以及status这几块：

-   apiVersion：Kubernetes API版本；
-   kind：Kubernetes对象资源类型；
-   metadata：包括名称、命名空间、标签和关于该容器的其他信息；
-   spec：包含pod内容的实际说明，例如pod的容器、卷和其他数据；
-   status：包含运行中的pod的当前信息，例如pod所处的条件、每个容器的描述和状态，以及内部IP和其他基本信息；

#### 1.2 创建YAML描述文件

```
apiVersion: v1
kind: Pod
metadata: 
   name: kubia-manual
spec: 
   containers: 
   - image: ksfzhaohui/kubia
     name: kubia
     ports: 
     - containerPort: 8080
       protocol: TCP
```

kubia-manual为指定的pod的名称，ksfzhaohui/kubia使用的镜像，kubia为容器的名称，8080为应用监听的端口；这里的端口纯粹是展示性的，最终以容器中应用绑定的端口为准；

#### 1.3 创建pod

```
D:\k8s>kubectl create -f kubia-manual.yaml
pod/kubia-manual created
```

以上创建了一个名称为kubia-manual的pod，可以查询当前的pod列表：

```
D:\k8s>kubectl get pods
NAME           READY   STATUS    RESTARTS   AGE
kubia-dms8n    1/1     Running   0          25h
kubia-manual   1/1     Running   0          2m8s
```

kubia-manual已经准备就绪，当然也可以查询指定pod的描述文件(同1.1)；

#### 1.4 查看应用日志

```
D:\k8s>kubectl logs kubia-manual -c kubia
kubia server is starting...
```

以上表示查询Pod为kubia-manual中的容器kubia的日志（因为一个pod中可以包含多个容器），目前只有服务启动成功的日志，为了更好的观察日志变动，对容器中部署的应用发起请求，之前是通过穿件service来和pod进行通信，Kubernetes还提供了配置端口转发到pod的方式：

```
[d:\k8s]$ kubectl port-forward kubia-manual 8888:8080
Forwarding from 127.0.0.1:8888 -> 8080
Forwarding from [::1]:8888 -> 8080
```

然后在本地通过浏览器访问：[http://localhost:8888/](http://localhost:8888/)；  
![](https://oscimg.oschina.net/oscnet/up-e4025f18e3284c2e9150b424485782dbf41.png)

继续观察容器中的日志，因为Pod中只有一个容器，所以这里可以不指定具体容器

```
C:\Users\hui.zhao.cfs>kubectl logs kubia-manual
kubia server is starting...
Received request from ::ffff:127.0.0.1
Received request from ::ffff:127.0.0.1
```

### 2.Pod标签

标签是一种简单却功能强大的Kubernetes特性，不仅可以组织pod，也可以组织所有其他的Kubernetes资源；标签是可以附加到资源的任意键值对，用以选择具有该确切标签的资源；

#### 2.1 创建Pod指定标签

```
apiVersion: v1
kind: Pod
metadata: 
   name: kubia-manual-v2
   labels: 
     creation_method: manual
     env: prod
spec: 
   containers: 
   - image: ksfzhaohui/kubia
     name: kubia
     ports: 
     - containerPort: 8080
       protocol: TCP
```

在指定Pod名称kubia-manual-v2的下面指定了两个标签键值对，创建Pod：

```
D:\k8s>kubectl create -f kubia-manual-with-labels.yaml
pod/kubia-manual-v2 created
```

查询所有Pod，并显示标签：

```
[d:\k8s]$ kubectl get po --show-labels
NAME              READY   STATUS    RESTARTS   AGE   LABELS
kubia-dms8n       1/1     Running   0          26h   run=kubia
kubia-manual      1/1     Running   0          49m   <none>
kubia-manual-v2   1/1     Running   0          51s   creation_method=manual,env=prod
```

#### 2.2 修改Pod标签

```
[d:\k8s]$ kubectl label po kubia-manual creation_method=manual
pod/kubia-manual labeled

[d:\k8s]$ kubectl get po -L creation_method,env
NAME              READY   STATUS    RESTARTS   AGE   CREATION_METHOD   ENV
kubia-dms8n       1/1     Running   0          26h                     
kubia-manual      1/1     Running   0          55m   manual            
kubia-manual-v2   1/1     Running   0          7m    manual            prod  
```

kubia-manual没有指定标签，可以进行添加，同时也可以修改：

```
[d:\k8s]$ kubectl label po kubia-manual-v2 env=debug --overwrite
pod/kubia-manual-v2 labeled

[d:\k8s]$ kubectl get po -L creation_method,env
NAME              READY   STATUS    RESTARTS   AGE     CREATION_METHOD   ENV
kubia-dms8n       1/1     Running   0          26h                       
kubia-manual      1/1     Running   0          57m     manual            
kubia-manual-v2   1/1     Running   0          8m13s   manual            debug
```

在更改现有标签时，需要使用--overwrite选项；

#### 2.3 使用标签选择器列出pod

标签选择器允许我们选择标记有特定标签的pod子集，并对这些pod执行操作；

```
[d:\k8s]$ kubectl get po -l creation_method=manual
NAME              READY   STATUS    RESTARTS   AGE
kubia-manual      1/1     Running   0          137m
kubia-manual-v2   1/1     Running   0          88m
```

这里使用了creation_method=manual的标签选择器，这里可以是一个表达式，比如!=，in，notin等，多个条件用逗号分隔：

```
[d:\k8s]$ kubectl get po -l creation_method=manual,env=debug
NAME              READY   STATUS    RESTARTS   AGE
kubia-manual-v2   1/1     Running   0          98m
```

#### 2.4 使用标签和选择器来约束pod调度

Kubernetes将集群中的所有节点抽象为一个整体的大型部署平台，所有pod都是近乎随机地调度到工作节点上；但是有时候我们希望调度到固定的硬件基础设施上，比如GPU加速，固态硬盘等等，Kubernetes也提供了相关的支持，通过标签和选择器来实现；

```
[d:\k8s]$ kubectl get nodes
NAME       STATUS   ROLES    AGE   VERSION
minikube   Ready    master   29h   v1.17.0

[d:\k8s]$ kubectl label node minikube gpu=true
node/minikube labeled

[d:\k8s]$ kubectl get nodes -l gpu=true
NAME       STATUS   ROLES    AGE   VERSION
minikube   Ready    master   29h   v1.17.0
```

以上我们给默认的节点minikube添加一个gpu=true的标签来表示此工作节点是GPU加速节点；接下来需要给Pod指定节点选择器指定为gpu=true，这样就可以将Pod调度到特地的工作节点上；

```
apiVersion: v1
kind: Pod
metadata: 
   name: kubia-gpu
spec: 
   nodeSelector:
     gpu: "true"
   containers: 
   - image: ksfzhaohui/kubia
     name: kubia
```

以上创建了YAML描述文件，指定了nodeSelector并且设置为gpu=true，创建Pod：

```
[d:\k8s]$ kubectl create -f kubia-gpu.yaml
pod/kubia-gpu created
```

然后查看Pod的详细信息，看对应的节点信息：

```
[d:\k8s]$ kubectl get po kubia-gpu -o yaml
apiVersion: v1
kind: Pod
metadata:
  name: kubia-gpu
spec:
  nodeName: minikube
  nodeSelector:
    gpu: "true"
```

节点名称调度到了minikube，并且显示了节点选择器信息；

### 3.Pod注解

除标签外，pod和其他对象还可以包含庄解；注解也是键值对，与标签不同，注解并不是为了保存标识信息而存在的，它们不能像标签一样用千对对象进行分组，但是注解可以容纳更多的信息；可以给Pod添加和修改注解：

```
[d:\k8s]$ kubectl annotate pod kubia-manual mycompany.com/someannotation="foo bar"
pod/kubia-manual annotated
[d:\k8s]$ kubectl describe pod kubia-manual
Name:         kubia-manual
Annotations:  mycompany.com/someannotation: foo bar
......
```

### 4.Pod命名空间

标签和命令空间都可以用来组织成组的，Pod是可以有多个标签的，可以重叠；当时命名空间是不可用重叠的，将Pod分隔成完全独立的组，这样分组首先更加类型更加清晰，另外就是不同的命名空间可以使用相同的资源名称；

#### 4.1 查找命名空间

```
[d:\k8s]$ kubectl get ns
NAME                   STATUS   AGE
default                Active   44h
kube-node-lease        Active   44h
kube-public            Active   44h
kube-system            Active   44h
kubernetes-dashboard   Active   44h
```

通过如上命令发现命名空间，以上这些命名空间都是默认创建好的，我们创建的Pod都在default命令空间下，其他的都是系统自带的；

```
[d:\k8s]$ kubectl get po --namespace default
NAME              READY   STATUS    RESTARTS   AGE
kubia-dms8n       1/1     Running   0          2d2h
kubia-gpu         1/1     Running   0          30m
kubia-manual      1/1     Running   0          25h
kubia-manual-v2   1/1     Running   0          24h
```

也可以通过查看指定命名空间对应的Pod列表；

#### 4.2 创建命名空间

命名空间是一种和其他资源一样的Kubernetes资源，因此可以通过将YAML文件提交到Kubernetes API服务器来创建该资源；

```
apiVersion: v1
kind: Namespace
metadata: 
  name: custom-namespace
```

kind指定为Namespace，表示正在定义一个命名空间，custom-namespace为命名空间的名字；

```
[d:\k8s]$ kubectl create -f custom-namespace.yaml
namespace/custom-namespace created
[d:\k8s]$ kubectl get ns
NAME                   STATUS   AGE
custom-namespace       Active   4s
......
```

以上通过yaml描述文件创建命名空间；

#### 4.3 指定命名空间

可以在创建Pod的时候指定命名空间：

```
[d:\k8s]$ kubectl create -f kubia-manual.yaml -n custom-namespace
pod/kubia-manual created

[d:\k8s]$ kubectl get po --namespace default
NAME              READY   STATUS    RESTARTS   AGE
kubia-manual      1/1     Running   0          19h

[d:\k8s]$ kubectl get po --namespace custom-namespace
NAME           READY   STATUS    RESTARTS   AGE
kubia-manual   1/1     Running   0          2m19s
```

可以看到在default和custom-namespace命名空间中存在相同名称的Pod，这就是命名空间的隔离功能；**注：尽管命名空间将对象分隔到不同的组，只允许你对属于特定命名空间的对象进行操作， 但实际上命名空间之间并不提供对正在运行的对象的任何隔离。**

### 5.停止和移除pod

#### 5.1 按名称删除pod

```
[d:\k8s]$ kubectl delete po kubia-gpu
pod "kubia-gpu" deleted
[d:\k8s]$ kubectl get po --namespace default
NAME              READY   STATUS        RESTARTS   AGE
kubia-gpu         1/1     Terminating   0          104m
```

在删除pod的过程中，实际上我们在指示Kubernetes终止该pod中的所有容器，Kubernetes向进程发送一个SIGTERM信号并等待一定的秒数使其正常关闭；关闭的过程中可以发现Pod的状态为Terminating终结中；

#### 5.2 使用标签选择器删除pod

```
[d:\k8s]$ kubectl delete po -l creation_method=manual
pod "kubia-manual" deleted
pod "kubia-manual-v2" deleted
```

会列出所有被删除的Pod；

#### 5.3 通过删除整个命名空间来删除pod

```
[d:\k8s]$ kubectl delete ns custom-namespace
namespace "custom-namespace" deleted

[d:\k8s]$ kubectl get po --namespace custom-namespace
No resources found in custom-namespace namespace.
```

删除命名空间的同时，其中包含的Pod都会被删除；

#### 5.4 删除所有Pod

```
[d:\k8s]$ kubectl delete po --all
pod "kubia-dms8n" deleted
pod "kubia-gkv4v" deleted
pod "kubia-zxd9q" deleted

[d:\k8s]$ kubectl get po
NAME          READY   STATUS    RESTARTS   AGE
kubia-c9hlb   1/1     Running   0          46s
kubia-f4kwr   1/1     Running   0          46s
kubia-vttrc   1/1     Running   0          46s
```

删除完所有Pod之后，又重新出现了三个Pod，主要原因是上面三个Pod是不是直接创建的，而是创建一个ReplicationController，然后再由ReplicationController创建pod；如果想要删除该pod需要删除ReplicationController，可以通过使用如下命令：

```
[d:\k8s]$ kubectl delete all --all
pod "kubia-c9hlb" deleted
pod "kubia-f4kwr" deleted
pod "kubia-vttrc" deleted
replicationcontroller "kubia" deleted
service "kubernetes" deleted
service "kubia" deleted

[d:\k8s]$ kubectl get po
No resources found in default namespace.
```

## 总结

本文继续在阅读**Kubernetes in Action**过程中，实际操作的笔记；主要介绍了Kubernetes的核心组件Pod，包括Pod的创建，标签，主键，命名空间以及停止和移除pod。

## 参考

Kubernetes in Action

## 博客地址

[Github](https://github.com/ksfzhaohui/blog)