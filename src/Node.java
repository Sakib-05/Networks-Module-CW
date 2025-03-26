// IN2011 Computer Networks
// Coursework 2024/2025
//
// Submission by
//  Sakib Imamul Hoque
//  230065540
//  sakib.imamul-hoque@city.ac.uk


// DO NOT EDIT starts
// This gives the interface that your code must implement.
// These descriptions are intended to help you understand how the interface
// will be used. See the RFC for how the protocol works.

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

interface NodeInterface {

    /* These methods configure your node.
     * They must both be called once after the node has been created but
     * before it is used. */

    // Set the name of the node.
    public void setNodeName(String nodeName) throws Exception;

    // Open a UDP port for sending and receiving messages.
    public void openPort(int portNumber) throws Exception;


    /*
     * These methods query and change how the network is used.
     */

    // Handle all incoming messages.
    // If you wait for more than delay milliseconds and
    // there are no new incoming messages return.
    // If delay is zero then wait for an unlimited amount of time.
    public void handleIncomingMessages(int delay) throws Exception;

    // Determines if a node can be contacted and is responding correctly.
    // Handles any messages that have arrived.
    public boolean isActive(String nodeName) throws Exception;

    // You need to keep a stack of nodes that are used to relay messages.
    // The base of the stack is the first node to be used as a relay.
    // The first node must relay to the second node and so on.

    // Adds a node name to a stack of nodes used to relay all future messages.
    public void pushRelay(String nodeName) throws Exception;

    // Pops the top entry from the stack of nodes used for relaying.
    // No effect if the stack is empty
    public void popRelay() throws Exception;


    /*
     * These methods provide access to the basic functionality of
     * CRN-25 network.
     */

    // Checks if there is an entry in the network with the given key.
    // Handles any messages that have arrived.
    public boolean exists(String key) throws Exception;

    // Reads the entry stored in the network for key.
    // If there is a value, return it.
    // If there isn't a value, return null.
    // Handles any messages that have arrived.
    public String read(String key) throws Exception;

    // Sets key to be value.
    // Returns true if it worked, false if it didn't.
    // Handles any messages that have arrived.
    public boolean write(String key, String value) throws Exception;

    // If key is set to currentValue change it to newValue.
    // Returns true if it worked, false if it didn't.
    // Handles any messages that have arrived.
    public boolean CAS(String key, String currentValue, String newValue) throws Exception;

}
// DO NOT EDIT ends

// Complete this!
public class Node implements NodeInterface {

    //node name
    String name;

    //hashID of a node is the Hash(node name)
    String hashID;

    //node IP address
    String IPAddress;

    //node UDP port
    int UDPPort;

    private DatagramSocket socket;

    // It MUST store at most three address key/value pairs for every distance
    //for each distance int dist, there is a list of address key/value pairs
    // each pair is this way (node name, IP address : UDP port number)
    HashMap<Integer, Map<String, String>> addressMap = new HashMap<>();

    //data map : data name, any string
    HashMap<String,String> dataMap = new HashMap<>();

    public void setNodeName(String nodeName) throws Exception {
	    this.name = nodeName;
    }

    public void openPort(int portNumber) throws Exception {
        this.socket = new DatagramSocket(portNumber);
        this.UDPPort = portNumber;
    }

    public void handleIncomingMessages(int delay) throws Exception {
        byte[] buffer = new byte[65535];  // Buffer to store received data
        DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);

//        The transaction ID of a response must be the same as the transaction ID of the request.

        if (delay > 0) {
            socket.setSoTimeout(delay); // Set timeout for receiving messages
        } else {
            socket.setSoTimeout(0); // Wait indefinitely if delay is 0
        }

        System.out.println("Listening for incoming messages on port " + UDPPort);

