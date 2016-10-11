package com.adsocket;

import java.io.ObjectOutputStream;
import java.io.IOException;
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

    private List<String> ports;
    public List<String> getPorts()
    {
        return ports;
    }

    private ConcurrentHashMap<Integer, ObjectOutputStream> socketConnections;

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

    public ADSConnection(int port, ADSConnectionDelegate delegate)
    {
        this.port = port;
        this.delegate = delegate;

        connectionHandler = new ADSConnectionHandler(port, this);
    }

    public void start()
    {
        // Create threads that try to connect to other ports
        socketConnections = new ConcurrentHashMap<>();
        try
        {
            ports = ADSHelper.getPortsFromFile("ports.txt");
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

    public void sendMessage(Object obj, int destination) throws IOException
    {
        //Socket socket = socketConnections.get(port);
        ObjectOutputStream out = socketConnections.get(destination);
        //out.flush();

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
}
