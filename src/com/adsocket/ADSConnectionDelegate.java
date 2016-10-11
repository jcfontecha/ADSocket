package com.adsocket;

/**
 * Created by Juan Carlos Fontecha on 04/10/2016.
 */
public interface ADSConnectionDelegate
{
    void didReceiveMessage(Object obj);
    void didConnectNode(int port);
}