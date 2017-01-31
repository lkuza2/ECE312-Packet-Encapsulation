package com.ece312.packetencap.rhp;

import com.ece312.packetencap.util.Constants;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

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
        byte fragment1 = message.readByte();
        byte fragment2 = message.readByte();
        ByteBuf buf = Unpooled.buffer();

        this.type = fragment1 >>> 2;
        this.commID = (fragment2 + (fragment1 << 2));
        this.length = message.readUnsignedShortLE();

        switch (getType()) {
            case Constants.RHMP_ID_RESPONSE_TYPE:
                this.payload = new RoseObject(message.readUnsignedIntLE());
                break;
            default:
                break;
        }
        //message.readBytes(buf, getLength());
        //payload = new RoseObject(buf);
    }

    public ByteBuf createMessage() {
        ByteBuf message = Unpooled.buffer();

        byte fragment1 = (((byte) ((((byte) getType()) << 2) + 1)));
        System.out.println(fragment1);
        byte fragment2 = 0b00111000;

        message.writeByte(fragment2).writeByte(fragment1);

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
