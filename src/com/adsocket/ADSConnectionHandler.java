package com.adsocket;

import java.io.IOException;
import java.io.ObjectOutputStream;
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
    private ExecutorService executor = Executors.newCachedThreadPool();

    private ADSConnection delegate;
    private ServerSocket server;

    /******** port Property ********/

    private int port;

    public int getPort() { return port; }

    /******** sendConfirmation Property ********/

    private Consumer<ObjectOutputStream> sendConfirmation;

    /**
     * Consumer code to send a custom object announcing a new connection
     * @param sendConfirmation
     */
    public void setSendConfirmation(Consumer<ObjectOutputStream> sendConfirmation)
    {
        this.sendConfirmation = sendConfirmation;
    }

    /******** objectIsNewConnectionRequest Property ********/

    private Predicate<Object> objectIsNewConnectionRequest;

    /**
     * Predicate to determine if a received object is a new connection request.
     * @param objectIsNewConnectionRequest
     */
    public void setObjectIsNewConnectionRequest(Predicate<Object> objectIsNewConnectionRequest)
    {
        this.objectIsNewConnectionRequest = objectIsNewConnectionRequest;
    }

    /******** getPortFromObject Property ********/

    private Function<Object, Integer> getPortFromObject;

    /**
     * Function that received a custom object and should extract and return the sender's port.
     * @param getPortFromObject
     */
    public void setGetPortFromObject(Function<Object, Integer> getPortFromObject)
    {
        this.getPortFromObject = getPortFromObject;
    }

    /******** objectIsValid Property ********/

    private Predicate<Object> objectIsValid;

    /**
     * Predicate to filter out received messages.
     * @param objectIsValid
     */
    public void setObjectIsValid(Predicate<Object> objectIsValid)
    {
        this.objectIsValid = objectIsValid;
    }

    /******** Accepted Connections Property ********/

    private ConcurrentHashMap<Integer, ObjectOutputStream> acceptedConnections;

    public ConcurrentHashMap<Integer, ObjectOutputStream> getAcceptedConnections()
    {
        return acceptedConnections;
    }

    /******** Pending Connections Property ********/

    private ConcurrentHashMap<Integer, Boolean> pendingConnections;

    public ConcurrentHashMap<Integer, Boolean> getPendingConnections()
    {
        return pendingConnections;
    }

    /**
     * Creates a new instance of ADSConnectionHandler
     * @param port The port to open the ServerSocket in.
     * @param delegate
     */
    public ADSConnectionHandler(int port, ADSConnection delegate) throws IOException
    {
        this.port = port;
        this.delegate = delegate;

        this.acceptedConnections = new ConcurrentHashMap<>();
        this.pendingConnections = new ConcurrentHashMap<>();

        server = new ServerSocket(port);
    }

    /**
     * Handles a new connection from a client to this node's server.
     * @param client The client's socket.
     * @throws IOException Failed to create the new client connection
     */
    public void handleNewClientConnection(Socket client) throws IOException
    {
        ADSClientConnection connection = new ADSClientConnection(client, this);
        connection.setObjectIsNewConnectionRequest(objectIsNewConnectionRequest);
        connection.setGetPortFromObject(getPortFromObject);
        connection.setObjectIsValid(objectIsValid);

        executor.execute(connection);
    }

    /**
     * Opens a new connection to a particular port's server.
     * @param port The port that we want to become clients of.
     * @throws IOException Failed to open the connection
     */
    public void openNewConnection(int port) throws IOException
    {
        ADSServerConnection connection = new ADSServerConnection(port, this);
        connection.setSendConfirmation(sendConfirmation);

        executor.execute(connection);
    }

    /**
     * Start loop in thread to accept incoming connections.
     */
    public void beginAcceptingIncomingConnections()
    {
        ADSConnectionRequests requestsTask = new ADSConnectionRequests(server, this);
        executor.execute(requestsTask);
    }

    /********** START Accessors for pendingConnections and acceptedConnections *************/

    public Boolean hasPendingConnection(int port)
    {
        return pendingConnections.get(port) != null;
    }

    public void addPendingConnection(int port)
    {
        pendingConnections.put(port, true);
    }

    public void removePendingConnection(int port)
    {
        pendingConnections.remove(port);
    }

    public Boolean hasAcceptedConnection(int port)
    {
        return acceptedConnections.get(port) != null;
    }

    public void addAcceptedConnection(int port, ObjectOutputStream out)
    {
        acceptedConnections.put(port, out);
    }

    public ObjectOutputStream removeAcceptedConnection(int port)
    {
        return acceptedConnections.remove(port);
    }

    /********** END Accessors for pendingConnections and acceptedConnections *************/

    /**
     * Escalates the event of receiving a new message to ADSConnection.
     * @param obj
     */
    public void didReceiveMessage(Object obj)
    {
        delegate.didReceiveMessage(obj);
    }

    /**
     * Escalates the event of accepting a new connection to ADSConnection.
     * @param port The port of the connection that was established.
     * @param out The ObjectOutputStream of the new connection.
     */
    public void didAcceptConnection(int port, ObjectOutputStream out)
    {
        delegate.didEstablishConnection(port, out);
    }

    /**
     * Escalates the event of losing a connection to ADSConnection
     * @param port The port of the recently lost connection.
     */
    public void didLostConnection(int port)
    {
        delegate.didLoseConnection(port);
    }
}
