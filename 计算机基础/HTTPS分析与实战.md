**名称解释**  
_**https：**_一种安全的http协议，因此可以称为安全的超文本传输协议，https提出在http和tcp之间添加一层加密层(SSL层)，这一层负责数据的加密和解密。  
_**数字证书：**_简称CA，它由权威机构给某网站颁发的一种认可凭证，是被浏览器所认可的，当然证书也可以自己生成，但是这样就不被浏览器所认可，想想如果自己随便生成一个证书就被浏览器认为是安全的，那也挺可怕的；最简单的证书包含一个公开密钥、名称以及证书授权中心的数字签名，证书都有一个有效期。  
_**数字签名：**_就是只有信息的发送者才能产生的别人无法伪造的一段数字串，这段数字串同时也是对信息的发送者发送信息真实性的一个有效证明。  
_**对称加密：**_又叫共享密钥加密，对称密钥在加密和解密的过程中使用的密钥是相同的，常见的对称加密算法有DES，AES；优点是计算速度快，缺点是在数据传送前，发送方和接收方必须商定好秘钥，然后使双方都能保存好秘钥，如果一方的秘钥被泄露，那么加密信息也就不安全了。  
_**非对称加密：**_服务端会生成一对密钥，私钥存放在服务器端，公钥可以发布给任何人使用；优点就是比起对称加密更加安全，但是加解密的速度比对称加密慢太多了。

**HTTPS握手流程**  
四次握手过程如下图所示(图片来源网上)：

