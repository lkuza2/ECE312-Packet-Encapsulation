# ECE312-packet-encapsulation

ECE312 Project 2, packet encapsulation project.
This branch of the ECE312 project contains the code to communicate with the ECE312 test server and supports the Rose Hulman Protocol, and Rose Hulman Message Protocol.  The code utilizes Java SDK 8, and the Netty.IO sockets API.  Netty is an asynchronous event-driven network application framework, allowing UDP connections to occur without blocking the main thread.  Netty also allows for a modularized framework, and UDP transmission speeds as efficently as possible (inline with C code speed).  This moularity allows for multiple instances of the client to run at once.

The application itself is threaded, with the main thread controlling the user interface on the command line, and the second thread managing the UDP connections. There application supports multiple commands, allowing for the program to operate to the specifications required.

Upon starting the application, you must enter a Srcport that will be sent to the ECE312 test server.

The most important command is the "start" command.  Simply using this commmand, will begin transmission for a RHP Control message, and two different RHMP messages. It will check checksums, and retransmit until proper checksums are verified.  In addition, it will print out all parameters of the messaging being sent, and the once it recieves.  

The command "change srcport" allows you to change the Srcport to send to the ECE312 test server.

There are three debug commands, "1", "2", "3", which will send the transmissions seperately from the "start" command.

The application also recognizes the kill command "exit", which simply exits the application.

# Compiling

Compiling is simple with Maven.  You will need Maven and the Java SDK 8 or above. From there, compiling to a runnable "executable" is as simple as "mvn compiler:compile package".  A file will be created called packet-encapsulation-1.0-SNAPSHOT-jar-with-dependencies.jar.

# Running
To run, simply execute the JAR in the command line.  If you downloaded the release packages, "run-windows.bat" will work in windows, and "run-linux.sh" will work on Linux.  Linux users need to type "sh run-linux.sh".

If you compiled the project yourself, simply type "java -jar target.jar" in the command line to run the program.
