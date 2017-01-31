package com.ece312.packetencap.rhp;

import com.ece312.packetencap.util.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

/**
 * Created by kuzalj on 1/30/2017.
 */
public class RoseHulmanMessageProtocol {

    private int type;
    private int commID;
    private int length;
    private RoseObject payload;

    public RoseHulmanMessageProtocol(int type) {
        this.type = type;
        this.commID = 312;
    }

    public RoseHulmanMessageProtocol(ByteBuf message) {
        int fragment = message.readUnsignedShortLE();

        this.commID = fragment >>> 6;
        this.type = (0b0000000000111111 & fragment);
        this.length = message.readUnsignedByte();
        System.out.println(getLength());
        switch (getType()) {
            case Constants.RHMP_ID_RESPONSE_TYPE:
                this.payload = new RoseObject((int) message.readUnsignedIntLE());
                break;
            case Constants.RHMP_MESSAGE_RESPONSE_TYPE:
                this.payload = new RoseObject(message.readCharSequence(getLength(), CharsetUtil.US_ASCII).toString());
                System.out.println("payload");
                break;
            default:
                break;
        }
    }

    public ByteBuf createMessage() {
        ByteBuf message = Unpooled.buffer();

        short fragment = (short) (getType() | (Constants.COMM_ID << 6));

        message.writeShortLE(fragment);

        switch (getType()) {
            case Constants.RHMP_RESERVED_TYPE:
                this.length = 0;
                break;
            case Constants.RHMP_MESSAGE_REQUEST_TYPE:
                this.length = 0;
                break;
            case Constants.RHMP_ID_REQUEST_TYPE:
                this.length = 0;
                break;
            default:
                this.length = 0;
                break;
        }
        message.writeByte(getLength());
        return message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCommID() {
        return commID;
    }

    public void setCommID(int commID) {
        this.commID = commID;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public RoseObject getPayload() {
        return payload;
    }

    public void setPayload(RoseObject payload) {
        this.payload = payload;
    }
}
