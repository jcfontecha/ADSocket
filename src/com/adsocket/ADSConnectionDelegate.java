package com.adsocket;

/**
 * Created by Juan Carlos Fontecha on 04/10/2016.
 */
public interface ADSConnectionDelegate
{
    /**
     * Called when ADSConnection receives a new message.
     * @param obj The received custom object.
     */
    void didReceiveMessage(Object obj);

    /**
     * Called when a new node is connected to ADSConnection.
     * @param port The port of the new connection.
     */
    void didConnectNode(int port);


    /**
     * Called when a connection is lost from ADSConnection.
     * @param port The port of the lost connection.
     */
    void didDisconnectNode(int port);
}