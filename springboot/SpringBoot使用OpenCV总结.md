## 前言

最近有个项目需要对图片图像进行处理，使用到了开源框架OpenCV全称是Open Source Computer Vision Library，是一个跨平台的计算机视觉库；而现在的项目都是基于SpringBoot，需要把OpenCv整合进去，下面把在使用中遇到的问题进行一个汇总整理。

## 下载安装

Opencv官网提供了一个多个平台的版本包括：Windows，IOS，Android，地址如下：[https://opencv.org/releases/](https://opencv.org/releases/)；因为开发在Windows平台，发布在Linux平台，所以我们这里至少需要两个版本；

### windows平台

直接可以在官网下载[opencv-3.4.10-vc14_vc15.exe](https://nchc.dl.sourceforge.net/project/opencvlibrary/3.4.10/opencv-3.4.10-vc14_vc15.exe)安装即可，安装完会出现opencv文件夹在buildjava目录下有我们需要的opencv-3410.jar，x64/opencv\_java3410.dll，x86/opencv\_java3410.dll文件；

### Linux平台

Linux平台需要我们手动编译，下载[opencv-3.4.10.zip](https://codeload.github.com/opencv/opencv/zip/3.4.10)，解压到/user/local目录下，然后编译安装，执行如下命令：

```
cd /usr/local/opencv-3.4.10
mkdir build
cd build
cmake -D CMAKE_BUILD_TYPE=RELEASE -D CMAKE_INSTALL_PREFIX=/usr/local -DBUILD_TESTS=OFF ..
make -j8
sudo make install
```

安装完之后可以在build/bin目录下找到opencv-3410.jar，在build/lib目录下找到libopencv_java3410.so

## 整合使用

两个平台分别安装完之后，获取了对应的dll和so文件；两个平台获取到的jar都是一样的，随便用哪个都可以，下面看看如何使用

### 外部引用方式

通过把应用jar与本地库文件进行分隔开，然后在项目中进行引用

#### 相对路径方式

可以通过System.loadLibrary来指定本地库文件，但是这种方式需要在运行时指定-Djava.library.path，具体可以提供配置类：

```
@Configuration
public class NativeConfig {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
}
```

运行时需要在VM arguments中添加-Djava.library.path=对应dll存放的路径，不然会出现如下错误：

```
Caused by: java.lang.UnsatisfiedLinkError: no opencv_java3410 in java.library.path
    at java.lang.ClassLoader.loadLibrary(ClassLoader.java:1860) ~[na:1.8.0_251]
    at java.lang.Runtime.loadLibrary0(Runtime.java:870) ~[na:1.8.0_251]
    at java.lang.System.loadLibrary(System.java:1122) ~[na:1.8.0_251]
    at com.springboot.opencv.NativeConfig.<clinit>(NativeConfig.java:10) ~[classes/:na]
```

#### 绝对路径方式

可以通过System.load来指定本地库函数的绝对路径：

```
@Configuration
public class NativeConfig {
    static {
        System.load("C:\\Users\\opencv\\build\\java\\x64\\opencv_java3410.dll");
    }
}
```

#### 踩坑1

在IDE中运行使用Opencv功能的时候，出现如下错误：

```
java.lang.UnsatisfiedLinkError: org.opencv.imgcodecs.Imgcodecs.imread_1(Ljava/lang/String;)J
    at org.opencv.imgcodecs.Imgcodecs.imread_1(Native Method) ~[opencv-3.4.10.jar:unknown]
    at org.opencv.imgcodecs.Imgcodecs.imread(Imgcodecs.java:332) ~[opencv-3.4.10.jar:unknown]
    at com.springboot.opencv.OpenCVController.testOpenCV(OpenCVController.java:13) ~[classes/:na]
```

很明显是在使用jar包里面的方法时没有找到对应的本地库函数，也就是说loadLibrary没有成功，但是之前其实在本地Java项目中是有进行测试的，可以通过的，猜测是不是使用了什么工具导致加载失败，最终锁定在[spring-boot-devtools](https://docs.spring.io/spring-boot/docs/1.5.16.RELEASE/reference/html/using-boot-devtools.html)工具包，提供了动态加载等功能，直接移除此工具包，或者配置如下开关：

```
System.setProperty("spring.devtools.restart.enabled", "false");
```

### 内部引用方式

为了更加简单部署，可以把本地库文件和项目文件打成一个jar包，可以把本地库文件放在resources目录下，这样可以打成一个jar包，现在的主要问题就是如何加载jar包里面的本地库文件，通过测试发现可以读取到resources目录下的库文件，但是通过system.load并不能去加载成功，对应的是一个类似如下的路径：

```
file:/C:/Users/Administrator.SKY-20170404CXG/Desktop/springboot-0.0.1-SNAPSHOT.j
ar!/BOOT-INF/classes!/opencv
```

最后采用的方式是把读取的库文件，存放到系统的一个临时文件夹下，然后拿到库文件的绝对路径，这样就可以通过system.load直接去加载，具体实现代码可以参考[Github](https://github.com/ksfzhaohui/blog/tree/master/springboot/springboot)

#### 踩坑2

在执行maven编译打包的时候，发现本地库文件(dll或者so文件)体积会变大，猜测maven在编译的时候对本地库文件也进行了编译，具体如何禁用指定的文件格式编译，而只需要拷贝即可：

```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-resources-plugin</artifactId>
    <configuration>
       <encoding>UTF-8</encoding>
       <!-- maven编译下面扩展类型文件的时候直接复制原文件，而不会进行二次编码-->                 
       <nonFilteredFileExtensions>dll,so</nonFilteredFileExtensions>
    </configuration>
</plugin>
```

### 第三方Jar包

除了以上两种需要我们自己去实现加载的方式，其实还可以直接使用第三方提供的jar包[OpenPnp](https://github.com/openpnp/opencv)，里面包含了OpenCV.jar，对应各个平台的本地库，以及加载本地库的封装类；查看其源码可以发现，其实也是通过判断当前系统，然后将对应的本地库文件拷贝到系统的临时文件夹下，最后通过system.load去加载：

```
Files.createTempDirectory(`opencv_openpnp`)；
```

因为此包兼顾了所有平台，所以整个包有点大，一百多M，如果部署的系统确定，其实可以自己去加载指定库文件就可以了，然后以相同的方式打成一个公共包供各个系统使用；

## 总结

本文虽然介绍的是在项目中使用OpenCV的一些总结，但其实其他的本地库也可以使用相同的方式；本文重点记录一下在使用过程中遇到的那些坑，以及加载库文件的方式。

## 代码地址

[Github](https://segmentfault.com/a/1190000023556069#)