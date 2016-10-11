package com.adsocket;

import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Juan Carlos Fontecha on 04/10/2016.
 */
public class ADSConnectionHandler
{
    private ADSConnection handler;
    private ServerSocket server;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private int port;

    public int getPort() { return port; }

    private Consumer<ObjectOutputStream> sendConfirmation;

    public void setSendConfirmation(Consumer<ObjectOutputStream> sendConfirmation)
    {
        this.sendConfirmation = sendConfirmation;
    }

    private Predicate<Object> objectIsNewConnectionRequest;

    public void setObjectIsNewConnectionRequest(Predicate<Object> objectIsNewConnectionRequest)
    {
        this.objectIsNewConnectionRequest = objectIsNewConnectionRequest;
    }

    private Function<Object, Integer> getPortFromObject;

    public void setGetPortFromObject(Function<Object, Integer> getPortFromObject)
    {
        this.getPortFromObject = getPortFromObject;
    }

    private Predicate<Object> objectIsValid;

    public void setObjectIsValid(Predicate<Object> objectIsValid)
    {
        this.objectIsValid = objectIsValid;
    }

    private ConcurrentHashMap<Integer, ObjectOutputStream> acceptedConnections;

    public ConcurrentHashMap<Integer, ObjectOutputStream> getAcceptedConnections()
    {
        return acceptedConnections;
    }

    private ConcurrentHashMap<Integer, Boolean> pendingConnections;

    public ConcurrentHashMap<Integer, Boolean> getPendingConnections()
    {
        return pendingConnections;
    }

    public ADSConnectionHandler(int port, ADSConnection handler)
    {
        this.port = port;
        this.handler = handler;

        this.acceptedConnections = new ConcurrentHashMap<>();
        this.pendingConnections = new ConcurrentHashMap<>();

        try
        {
            server = new ServerSocket(port);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void handleNewClientConnection(Socket client) throws IOException
    {
        ADSClientConnection connection = new ADSClientConnection(client, this);
        connection.setObjectIsNewConnectionRequest(objectIsNewConnectionRequest);
        connection.setGetPortFromObject(getPortFromObject);
        connection.setObjectIsValid(objectIsValid);

        executor.execute(connection);
    }

    public void openNewConnection(int port) throws IOException
    {
        ADSServerConnection connection = new ADSServerConnection(port, this);
        connection.setSendConfirmation(sendConfirmation);

        executor.execute(connection);
    }

    public void beginAcceptingIncomingConnections()
    {
        ADSConnectionRequests requestsTask = new ADSConnectionRequests(server, this);
        executor.execute(requestsTask);
    }

    public void didReceiveMessage(Object obj)
    {
        handler.didReceiveMessage(obj);
    }

    public void didAcceptConnection(int port, ObjectOutputStream out)
    {
        handler.didEstablishConnection(port, out);
    }
}
