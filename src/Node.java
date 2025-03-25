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
    // If you wait for more than delay miliseconds and
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

        if (delay > 0) {
            socket.setSoTimeout(delay); // Set timeout for receiving messages
        } else {
            socket.setSoTimeout(0); // Wait indefinitely if delay is 0
        }

        System.out.println("Listening for incoming messages on port " + UDPPort);

        try {
            socket.receive(receivedPacket);  // Wait for an incoming packet

            // Convert received data to string
            String receivedMessage = new String(receivedPacket.getData(), 0, receivedPacket.getLength(), StandardCharsets.UTF_8);

            System.out.println("Received message: " + receivedMessage);
            System.out.println("From: " + receivedPacket.getAddress() + " Port: " + receivedPacket.getPort());

        } catch (SocketTimeoutException e) {
            System.out.println("Timeout: No incoming messages received.");
        } finally {
            socket.close(); // Close socket after use
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
	    if(dataMap.containsKey(key)){
            return dataMap.get(key);
        }
        return null;
    }

    public boolean write(String key, String value) throws Exception {
//        dataMap.put(key,value);
        //A : Does the node have a key/value pair whose key matches the
        //       requested key?
        //   B : Is the node one of the three closest address key/value pairs to
        //       the requested key?

        //   * A true --> the node MUST replace the key/value pair with the
        //     requested key/value pair and the response character MUST be 'R'.
        //   * A false, B true --> the node MUST store the requested key/value
        //     and the pair the response character MUST be 'A'. THIS SENTENCE DOESN’T MAKE SENSE
        //   * A false, B false --> the response character MUST be 'X'.
        if(dataMap.containsKey(key)){
            dataMap.put(key, value);
            return true;
        }

        dataMap.put(key, value);
        return true;

    }

    public boolean CAS(String key, String currentValue, String newValue) throws Exception {
	throw new Exception("Not implemented");
    }
}