        while (true) {  // Infinite loop, but will break on timeout
            try {
                socket.receive(receivedPacket);  // Wait for an incoming packet

                // Convert received data to string
                String receivedMessage = new String(receivedPacket.getData(), 0, receivedPacket.getLength(), StandardCharsets.UTF_8);

                System.out.println("Received:" + receivedMessage);
                System.out.println("From: " + receivedPacket.getAddress() + " Port: " + receivedPacket.getPort());

            } catch (SocketTimeoutException e) {
                System.out.println("Timeout: No incoming messages received.");
                break;  // Exit the loop when timeout occurs
            } catch (Exception e) {
                System.out.println("Error while receiving message: " + e.getMessage());
                break;  // Break on any unexpected error
            }
        }
    }


    // Handles different message types
    private void processIncomingMessage(String message, java.net.InetAddress senderIP, int senderPort) throws Exception {
        String[] parts = message.split(" ");
        if (parts.length < 3) return;

        String transactionID = parts[0];
        String messageType = parts[1];

        if (messageType.equals("R")) {  // Read Request
            String key = parts[3].substring(2);
            String response;

            if (dataMap.containsKey(key)) {
                response = transactionID + " S Y 0 " + dataMap.get(key) + " ";
            } else {
                response = transactionID + " O 0 N:" + name + " 0 " + IPAddress + ":" + UDPPort + " ";
            }

            byte[] responseData = response.getBytes(StandardCharsets.UTF_8);
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, senderIP, senderPort);
            socket.send(responsePacket);
        }
    }


    public boolean isActive(String nodeName) throws Exception {
	throw new Exception("Not implemented");
    }

    public void pushRelay(String nodeName) throws Exception {
	throw new Exception("Not implemented");
    }

    public void popRelay() throws Exception {
        throw new Exception("Not implemented");
    }

    public boolean exists(String key) throws Exception {
        //A : Does the node have a key/value pair whose key matches the
        //       requested key?
        //   B : Is the node one of the three closest address key/value pairs to
        //       the requested key?

        //   * A true --> the response character MUST be 'Y'.
        //   * A false, B true --> the response character MUST be 'N'.
        //   * A false, B false --> the response character MUST be '?'.

        if(key.equals(name)){
            return true;
        }

        for (int distance : addressMap.keySet() /* for each distance*/){
            for(String searchKey : addressMap.get(distance).keySet() /* for each key in the key/value pairs at that distance*/){
                if(searchKey.equals(key)){
                    return true;
                }
            }
        }
        return false;
    }

    public String read(String key) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(2000);  // Set timeout for responses

        // Format the Read request: "AA R 0 D:key "
        String request = "AA R 0 D:" + key + " ";
        byte[] sendData = request.getBytes(StandardCharsets.UTF_8);

        for (int distance : addressMap.keySet()) {
            for (Map.Entry<String, String> entry : addressMap.get(distance).entrySet()) {
                String nodeName = entry.getKey();
                String addressPort = entry.getValue();

                String[] parts = addressPort.split(":");
                String ipAddress = parts[0];
                int port = Integer.parseInt(parts[1]);

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
                        java.net.InetAddress.getByName(ipAddress), port);
                socket.send(sendPacket);

                byte[] buffer = new byte[65535];
                DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(receivedPacket);
                    String response = new String(receivedPacket.getData(), 0, receivedPacket.getLength(), StandardCharsets.UTF_8);

                    // If we receive "AA S Y", extract and return the value
                    if (response.startsWith("AA S Y")) {
                        return response.substring(7).trim(); // Extract value from response
                    }

                    // If we receive "AA O", extract the nearest nodes and retry
                    else if (response.startsWith("AA O")) {
                        // Extract nearest nodes and continue querying them
                        String[] nearestInfo = response.substring(5).trim().split(" ");
                        for (int i = 0; i < nearestInfo.length; i += 2) {
                            String nearestNode = nearestInfo[i].substring(2); // Remove "N:"
                            String nearestAddr = nearestInfo[i + 1];

                            if (!addressMap.containsKey(distance + 1)) {
                                addressMap.put(distance + 1, new HashMap<>());
                            }
                            addressMap.get(distance + 1).put(nearestNode, nearestAddr);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout waiting for response from " + nodeName);
                }
            }
        }

        socket.close();
        return null; // Data not found
    }


    public boolean write(String key, String value) throws Exception {

        //        The transaction ID of a response must be the same as the transaction ID of the request.
        //   A write request consists of a message header followed by a single
        //   'W' character, a space, a key and a value.

        //example:
        // Received: *; W 0 N:yellow 0 10.200.51.18:20114
        // {"*;", "W", "0", "N:yellow", "0", "10.200.51.18:20114"}
        //0 - transactionID
        //1 - 'W' meaning it's a write request
        //2 - spaces in key
        //3 - key
        //4 - spaces in value
        //5 - value (IP : Port), split at ":"

        //A : Does the node have a key/value pair whose key matches the requested key?
        //B : Is the node one of the three closest address key/value pairs to the requested key?


        if (dataMap.containsKey(key)) {
            dataMap.put(key, value);
            return true; // Updated value
        }

        int closestCount = 0;
        for (int distance : addressMap.keySet()) {
            if (addressMap.get(distance).containsKey(key)) {
                closestCount++;
            }
        }

        if (closestCount < 3) {  // Only store if it's within the closest 3
            dataMap.put(key, value);
            return true;
        }

        return false; // Not within closest 3, do not store
    }


    public boolean CAS(String key, String currentValue, String newValue) throws Exception {
        /* do the following using chat:
        * figure out the distance thing, what it means and how to implement it
        * figure out why CAS return a boolean
        * figure out if key/value pairs in the CRN document are for stored data pairs or node data pairs
        * figure out a way to test for handleIncomingMessages method
        * */

        return false;
    }

    //to do extra methods:
    /*
    *name request-response messages
    *nearest
    *information
    * */

}
