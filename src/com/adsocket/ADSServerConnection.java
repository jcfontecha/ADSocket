package com.adsocket;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Created by Fontecha on 04/10/2016.
 */
public class ADSServerConnection implements Runnable
{
    private ADSConnectionHandler delegate;
    private int port;
    private Consumer<ObjectOutputStream> sendConfirmation;

    /**
     * Sets the custom code to send a custom new connection request.
     * @param sendConfirmation
     */
    public void setSendConfirmation(Consumer<ObjectOutputStream> sendConfirmation)
    {
        this.sendConfirmation = sendConfirmation;
    }


    /**
     * Creates a new instance of ADSServerConnection
     * @param port The port in which ADSConnection is running on.
     * @param delegate
     */
    public ADSServerConnection(int port, ADSConnectionHandler delegate)
    {
        this.port = port;
        this.delegate = delegate;
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

                // Has ClientConnection established connection with the other
                // node's server? If so, we are done and can accept the connection.
                if (delegate.hasPendingConnection(port))
                {
                    delegate.didAcceptConnection(port, out);
                    delegate.removePendingConnection(port);
                }
                // If not, we save the OOS and wait for ClientConnection to finish
                // connect to the other node's server.
                else
                {
                    delegate.addAcceptedConnection(port, out);
                }

                sendConfirmation.accept(out);
            }
            catch (IOException e) { }
        }

    }
}
