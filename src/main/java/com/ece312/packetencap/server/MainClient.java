package com.ece312.packetencap.server;

import com.ece312.packetencap.util.MainUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * Created by kuzalj on 1/28/2017.
 */
public class MainClient implements Runnable{

    @Override
    public void run() {
        MainUtil.getInstance().setWorkerGroup(new NioEventLoopGroup());
        EventLoopGroup group = MainUtil.getInstance().getWorkerGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new MainClientHandler());

            MainUtil.getInstance().setChannel(b.bind(0).sync().channel());


            // Broadcast the QOTM request to port 8080.
//            ch.writeAndFlush(new DatagramPacket(
//                    Unpooled.copiedBuffer("QOTM?", CharsetUtil.UTF_8),
//                    new InetSocketAddress("137.112.38.47", 1874))).sync();

            // QuoteOfTheMomentClientHandler will close the DatagramChannel when a
            // response is received.  If the channel is not closed within 5 seconds,
            // print an error message and quit.
//            if (!ch.closeFuture().await(5000)) {
//                System.err.println("QOTM request timed out.");
//            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
