package com.ece312.packetencap.server;

import com.ece312.packetencap.util.MainUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

/**
 * Created by kuzalj on 1/28/2017.
 */
public class MainClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf message = msg.content();
        ByteBuf cleanMessage = message.copy();
//        StringBuilder buffer = new StringBuilder();
//        try {
//            while (message.isReadable()) { // (1)
//                char read = (char) message.readByte();
//                buffer.append(read);
//            }
//        } finally {
//            ReferenceCountUtil.release(msg); // (2)
//        }
//
//        System.out.println(buffer);

//        byte messageBytes[] = new byte[message.readableBytes()];
//        int i = message.readableBytes() - 1;
//
//        while (message.readableBytes() != 0) { // (1)
//            messageBytes[i] = message.readByte();
//            System.out.println(i);
//            i--;
//        }


        int type = message.readUnsignedByte();
        System.out.println(type);
        int dstPort = message.readUnsignedShortLE();
        int srcPort = message.readUnsignedShortLE();
        String payload = message.readCharSequence(dstPort, CharsetUtil.US_ASCII).toString();
        int buffer = -1;

        if (((5 + dstPort) % 2) != 0) {
            buffer = message.readUnsignedByte();
        }

        int checksum = message.readUnsignedShort();
        message.resetReaderIndex();
        message.resetWriterIndex();

        int byteBufSize = buffer == -1 ? 5 + dstPort : 5 + dstPort + 1;
        System.out.println(byteBufSize);

        byte bb[] = new byte[byteBufSize];
        cleanMessage.readBytes(bb, 0, byteBufSize);
        long checksumCalc = MainUtil.getInstance().calculateChecksum(bb);

        System.out.println("Type: " + type + " Length: " + dstPort + " Src Port: " + srcPort
                + " Message: " + payload + " Buffer: " + buffer + " Checksum: " + checksum);

        System.out.println(checksumCalc);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}