**前言**  
在上一篇文章[Paxos算法浅析](https://my.oschina.net/OutOfMemory/blog/807634)中主要介绍了Paxos一致性算法应用的场景，以及对协议本身的介绍；Google Chubby是一个分布式锁服务，其底层一致性实现就是以Paxos算法为基础的；但这篇文件并不是介绍Chubby，而是介绍了一个和Chubby拥有类似功能的开放源码的分布式协调服务Zookeeper，以及Zookeeper数据一致性的核心算法ZAB。

**Zookeeper简介**  
Zookeeper是一个分布式数据一致性的解决方案，分布式应用可以基于它实现诸如数据发布/订阅，负载均衡，命名服务，分布式协调/通知，集群管理，Master选举，分布式锁和分布式队列等功能。Zookeeper致力于提供一个高性能、高可用、且具有严格的顺序访问控制能力的分布式协调系统。  
考虑到Zookeeper主要操作数据的状态，为了保证状态的一致性，Zookeeper提出了两个安全属性:  
1.全序（Total order）：如果消息a在消息b之前发送，则所有Server应该看到相同的结果  
2.因果顺序（Causal order）：如果消息a在消息b之前发生（a导致了b），并被一起发送，则a始终在b之前被执行。  
为了保证上述两个安全属性，Zookeeper使用了**TCP协议和Leader**：  
通过使用TCP协议保证了消息的全序特性（先发先到），  
通过Leader解决了因果顺序问题：先到Leader的先执行，但是这样的话Leader有可能出现出现网络中断、崩溃退出与重启等异常情况，这就有必要引入Leader选举算法。  
而ZAB(Zookeeper Atomic Broadcast即Zookeeper原子消息广播协议)正是作为其数据一致性的核心算法，下面介绍一下ZAB协议。

**ZAB协议**  
ZAB协议包括两种基本的模式：**崩溃恢复和消息广播**  
当整个服务框架在启动过程中，或是当Leader服务器出现网络中断崩溃退出与重启等异常情况时，ZAB就会进入恢复模式并选举产生新的Leader服务器。  
当选举产生了新的Leader服务器，同时集群中已经有过半的机器与该Leader服务器完成了状态同步之后，ZAB协议就会退出崩溃恢复模式，进入消息广播模式。  
当有新的服务器加入到集群中去，如果此时集群中已经存在一个Leader服务器在负责进行消息广播，那么新加入的服务器会自动进入数据恢复模式，找到Leader服务器，并与其进行数据同步，然后一起参与到消息广播流程中去。  
以上其实大致经历了三个步骤：  
1.崩溃恢复：主要就是Leader选举过程  
2.数据同步：Leader服务器与其他服务器进行数据同步  
3.消息广播：Leader服务器将数据发送给其他服务器

下面具体看看这三个步骤  
**1.消息广播**  
ZAB协议的消息广播过程使用的是一个原子广播协议，类似二阶段提交([2PC/3PC到底是啥](https://my.oschina.net/OutOfMemory/blog/801096))，具体可以看来源网上的一张图片：

![](https://static.oschina.net/uploads/space/2016/1227/153210_Y1rg_159239.jpg)

客户端的请求，Leader服务器为其生成对于的Propose，并将其发送给其他服务器，然后再分别收集选票，最后进行提交；在广播Propose之前，Leader会为这个Propose分配一个全局单调递增的唯一ID，称之为事务ID(ZXID)；由于ZAB协议需要保证每一个消息严格的因果关系，因此必须将每一个Propose按照其ZXID的先后顺序来进行排序与处理。  
具体做法就是Leader为每一个Follower都各自分配一个单独的队列，然后将需要广播的Propose依次放入队列中。

**2.崩溃恢复**  
消息广播中如果Leader出现网络中断、崩溃退出与重启等异常，将进入崩溃恢复，恢复的过程中有2个问题需要解决：  
1.ZAB协议需要确保那些已经在Leader服务器上提交的事务，最终被所有服务器都提交  
2.ZAB协议需要确保丢弃那些只在Leader服务器上被提交的事务  
针对以上两个问题，如果让Leader选举算法能够保证新选出来的Leader服务器拥有集群中所有机器最高编号(ZXID)的Propose，那么就可以保证这个新选出来的Leader一定具有所有已经提交的提案；如果让具有最高编号的机器成为Leader，就可以省去Leader服务器检查Propose的提交和抛弃了。

**3.数据同步**  
Leader服务器会为每个Follower服务器都准备一个队列，并将那些没有被各Follower同步的事务以propose消息的形式逐个发送给Follower服务器，并在每个消息的后面发送一个commit消息，表示提交事务；等到同步完成之后，leader服务器会将该服务器加入到真正的可用Follower列表中。  
崩溃恢复中提到2个问题，看看如何解决ZAB协议需要确保丢弃那些只在Leader服务器上被提交的事务：  
事务编号ZXID被设计为一个64位的数字，低32位是一个简单的递增计数器，高32位是Leader周期的epoch编码，每当选举产生一个新的Leader服务器，就会从这个Leader服务器上取出本地日志中最大事务propose的ZXID，然后解析出epoch，最后对epoch加1；低32位就从0开始重新生成新的ZXID。ZAB协议通过epoch编号来区分Leader周期变化的策略，来保证丢弃那些只在上一个Leader服务器上被提交的事务。

**Zab与Paxos**  
Zab的作者认为Zab与paxos并不相同，只所以没有采用Paxos是因为Paxos保证不了全序顺序：  
Because multiple leaders can propose a value for a given instance two problems arise.  
First, proposals can conflict. Paxos uses ballots to detect and resolve conflicting proposals.  
Second, it is not enough to know that a given instance number has been committed, processes must also be able to figure out which value has been committed.  
Paxos算法的确是不关心请求之间的逻辑顺序，而只考虑数据之间的全序，但很少有人直接使用paxos算法，都会经过一定的简化、优化。

**Paxos算法优化**  
Paxos算法在出现竞争的情况下，其收敛速度很慢，甚至可能出现活锁的情况，例如当有三个及三个以上的proposer在发送prepare请求后，很难有一个proposer收到半数以上的回复而不断地执行第一阶段的协议。因此，为了避免竞争，加快收敛的速度，在算法中引入了一个Leader这个角色，在正常情况下同时应该最多只能有一个参与者扮演Leader角色，而其它的参与者则扮演Acceptor的角色。  
在这种优化算法中，只有Leader可以提出议案，从而避免了竞争使得算法能够快速地收敛而趋于一致；而为了保证Leader的健壮性，又引入了Leader选举，再考虑到同步的阶段，渐渐的你会发现对Paxos算法的简化和优化已经和上面介绍的ZAB协议很相似了。

**总结**  
Google的粗粒度锁服务Chubby的设计开发者Burrows曾经说过：“所有一致性协议本质上要么是Paxos要么是其变体”。这句话还是有一定道理的，ZAB本质上就是Paxos的一种简化形式。

**个人博客：[codingo.xyz](http://codingo.xyz/)**