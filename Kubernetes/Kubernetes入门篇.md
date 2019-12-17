## 前言

随着越来越多的公司使用Kubernetes作为它们运行应用的最佳平台，Kubernetes帮助企业标准化了无论是云端部署还是内部部署的应用交付方式；作为研发人员我们还是很有必要去了解其使用方式，了解其内部机制，接下来的一段时间准备通过阅读**<Kubernetes in Action>**来更多的了解Kubernetes。

## Docker安装

### 1.删除旧版本

因为最早之前安装使用过docker，后面一段时间都没有用过，而Docker从17.03开始分为docker-ce(社区版)和docker-ee(企业版)，所以先要删除本地的旧版本；

```
# 移除掉旧的版本
sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-selinux \
                  docker-engine-selinux \
                  docker-engine

# 删除所有旧的数据
sudo rm -rf /var/lib/docker
```

### 2.安装依赖

docker依赖devicemapper存储类型，逻辑卷管理lvm2；

```
sudo yum install -y yum-utils \
  device-mapper-persistent-data \
  lvm2
```

### 3.添加yum软件源

添加Docker稳定版本的yum软件源；

```
sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo
```

使用了阿里云镜像

```
sudo yum-config-manager \
    --add-repo \
    http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
```

### 4.安装docker

安装最新稳定版本的docker

```
sudo yum install -y docker-ce
```

如果想安装指定版本的Docker，可以查看一下版本并安装

```
yum list docker-ce --showduplicates | sort -r
```

可以指定版本安装,版本号可以忽略:和el7，如 docker-ce-19.03.4

```
sudo yum install docker-ce-<VERSION STRING>
```

### 5.查看版本

使用命令docker version

```
[root@localhost /]# docker version
Client: Docker Engine - Community
 Version:           19.03.5
 API version:       1.40
 Go version:        go1.12.12
 Git commit:        633a0ea
 Built:             Wed Nov 13 07:25:41 2019
 OS/Arch:           linux/amd64
 Experimental:      false
```

### 6.启动关闭docker

启动docker

```
sudo systemctl start docker
```

验证是否启动成功

```
[root@localhost ~]# docker ps
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
```

如果没有启动成功会显示Cannot connect to the Docker daemon；

```
sudo systemctl stop docker
```

关闭使用stop命令即可；

### 7.更新和卸载

更新docker

```
sudo yum update docker-ce
```

卸载docker

```
sudo yum remove docker-ce
```

### 8.安装遇到的问题

selinux-policy包与预期下载不匹配

```
selinux-policy-3.13.1-252.el7_ FAILED                                          
http://mirrors.aliyun.com/centos/7.7.1908/updates/x86_64/Packages/selinux-policy-3.13.1-252.el7_7.6.noarch.rpm: [Errno -1] Package does not match intended download. Suggestion: run yum --enablerepo=updates clean metadata
```

手动下载安装即可

## Docker使用

### 1.运行hello world容器

```
[root@localhost ~]# docker run busybox echo "Hello world"
Unable to find image 'busybox:latest' locally
latest: Pulling from library/busybox
322973677ef5: Pull complete 
Digest: sha256:1828edd60c5efd34b2bf5dd3282ec0cc04d47b2ff9caa0b6d4f07a21d1c08084
Status: Downloaded newer image for busybox:latest
Hello world
```

busybox是一个单一可执行文件，包含多种标准UNIX命令行工具，如：echo、ls 、gzip等；由上面的日志可以看出首先在本地找busybox:latest镜像，找不到会从Docker镜像中心拉取镜像，存放本地等待下次使用；

```
[root@localhost ~]# docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
busybox             latest              b534869c81f0        8 days ago          1.22MB
```

可以通过docker images查看当前的镜像列表；

### 2.创建Node.js镜像

创建一个Node.js应用的docker镜像，至少需要两个文件：应用文件，Dockerfile文件；

#### 2.1 应用文件app.js

```
const http = require('http');
const os = require('os');
console.log("kubia server is starting...");
var handler = function(request,response){
    console.log("Received request from " + request.connection.remoteAddress);
    response.writeHead(200);
    response.end("You've hit " + os.hostname()+"\n");
};
var www = http.createServer(handler);
www.listen(8080);
```

启动了一个端口为8080的http服务器，每个请求服务器会返回主机名称；

#### 2.2 Dockerfile文件