![](https://static.oschina.net/uploads/space/2018/0208/175044_dDPy_159239.jpg)

1.客户端发送ClientHello  
具体数据报文如下：

```bash
*** ClientHello, TLSv1.2
RandomCookie:  GMT: 1518006972 bytes = { 65, 176, 64, 48, 101, 197, 66, 45, 233, 201, 4, 212, 39, 207, 197, 221, 77, 28, 131, 219, 59, 92, 71, 77, 188, 128, 9, 85 }
Session ID:  {}
Cipher Suites: [TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA, TLS_RSA_WITH_AES_128_CBC_SHA, TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA, TLS_ECDH_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_RSA_WITH_AES_128_CBC_SHA, TLS_DHE_DSS_WITH_AES_128_CBC_SHA, TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA, SSL_RSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA, TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA, SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA, TLS_ECDHE_ECDSA_WITH_RC4_128_SHA, TLS_ECDHE_RSA_WITH_RC4_128_SHA, SSL_RSA_WITH_RC4_128_SHA, TLS_ECDH_ECDSA_WITH_RC4_128_SHA, TLS_ECDH_RSA_WITH_RC4_128_SHA, SSL_RSA_WITH_RC4_128_MD5, TLS_EMPTY_RENEGOTIATION_INFO_SCSV]
Compression Methods:  { 0 }
```

其中提供的信息包括：TLS的协议版本，客户端生成的随机数，支持的加密方法，支持的压缩方法；

2.服务器返回SeverHello

```
*** ServerHello, TLSv1.2
RandomCookie:  GMT: 1518006972 bytes = { 103, 152, 99, 6, 122, 253, 175, 18, 69, 135, 32, 101, 52, 209, 212, 68, 77, 6, 58, 123, 185, 243, 135, 155, 70, 15, 167, 109 }
Session ID:  {90, 123, 243, 188, 13, 250, 24, 48, 62, 145, 5, 130, 84, 81, 156, 246, 107, 149, 19, 110, 245, 190, 163, 34, 163, 100, 83, 11, 192, 218, 82, 39}
Cipher Suite: TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA
Compression Method: 0
```

包括：确定使用的TLS协议版本，服务器生成的随机数，确认使用的加密方法和压缩方法；

```
*** Certificate chain
chain [0] = [
[
  Version: V3
  Subject: CN=localhost, OU=codingo, O=codingo, L=nj, ST=js, C=zh
  Signature Algorithm: SHA256withRSA, OID = 1.2.840.113549.1.1.11
 
  Key:  Sun RSA public key, 1024 bits
  modulus: 110417951066462499519290586842003643785934040448390902213730986659030452849006148579719535028965929817510494601937968191944831386030431983435172428185729866570463573441808893521679337608573348913665909433691909748813081253262417347690775527606654237333218231191786649572821638992076667126149380228808647107891
  public exponent: 65537
  Validity: [From: Wed Feb 07 11:07:44 CST 2018,
               To: Sat Feb 05 11:07:44 CST 2028]
  Issuer: CN=localhost, OU=codingo, O=codingo, L=nj, ST=js, C=zh
  SerialNumber: [    5123c55c]
  .....
```

主要提供的是服务器的证书信息，包括服务器的名称、受信任的证书颁发机构（CA）和服务器的公钥；

```
*** ECDH ServerKeyExchange
Signature Algorithm SHA512withRSA
Server key: Sun EC public key, 256 bits
  public x coord: 4476231809895366488986368567243788140155223860343081368472893244611384219615
  public y coord: 109050381572001188616470974606466310815898076295756191171855198021565796560756
  parameters: secp256r1 [NIST P-256, X9.62 prime256v1] (1.2.840.10045.3.1.7)
[read] MD5 and SHA1 hashes:  len = 205
```

如果是DH算法，这里发送服务器使用的DH参数，RSA算法没有参数；

```
*** CertificateRequest
Cert Types: RSA, DSS, ECDSA
Supported Signature Algorithms: SHA512withECDSA, SHA512withRSA, SHA384withECDSA, SHA384withRSA, SHA256withECDSA, SHA256withRSA, SHA224withECDSA, SHA224withRSA, SHA1withECDSA, SHA1withRSA, SHA1withDSA, MD5withRSA
Cert Authorities:
<CN=localhost, OU=codingo, O=codingo, L=nj, ST=js, C=zh>
<CN=localhost, OU=codingo, O=codingo, L=nj, ST=js, C=zh>
[read] MD5 and SHA1 hashes:  len = 234
```

服务器要求客户端发送客户端证书，只有在服务器配置了双向认证的情况下才需要客户端证书，我们大部分情况下访问的网站都不需要客户端证书，只会在一些对安全性要求很高的场景下才需要，比如银行领域。

```
*** ServerHelloDone
[read] MD5 and SHA1 hashes:  len = 4
0000: 0E 00 00 00 
```

告诉客户端ServerHello结束。

3.客户端返回

```
*** Certificate chain
chain [0] = [
[
  Version: V3
  Subject: CN=localhost, OU=codingo, O=codingo, L=nj, ST=js, C=zh
  Signature Algorithm: SHA256withRSA, OID = 1.2.840.113549.1.1.11
 
  Key:  Sun RSA public key, 1024 bits
  modulus: 143615763343122571917822119532441620876017906006239941866997048768454765923738744303428875033219696709502897773614201648358563889389642376484119370521476221520354430179070803876160761551600474650108009364863645371886530280727392383823083132045866415069619367905230488064564105401690196419355382565701271042183
  public exponent: 65537
  Validity: [From: Wed Feb 07 11:08:24 CST 2018,
               To: Sat Feb 05 11:08:24 CST 2028]
  Issuer: CN=localhost, OU=codingo, O=codingo, L=nj, ST=js, C=zh
  SerialNumber: [    06be5a82]
```

服务器配置了双向认证，所有客户端需要发送客户端证书给服务器，进行认证；

```
*** ECDHClientKeyExchange
ECDH Public value:  { 4, 76, 128, 114, 175, 136, 51, 128, 201, 195, 101, 5, 13, 104, 146, 150, 177, 126, 39, 78, 212, 106, 81, 93, 253, 171, 132, 162, 40, 118, 146, 65, 113, 156, 141, 114, 237, 127, 163, 208, 73, 9, 17, 235, 110, 166, 148, 93, 56, 159, 47, 6, 9, 119, 68, 109, 48, 4, 152, 89, 18, 54, 95, 214, 196 }
main, WRITE: TLSv1.2 Handshake, length = 684
SESSION KEYGEN:
PreMaster Secret:
......
```

客户端收到服务器的证书，首先会认证证书，认证证书是否是有效的或者可信任的，如果不是浏览器会显示警告页面；如果没有问题客户端会从服务器端的证书中获取公钥，再生成一个随机数，再用公钥对这个随机数加密生成PreMaster Secret，发送给服务器；这样服务器和客户端都有了三个随机数，在通过相同的算法生成一个应用层通讯的对称密钥；

```
main, WRITE: TLSv1.2 Change Cipher Spec, length = 1
[Raw write]: length = 6
0000: 14 03 03 00 01 01                                  ......
*** Finished
verify_data:  { 181, 183, 221, 94, 78, 87, 6, 226, 234, 202, 181, 9 }
```

Change Cipher Spec消息客户端通知服务端后面再发送的消息都会使用前面协商出来的秘钥加密了；  
Finished消息客户端将前面的握手消息加密发出了第一条加密数据，服务器如果可以解密说明密钥是一致的；

4.服务器返回

```
main, READ: TLSv1.2 Change Cipher Spec, length = 1
[Raw read]: length = 5
0000: 16 03 03 00 40     
*** Finished
verify_data:  { 54, 167, 25, 231, 214, 98, 110, 203, 169, 108, 148, 225 }
***
```

Change Cipher Spec消息服务端通知客户端后面再发送的消息都会使用前面协商出来的秘钥加密了；  
Finished消息服务端将前面的握手消息加密发出了第一条加密数据发送给客户端；  
整个握手流程大致如此，接下来就是通过对称加密来传输业务数据了。

**双向认证实现**  
正规的网站一般数字证书都是由权威机构发布的证书，操作系统和浏览器都认可，但是价格比较昂贵（当然也有免费的比如：[Let’s Encrypt](https://zh.wikipedia.org/wiki/Let%27s_Encrypt)）；当然也可以使用自签名证书，下面使用JDK自带工具KeyTool来生成自签名证书，并且在tomcat中使用；

1.生成服务器端证书  
使用keyTool命令生成服务器端证书：

```bash
C:\Program Files\Java\jdk1.7.0_80\bin>keytool -genkey -alias tomcat -keypass 111111 -keyalg RSA -keysize 1024 -validity 3650 -keystore E:\tomcat.keystore -storepass 111111
```

参数说明：-alias：别名，-keypass：别名密码，-keyalg：指定的加密算法，-validity：有效期，-keystore：keystore存储的目录，-storepass：获取keystroe的密码  
因为在本地测试，所以”您的名字与姓氏是什么?” 填localhost，一步步添加数据完之后生成了tomcat.keystore文件；

2.生成客户端证书  
使用keyTool命令生成客户端证书：

```
C:\Program Files\Java\jdk1.7.0_80\bin>keytool -genkey -alias client -keypass 111111 -keyalg RSA -keysize 1024 -validity 3650 -storetype PKCS12 -keystore E:\client.p12 -storepass 111111
```

为了能够导入到浏览器中，证书格式应该是PKCS12，一步步添加数据完之后生成了client.p12文件；

3.让服务器信任客户端证书  
让服务器信任客户端证书，需要将客户端证书导入到服务器证书中，添加为一个信任证书，但是首先需要把PKCS12格式转为cer文件：

```
C:\Program Files\Java\jdk1.7.0_80\bin>keytool -export -alias client -keystore E:\client.p12 -storetype PKCS12 -keypass 111111 -file E:\client.cer
C:\Program Files\Java\jdk1.7.0_80\bin>keytool -import -v -file E:\client.cer -keystore E:\tomcat.keystore -storepass 111111
C:\Program Files\Java\jdk1.7.0_80\bin>keytool -list -v -keystore E:\tomcat.keystore

```

最后的list命令，查看一下当前您的密钥库是否包含2个条目；

4.让客户端信任服务器  
需要将服务器证书导入到浏览器的”受信任的根证书颁发机构”，导入之前先将tomcat.keystore转为server.cer，方便浏览器导入；

```
C:\Program Files\Java\jdk1.7.0_80\bin>keytool -keystore E:\tomcat.keystore -export -alias tomcat -file E:\server.cer
```

浏览器导入：设置->管理证书->受信任的根证书颁发机构->导入server.cer；  
因为是双向认证，所有同样需要在”个人”一栏中添加client.p12：设置->管理证书->个人->导入client.p12；

5.配置tomcat

```
<Connector port="8443" protocol="org.apache.coyote.http11.Http11Protocol" SSLEnabled="true" maxThreads="150" scheme="https" chemeecure="true" clientAuth="true"    sslProtocol="TLS" keystoreFile="E:\\tomcat.keystore" keystorePass="111111" truststoreFile="E:\\tomcat.keystore" truststorePass="111111"/>
```

keystoreFile：服务器证书文件路径，keystorePass：服务器证书密码，truststoreFile：用来验证客户端证书的根证书，truststorePass：根证书密码，clientAuth：是否双向验证

6.浏览器访问  
在chrome中输入：https://localhost:8443，显示如下图所示：

![](https://static.oschina.net/uploads/space/2018/0208/175553_qwI3_159239.jpg)![](https://static.oschina.net/uploads/space/2018/0427/135848_zjyZ_159239.png)

虽然已经设置了服务器端证书为受信任的，但是不是权威机构认证的，浏览器还是显示为不安全的连接；

7.HttpClient4访问  
使用HttpClient4同样需要认证服务器端证书，并且需要发送客户端证书进行双向认证，具体代码如下：

```java
public class HttpClient4 {
 
    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.debug", "all");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(new File("E:\\client.p12")), "111111".toCharArray());
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return true;
            }
        }).loadKeyMaterial(keyStore, "111111".toCharArray()).build();
 
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new String[] { "TLSv1.2" }, null,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
        HttpGet get = new HttpGet();
        get.setURI(new URI("https://localhost:8443/"));
        httpClient.execute(get);
    }
}
```

如果不传客户端证书进行双向认证，回出现如下错误：

```java
Exception in thread "main" javax.net.ssl.SSLHandshakeException: Received fatal alert: bad_certificate
```

**总结**  
本文主要对https的握手过程介绍了一下，同时以自签名的方式在tomcat上进行实际演示；由Let’s Encrypt机构生成的证书可以在浏览器上看到“安全”的标识，[个人网站](https://codingo.xyz/)就使用了Let’s Encrypt的免费方式，具体可以看参考。

参考：  
[https://coolshell.cn/articles/18094.html](https://coolshell.cn/articles/18094.html)  
[https://zh.wikipedia.org/wiki/%E5%82%B3%E8%BC%B8%E5%B1%A4%E5%AE%89%E5%85%A8%E6%80%A7%E5%8D%94%E5%AE%9A](https://zh.wikipedia.org/wiki/%E5%82%B3%E8%BC%B8%E5%B1%A4%E5%AE%89%E5%85%A8%E6%80%A7%E5%8D%94%E5%AE%9A)  
[https://www.bf361.com/system/centos-apache-LetsEncrypt](https://www.bf361.com/system/centos-apache-LetsEncrypt)