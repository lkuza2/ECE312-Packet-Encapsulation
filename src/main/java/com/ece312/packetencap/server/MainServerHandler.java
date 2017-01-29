package com.ece312.packetencap.server;

import com.ece312.packetencap.util.MainUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;

/**
 * Created by kuzalj on 1/28/2017.
 */
public class MainServerHandler extends ChannelInboundHandlerAdapter {


    private boolean nameRead = false;

    /**
     * Called when a client connects for the first time
     *
     * @param ctx The clients channel
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) { // (1)
        MainUtil.getInstance().getBroadcast().add(ctx.channel());
    }

    /**
     * Called when a client disconnects
     *
     * @param ctx The channel
     * @throws Exception Throws exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        MainUtil.getInstance().getBroadcast().remove(ctx);
    }

    /**
     * Called when the server recieves data from the client
     *
     * @param ctx The channel
     * @param msg The message
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        ByteBuf in = (ByteBuf) msg;
        try {
            while (in.isReadable()) { // (1)
                System.out.print((char) in.readByte());
                System.out.flush();
            }
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }

}