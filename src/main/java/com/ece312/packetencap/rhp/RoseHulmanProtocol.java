package com.ece312.packetencap.rhp;

import com.ece312.packetencap.util.Constants;
import com.ece312.packetencap.util.MainUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class RoseHulmanProtocol {

    private int dstPort;
    private int srcPort;
    private int checksum;
    private long calculatedCheckSum;
    private int type;
    private boolean isBuffer;
    private RoseObject payload;


    public RoseHulmanProtocol(int type, int srcPort, RoseObject payload) {
        this.type = type;
        this.srcPort = srcPort;
        this.payload = payload;
    }

    public RoseHulmanProtocol(ByteBuf message) {
        ByteBuf cleanMessage = message.copy();
        this.type = message.readUnsignedByte();

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
        this.calculatedCheckSum = MainUtil.getInstance().calculateChecksum(bb);
        this.isBuffer = buffer != -1;

        //cleanMessage.release();
        //message.release();
    }

    private int readPayload(int type, ByteBuf message) {
        Object dataObj = null;
        int size = 0;
        switch (type) {
            case Constants.RHMP_MESSAGE_TYPE:
                dataObj = new RoseHulmanMessageProtocol(message);
                size = new RoseObject(dataObj).getAsRoseHulmanMessage().getLength() + 3;
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

    public ByteBuf createMessage() {
        ByteBuf message = Unpooled.buffer();

        setDstPort(getType() == Constants.CONTROL_MESSAGE_TYPE ?
                getPayload().getAsControlMessage().length() : Constants.DST_PORT);

        message.writeByte(getType()).writeShortLE(getDstPort()).writeShortLE(getSrcPort());
        int length = 0;
        switch (getType()) {
            case Constants.CONTROL_MESSAGE_TYPE:
                int writtenBytes = message.writeCharSequence(getPayload().getAsControlMessage(), CharsetUtil.US_ASCII);
                length = writtenBytes + 5;
                break;
            case Constants.RHMP_MESSAGE_TYPE:
                length = 8 + getPayload().getAsRoseHulmanMessage().getLength();
                message.writeBytes(getPayload().getAsRoseHulmanMessage().createMessage());
                break;
            default:
                break;
        }

        if (length % 2 == 1) {
            message.writeByte(0);
            setBuffer(true);
        }

        ByteBuf cleanMessage = message.copy();

        byte bb[] = new byte[length];
        cleanMessage.readBytes(bb, 0, length);

        long checksum = MainUtil.getInstance().calculateChecksum(bb);
        setChecksum((short) checksum);

        message.writeShort((short) checksum);

        return message;
    }

    @Override
    public String toString() {
        String messageTxt = "";
        String RHMPTxt = "";
        String RHMPayload = "RHMP Payload";

        if (getType() == Constants.CONTROL_MESSAGE_TYPE)
            RHMPayload = getPayload().getAsControlMessage();
        else {
            RoseHulmanMessageProtocol message = getPayload().getAsRoseHulmanMessage();
            switch (getPayload().getAsRoseHulmanMessage().getType()) {
                case Constants.RHMP_ID_RESPONSE_TYPE:
                    messageTxt += Integer.toString(message.getPayload().getAsIDResponse());
                    break;
                case Constants.RHMP_MESSAGE_RESPONSE_TYPE:
                    messageTxt += message.getPayload().getAsControlMessage();
                    break;
                default:
                    break;
            }
            RHMPTxt += "\n--Start RHMP Parameters--\nRHMP Type: " + message.getType() + "\nCommID: "
                    + message.getCommID() + "\nLength: " + message.getLength() + "\nPayload: " + messageTxt
                    + "\n--End RHMP Parameters--";
        }

        return "Type: " + getType() + "\nDst Port: " + getDstPort() + "\nSrc Port: " + getSrcPort()
                + "\nPayload: " + RHMPayload + RHMPTxt +
                "\nBuffer: " + isBuffer() + "\nChecksum: " + isChecksumValid() +
                "\nChecksum RAW: " + getChecksum() + "\nCalculated Checksum: " + getCalculatedCheckSum();
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

    public long getCalculatedCheckSum() {
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
        return isBuffer;
    }

    public void setBuffer(boolean buffer) {
        this.isBuffer = buffer;
    }
}
