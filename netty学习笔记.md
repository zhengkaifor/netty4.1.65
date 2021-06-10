# 基本概念

#### bossGroup

​		netty中用来处理连接的线程组。

​		当bossGroup监听到客户端连接事件后，生成新的NioSocketChannel 交给workerGroup

#### workerGroup

​		netty中用来处理读写事件的线程组。

​		bossGroup 连接事件处理完后的channel交由它处理

#### nioEventLoop

​		用来处理selector事件的任务线程。 不管是bossGroup还是wokerGroup最终都是在这个类中处理io事件

