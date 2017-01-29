package com.ece312.packetencap.util;

/**
 * Created by kuzalj on 1/28/2017.
 */

import com.ece312.packetencap.server.MainServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Main utility class
 */
public class MainUtil {

    private static MainUtil instance;
    private String localUsername;
    private int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelGroup broadcast;

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
        new Thread(new MainServer(getPort())).start();

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
                    sendData(command, null);
                    printCursor();
                    break;
            }
        }
    }

    /**
     * Prints the <username> cursor to the console
     */
    public void printCursor() {
        System.out.print("<SHELL CITY>");
    }

    public void sendData(String data, Channel exclude) {
        for (Channel channel : getBroadcast()) {
            if ((exclude != null && channel != exclude) || exclude == null) {
                final ByteBuf message = channel.alloc().buffer(1024);// (2)
                message.writeCharSequence(data, Charset.defaultCharset());
                channel.writeAndFlush(message).awaitUninterruptibly().isSuccess();
            }
        }
    }

    /**
     * Exits gracefully
     */
    public void exit() {
        sendData("exit", null);
        getBossGroup().shutdownGracefully();
        getWorkerGroup().shutdownGracefully();
        System.exit(0);
    }

    private String getLocalUsername() {
        return localUsername;
    }

    private void setLocalUsername(String localUsername) {
        this.localUsername = localUsername;
    }

    private int getPort() {
        return port;
    }

    private void setPort(int port) {
        this.port = port;
    }

    public EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    public void setBossGroup(EventLoopGroup bossGroup) {
        this.bossGroup = bossGroup;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public void setWorkerGroup(EventLoopGroup workerGroup) {
        this.workerGroup = workerGroup;
    }

    public ChannelGroup getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(ChannelGroup broadcast) {
        this.broadcast = broadcast;
    }
}
