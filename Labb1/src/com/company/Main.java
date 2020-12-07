package com.company;

import java.io.IOException;
import java.net.*;
import java.util.TreeMap;

public class Main {

    private final String[] timeServers = new String[8];
    private final TreeMap<String, Double> serverOffsets = new TreeMap<>();

    private Main() throws IOException {
        initiateTimeServers();
        testConnection();
        sortOffsets();
    }

    private void initiateTimeServers() {

        timeServers[0] = "gbg1.ntp.se";
        timeServers[1] = "gbg2.ntp.se";
        timeServers[2] = "mmo1.ntp.se";
        timeServers[3] = "mmo2.ntp.se";
        timeServers[4] = "sth1.ntp.se";
        timeServers[5] = "sth2.ntp.se";
        timeServers[6] = "svl1.ntp.se";
        timeServers[7] = "svl2.ntp.se";
    }

    /**
     * Ta bort break; längst ner för att loopa igenom alla servers, just nu hoppar vi ur om vi får
     * svar från en av dem, får vi inget svar på två sekunder så kastar vi exception.
     *
     * Har lagt till en sorteringsmetod för offset-tider om vi loopar igenom alla, bara för att testa lite. Den skulle inte vara med.
     */
    
    private void testConnection() throws IOException {

        for (int i = 0; i < timeServers.length; i++) {

            try {

                DatagramSocket socket = new DatagramSocket();
                socket.setSoTimeout(2000);
                InetAddress address = InetAddress.getByName(timeServers[i]);
                SNTPMessage message = new SNTPMessage();

                byte[] buf = message.toByteArray();

                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);

                socket.send(packet);
                System.out.println();
                System.out.println("Sent request to: " + timeServers[i].toUpperCase());

                socket.receive(packet);
                SNTPMessage response = new SNTPMessage(packet.getData());

                System.out.println("Received reply");
                socket.close();

                System.out.println();
                calculateOffsetBetweenComputerAndTimeServer(response, i);
                System.out.println(response.toString());
                System.out.println();
                System.out.println("-----------------------------------");

                break;

            } catch (UnknownHostException | SocketTimeoutException e) {
                System.err.println("Cant connect to server, or unkown host.");
                e.printStackTrace();
            }
        }
    }

    private void calculateOffsetBetweenComputerAndTimeServer(SNTPMessage message, int i) {

        double t1 = message.getOriginateTimestamp();
        double t2 = message.getReceiveTimestamp();
        double t3 = message.getTransmitTimestamp();
        double t4 = message.getReferenceTimestamp();

        double offset = ((t2 - t1) + (t3 - t4)) / 2;

        double v1 = Math.round((offset) * 100.0) / 100.0;

        System.out.println("Server Offset: " + v1 + " sec");

        serverOffsets.put(timeServers[i], offset);
    }

    private void sortOffsets() {

        System.out.println();
        System.out.println("List of sorted Server-Offsets");

        int i = 1;

        for (String key : serverOffsets.descendingKeySet()) {
            String value = serverOffsets.get(key).toString();

            double v2 = Math.round(Double.parseDouble(value) * 100.0) / 100.0;

            System.out.println(i++ + ": " + key.toUpperCase() + " = " + v2 + " sec");
        }
    }

    public static void main(String[] args) throws IOException {
        new Main();
    }
}
