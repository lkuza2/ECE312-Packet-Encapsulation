package com.ece312.packetencap.rhp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Created by kuzalj on 1/30/2017.
 */
public class RoseHulmanMessageProtocol {

    int type;
    int commID;
    int length;
    RoseObject payload;

    public RoseHulmanMessageProtocol(ByteBuf message) {
        byte fragment1 = message.readByte();
        byte fragment2 = message.readByte();
        ByteBuf buf = Unpooled.buffer();

        this.type = fragment1 >> 2;
        this.commID = (fragment2 + (fragment1 << 2));
        this.length = message.readUnsignedShortLE();

        message.readBytes(buf, getLength());
        payload = new RoseObject(buf);
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
