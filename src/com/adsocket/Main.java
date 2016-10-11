package com.adsocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Main implements ADSConnectionDelegate
{

    public static void main(String[] args) {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Port: ");
        try
        {
            int port = Integer.parseInt(br.readLine());

            // Create the object, set the port and the delegate of
            // the connection. (This same class in this case)
            ADSConnection node = new ADSConnection(port, new Main());

            // Your code to send a confirmation object and announce this node's
            // port to the receiving node.
            node.setSendConfirmation((out) -> {
                HashMap<String, String> p = new HashMap<String, String>();
                p.put("header", "new connection");
                p.put("value", Integer.toString(port));

                try
                {
                    out.writeObject(p);
                }
                catch (IOException e) { }
            });

            // Your code to read the confirmation object you sent in the previous
            // piece of code. Returns a boolean.
            node.setObjectIsNewConnectionRequest((obj) -> {
                HashMap<String, String> p = (HashMap<String, String>)obj;
                return p.get("header").equals("new connection");
            });

            // Your code to extract the port number from whatever
            // confirmation object you decided to use.
            node.setGetPortFromObject((obj) -> {
                HashMap<String, String> p = (HashMap<String, String>)obj;

                if (p.get("header").equals("new connection"))
                    return Integer.valueOf(p.get("value"));
                else
                    return Integer.valueOf(p.get("sender"));
            });

            // Adds one extra layer of validation to is object that is received.
            // If this returns false, didReceiveMessage() is not called
            // and the received object is discarded.
            node.setObjectIsValid((obj) -> {
                return true;
            });

            node.start();
        }
        catch(NumberFormatException nfe){ }
        catch (IOException e) { }
        catch (Exception e) { }
    }

    @Override
    public void didReceiveMessage(Object obj)
    {
        System.out.print("New Message Code here.\n");
    }

    @Override
    public void didConnectNode(int port)
    {
        System.out.print("New Connection Code here.\n");
    }
}
