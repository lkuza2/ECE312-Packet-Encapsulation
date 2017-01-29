package com.ece312.packetencap.server;

import com.ece312.packetencap.util.MainUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Created by kuzalj on 1/28/2017.
 */
public class MainServer implements Runnable {

    private int port;

    public MainServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        MainUtil util = MainUtil.getInstance();
        util.setBossGroup(new NioEventLoopGroup());
        util.setWorkerGroup(new NioEventLoopGroup());
        EventLoopGroup bossGroup = util.getBossGroup(); // (1)
        EventLoopGroup workerGroup = util.getWorkerGroup();
        ServerBootstrap b = new ServerBootstrap(); // (2)
        util.setBroadcast(new DefaultChannelGroup("broadcast", GlobalEventExecutor.INSTANCE));

        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class) // (3)
                .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new MainServerHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
        try {
            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Done!");
    }
}