```
FROM node:7
ADD app.js /app.js
ENTRYPOINT ["node","app.js"]
```

From行定义了镜像的起始内容，使用的是node镜像的tag7版本；第二行中把app.js文件从本地文件夹添加到镜像的根目录；最后一行定义了当镜像被运行时需要被执行的命令；

#### 2.3构建镜像

```
[root@localhost docker]# docker build -t kubia .
Sending build context to Docker daemon  3.072kB
Step 1/3 : FROM node:7
7: Pulling from library/node
ad74af05f5a2: Pull complete 
2b032b8bbe8b: Pull complete 
a9a5b35f6ead: Pull complete 
3245b5a1c52c: Pull complete 
afa075743392: Pull complete 
9fb9f21641cd: Pull complete 
3f40ad2666bc: Pull complete 
49c0ed396b49: Pull complete 
Digest: sha256:af5c2c6ac8bc3fa372ac031ef60c45a285eeba7bce9ee9ed66dad3a01e29ab8d
Status: Downloaded newer image for node:7
 ---> d9aed20b68a4
Step 2/3 : ADD app.js /app.js
 ---> 28e5c631a15f
Step 3/3 : ENTRYPOINT ["node","app.js"]
 ---> Running in 63035bc6504d
Removing intermediate container 63035bc6504d
 ---> bfb268fa87e0
Successfully built bfb268fa87e0
Successfully tagged kubia:latest
```

使用docker build构建镜像，Docker 需要基于当前目录（注意命令结尾的点）构建一个叫kubia的镜像，Docker会在目录中寻找Dockerfile，然后基于其中的指令构建镜像。

```
[root@localhost docker]# docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
kubia               latest              bfb268fa87e0        12 minutes ago      660MB
node                7                   d9aed20b68a4        2 years ago         660MB
```

构建完之后可以查看当前镜像，除了我们构建的kubia镜像，还有node镜像，因为docker镜像不是一个大的二进制块， 而是由多层组成的；

### 3.运行容器镜像

```
[root@localhost docker]# docker run --name kubia-container -p 8080:8080 -d kubia
2f7a60412ae6f067226343550ad01cbcb1de7808ed2a0cdeed0b62be5c90f556
```

Docker基于kubia镜像创建一个叫kubia-container的新容器，本机上的8080端口会被映射到容器内的8080端口，并且在后台运行；本机和其他内网机器都可以访问8080端口：

```
[root@localhost docker]# curl localhost:8080
You've hit 2f7a60412ae6
```

