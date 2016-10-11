package com.adsocket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Juan Carlos Fontecha on 04/10/2016.
 */
public class ADSConnectionRequests implements Runnable
{
    private ServerSocket server;
    private ADSConnectionHandler connectionHandler;

    public ADSConnectionRequests(ServerSocket server, ADSConnectionHandler connectionHandler)
    {
        this.server = server;
        this.connectionHandler = connectionHandler;
    }

    @Override
    public void run()
    {
        while (true)
        {
            Socket client = null;
            try
            {
                client = server.accept();

                connectionHandler.handleNewClientConnection(client);
            }
            catch (IOException e)
            {
                System.out.print("There was an error opening an incoming connection.");
            }
        }
    }

}
