# 基本概念

#### bossGroup

​		netty中用来处理连接的线程组。

​		当bossGroup监听到客户端连接事件后，生成新的NioSocketChannel 交给workerGroup

#### workerGroup

​		netty中用来处理读写事件的线程组。

​		bossGroup 连接事件处理完后的channel交由它处理

#### nioEventLoop

​		用来处理selector事件的任务线程。 不管是bossGroup还是wokerGroup最终都是在这个类中处理io事件

#### Unsafe

​	  读写数据真实发生的类，最终的读写操作都经过该类进行,将数据读写到buffer中后，触发ChannelPipeline执行后续操作

#### ChannelPipeline

​		负责io事件的任务链，默认的pipeline中会默认生成一个headContext，tailContext 。由Unsafe读写数据到buffer中后，通过fireChannelxxx 使我们自己添加的hander执行。

​       其中读事件从head开始往tail执行

​	  写事件从tail开始往head执行

##### 注意点

​		其中outHandler必须在最后一个inHandler之前，否则会出现outHandler无法执行的问题

#### ChannelOutboundBuffer

​    负责管理写出数据流，netty中将消息以entry的形式保存，通过一个链表顺序写出。

​	其中

# 流程

### 启动server

##### dobind

```java
private ChannelFuture doBind(final SocketAddress localAddress) {
    //往selector中注册channel
    //包括初始化channel 并且在pipeline中增加ServerBootstrapAcceptor handler
    //ServerBootstrapAcceptor 负责将建立完连接的channel交给workGroup
    final ChannelFuture regFuture = initAndRegister();
    final Channel channel = regFuture.channel();
    if (regFuture.cause() != null) {
        return regFuture;
    }

    if (regFuture.isDone()) {
        // At this point we know that the registration was complete and successful.
        ChannelPromise promise = channel.newPromise();
        //绑定端口,并且开启监听 listen
        doBind0(regFuture, channel, localAddress, promise);
        return promise;
    } else {
        // Registration future is almost always fulfilled already, but just in case it's not.
        final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);
        regFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Throwable cause = future.cause();
                if (cause != null) {
                    // Registration on the EventLoop failed so fail the ChannelPromise directly to not cause an
                    // IllegalStateException once we try to access the EventLoop of the Channel.
                    promise.setFailure(cause);
                } else {
                    // Registration was successful, so set the correct executor to use.
                    // See https://github.com/netty/netty/issues/2586
                    promise.registered();

                    doBind0(regFuture, channel, localAddress, promise);
                }
            }
        });
        return promise;
    }
}
```



```java
static final boolean SSL = System.getProperty("ssl") != null;
static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8992" : "8023"));

public static void main(String[] args) throws Exception {
    // Configure SSL.
    final SslContext sslCtx;
    if (SSL) {
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
    } else {
        sslCtx = null;
    }

    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                //会调用工厂类 执行传入类的构造器方法生成channel
                //生成channel过程中会生成默认的pipeLine以及 pipeLine中的heal与tail
                .channel(NioServerSocketChannel.class)
                //handler在初始化的时候就会执行
                .handler(new LoggingHandler(LogLevel.INFO, ByteBufFormat.SIMPLE))
                //childHandler会在初始化后channelRead中被添加到pipeLine中
                .childHandler(new TelnetServerInitializer(sslCtx));

        b.bind(PORT).sync().channel().closeFuture().sync();
    } finally {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
```

