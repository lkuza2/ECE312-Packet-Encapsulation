package com.ece312.packetencap.util;

import com.ece312.packetencap.server.MainClient;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

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

        System.out.println("Ready to handle commands.");
        printCursor();
        handleCommands(scanner);
    }

    /**
     * This handles data that is typed in by the user on the server side, and sends that data to clients
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
                case "":
                    break;
                default:
                    sendData(command);
                    printCursor();
                    break;
            }
        }
    }

    /**
     * Prints a simple cursor
     */
    private void printCursor() {
        System.out.print("<SHELL CITY>");
    }

    public void sendData(String data) {
        try {

            getChannel().writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer(data, CharsetUtil.UTF_8),
                    new InetSocketAddress(Constants.SERVER_IP, Constants.SERVER_PORT))).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
