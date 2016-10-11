package com.adsocket;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Juan Carlos Fontecha on 04/10/2016.
 */
public class ADSClientConnection implements Runnable
{
    private ADSConnectionHandler handler;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

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

    public ADSClientConnection(Socket socket, ADSConnectionHandler hanlder) throws IOException
    {
        try
        {
            socket.setSoTimeout(0);
            socket.setKeepAlive(true);
        }
        catch (SocketException e) {}

        this.socket = socket;
        this.handler = hanlder;

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

                if (obj != null)
                {
                    if (objectIsValid.test(obj))
                    {
                        if (objectIsNewConnectionRequest.test(obj))
                        {
                            int port = getPortFromObject.apply(obj);

                            out = handler.getAcceptedConnections().get(port);
                            if (out != null)
                            {
                                handler.didAcceptConnection(port, out);
                            }
                            else
                            {
                                handler.getPendingConnections().put(port, true);
                            }
                        }
                        else
                        {
                            handler.didReceiveMessage(obj);
                        }
                    }
                    else
                    {
                        return;
                    }
                }
            }
            catch (IOException e) {}
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
