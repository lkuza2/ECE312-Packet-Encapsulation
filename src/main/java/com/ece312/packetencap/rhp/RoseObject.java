package com.ece312.packetencap.rhp;

/**
 * Created by kuzalj on 1/30/2017.
 */
public class RoseObject {

    private Object dataObj;

    public RoseObject(Object dataObj) {
        this.dataObj = dataObj;
    }

    public String getAsControlMessage() {
        return dataObj.toString().replace('\0', ' ');
    }

    public int getAsIDResponse() {
        return (int) dataObj;
    }


    public RoseHulmanMessageProtocol getAsRoseHulmanMessage() {
        return (RoseHulmanMessageProtocol) this.dataObj;
    }

}
