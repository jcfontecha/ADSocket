package com.adsocket;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Juan Carlos Fontecha on 04/10/2016.
 */
public class ADSHelper
{
    static public List<String> getPortsFromFile(String fileName) throws IOException
    {
        return separateString(readFile(fileName));
    }

    static private String readFile(String fileName) throws IOException
    {
        String everything = "";
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            everything = sb.toString().substring(0, sb.length() - 2);
        }
        catch (IOException e)
        {
            throw e;
        }

        return everything;
    }

    static private List<String> separateString(String in)
    {
        return Arrays.asList(in.split(","));
    }
}
