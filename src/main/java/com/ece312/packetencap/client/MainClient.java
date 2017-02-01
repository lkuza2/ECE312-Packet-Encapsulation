package com.ece312.packetencap.client;

import com.ece312.packetencap.util.MainUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;


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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}