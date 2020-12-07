package com.company;

import java.nio.charset.StandardCharsets;

public class SNTPMessage {

    private byte leapIndicator = 0;
    private byte versionNumber = 4;
    private byte mode = 0;

    private short stratum = 0;
    private short pollInterval = 0;
    private byte precision = 0;

    private double rootDelay = 0;
    private double rootDispersion = 0;

    private byte[] referenceIdentifier = {0, 0, 0, 0};

    private double referenceTimestamp = 0;
    private double originateTimestamp = 0;
    private double receiveTimestamp = 0;
    private double transmitTimestamp = 0;

    public SNTPMessage() {
        this.mode = 3;
        this.transmitTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;
    }

    public SNTPMessage(byte[] buf) {
        byte b = buf[0];

        this.leapIndicator = (byte) ((b>>6 & 0x3));
        this.versionNumber = (byte) ((b>>3 & 0x4));
        this.mode = (byte) ((b & 0x7));

        this.stratum = unsignedByteToShort(buf[1]);
        this.pollInterval = unsignedByteToShort(buf[2]);
        this.precision = buf[3];

        //vi får datan för rootDelay som 4 bytes d.v.s 32 bits i en följd.
        this.rootDelay = (buf[4] * 256.0) + unsignedByteToShort(buf[5]) +
                (unsignedByteToShort(buf[6]) / 256.0) + (unsignedByteToShort(buf[7]) / 65536.0);

        this.rootDispersion = (buf[8] * 256.0) + unsignedByteToShort(buf[9]) +
                (unsignedByteToShort(buf[10]) / 256.0) + (unsignedByteToShort(buf[11]) / 65536.0);
                                              //0xff+1.0                              //0xffff+1.0           skriv om till hexadecimal(snyggare).
        //ASCII PPS String
        this.referenceIdentifier[0] = buf[12];
        this.referenceIdentifier[1] = buf[13];
        this.referenceIdentifier[2] = buf[14];
        this.referenceIdentifier[3] = buf[15];

        this.referenceTimestamp = byteArrayToDouble(buf, 16);
        this.originateTimestamp = byteArrayToDouble(buf, 24);
        this.receiveTimestamp = byteArrayToDouble(buf, 32);
        this.transmitTimestamp = byteArrayToDouble(buf, 40);
    }

    private double byteArrayToDouble(byte[] buf, int index) {

        double result = 0.0;
        for (int i = 0; i < 8; i++) {
            result = result + unsignedByteToShort(buf[index+i]) * Math.pow(2, (3-i)*8);
        }

        return result;
    }

    private short unsignedByteToShort(byte b) {

        if ((b & 0x80) == 0x80) {
            return (short) (128 +(b & 0x7F));
        }                          //ett hexadecimaltal i java 0x prefix

        return b;
    }

    public byte[] toByteArray() {

        byte[] array = new byte[48];

        array[0] = (byte) (leapIndicator << 6 | versionNumber << 3 | mode);
        array[1] = (byte) stratum;
        array[2] = (byte) pollInterval;
        array[3] = precision;

        int data = (int) (rootDelay * (0xff+1));
        array[4] = (byte) ((data >> 24) & 0xff);
        array[5] = (byte) ((data >> 16) & 0xff);
        array[6] = (byte) ((data >> 8) & 0xff);
        array[7] = (byte) (data & 0xff);

        int rd = (int) (rootDispersion * (0xff+1));
        array[8] = (byte) ((rd >> 24) & 0xff);
        array[9] = (byte) ((rd >> 16) & 0xff);
        array[10] = (byte) ((rd >> 8) & 0xff);
        array[11] = (byte) (rd & 0xff);

        array[12] = referenceIdentifier[0];
        array[13] = referenceIdentifier[1];
        array[14] = referenceIdentifier[2];
        array[15] = referenceIdentifier[3];

        doubleToByteArray(array, 16, referenceTimestamp);
        doubleToByteArray(array, 24, originateTimestamp);
        doubleToByteArray(array, 32, receiveTimestamp);
        doubleToByteArray(array, 40, transmitTimestamp);

        return array;
    }

    private void doubleToByteArray(byte[] array, int index, double data) {

        for (int i = 0; i < 8; i++) {
            array[index+i] = (byte) (data / Math.pow(2, (3-i) * 8));
            data = data - (unsignedByteToShort(array[index+i]) * Math.pow(2, (3-i) * 8));
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.insert(sb.length(), "Leap Indicator: " + this.leapIndicator + " \n");
        sb.insert(sb.length(), "Version Number: " + this.versionNumber + " \n");
        sb.insert(sb.length(), "Mode: " + this.mode + " \n");

        sb.insert(sb.length(), "Reference Identifier: " + new String(referenceIdentifier, StandardCharsets.US_ASCII));

        return sb.toString();
    }

    public double getOriginateTimestamp() {
        return this.originateTimestamp;
    }
    public double getReceiveTimestamp() {
        return this.receiveTimestamp;
    }
    public double getReferenceTimestamp() {
        return this.referenceTimestamp;
    }
    public double getTransmitTimestamp() {
        return this.transmitTimestamp;
    }
    public byte getMode() {
        return this.mode;
    }
}
