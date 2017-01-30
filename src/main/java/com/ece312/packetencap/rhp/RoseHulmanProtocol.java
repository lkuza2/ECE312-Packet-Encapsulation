package com.ece312.packetencap.rhp;

import com.ece312.packetencap.util.Constants;
import com.ece312.packetencap.util.MainUtil;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

/**
 * Created by kuzalj on 1/30/2017.
 */
public class RoseHulmanProtocol {

    private int dstPort;
    private int srcPort;
    private int checksum;
    private int calculatedCheckSum;
    private int type;
    private boolean buffer;
    private RoseObject payload;


    public RoseHulmanProtocol(ByteBuf message) {
        this.type = message.readUnsignedByte();
        ByteBuf cleanMessage = message.copy();

        this.dstPort = message.readUnsignedShortLE();
        this.srcPort = message.readUnsignedShortLE();

        int size = readPayload(getType(), message);

        int buffer = -1;

        if (((5 + size) % 2) != 0) {
            buffer = message.readUnsignedByte();
        }

        this.checksum = message.readUnsignedShort();
        message.resetReaderIndex();
        message.resetWriterIndex();

        int byteBufSize = buffer == -1 ? 5 + size : 5 + size + 1;

        byte bb[] = new byte[byteBufSize];
        cleanMessage.readBytes(bb, 0, byteBufSize);
        this.calculatedCheckSum = (int) MainUtil.getInstance().calculateChecksum(bb);
        this.buffer = buffer != -1;

        //cleanMessage.release();
        //message.release();
    }

    private int readPayload(int type, ByteBuf message) {
        Object dataObj = null;
        int size = 0;
        switch (type) {
            case Constants.RHMP_MESSAGE_TYPE:
                dataObj = new RoseHulmanMessageProtocol(message);
                break;
            case Constants.CONTROL_MESSAGE_TYPE:
                dataObj = message.readCharSequence(getDstPort(), CharsetUtil.US_ASCII).toString();
                size = this.dstPort;
                break;
            default:
                this.payload = null;
                break;
        }
        setPayload(new RoseObject(dataObj));
        return size;
    }

    @Override
    public String toString() {
        return "Type: " + getType() + " Dst Port: " + getDstPort() + " Src Port: " + getSrcPort()
                + " Message: " + getPayload().getAsControlMessage() + " Buffer: " + isBuffer() + " Checksum: " + isChecksumValid();
    }

    public boolean isChecksumValid() {
        return getChecksum() == getCalculatedCheckSum();
    }

    public int getDstPort() {
        return dstPort;
    }

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }

    public int getChecksum() {
        return checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    public int getCalculatedCheckSum() {
        return calculatedCheckSum;
    }

    public void setCalculatedCheckSum(int calculatedCheckSum) {
        this.calculatedCheckSum = calculatedCheckSum;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public RoseObject getPayload() {
        return payload;
    }

    public void setPayload(RoseObject payload) {
        this.payload = payload;
    }

    public boolean isBuffer() {
        return buffer;
    }

    public void setBuffer(boolean buffer) {
        this.buffer = buffer;
    }
}
