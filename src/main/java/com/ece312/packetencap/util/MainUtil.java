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
        System.out.println("Welcome to Java Socket Chat Server v1.00");
        System.out.println("Please Enter a Port Number: ");
        setPort(scanner.nextInt());

        // This gets the current IP of the host
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));

            String ip = in.readLine(); //you get the IP as a String

            System.out.println("Starting Server on local address: " + InetAddress.getLocalHost().getHostAddress() +
                    " Global Address: " + ip + " and port: " + getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new MainClient()).start();

        while (getChannel() == null) ;

        System.out.println("Ready to handle commands.");
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
                case "1":
                    sendRHPControlMessage(new RoseObject("hello"), Constants.CONTROL_MESSAGE_TYPE);
                    break;
                case "2":
                    sendRHPControlMessage(new RoseObject(new RoseHulmanMessageProtocol(Constants.RHMP_ID_REQUEST_TYPE)),
                            Constants.RHMP_MESSAGE_TYPE);
                    break;
                case "3":
                    sendRHPControlMessage(new RoseObject(new RoseHulmanMessageProtocol(Constants.RHMP_MESSAGE_REQUEST_TYPE)),
                            Constants.RHMP_MESSAGE_TYPE);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Prints a simple cursor
     */
    public void printCursor() {
        System.out.print("<SHELL CITY>");
    }

    private void sendRHPControlMessage(RoseObject payload, int type) {
        RoseHulmanProtocol protocol = new RoseHulmanProtocol(type, getPort(), payload);
        ByteBuf message = protocol.createMessage();

        try {
            getChannel().writeAndFlush(new DatagramPacket(
                    message,
                    new InetSocketAddress(Constants.SERVER_IP, Constants.SERVER_PORT))).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
}
