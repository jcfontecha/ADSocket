package com.adsocket;

import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Created by Fontecha on 04/10/2016.
 */
public class ADSServerConnection implements Runnable
{
    private ADSConnectionHandler handler;

    private int port;

    private Consumer<ObjectOutputStream> sendConfirmation;

    public void setSendConfirmation(Consumer<ObjectOutputStream> sendConfirmation)
    {
        this.sendConfirmation = sendConfirmation;
    }

    public ADSServerConnection(int port, ADSConnectionHandler handler)
    {
        this.port = port;
        this.handler = handler;
    }

    @Override
    public void run()
    {
        Socket socket = null;
        while (socket == null)
        {
            try
            {
                socket = new Socket("localhost", port);

                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                if (handler.getPendingConnections().get(port))
                {
                    handler.didAcceptConnection(port, out);
                    handler.getPendingConnections().remove(port);
                }
                else
                {
                    handler.getAcceptedConnections().put(port, out);
                }

                sendConfirmation.accept(out);
            } catch (IOException e) { }
        }

    }
}
