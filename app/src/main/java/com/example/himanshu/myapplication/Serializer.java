package com.example.himanshu.myapplication;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Created by Himanshu on 9/6/2016.
 */

public class Serializer {

    public static byte[] ObjectToBytes (MessageFormat yourObject)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] yourBytes=new byte[100];
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(yourObject);
            yourBytes = bos.toByteArray();
            Log.d("Serializer","Returning from here 1!!");
            return yourBytes;
        }
        catch(Exception e)
        {
            Log.d("avBar","Exception occured!!"+e);
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        Log.d("Serializer","returning from here 2!!Value of yourBytes::"+yourBytes);
        return yourBytes;
    }
    public static MessageFormat bytesToObject(byte[] yourBytes)
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes);
        MessageFormat o=new MessageFormat();
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            o = (MessageFormat) in.readObject();
            return o;
        }
        catch(Exception e)
        {
            Log.d("avBar","Exception occured!!"+e);
        }
        finally {
            try {
                bis.close();
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return o;
    }

}
