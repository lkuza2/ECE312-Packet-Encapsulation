package com.ece312.packetencap.server;

import com.ece312.packetencap.rhp.RoseHulmanProtocol;
import com.ece312.packetencap.util.MainUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

/**
 * Created by kuzalj on 1/28/2017.
 */
public class MainClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf message = msg.content();

        RoseHulmanProtocol protocol = new RoseHulmanProtocol(message);
        System.out.println(protocol);
        MainUtil.getInstance().printCursor();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}