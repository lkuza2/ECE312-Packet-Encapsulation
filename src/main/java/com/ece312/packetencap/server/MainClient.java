package com.ece312.packetencap.server;

import com.ece312.packetencap.util.Constants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by kuzalj on 1/28/2017.
 */
public class MainClient implements Runnable{

    @Override
    public void run() {
        String host = Constants.SERVER_IP;
        int port = Constants.SERVER_PORT;
        EventLoopGroup workerGroup = new NioEventLoopGroup();

            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new MainClientHandler());
                }
            });

            // Start the client.
            try {
                ChannelFuture f = b.connect(host, port).sync(); // (5)

                // Wait until the connection is closed.
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

}
