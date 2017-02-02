package com.ece312.packetencap.util;

import com.ece312.packetencap.client.MainClient;
import com.ece312.packetencap.rhp.RoseHulmanMessageProtocol;
import com.ece312.packetencap.rhp.RoseHulmanProtocol;
import com.ece312.packetencap.rhp.RoseObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.DatagramPacket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Scanner;

/**
 * Main utility class
 */
public class MainUtil {

    private static MainUtil instance;
    private int port;
    private EventLoopGroup workerGroup;
    private Channel channel;
    private RoseHulmanProtocol response = null;

    private MainUtil() {

    }

    public static MainUtil getInstance() {
        if (instance == null)
            instance = new MainUtil();
        return instance;
    }

    /**
     * Initial run method
     */
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Java Packet Encapsulation v1.00");
        System.out.print("Please Enter a srcPort Number: ");
        setPort(scanner.nextInt());

        // This gets the current IP of the host
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));

            String ip = in.readLine(); //you get the IP as a String

            System.out.println("Starting up on local address: " + InetAddress.getLocalHost().getHostAddress() +
                    " Global Address: " + ip + " and srcPort: " + getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new MainClient()).start();

        while (getChannel() == null) ;

        System.out.println("Ready to handle commands. Type \"start\" to begin");
        printCursor();
        handleCommands(scanner);
    }

    /**
     * This handles data that is typed in by the user on the client side, and sends that data to clients
     *
     * @param scanner System.in scanner
     */
    private void handleCommands(Scanner scanner) {
        String command;
        while (!(command = scanner.nextLine()).equals("escape")) {
            switch (command.trim()) {
                case "exit":
                    scanner.close();
                    exit();
                    break;
                case "start":
                    startMultipleTransmission();
                    break;
                case "change srcport":
                    changeSrcPort(scanner);
                    break;
                case "1":
                    sendRHPControlMessage(new RoseObject("hello"), Constants.CONTROL_MESSAGE_TYPE);
                    while (getResponse() == null) ;
                    System.out.println(getResponse());
                    break;
                case "2":
                    sendRHPControlMessage(new RoseObject(new RoseHulmanMessageProtocol(Constants.RHMP_ID_REQUEST_TYPE)),
                            Constants.RHMP_MESSAGE_TYPE);
                    while (getResponse() == null) ;
                    System.out.println(getResponse());
                    break;
                case "3":
                    sendRHPControlMessage(new RoseObject(new RoseHulmanMessageProtocol(Constants.RHMP_MESSAGE_REQUEST_TYPE)),
                            Constants.RHMP_MESSAGE_TYPE);
                    while (getResponse() == null) ;
                    System.out.println(getResponse());
                    break;
                default:
                    if (!command.trim().isEmpty())
                        System.out.println("Invalid command!");
                    break;
            }
            if (!command.trim().isEmpty())
                printCursor();
            setResponse(null);
        }
    }

    private void changeSrcPort(Scanner scanner) {
        System.out.print("Please Enter a srcPort Number: ");
        setPort(scanner.nextInt());
    }

    private void startMultipleTransmission() {
        System.out.println("Starting multiple transmissions...");
        System.out.println("This will transmit the UDP messages...");
        System.out.println("1. A RHP Control Message with the type CONTROL (1) and a payload of 'hello'");
        System.out.println("2. A RHMP message with ID_REQUEST type");
        System.out.println("3. A RHMP message with MESSAGE_REQUEST type");
        System.out.println("All parameters will be printed out before and after transmission. \n" +
                "If a checksum fails, transmission will be repeated.");

        System.out.println("Beginning transmission in 5 seconds...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sendRHPMessageWithDataChecking(new RoseObject("hello"), Constants.CONTROL_MESSAGE_TYPE);
        sendRHPMessageWithDataChecking(new RoseObject(new RoseHulmanMessageProtocol(Constants.RHMP_ID_REQUEST_TYPE)),
                Constants.RHMP_MESSAGE_TYPE);
        sendRHPMessageWithDataChecking(new RoseObject(new RoseHulmanMessageProtocol(Constants.RHMP_MESSAGE_REQUEST_TYPE)),
                Constants.RHMP_MESSAGE_TYPE);

    }

    private void sendRHPMessageWithDataChecking(RoseObject roseObject, int type) {
        RoseHulmanProtocol protocol;
        boolean checksumValid = false;

        while (!checksumValid) {
            protocol = sendRHPControlMessage(roseObject, type);
            System.out.println("Sent message to " + Constants.SERVER_IP + " at port " + Constants.SERVER_PORT + " with parameters,");
            System.out.println(protocol);
            while (getResponse() == null) ;
            System.out.println();
            System.out.println("Response received,");

            checksumValid = getResponse().isChecksumValid();
            if (!getResponse().isChecksumValid()) {
                System.out.println("Checksum NOT valid! Repeating transmission!");
                System.out.println();
            } else {
                System.out.println(getResponse());
                System.out.println();
            }
        }
        setResponse(null);
    }

    /**
     * Prints a simple cursor
     */
    public void printCursor() {
        System.out.print("<SHELL CITY>");
    }

    private RoseHulmanProtocol sendRHPControlMessage(RoseObject payload, int type) {
        RoseHulmanProtocol protocol = new RoseHulmanProtocol(type, getPort(), payload);
        ByteBuf message = protocol.createMessage();

        try {
            getChannel().writeAndFlush(new DatagramPacket(
                    message,
                    new InetSocketAddress(Constants.SERVER_IP, Constants.SERVER_PORT))).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return protocol;
    }

    public long calculateChecksum(byte[] buf) {
        int length = buf.length;
        int i = 0;

        long sum = 0;
        long data;

        // Handle all pairs
        while (length > 1) {
            // Corrected to include @Andy's edits and various comments on Stack Overflow
            data = (((buf[i] << 8) & 0xFF00) | ((buf[i + 1]) & 0xFF));
            sum += data;
            // 1's complement carry bit correction in 16-bits (detecting sign extension)
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }

            i += 2;
            length -= 2;
        }

        // Handle remaining byte in odd length buffers
        if (length > 0) {
            // Corrected to include @Andy's edits and various comments on Stack Overflow
            sum += (buf[i] << 8 & 0xFF00);
            // 1's complement carry bit correction in 16-bits (detecting sign extension)
            if ((sum & 0xFFFF0000) > 0) {
                sum = sum & 0xFFFF;
                sum += 1;
            }
        }

        // Final 1's complement value correction to 16-bits
        sum = ~sum;
        sum = sum & 0xFFFF;
        return sum;

    }

    /**
     * Exits gracefully
     */
    private void exit() {
        getWorkerGroup().shutdownGracefully();
        System.exit(0);
    }

    private int getPort() {
        return port;
    }

    private void setPort(int port) {
        this.port = port;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public synchronized RoseHulmanProtocol getResponse() {
        return response;
    }

    public synchronized void setResponse(RoseHulmanProtocol response) {
        this.response = response;
    }
}