浏览器访问8080端口：  
![](https://oscimg.oschina.net/oscnet/up-156894233ad70ced392071904ea8b365318.png)

容器启动成功后，列出运行中的容器：

```
[root@localhost docker]# docker ps
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS                    NAMES
2f7a60412ae6        kubia               "node app.js"       11 minutes ago      Up 11 minutes       0.0.0.0:8080->8080/tcp   kubia-container
```

### 4.探索运行容器的内部

```
[root@localhost docker]# docker exec -it kubia-container bash
root@2f7a60412ae6:/# ps aux
USER        PID %CPU %MEM    VSZ   RSS TTY      STAT START   TIME COMMAND
root          1  0.0  1.6 682524 16232 ?        Ssl  10:55   0:00 node app.js
root         11  0.2  0.1  20240  1912 pts/0    Ss   11:13   0:00 bash
root         16  0.0  0.1  17496  1136 pts/0    R+   11:13   0:00 ps aux
```

在已有的kubia-container容器内部运行bash；-i确保标准输入流保持开放，需要在shell 中输入命令；-t分配一个伪终端(TTY)；使用命令ps aux查看进程，可以看到其中就有app.js进程；

### 5.停止和删除容器

```
[root@localhost docker]# docker stop kubia-container
kubia-container
[root@localhost docker]# docker ps
CONTAINER ID        IMAGE               COMMAND             CREATED             STATUS              PORTS               NAMES
[root@localhost docker]# docker ps -a
CONTAINER ID        IMAGE               COMMAND                CREATED             STATUS                       PORTS               NAMES
2f7a60412ae6        kubia               "node app.js"          29 minutes ago      Exited (137) 6 minutes ago                       kubia-container
```

使用stop命令停止容器之后，ps命令就查不到正在运行的容器；ps -a列出所有容器(包括正在运行和不在运行的)；

```
[root@localhost docker]# docker rm kubia-container
kubia-container
[root@localhost docker]# docker ps -a
CONTAINER ID        IMAGE               COMMAND                CREATED             STATUS                   PORTS               NAMES
```

使用rm命令删除容器，这样ps -a也查不到容器了；

### 6.向镜像仓库推送镜像

#### 6.1 使用附加标签标注镜像

```
[root@localhost docker]# docker tag kubia ksfzhaohui/kubia
[root@localhost docker]# docker images
REPOSITORY          TAG                 IMAGE ID            CREATED             SIZE
ksfzhaohui/kubia    latest              bfb268fa87e0        2 hours ago         660MB
kubia               latest              bfb268fa87e0        2 hours ago         660MB
```

给同一个镜像创建一个额外的标签，可以发现IMAGE ID都是同一个；这里的ksfzhaohui用自己的[Docker Hub ID](https://hub.docker.com/)代替；

#### 6.2 向Docker Hub推送镜像

推送之前是需要登录[docker hub](https://hub.docker.com/)的，使用login命令：

```
[root@localhost docker]# docker login
Login with your Docker ID to push and pull images from Docker Hub. If you don't have a Docker ID, head over to https://hub.docker.com to create one.
Username: ksfzhaohui
Password: 
WARNING! Your password will be stored unencrypted in /root/.docker/config.json.
Configure a credential helper to remove this warning. See
https://docs.docker.com/engine/reference/commandline/login/#credentials-store

Login Succeeded
```

登录成功之后就可以，推送镜像到hub了，使用push命令：

```
[root@localhost docker]# docker push ksfzhaohui/kubia
The push refers to repository [docker.io/ksfzhaohui/kubia]
d6851d29939a: Pushed 
ab90d83fa34a: Mounted from library/node 
8ee318e54723: Mounted from library/node 
e6695624484e: Mounted from library/node 
da59b99bbd3b: Mounted from library/node 
5616a6292c16: Mounted from library/node 
f3ed6cb59ab0: Mounted from library/node 
654f45ecb7e3: Mounted from library/node 
2c40c66f7667: Mounted from library/node 
latest: digest: sha256:6ef8bb39b65fbc08137f8bd58531195289f8dcfc1c1a6bb482c993c5667cc8f3 size: 2213
```

登录[docker hub](https://hub.docker.com/)查看是否已经上传成功  
![](https://oscimg.oschina.net/oscnet/up-a1f3af1f493d8be5b2aa7352698f2c296da.png)

## 配置Kubernetes集群

安装Kubemetes集群的方法有许多，Kubemetes可以在本地的开发机器、自己组织的机器集群或是虚拟机提供商(Google Compute Engine、Amazon EC2、Microsoft Azure 等）上运行，或者使用托管的Kubemetes集群，如Google Kubemetes Engine；最简单的方式是用Minikube 运行一个本地单节点Kubernetes集群；

### 1.安装minikube

[Minikube](https://github.com/kubernetes/minikube)是一个需要下载并放到路径中的二进制文件；它适用于osx 、Linux和Windows系统；更多详细[Getting Started](https://minikube.sigs.k8s.io/docs/start/)，这里在windows系统下安装，下载[minikube installer](https://storage.googleapis.com/minikube/releases/latest/minikube-installer.exe)即可，双击安装到**D:\\Program Files\\Kubernetes\\Minikube**目录下;

### 2.启动Kubernetes集群

```
D:\Program Files\Kubernetes\Minikube>minikube start --vm-driver=virtualbox --iso
-url=https://kubernetes.oss-cn-hangzhou.aliyuncs.com/minikube/iso/minikube-v1.6.
0.iso --image-repository registry.aliyuncs.com/google_containers --image-mirror-
country cn
```

**--iso-url：**利用阿里云的镜像地址下载相应的.iso文件；  
**--image-repository：**[默认值是k8s.gcr.io](http://xn--k8s-2w0el71h170cgd2a.gcr.io/)，指向阿里云的镜像地址：`registry.aliyuncs.com/google_containers`  
**--image-mirror-country：**指定容器镜像仓库；

### 3.安装kubectl

下载windows版本[kubectl](https://storage.googleapis.com/kubernetes-release/release/v1.17.0/bin/windows/amd64/kubectl.exe)，无需安装直接在cmd中使用命令即可，如查看集群是否正常工作：

```
D:\k8s>kubectl cluster-info
Kubernetes master is running at https://192.168.99.107:8443
KubeDNS is running at https://192.168.99.107:8443/api/v1/namespaces/kube-system/
services/kube-dns:dns/proxy

To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'.
```

### 4.Kubernetes控制台

使用命令minikube dashboard：

```
D:\Program Files\Kubernetes\Minikube>minikube dashboard
* Verifying dashboard health ...
* Launching proxy ...
* Verifying proxy health ...
* Opening http://127.0.0.1:59371/api/v1/namespaces/kubernetes-dashboard/services
/http:kubernetes-dashboard:/proxy/ in your default browser...
```

会自动跳转到浏览器中，控制台如下所示：  
![](https://oscimg.oschina.net/oscnet/up-eb050ae5399f9a16dd38da1e229ed0fff83.JPEG)

## 在Kubernetes上运行第一个应用

### 1.部署kubia镜像

在Kubernetes上部署上面推送到Docker Hub的kubia镜像

```
D:\k8s>kubectl run kubia --image=ksfzhaohui/kubia --port=8080 --generator=run/v1
kubectl run --generator=run/v1 is DEPRECATED and will be removed in a future ver
sion. Use kubectl run --generator=run-pod/v1 or kubectl create instead.
replicationcontroller/kubia created
```

可以通过命令或者在控制台查看当前部署的情况：

```
C:\Users\hui.zhao.cfs>kubectl get pods
NAME          READY   STATUS             RESTARTS   AGE
kubia-797mx   0/1     ImagePullBackOff   0          16m
```

一个pod是一组紧密相关的容器，它们总是一起运行在同一个工作节点上，以及同一个Linux 命名空间中。每个pod就像一个独立的逻辑机器，拥有自己的IP 、主机名、进程等，运行一个独立的应用程序；  
以上的ready为0/1表示为挂起状态，因为需要下载镜像，启动容器等一系列操作；

```
C:\Users\hui.zhao.cfs>kubectl get pods
NAME           READY   STATUS              RESTARTS   AGE
kubia-dms8n    1/1     Running             0          18m
```

ready为1/1表示已经部署就绪了；

### 2.访问kubia应用

每个pod都有自己的IP 地址，但是这个地址是集群内部的，不能从集群外部访问。要让pod能够从外部访问，需要通过服务对象公开它，要创建一个特殊的LoadBalancer类型的服务；

```
C:\Users\hui.zhao.cfs>kubectl expose rc kubia --type=NodePort
service/kubia exposed
```

列出所有服务

```
C:\Users\hui.zhao.cfs>kubectl get services
NAME         TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)          AGE
kubernetes   ClusterIP   10.96.0.1       <none>        443/TCP          3h13m
kubia        NodePort    10.96.174.172   <none>        8080:30265/TCP   2m12s
```

查看kubia service的url

```
D:\Program Files\Kubernetes\Minikube>minikube service kubia --url
http://192.168.99.108:30265
```

可以拿着提供的对外ip在浏览器中进行访问  
![](https://oscimg.oschina.net/oscnet/up-d73c380f2cd4aec7370e3bf20a3a368e6a7.png)

### 3.水平伸缩应用

使用Kubemetes的一个主要好处是可以简单地扩展部署，把运行实例的数量增加到三个；

```
C:\Users\hui.zhao.cfs>kubectl get replicationcontrollers
NAME    DESIRED   CURRENT   READY   AGE
kubia   1         1         1       172m
```

当前的副本数为1，然后设置副本数为3

```
C:\Users\hui.zhao.cfs>kubectl scale rc kubia --replicas=3
replicationcontroller/kubia scaled

C:\Users\hui.zhao.cfs>kubectl get replicationcontrollers
NAME    DESIRED   CURRENT   READY   AGE
kubia   3         3         1       174m
```

从控制台看也可以看到有三个副本：  
![](https://oscimg.oschina.net/oscnet/up-21d361fc8cbdcc633fe42a403406954cd30.JPEG)

## 总结

本文从Docker安装和使用开始，到配置Kubernetes集群，最后介绍了如何在Kubernetes上运行第一个应用；通过实战操作的方式对Kubernetes有个简单的认识。

## 参考

Kubernetes in Action

## 博客地址

[Github](https://github.com/ksfzhaohui/blog)