package com.adsocket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Juan Carlos Fontecha on 04/10/2016.
 */
public class ADSClientConnection implements Runnable
{
    private ADSConnectionHandler delegate;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private int port = -1;

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

    /**
     * Creates a new instance of ADSClientConnection
     * @param socket The recently connected client socket.
     * @param delegate
     * @throws IOException
     */
    public ADSClientConnection(Socket socket, ADSConnectionHandler delegate) throws IOException
    {
        socket.setSoTimeout(0);
        socket.setKeepAlive(true);

        this.socket = socket;
        this.delegate = delegate;

        in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run()
    {
        while(!socket.isClosed())
        {
            try
            {
                Object obj = in.readObject();

                if (obj != null && objectIsValid.test(obj))
                {
                    // Handle the case where the object is a new connection request.
                    if (objectIsNewConnectionRequest.test(obj))
                    {
                        port = getPortFromObject.apply(obj);

                        // Has ServerConnection received the other node's client
                        // request? If so, we are done.
                        if (delegate.hasAcceptedConnection(port))
                        {
                            out = delegate.removeAcceptedConnection(port);
                            delegate.didAcceptConnection(port, out);
                        }
                        // If not, we need to wait for that connection.
                        else
                        {
                            delegate.addPendingConnection(port);
                        }
                    }
                    // Handle the case where it is a regular message.
                    else
                    {
                        delegate.didReceiveMessage(obj);
                    }
                }
            }
            catch (IOException e)
            {
                // Lost connection
                if (port > 0)
                {
                    delegate.didLostConnection(port);
                }

                return;
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            closeConnection();
        }
        catch (IOException e) {}
    }

    private void closeConnection() throws IOException
    {
        socket.close();
    }
}
