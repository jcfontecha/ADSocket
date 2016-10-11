package com.adsocket;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Juan Carlos Fontecha on 04/10/2016.
 */
public class ADSConnection
{
    private ADSConnectionHandler connectionHandler;
    private int port = 1200;
    private ADSConnectionDelegate delegate;

    private ConcurrentHashMap<Integer, ObjectOutputStream> socketConnections;

    /*************** ports Property ***************/
    private List<String> ports;

    public List<String> getPorts()
    {
        return ports;
    }

    /*************** fileName Property ***************/
    private String fileName = "ports.txt";

    public String getFileName()
    {
        return fileName;
    }

    /**
     * File name or path to comma separated desired ports.
     * @param fileName
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    /*************** Helper properties Property ***************/

    public void setSendConfirmation(Consumer<ObjectOutputStream> sendConfirmation)
    {
        connectionHandler.setSendConfirmation(sendConfirmation);
    }

    public void setObjectIsNewConnectionRequest(Predicate<Object> objectIsNewConnectionRequest)
    {
        connectionHandler.setObjectIsNewConnectionRequest(objectIsNewConnectionRequest);
    }

    public void setGetPortFromObject(Function<Object, Integer> getPortFromObject)
    {
        connectionHandler.setGetPortFromObject(getPortFromObject);
    }

    public void setObjectIsValid(Predicate<Object> objectIsValid)
    {
        connectionHandler.setObjectIsValid(objectIsValid);
    }

    /**
     * Creates a new instance of ADSConnection
     * @param port The port in which the new ServerSocket should be opened.
     * @param delegate Delegate
     */
    public ADSConnection(int port, ADSConnectionDelegate delegate) throws IOException
    {
        this.port = port;
        this.delegate = delegate;

        // Create the connection handler
        try
        {
            connectionHandler = new ADSConnectionHandler(port, this);
        }
        catch (IOException e)
        {
            System.out.print("Error while creating the server in port " + port + ".\n" +
                    "Make sure a server is not already running on that port.\n");
            throw e;
        }
    }

    /**
     * Starts the server and opens the necessary connections. Before calling this,
     * make sure that you setFileName. (Default is "ports.txt")
     */
    public void start()
    {
        // Create threads that try to connect to other ports
        socketConnections = new ConcurrentHashMap<>();
        try
        {
            ports = ADSHelper.getPortsFromFile(fileName);
            for (String portString : ports)
            {
                int newPort = Integer.valueOf(portString);
                if (newPort != this.port)
                {
                    // add port to the list
                    connectionHandler.openNewConnection(newPort);
                }
            }
        } catch (IOException e)
        {
            System.out.print("There was an error opening connections to the rest of the ports.");
        }

        // Start thread to listen to incoming connections
        connectionHandler.beginAcceptingIncomingConnections();
    }

    /**
     * Broadcasts a message to all connected nodes.
     * @param obj The custom object to send
     * @param portPredicate Filters out which ports the message will be sent to.
     * @throws Exception
     */
    public void broadcastMessage(Object obj, Predicate<Integer> portPredicate) throws Exception
    {
        Enumeration<Integer> keys = socketConnections.keys();
        while (keys.hasMoreElements())
        {
            int nextPort = keys.nextElement();
            if (portPredicate.test(nextPort))
            {
                sendMessage(obj, nextPort);
            }
        }
    }

    /**
     * Sends a custom object to a particular node or port.
     * @param obj The custom object to send
     * @param destination The desination's port.
     * @throws IOException
     */
    public void sendMessage(Object obj, int destination) throws IOException
    {
        ObjectOutputStream out = socketConnections.get(destination);

        if (out != null)
            out.writeObject(obj);
    }

    public void didReceiveMessage(Object obj)
    {
        delegate.didReceiveMessage(obj);
    }

    public void didEstablishConnection(int port, ObjectOutputStream out)
    {
        socketConnections.put(port, out);
        delegate.didConnectNode(port);
    }

    public void didLoseConnection(int port)
    {
        socketConnections.remove(port);

        try
        {
            connectionHandler.openNewConnection(port);
        }
        catch (IOException ignored) { }

        delegate.didDisconnectNode(port);
    }
}
