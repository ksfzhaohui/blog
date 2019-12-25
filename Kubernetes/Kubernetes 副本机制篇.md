## 前言

在上文[Kubernetes Pod操作篇](https://my.oschina.net/OutOfMemory/blog/3145092)介绍了kubernetes的核心组件Pod，本文继续介绍kubernetes的副本机制，正是因为副本机制你的部署能自动保待运行，并且保持健康，无须任何手动干预。

## 探针

kubernetes可以通过存活探针(liveness probe)检查容器是否还在运行。可以为pod中的每个容器单独指定存活探针；如果探测失败，kubernetes将定期执行探针并重新启动容器；  
kubernetes有以下三种探测容器的机制：

-   HTTP GET探针对容器的IP地址执行HTTP GET请求；
-   TCP套接字探针尝试与容器指定端口建立TCP连接；
-   Exec探针在容器内执行任意命令，并检查命令的退出状态码。

### 1.准备镜像

#### 1.1 准备App.js

为了测试探针的作用，需要准备新的镜像；在之前的服务中稍作改动，在第五个请求之后，给每个请求返回HTTP状态码500(Internal Server Error)，app.js做如下改动：

```
const http = require('http');
const os = require('os');
console.log("kubia server is starting...");
var requestCount = 0;
var handler = function(request,response){
    console.log("Received request from " + request.connection.remoteAddress);
    requestCount++;
    if (requestCount > 5) {
      response.writeHead(500);
      response.end("I'm not well. Please restart me!");
      return;
    }
    response.writeHead(200);
    response.end("You've hit " + os.hostname()+"\n");
};
var www = http.createServer(handler);
www.listen(8080);
```

requestCount记录请求的次数，大于5次直接返回500状态码，这样探针可以捕获状态码进行服务器重启；

#### 1.2 构建镜像

```
[root@localhost unhealthy]# docker build -t kubia-unhealthy .
Sending build context to Docker daemon  3.584kB
Step 1/3 : FROM node:7
 ---> d9aed20b68a4
Step 2/3 : ADD app.js /app.js
 ---> e9e1b44f8f54
Step 3/3 : ENTRYPOINT ["node","app.js"]
 ---> Running in f58d6ff6bea3
Removing intermediate container f58d6ff6bea3
 ---> d36c6390ec66
Successfully built d36c6390ec66
Successfully tagged kubia-unhealthy:latest
```

通过docker build构建kubia-unhealthy镜像

#### 1.3 推送镜像

```
[root@localhost unhealthy]# docker tag kubia-unhealthy ksfzhaohui/kubia-unhealthy
[root@localhost unhealthy]# docker login
Authenticating with existing credentials...
WARNING! Your password will be stored unencrypted in /root/.docker/config.json.
Configure a credential helper to remove this warning. See
https://docs.docker.com/engine/reference/commandline/login/#credentials-store

Login Succeeded
[root@localhost unhealthy]# docker push ksfzhaohui/kubia-unhealthy
The push refers to repository [docker.io/ksfzhaohui/kubia-unhealthy]
40d9e222a827: Pushed 
......
latest: digest: sha256:5fb3ebeda7f98818bc07b2b1e3245d6a21014a41153108c4dcf52f2947a4dfd4 size: 2213
```

首先给镜像附加标签，然后登录[docker hub](https://hub.docker.com/)，最后推送到[docker hub](https://hub.docker.com/)：  
![](https://oscimg.oschina.net/oscnet/up-af7ebd51d0e7e58258006525e237afb472d.png)

### 2.探针实战

#### 2.1 Http探针YAML文件

创建YAML描述文件，指定了一个Http Get存活探针，告诉Kubernetes定期在端口路径下执行Http Get请求，以确定容器是否健康；

```
apiVersion: v1
kind: Pod
metadata: 
   name: kubia-liveness
spec: 
   containers: 
   - image: ksfzhaohui/kubia-unhealthy
     name: kubia
     livenessProbe: 
        httpGet: 
           path: /
           port: 8080
```

#### 2.2 创建Pod

```
[d:\k8s]$ kubectl create -f kubia-liveness-probe.yaml
pod/kubia-liveness created

[d:\k8s]$ kubectl get pods
NAME             READY   STATUS              RESTARTS   AGE
kubia-liveness   0/1     ContainerCreating   0          3s
```

创建名称为kubia-liveness的Pod，查看的RESTARTS为0，隔一段时间再次观察：

```
[d:\k8s]$ kubectl get pods
NAME             READY   STATUS    RESTARTS   AGE
kubia-liveness   1/1     Running   2          4m
```

观察可以发现此时的RESTARTS=2，表示重启了2次，因为每次探测都会发送http请求，而服务在接收5次请求之后会返回500状态码，Kubernetes探测之后就会重启容器；

### 2.3 Pod探针描述

```
[d:\k8s]$ kubectl describe po kubia-liveness
Name:         kubia-liveness
......
    State:          Running
      Started:      Mon, 23 Dec 2019 15:42:45 +0800
    Last State:     Terminated
      Reason:       Error
      Exit Code:    137
      Started:      Mon, 23 Dec 2019 15:41:15 +0800
      Finished:     Mon, 23 Dec 2019 15:42:42 +0800
    Ready:          True
    Restart Count:  2
    Liveness:       http-get http://:8080/ delay=0s timeout=1s period=10s #success=1 #failure=3
......
Events:
  Type     Reason     Age                    From               Message
  ----     ------     ----                   ----               -------
 ......
  Warning  Unhealthy  85s (x9 over 5m5s)     kubelet, minikube  Liveness probe failed: HTTP probe failed with statuscode: 500
  Normal   Killing    85s (x3 over 4m45s)    kubelet, minikube  Container kubia failed liveness probe, will be restarted
......
```

**State**：当前状态是运行中；  
**Last State**：最后的状态是终止，原因是出现了错误，退出代码为137有特殊的含义：表示该进程由外部信号终止，数字137是两个数字的总和：128+x, 其中x是终止进程的信号编号，这里x=9是SIGKILL的信号编号，意味着这个进程被强行终止；  
**Restart Count**：重启的次数；  
**Liveness**：存活探针的附加信息，delay(延迟）、timeout(超时）、period(周期）；大致意思就是开始探测延迟为0秒，探测超时时间为1秒，每隔10秒检测一次，探测连续失败三次重启容器；定义探针时可以自定义这些参数，比如initialDelaySeconds设置初始延迟等；  
**Events**：列出了发生的事件，比如探测到失败，杀进程，重启容器等；

### 3.探针总结

首先生产环境运行的pod一定要配置探针；其次探针一定要检查程序的内部，不受外部因数影响比如外部服务，数据库等；最后就是探针应该足够轻量。  
以上方式创建的pod，kubernetes在使用探针发现服务不可能就会重启服务，这项任务由承载pod的节点上的Kubelet执行，在主服务器上运行的Kubernetes Control Plane组件不会参与此过程；但如果节点本身崩溃，由于Kubelet本身运行在节点上，所以如果节点异常终止，它将无法执行任何操作，这时候就需要ReplicationController或类似机制管理pod。

## ReplicationController

ReplicationController是一种kubernetes资源，可确保它的pod始终保持运行状态；如果pod因任何原因消失(包括**节点崩溃**)，则ReplicationController会重新创建Pod；  
ReplicationController会持续监控正在运行的pod列表，是确保pod的数量始终与其标签选择器匹配，一个ReplicationController有三个主要部分：

-   label selector(标签选择器)，用于确定ReplicationController作用域中有哪些pod；
-   replica count(副本个数)，指定应运行的pod数量；
-   pod template(pod模板)，用于创建新的pod副本。

以上三个属性可以随时修改，但是只有副本个数修改对当前pod会有影响，比如当前副本数量减少了，那当前pod有可能会被删除；ReplicationController提供的好处：

-   确保一个pod(或多个pod副本)持续运行，失败重启新pod；
-   集群节点发生故障时，它将为故障节点上运行的所有pod创建副本；
-   轻松实现pod的水平伸缩。

### 1.创建ReplicationController

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
```

指定了类型为ReplicationController，名称为kubia；replicas设置副本为3，selector为标签选择器，template为pod创建的模版，三个要素都指定了，执行创建命令：

```
[d:\k8s]$ kubectl create -f kubia-rc.yaml
replicationcontroller/kubia created

[d:\k8s]$ kubectl get pods
NAME          READY   STATUS    RESTARTS   AGE
kubia-dssvz   1/1     Running   0          73s
kubia-krlcr   1/1     Running   0          73s
kubia-tg29c   1/1     Running   0          73s
```

创建完之后等一会执行获取pod列表可以发现创建了三个容器，删除其中一个，再次观察：

```
[d:\k8s]$ kubectl delete pod kubia-dssvz
pod "kubia-dssvz" deleted

[d:\k8s]$ kubectl get pods
NAME          READY   STATUS        RESTARTS   AGE
kubia-dssvz   1/1     Terminating   0          2m2s
kubia-krlcr   1/1     Running       0          2m2s
kubia-mgz64   1/1     Running       0          11s
kubia-tg29c   1/1     Running       0          2m2s
```

被删除的pod结束中，新的pod已经启动，获取有关ReplicationController的信息：

```
[d:\k8s]$ kubectl get rc
NAME    DESIRED   CURRENT   READY   AGE
kubia   3         3         3       4m20s
```

期望3个副本，当前3个副本，准备好的也是3个，更详细的可以使用describe命令：

```
[d:\k8s]$ kubectl describe rc kubia
Name:         kubia
Namespace:    default
Selector:     app=kubia
Labels:       app=kubia
Annotations:  <none>
Replicas:     3 current / 3 desired
Pods Status:  3 Running / 0 Waiting / 0 Succeeded / 0 Failed
Pod Template:
......
Events:
  Type    Reason            Age    From                    Message
  ----    ------            ----   ----                    -------
  Normal  SuccessfulCreate  5m20s  replication-controller  Created pod: kubia-dssvz
  Normal  SuccessfulCreate  5m20s  replication-controller  Created pod: kubia-tg29c
  Normal  SuccessfulCreate  5m20s  replication-controller  Created pod: kubia-krlcr
  Normal  SuccessfulCreate  3m29s  replication-controller  Created pod: kubia-mgz64
  Normal  SuccessfulCreate  75s    replication-controller  Created pod: kubia-vwnmf
```

Replicas显示副本期望数和当前数，Pods Status显示每种状态下的副本数，最后的Events为发生的事件，测试一共删除2个pod，可以看到一个创建了5个pod；

**注：因为使用的是Minikube，只有一个节点同时充当主节点和工作节点，节点故障无法模拟。**

### 2.修改标签

通过更改pod的标签，可以将它从ReplicationController的作用域中添加或删除：

```
[d:\k8s]$ kubectl get pods --show-labels
NAME          READY   STATUS    RESTARTS   AGE   LABELS
kubia-mgz64   1/1     Running   0          27m   app=kubia
kubia-tg29c   1/1     Running   0          28m   app=kubia
kubia-vwnmf   1/1     Running   0          24m   app=kubia

[d:\k8s]$ kubectl label pod kubia-mgz64 app=foo --overwrite
pod/kubia-mgz64 labeled

[d:\k8s]$ kubectl get pods --show-labels
NAME          READY   STATUS              RESTARTS   AGE   LABELS
kubia-4dzw8   0/1     ContainerCreating   0          2s    app=kubia
kubia-mgz64   1/1     Running             0          27m   app=foo
kubia-tg29c   1/1     Running             0          29m   app=kubia
kubia-vwnmf   1/1     Running             0          25m   app=kubia
```

可以发现初始创建的是三个Pod标签都是app=kubia，当把kubia-mgz64的标签设置为foo之后就脱离了当前ReplicationController的控制，这样ReplicationController控制的副本就变成了2个，所以会里面重新创建一个Pod；脱离控制的Pod还是照常运行，除非我们手动删除；

```
[d:\k8s]$ kubectl delete pod kubia-mgz64
pod "kubia-mgz64" deleted

[d:\k8s]$ kubectl get pods --show-labels
NAME          READY   STATUS    RESTARTS   AGE   LABELS
kubia-4dzw8   1/1     Running   0          20h   app=kubia
kubia-tg29c   1/1     Running   0          21h   app=kubia
kubia-vwnmf   1/1     Running   0          21h   app=kubia
```

### 3.修改Pod模版

ReplicationController的pod模板可以随时修改：

```
[d:\k8s]$ kubectl edit rc kubia
......
replicationcontroller/kubia edited
```

使用如上命令即可，会弹出文本编辑器，修改Pod模版标签，如下所示：

```
  template:
    metadata:
      creationTimestamp: null
      labels:
        app: kubia
        type: special
```

添加新的标签type：special，保存退出即可；修改Pod模版之后并不影响现有的pod，只会影响重新创建的pod：

```
[d:\k8s]$ kubectl get pods --show-labels
NAME          READY   STATUS    RESTARTS   AGE   LABELS
kubia-4dzw8   1/1     Running   0          21h   app=kubia
kubia-tg29c   1/1     Running   0          21h   app=kubia
kubia-vwnmf   1/1     Running   0          21h   app=kubia

[d:\k8s]$ kubectl delete pod kubia-4dzw8
pod "kubia-4dzw8" deleted

[d:\k8s]$ kubectl get pods --show-labels
NAME          READY   STATUS    RESTARTS   AGE     LABELS
kubia-6qrxj   1/1     Running   0          2m12s   app=kubia,type=special
kubia-tg29c   1/1     Running   0          21h     app=kubia
kubia-vwnmf   1/1     Running   0          21h     app=kubia
```

删除一个pod，重新创建的pod有了新的标签；

### 4.水平缩放pod

通过文本编辑器来修改副本数，修改spec.replicas为5

```
[d:\k8s]$ kubectl edit rc kubia
replicationcontroller/kubia edited

[d:\k8s]$ kubectl get pods --show-labels
NAME          READY   STATUS              RESTARTS   AGE     LABELS
kubia-6qrxj   1/1     Running             0          9m49s   app=kubia,type=special
kubia-9crmf   0/1     ContainerCreating   0          4s      app=kubia,type=special
kubia-qpwbl   0/1     ContainerCreating   0          4s      app=kubia,type=special
kubia-tg29c   1/1     Running             0          21h     app=kubia
kubia-vwnmf   1/1     Running             0          21h     app=kubia
```

可以发现自动创建了2个Pod，达到副本数5；通过kubectl scale重新修改为3：

```
[d:\k8s]$ kubectl scale rc kubia --replicas=3
replicationcontroller/kubia scaled

[d:\k8s]$ kubectl get pods --show-labels
NAME          READY   STATUS    RESTARTS   AGE   LABELS
kubia-6qrxj   1/1     Running   0          15m   app=kubia,type=special
kubia-tg29c   1/1     Running   0          22h   app=kubia
kubia-vwnmf   1/1     Running   0          21h   app=kubia
```

### 5.删除ReplicationController

通过kubectl delete删除ReplicationController时默认会删除pod，但是也可以指定不删除：

```
[d:\k8s]$ kubectl delete rc kubia --cascade=false
replicationcontroller "kubia" deleted

[d:\k8s]$ kubectl get pods --show-labels
NAME          READY   STATUS    RESTARTS   AGE    LABELS
kubia-6qrxj   1/1     Running   0          103m   app=kubia,type=special
kubia-tg29c   1/1     Running   0          23h    app=kubia
kubia-vwnmf   1/1     Running   0          23h    app=kubia

[d:\k8s]$ kubectl get rc kubia
Error from server (NotFound): replicationcontrollers "kubia" not found
```

--cascade=false可以不删除pod，只删除ReplicationController

## ReplicaSet

ReplicaSet是新一代ReplicationController，将完全替代ReplicationController；ReplicaSet的行为与ReplicationController完全相同，但pod选择器的表达能力更强；

### 1.创建ReplicaSet

```
apiVersion: apps/v1
kind: ReplicaSet
metadata: 
   name: kubia
spec: 
   replicas: 3
   selector: 
      matchLabels: 
         app: kubia      
   template:
      metadata: 
         labels:
            app: kubia
      spec: 
         containers: 
         - name: kubia
           image: ksfzhaohui/kubia
```

apiVersion指定为apps/v1：apps表示API组，v1表示实际的API版本；如果是在核心的API组中，API是可以不用指定的，比如之前的ReplicationController只需要指定v1；  
其他定义基本和ReplicationController类似，除了在selector下使用了matchLabels选择器；

```
[d:\k8s]$ kubectl create -f kubia-replicaset.yaml
replicaset.apps/kubia created

[d:\k8s]$ kubectl get pods --show-labels
NAME          READY   STATUS    RESTARTS   AGE    LABELS
kubia-6qrxj   1/1     Running   0          150m   app=kubia,type=special
kubia-tg29c   1/1     Running   0          24h    app=kubia
kubia-vwnmf   1/1     Running   0          24h    app=kubia

[d:\k8s]$ kubectl get rs
NAME    DESIRED   CURRENT   READY   AGE
kubia   3         3         3       49s
```

创建完ReplicaSet之后，重新接管了原来的3个pod；更详细的可以使用describe命令：

```
[d:\k8s]$ kubectl describe rs
Name:         kubia
Namespace:    default
Selector:     app=kubia
Labels:       <none>
Annotations:  <none>
Replicas:     3 current / 3 desired
Pods Status:  3 Running / 0 Waiting / 0 Succeeded / 0 Failed
Pod Template:
  Labels:  app=kubia
  Containers:
   kubia:
    Image:        ksfzhaohui/kubia
    Port:         <none>
    Host Port:    <none>
    Environment:  <none>
    Mounts:       <none>
  Volumes:        <none>
Events:           <none>
```

可以看到Events事件列表为空，当前的3个pod都是接管的原来已经创建的pod；

### 2.ReplicaSet标签选择器

ReplicaSet相对于ReplicationController的主要改进是它更具表达力的标签选择器；

```
   selector: 
      matchExpressions:
       - key: app
         operator: In
         values: 
            - kubia
```

ReplicaSet除了可以使用matchLabels，还可以使用功能更强大的matchExpressions；每个表达式都必须包含一个key、一个operator(运算符)、可能还有一个values的列表，运算符可以有：

-   In：Label的值必须与其中一个指定的values匹配；
-   Notln：Label的值与任何指定的values不匹配；
-   Exists：pod必须包含一个指定名称的标签，使用此运算符时，不应指定values字段；
-   DoesNotExist：pod不得包含有指定名称的标签，不应指定values字段；

### 3.删除ReplicaSet

```
[d:\k8s]$ kubectl delete rs kubia
replicaset.apps "kubia" deleted

[d:\k8s]$ kubectl get pods --show-labels
No resources found in default namespace.
```

删除ReplicaSet的同时会删除其管理的pod；

## DaemonSet

Replicationcontroller和ReplicaSet都用于在kubernetes集群上运行部署特定数量的pod；而DaemonSet可以在所有集群节点上运行一个pod，比如希望在每个节点上运行日志收集器和资源监控器；当然也可以通过节点选择器控制只有哪些节点运行pod；

### 1.创建DaemonSet

```
apiVersion: apps/v1
kind: DaemonSet
metadata: 
   name: ssd-monitor
spec: 
   selector: 
      matchLabels: 
         app: ssd-monitor
   template:
      metadata: 
         labels:
           app: ssd-monitor
      spec: 
         nodeSelector: 
           disk: ssd
         containers: 
         - name: main
           image: ksfzhaohui/kubia
```

准备如上创建DaemonSet的YAML文件，以上属性基本和ReplicaSet类似，除了nodeSelector也就是节点选择器，指定了选择disk=ssd标签；  
的节点标签；

```
[d:\k8s]$ kubectl create -f ssd-monitor-daemonset.yaml
daemonset.apps/ssd-monitor created

[d:\k8s]$ kubectl get ds
NAME          DESIRED   CURRENT   READY   UP-TO-DATE   AVAILABLE   NODE SELECTOR   AGE
ssd-monitor   0         0         0       0            0           disk=ssd        24s

[d:\k8s]$ kubectl get pods --show-labels
No resources found in default namespace.
```

创建完之后，并没有给当前节点创建pod，因为当前节点没有指定disk=ssd标签；

```
[d:\k8s]$ kubectl get node
NAME       STATUS   ROLES    AGE   VERSION
minikube   Ready    master   8d    v1.17.0

[d:\k8s]$ kubectl label node minikube disk=ssd
node/minikube labeled

[d:\k8s]$ kubectl get node --show-labels
NAME       STATUS   ROLES    AGE   VERSION   LABELS
minikube   Ready    master   8d    v1.17.0   beta.kubernetes.io/arch=amd64,beta.kubernetes.io/os=linux,disk=ssd,gpu=true,kubernetes.io/arch=amd64,kubernetes.io/hostname=minikube,kubernetes.io/os=linux,node-role.kubernetes.io/master=

[d:\k8s]$ kubectl get pods --show-labels
NAME                READY   STATUS    RESTARTS   AGE   LABELS
ssd-monitor-84hxd   1/1     Running   0          31s   app=ssd-monitor,controller-revision-hash=5dc77f567d,pod-template-generation=1
```

首先获取当前节点名称为minikube，然后设置标签disk=ssd，这时候会自动在当前节点创建一个pod，因为在minikube中只有一个节点不好在多个节点上模拟；

### 2.删除pod和DaemonSet

```
[d:\k8s]$ kubectl label node minikube disk=hdd --overwrite
node/minikube labeled

[d:\k8s]$ kubectl get pods --show-labels
No resources found in default namespace.
```

修改节点minkube的标签，可以发现节点上的pod会自动删除，因为不满足节点选择器；

```
[d:\k8s]$ kubectl delete ds ssd-monitor
daemonset.apps "ssd-monitor" deleted

[d:\k8s]$ kubectl get ds
No resources found in default namespace.
```

删除DaemonSet也会一起删除这些pod；

## Job

ReplicationController、ReplicaSet和DaemonSet会持续运行任务，永远达不到完成态，这些pod中的进程在退出时会重新启动；kubernetes通过Job资源允许你运行一种pod, 该pod在内部进程成功结束时，不重启容器，一旦任务完成，pod就被认为处千完成状态；  
在发生节点故障时，该节点上由Job管理的pod，重新安排到其他节点；如果进程本身异常退出，可以将Job配置为重新启动容器；

### 1.创建Job

在创建Job前先准备一个构建在busybox的镜像，该容器将调用sleep 命令两分钟：

```
FROM busybox
ENTRYPOINT echo "$(date) Batch job starting"; sleep 120; echo "$(date) Finished succesfully"
```

此镜像已经推送到[docker hub](https://hub.docker.com/)：  
![](https://oscimg.oschina.net/oscnet/up-bf2e9e9f8205968ff7260407fe4a332b71b.png)

```
apiVersion: batch/v1
kind: Job
metadata: 
   name: batch-job
spec: 
   template:
      metadata: 
         labels:
           app: batch-job
      spec: 
         restartPolicy: OnFailure
         containers: 
         - name: main
           image: ksfzhaohui/batch-job
```

Job属于batch API组，其中重要的属性是restartPolicy默认为Always表示无限期运行，其他选项还有OnFailure或Never，表示进程失败重启和不重启；

```
[d:\k8s]$ kubectl create -f exporter.yaml
job.batch/batch-job created

[d:\k8s]$ kubectl get job
NAME        COMPLETIONS   DURATION   AGE
batch-job   0/1           7s         8s

[d:\k8s]$ kubectl get pod
NAME              READY   STATUS    RESTARTS   AGE
batch-job-7sw68   1/1     Running   0          25s
```

创建Job，会自动创建一个pod，pod中的进程运行2分钟后会结束：

```
[d:\k8s]$ kubectl get pod
NAME              READY   STATUS      RESTARTS   AGE
batch-job-7sw68   0/1     Completed   0          3m1s

[d:\k8s]$ kubectl get job
NAME        COMPLETIONS   DURATION   AGE
batch-job   1/1           2m11s      3m12s
```

可以发现pod状态为Completed，同样job的COMPLETIONS同样为完成；

### 2.Job中运行多个pod实例

作业可以配置为创建多个pod实例，并以并行或串行方式运行它们；可以通过设置completions和parallelism属性来完成；

#### 2.1 顺序运行Job pod

```
apiVersion: batch/v1
kind: Job
metadata: 
   name: multi-completion-batch-job
spec: 
   completions: 3
   template:
      metadata: 
         labels:
           app: multi-completion-batch-job
      spec: 
         restartPolicy: OnFailure
         containers: 
         - name: main
           image: ksfzhaohui/batch-job
```

completions设置为3，一个一个的运行3个pod，所有完成整个job完成；

```
[d:\k8s]$ kubectl get pod
NAME                               READY   STATUS      RESTARTS   AGE
multi-completion-batch-job-h75j8   0/1     Completed   0          2m19s
multi-completion-batch-job-wdhnj   1/1     Running     0          15s

[d:\k8s]$ kubectl get job
NAME                         COMPLETIONS   DURATION   AGE
multi-completion-batch-job   1/3           2m28s      2m28s
```

可以看到完成一个pod之后会启动第二pod，所有都运行完之后如下所示：

```
[d:\k8s]$ kubectl get pod
NAME                               READY   STATUS      RESTARTS   AGE
multi-completion-batch-job-4vjff   0/1     Completed   0          2m7s
multi-completion-batch-job-h75j8   0/1     Completed   0          6m16s
multi-completion-batch-job-wdhnj   0/1     Completed   0          4m12s

[d:\k8s]$ kubectl get job
NAME                         COMPLETIONS   DURATION   AGE
multi-completion-batch-job   3/3           6m13s      6m18s
```

#### 2.2 并行运行Job pod

```
apiVersion: batch/v1
kind: Job
metadata: 
   name: multi-completion-parallel-batch-job
spec: 
   completions: 3
   parallelism: 2
   template:
      metadata: 
         labels:
           app: multi-completion-parallel-batch-job
      spec: 
         restartPolicy: OnFailure
         containers: 
         - name: main
           image: ksfzhaohui/batch-job
```

同时设置了completions和parallelism，表示job可以同时运行两个pod，其中任何一个执行完成可以运行第三个pod：

```
[d:\k8s]$ kubectl create -f multi-completion-parallel-batch-job.yaml
job.batch/multi-completion-parallel-batch-job created

[d:\k8s]$ kubectl get pod
NAME                                        READY   STATUS              RESTARTS   AGE
multi-completion-parallel-batch-job-f7wn8   0/1     ContainerCreating   0          3s
multi-completion-parallel-batch-job-h9s29   0/1     ContainerCreating   0          3s
```

#### 2.3 限制Job pod完成任务的时间

在pod配置中设置activeDeadlineSeconds属性，可以限制pod的时间；如果pod运行时间超过此时间，系统将尝试终止pod, 并将Job标记为失败；

```
apiVersion: batch/v1
kind: Job
metadata:
  name: time-limited-batch-job
spec:
  activeDeadlineSeconds: 30
  template:
    metadata:
      labels:
        app: time-limited-batch-job
    spec:
      restartPolicy: OnFailure
      containers:
      - name: main
        image: ksfzhaohui/batch-job
```

指定activeDeadlineSeconds为30秒，超过30秒自动失败；

```
[d:\k8s]$ kubectl create -f time-limited-batch-job.yaml
job.batch/time-limited-batch-job created

[d:\k8s]$ kubectl get job
NAME                     COMPLETIONS   DURATION   AGE
time-limited-batch-job   0/1           3s         3s

[d:\k8s]$ kubectl get pod
NAME                           READY   STATUS    RESTARTS   AGE
time-limited-batch-job-jgmm6   1/1     Running   0          29s

[d:\k8s]$ kubectl get pod
NAME                           READY   STATUS        RESTARTS   AGE
time-limited-batch-job-jgmm6   1/1     Terminating   0          30s

[d:\k8s]$ kubectl get pod
No resources found in default namespace.

[d:\k8s]$ kubectl get job
NAME                     COMPLETIONS   DURATION   AGE
time-limited-batch-job   0/1           101s       101s
```

可以观察AGE标签下面的时间表示已经运行的时间，30秒之后pod状态变成Terminating；

#### 2.4 Job定期运行

job也支持定期执行，有点像quartz，也支持类似的quartz表达式：

```
apiVersion: batch/v1beta1
kind: CronJob
metadata:
  name: corn-batch-job
spec:
  schedule: "0-59 * * * *"
  jobTemplate:
    spec:
      template:
        metadata:
          labels:
            app: corn-batch-job
        spec:
          restartPolicy: OnFailure
          containers:
          - name: main
            image: ksfzhaohui/batch-job
```

指定schedule用来表示表达式分别是：分钟，小时，每个月中的第几天，月，星期几；以上配置表示每分钟运行一个job；

```
[d:\k8s]$ kubectl create -f cronjob.yaml
cronjob.batch/corn-batch-job created

[d:\k8s]$ kubectl get pod
NAME                              READY   STATUS              RESTARTS   AGE
corn-batch-job-1577263560-w2fq2   0/1     Completed           0          3m3s
corn-batch-job-1577263620-92pc7   1/1     Running             0          2m2s
corn-batch-job-1577263680-tmr8p   1/1     Running             0          62s
corn-batch-job-1577263740-jmzqk   0/1     ContainerCreating   0          2s

[d:\k8s]$ kubectl get job
NAME                        COMPLETIONS   DURATION   AGE
corn-batch-job-1577263560   1/1           2m5s       3m48s
corn-batch-job-1577263620   1/1           2m4s       2m47s
corn-batch-job-1577263680   0/1           107s       107s
corn-batch-job-1577263740   0/1           47s        47s
```

每个一分钟就运行一个job，可以删除CronJob

```
[d:\k8s]$ kubectl delete CronJob corn-batch-job
cronjob.batch "corn-batch-job" deleted
```

## 总结

本文继续在阅读**Kubernetes in Action**过程中，实际操作的笔记；主要介绍了相关的副本机制探针，ReplicationController，ReplicaSet，DaemonSet以及Job相关知识点。

## 参考

Kubernetes in Action

## 博客地址

[Github](https://github.com/ksfzhaohui/blog)