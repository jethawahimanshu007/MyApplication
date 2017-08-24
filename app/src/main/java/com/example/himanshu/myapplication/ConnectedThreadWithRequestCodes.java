package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


/**
 * Created by Himanshu on 8/3/2016.
 */
class ConnectedThreadWithRequestCodes extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    public final OutputStream mmOutStream;
    SQLiteDatabase mydatabase;
    Context context;
    int numImages=0;
    int imageCount=0;
    int numImagesBack=0;
    int imageBackCount=0;
    int imageBackAckCount=0;
    int numAcksRequired=0;
    String Role=new String();
    public ConnectedThreadWithRequestCodes(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;


        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.d("CTwReqCode", "Exception is thrown:" + e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

    }

    public ConnectedThreadWithRequestCodes(BluetoothSocket socket, Context context) {


        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.context = context;
        if (context == null) {
            Log.d("CTwRC", "context is null");
        }
        mydatabase = context.openOrCreateDatabase(Constants.DATABASE_NAME, context.MODE_PRIVATE, null);


        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.d("CTwReqCode", "Exception is thrown:" + e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        Log.d("CTwReqCodes", "ConnectedThreadWithRequestCodes is running,mmSocket is:" + mmSocket);

        byte[] preamble = new byte[Constants.PREAMBLE_SIZE];

        int bytes = 0;

        FileOutputStream fos = null;
        int lengthFileName = 0, lengthFile = 0;
        String fileNameImage="";
        String latitudeReceived="";
        String longitudeReceived="";
        String timestampReceived="";
        String tagsReceived="";
        int fileNameLength;
        String mimeReceived="",formatRecived="",macAddrReceived="",deviceNameReceived="",UUIDReceived="";
        long sizeReceived=0,qualityReceived=0,priorityReceived=0;
        int MessageType;
        int MessageSize = 0;
        String MessageFileName = "";
        int sleepFlag = 0;
        int i = 0;

        while (true) {

            MessageSize=0;
            try {

                //Log.d("CTwRC","Inside CTwRC first run function:: Is mmInStream null?:"+(mmInStream==null));

                if (mmInStream.available() > 0) {

                int countForPreamble = 0;
                    try {
                        Log.d("CTwRC","Length of preamble before reading is:"+preamble.length);
                        while (countForPreamble != Constants.PREAMBLE_SIZE) {
                            bytes = mmInStream.read(preamble, 0, preamble.length - countForPreamble);
                            countForPreamble += bytes;
                        }
                    }
                    catch(Exception e)
                    {
                        Log.d("CTwRC","Exception occured in suspected place"+e);
                    }
                    try {
                        Log.d("CTwRC","Trying inside the suspected block 2");
                        if (preamble == null) {
                            Log.d("CTwRC", "preable is null");
                        } else {
                            Log.d("CTwRC", "preable is not null");
                        }

                    }
                    catch(Exception e)
                    {
                        Log.d("CTwRC","Exception caught inside suspected block 2:"+e);
                    }
                Log.d("CTwReqCodes", "Preamble received is:" + new String((byte[])preamble));

                if (new String(preamble).split("::")[0].equals("Preamble")) {

                    MessageType = Integer.parseInt(new String(preamble).split("::")[1].split(":")[1]);

                    if(MessageType!=Constants.MESSAGE_IMAGE_ACK && MessageType!=Constants.MESSAGE_IMAGE_BACK_ACK && MessageType!=Constants.MESSAGE_INCENT_REQ && MessageType!=Constants.MESSAGE_INCENT_REW && MessageType!=Constants.MESSAGE_INCENT_REP && MessageType!=Constants.MESSAGE_TRANS_DONE) {
                        MessageSize = Integer.parseInt(new String(preamble).split("::")[2].split(":")[1]);
                        Role = new String(preamble).split("::")[3].split(":")[1];
                        if (new String(preamble).contains("Lengths")) {
                            fileNameImage = new String(preamble).split("::")[3].split(":")[1];
                            fileNameLength = Integer.parseInt(new String(preamble).split("::")[3].split(":")[2]);
                            latitudeReceived = new String(preamble).split("::")[3].split(":")[3];
                            longitudeReceived = new String(preamble).split("::")[3].split(":")[4];
                            timestampReceived = new String(preamble).split("::")[3].split(":")[5];
                            tagsReceived = new String(preamble).split("::")[4];
                            mimeReceived = new String(preamble).split("::")[5];
                            formatRecived = new String(preamble).split("::")[6];
                            macAddrReceived = new String(preamble).split("::")[7];
                            deviceNameReceived = new String(preamble).split("::")[8];
                            UUIDReceived = new String(preamble).split("::")[9];
                            sizeReceived=Long.parseLong(new String(preamble).split("::")[10]);
                            qualityReceived=Long.parseLong(new String(preamble).split("::")[11]);
                            priorityReceived=Long.parseLong(new String(preamble).split("::")[12]);
                        }
                    }
                    Log.d("CTwReqCode", "MessageType and MessageSize received are:" + MessageType + "  and " + MessageSize);
                    int bytesReadForTags = 0;
                    int currentForTags = 0;
                    byte[] bufferForTags = new byte[MessageSize];
                    switch (MessageType) {

                        case Constants.MESSAGE_IMAGE:
                            try {
                                DecimalFormat df = new DecimalFormat("#.####");
                                double incentive=0.0;
                                if(new String(preamble).contains("incentive"))
                                {
                                    String splittedPreamble[]=new String(preamble).split("::");
                                    for(int sI=0;sI<splittedPreamble.length;sI++)
                                    {
                                        if(splittedPreamble[sI].contains("incentive"))
                                        {
                                            incentive=Double.parseDouble(splittedPreamble[sI].split(":")[1]);
                                            break;
                                        }
                                    }
                                }
                                Cursor testTBLRows=mydatabase.rawQuery("SELECT * from INCENT_UUID_MAC_TBL",null);
                                Log.d("CTwRC","number of rows inside INCENT_UUID_MAC_TBL at the start of MES_IMAGE are:"+testTBLRows.getCount());

                                Log.d("CTwRC", "Entered case MESSAGE_IMAGE of switch");
                                currentForTags = 0;
                                byte bufferForMessage[] = new byte[MessageSize];
                                byte[] tempBytes;
                                int noOfBytes=4192;
                                if (MessageSize != 0) {

                                    Log.d("CTwReq", "waiting to read tags from remote device!!");
                                    while (currentForTags != MessageSize) {
                                        if((MessageSize-currentForTags<noOfBytes))
                                            noOfBytes=MessageSize-currentForTags;
                                        tempBytes=new byte[noOfBytes];
                                        bytesReadForTags=mmInStream.read(tempBytes);
                                        //bytesReadForTags = mmInStream.read(bufferForMessage, 0, MessageSize - currentForTags);
                                        System.arraycopy(tempBytes,0,bufferForMessage,currentForTags,bytesReadForTags);
                                        currentForTags += bytesReadForTags;
                                    }

                                }
                                FileOutputStream fos1 = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/"+fileNameImage);
                                fos1.write(bufferForMessage);
                                fos1.close();
                                DbFunctions dbFunctions=new DbFunctions();

                                dbFunctions.insertIntoMsgTblRemoteMsg(mydatabase,deviceNameReceived,macAddrReceived,tagsReceived,latitudeReceived,longitudeReceived,timestampReceived,android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address"),BluetoothAdapter.getDefaultAdapter().getName(),mimeReceived,formatRecived,Environment.getExternalStorageDirectory().getAbsoluteFile()+"/"+fileNameImage,UUIDReceived,sizeReceived,qualityReceived,priorityReceived);
                                new DbFunctions().insertIntoSentLogTBL(mydatabase,UUIDReceived,mmSocket.getRemoteDevice().getAddress(),1);
                                //Give incentive if I am the destination

                                int amIDest=0;
                                Cursor findUUIDInc=mydatabase.rawQuery("SELECT incentive,flag from INCENT_UUID_MAC_TBL WHERE UUID='"+ UUIDReceived+"' and MacAd='"+mmSocket.getRemoteDevice().getAddress()+"'",null);
                                Log.d("CTwRC","Number of rows for the current UUID"+UUIDReceived+" and mac address"+mmSocket.getRemoteDevice().getAddress()+" are:"+findUUIDInc.getCount());

                                if(findUUIDInc.getCount()>0) {
                                    while(findUUIDInc.moveToNext())
                                    {
                                        if(findUUIDInc.getInt(1)==1) {
                                            amIDest = 0;// I am relay greater than thres
                                            incentive=findUUIDInc.getDouble(0);
                                        }
                                        else {
                                            amIDest = 1;// I am destination
                                            incentive=findUUIDInc.getDouble(0);
                                        }
                                    }


                                }
                                else amIDest=2;// relay less than thres
                                 if(amIDest==1) {

                                     Log.d("CTwRC","I am destination for the message");
                                     //Find incentive value to pay
                                     while(findUUIDInc.moveToNext())
                                     {
                                         incentive=findUUIDInc.getDouble(0);
                                     }

                                     //Delete from total incentives
                                    mydatabase.execSQL("UPDATE INCENTIVES_TBL SET incentive=incentive-" + incentive);
                                     Log.d("CTwRC","Incentive before deletion in INCENTIVES_TBL:"+incentive);
                                     mydatabase.execSQL("INSERT INTO LAST_FIVE_TRANS VALUES('"+mmSocket.getRemoteDevice().getAddress()+"','"+UUIDReceived+"',"+incentive+",0.0,(datetime('now','localtime')))");
                                     new DbFunctions().deleteLeastRecent(mydatabase);
                                    //Update entry for a message
                                    mydatabase.execSQL("INSERT OR IGNORE INTO INCENT_FOR_MSG_TBL VALUES('" + UUIDReceived + "',0.0,0.0," + Double.parseDouble(df.format(incentive))+")");
                                     mydatabase.execSQL("DELETE FROM INCENT_UUID_MAC_TBL where UUID='"+UUIDReceived+"'");
                                    //Himanshu Himanshu
                                    String preambleString = "Preamble::MessageType:" + Constants.MESSAGE_INCENT_REW + "::" + UUIDReceived + "::" + incentive + "::";
                                    writePreamble(preambleString);
                                }
                                else if(amIDest==0)
                                 {
                                     //HimanshuToday-- Relay greater than threshold
                                     Log.d("CTwRC","I am destination for the message");
                                     //Find incentive value to pay
                                     while(findUUIDInc.moveToNext())
                                     {
                                         incentive=findUUIDInc.getDouble(0);
                                     }

                                     //Delete from total incentives
                                     mydatabase.execSQL("UPDATE INCENTIVES_TBL SET incentive=incentive-" + incentive);
                                     mydatabase.execSQL("INSERT INTO LAST_FIVE_TRANS VALUES('"+mmSocket.getRemoteDevice().getAddress()+"','"+UUIDReceived+"',"+incentive+",0.0,(datetime('now','localtime')))");
                                     new DbFunctions().deleteLeastRecent(mydatabase);
                                     //Update entry for a message
                                     mydatabase.execSQL("INSERT OR IGNORE INTO INCENT_FOR_MSG_TBL VALUES('" + UUIDReceived +"',"+ Double.parseDouble(df.format(incentive*5.0))+",0.0," + Double.parseDouble(df.format(incentive))+")");
                                     mydatabase.execSQL("DELETE FROM INCENT_UUID_MAC_TBL where UUID='"+UUIDReceived+"'");
                                     //Himanshu Himanshu
                                     String preambleString = "Preamble::MessageType:" + Constants.MESSAGE_INCENT_REW + "::" + UUIDReceived + "::" + incentive + "::";
                                     writePreamble(preambleString);
                                 }
                                else
                                {
                                        mydatabase.execSQL("INSERT OR IGNORE INTO INCENT_FOR_MSG_TBL VALUES('" + UUIDReceived + "',"+Double.parseDouble(df.format(incentive))+",0.0,0.0)" );
                                        String preambleString="Preamble::MessageType:"+Constants.MESSAGE_IMAGE_ACK+"::"+UUIDReceived+"::";
                                        writePreamble(preambleString);

                                }
                                //imageCount++;
                                //Log.d("CTwRC","Total number of images received:"+imageCount);
                                //Log.d("CTwRC","Sending acknowledgement to:"+mmSocket.getRemoteDevice().getName());
                                /*
                                String preambleString="Preamble::MessageType:" + Constants.MESSAGE_IMAGE_ACK+"::UUIDForMessage---"+UUIDReceived+"::";
                                Cursor cursorForMac=mydatabase.rawQuery("SELECT sourceMacAddr from MESSAGE_TBL where UUID='"+UUIDReceived+"'",null);
                                String sourceMac="";
                                Log.d("CTwRC","Inside case MESSAGE_IMAGE");
                                while(cursorForMac.moveToNext())
                                {
                                    sourceMac=cursorForMac.getString(0);
                                }
                                if(sourceMac.equals(mmSocket.getRemoteDevice().getAddress()))
                                {
                                    new DbFunctions().insertInc(mydatabase,-1.0);
                                }
                                else
                                {
                                    new DbFunctions().insertInc(mydatabase,-0.5);
                                }
                                Log.d("CTwRC","Preamble string for acknowledgement is:"+preambleString);
                                writePreamble(preambleString);
                                if(imageCount==numImages) {
                                    //imageCount=0;
                                    Log.d("CTwRC","Required number of messages completed, now sending back images");
                                    numAcksRequired=new DbFunctions().sendMessage(mydatabase, mmSocket.getRemoteDevice().getAddress(), mmSocket.getRemoteDevice().getName(), this, Constants.MESSAGE_IMAGE_BACK);

                                }//Log.d("CTwRC","File received");

                                */
                            } catch (Exception e) {
                                Log.d("CTwRC", "Exception occured in case MESSAGE_IMAGE in switch:" + e);
                            }
                            break;
                        case Constants.MESSAGE_INCENT_REW:
                            DecimalFormat df=new DecimalFormat("#.####");
                            String UUID=new String(preamble).split("::")[2];
                            double inc=Double.parseDouble(new String(preamble).split("::")[3]);
                            Cursor cursorForMesInc=mydatabase.rawQuery("SELECT * from INCENT_FOR_MSG_TBL where UUID='"+UUID+"'",null);
                            if(cursorForMesInc.getCount()>0) {
                                mydatabase.execSQL("UPDATE INCENT_FOR_MSG_TBL set received=received+" + Double.parseDouble(df.format(inc)) + " where UUID='" + UUID + "'");
                                mydatabase.execSQL("UPDATE INCENTIVES_TBL SET incentive=incentive+"+Double.parseDouble(df.format(inc)));
                                mydatabase.execSQL("INSERT INTO LAST_FIVE_TRANS VALUES('"+mmSocket.getRemoteDevice().getAddress()+"','"+UUIDReceived+"',0.0,"+inc+",(datetime('now','localtime')))");
                                new DbFunctions().deleteLeastRecent(mydatabase);
                            }
                            new DbFunctions().insertIntoSentLogTBL(mydatabase,UUID,mmSocket.getRemoteDevice().getAddress(),0);
                            break;
                        case Constants.MESSAGE_TSRS: {
                            byte remoteBytesRead[] = readBytes(MessageSize);
                            String remoteTSRs = new String(remoteBytesRead);

                            String ratingString = new String(preamble).split("::")[4];

                            if(!ratingString.equals("NO"))
                            {
                            String ratingStringAr[] = ratingString.split(",");
                            for (String curString : ratingStringAr) {
                                String MacAd = curString.split("--")[0];
                                if (!MacAd.equals(android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address"))) {
                                    double rating = Double.parseDouble(curString.split("--")[1]);

                                    Cursor noOfEntries = mydatabase.rawQuery("SELECT * from USER_RATING_MAP_TBL where MacAd='" + MacAd + "'", null);
                                    if (noOfEntries.getCount() > 0) {
                                        while (noOfEntries.moveToNext()) {
                                            double ratingInTable = noOfEntries.getDouble(1);
                                            rating = ratingInTable * 0.8 + rating * 0.2;
                                        }
                                        mydatabase.execSQL("UPDATE USER_RATING_MAP_TBL set rating=" + rating + " WHERE MacAd='" + MacAd + "'");
                                    } else {
                                        rating = 4 + rating * 0.2;
                                        mydatabase.execSQL("INSERT INTO USER_RATING_MAP_TBL VALUES('" + MacAd + "'," + rating + ",'" + mmSocket.getRemoteDevice().getAddress() + "')");
                                    }
                                }
                            }
                        }
                            Log.d("CTwRC", "Remote TSRs received:" + remoteTSRs);
                            //Process received TSRs
                            new DbFunctions().saveRemoteTSRs(mydatabase, mmSocket.getRemoteDevice(), remoteTSRs);
                            Cursor cursorForRole=mydatabase.rawQuery("SELECT Role from ROLE_TBL where MACAd='"+mmSocket.getRemoteDevice().getAddress()+"'",null);
                            if(cursorForRole.getCount()==0)
                            {
                                mydatabase.execSQL("INSERT OR IGNORE INTO ROLE_TBL VALUES("+Role+",'"+mmSocket.getRemoteDevice().getAddress()+"')");
                            }
                            else
                            {
                                mydatabase.execSQL("UPDATE ROLE_TBL SET Role="+Role+" WHERE MACAd='"+mmSocket.getRemoteDevice().getAddress()+"'");
                            }

                            //Find role of self
                            String Role=new String();
                            Cursor cursorForRoleSelf=mydatabase.rawQuery("SELECT Role from ROLE_TBL where MACAd='SELF'",null);
                            String RoleSelf=new String();
                            while(cursorForRoleSelf.moveToNext())
                            {
                                RoleSelf=cursorForRoleSelf.getString(0);
                            }
                            //Send its own TSRs
                            String TSRsToShare = new DbFunctions().calculateTSRsPre(mydatabase);

                            Log.d("ConnectThread", "new TSRs are:" + TSRsToShare);
                            byte[] byteArrayTSRsToShare = TSRsToShare.getBytes();

                            String ratingsDevices=new String();
                            int countR=0;
                            Cursor cursorForDeviceRatings=mydatabase.rawQuery("SELECT * from USER_RATING_MAP_TBL",null);
                            int countDevice=cursorForDeviceRatings.getCount();
                            if(countDevice==0)
                                ratingsDevices="NO";
                            while(cursorForDeviceRatings.moveToNext())
                            {
                                if(countR!=0) {
                                    ratingsDevices+=",";
                                }
                                String MacAd = cursorForDeviceRatings.getString(0);
                                double rating = cursorForDeviceRatings.getDouble(1);
                                ratingsDevices += MacAd + "--" + rating;

                                countR++;
                            }

                            Log.d("ConnectThread","ratingsDevices is:"+ratingsDevices);

                            String preambleString = "Preamble::MessageType:" + Constants.MESSAGE_TSRS_BACK + "::MessageSize:" + byteArrayTSRsToShare.length + "::Role:"+RoleSelf+"::"+ratingsDevices+"::";
                            Log.d("ConnectThread", "Preamble String is:" + preambleString);
                            Log.d("CTwRC","Sending TSRs back to"+mmSocket.getRemoteDevice().getName()+"--haha  TSRs:"+TSRsToShare);
                            writePreamble(preambleString);
                            write(byteArrayTSRsToShare);
                        }
                            break;
                        case Constants.MESSAGE_TSRS_BACK:
                        {
                            byte remoteBytesRead[] = readBytes(MessageSize);
                            String remoteTSRs = new String(remoteBytesRead);
                            Log.d("CTwRC", "Remote TSRs received:" + remoteTSRs);
                            //Process received TSRs
                            new DbFunctions().saveRemoteTSRs(mydatabase, mmSocket.getRemoteDevice(), remoteTSRs);

                            String ratingString=new String(preamble).split("::")[4];

                            if(!ratingString.equals("NO")) {
                                String ratingStringAr[] = ratingString.split(",");

                                for (String curString : ratingStringAr) {
                                    String MacAd = curString.split("--")[0];
                                    if (!MacAd.equals(android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address"))) {
                                        double rating = Double.parseDouble(curString.split("--")[1]);

                                        Cursor noOfEntries = mydatabase.rawQuery("SELECT * from USER_RATING_MAP_TBL where MacAd='" + MacAd + "'", null);
                                        if (noOfEntries.getCount() > 0) {
                                            while (noOfEntries.moveToNext()) {
                                                double ratingInTable = noOfEntries.getDouble(1);
                                                rating = ratingInTable * 0.8 + rating * 0.2;
                                            }
                                            mydatabase.execSQL("UPDATE USER_RATING_MAP_TBL set rating=" + rating + " WHERE MacAd='" + MacAd + "'");
                                        } else {
                                            rating = 4 + rating * 0.2;
                                            mydatabase.execSQL("INSERT INTO USER_RATING_MAP_TBL VALUES('" + MacAd + "'," + rating + ",'" + mmSocket.getRemoteDevice().getAddress() + "')");
                                        }
                                    }
                                }
                            }

                            Cursor cursorForRole=mydatabase.rawQuery("SELECT Role from ROLE_TBL where MACAd='"+mmSocket.getRemoteDevice().getAddress()+"'",null);
                            if(cursorForRole.getCount()==0)
                            {
                                mydatabase.execSQL("INSERT OR IGNORE INTO ROLE_TBL VALUES("+Role+",'"+mmSocket.getRemoteDevice().getAddress()+"')");
                            }
                            else
                            {
                                mydatabase.execSQL("UPDATE ROLE_TBL SET Role="+Role+" where MACAd='"+mmSocket.getRemoteDevice().getAddress()+"'");
                            }
                            mydatabase.execSQL("DELETE FROM TSR_SHARE_DONE_TBL WHERE MacAd='"+mmSocket.getRemoteDevice().getAddress()+"'");
                            Cursor cursorForTSRShareDone=mydatabase.rawQuery("SELECT * from TSR_SHARE_DONE_TBL",null);
                            if(cursorForTSRShareDone.getCount()==0)
                            {
                                postConnMTransfer();
                            }
                            //new DbFunctions().sendMessage(mydatabase,mmSocket.getRemoteDevice().getAddress(),mmSocket.getRemoteDevice().getName(),this,Constants.MESSAGE_IMAGE);
                        }
                            break;


                        case Constants.MESSAGE_IMAGE_BACK:
                            try {
                                Log.d("CTwRC", "Entered case MESSAGE_IMAGE_BACK of switch");
                                currentForTags = 0;
                                byte bufferForMessage[] = new byte[MessageSize];
                                byte[] tempBytes;
                                int noOfBytes=4192;
                                if (MessageSize != 0) {

                                    Log.d("CTwReq", "waiting to read tags from remote device!!");
                                    while (currentForTags != MessageSize) {
                                        if((MessageSize-currentForTags<noOfBytes))
                                            noOfBytes=MessageSize-currentForTags;
                                        tempBytes=new byte[noOfBytes];
                                        bytesReadForTags=mmInStream.read(tempBytes);
                                        //bytesReadForTags = mmInStream.read(bufferForMessage, 0, MessageSize - currentForTags);
                                        System.arraycopy(tempBytes,0,bufferForMessage,currentForTags,bytesReadForTags);
                                        currentForTags += bytesReadForTags;
                                    }



                                }
                                FileOutputStream fos1 = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/"+fileNameImage);
                                fos1.write(bufferForMessage);
                                fos1.close();
                                DbFunctions dbFunctions=new DbFunctions();
                                dbFunctions.insertIntoMsgTblRemoteMsg(mydatabase,deviceNameReceived,macAddrReceived,tagsReceived,latitudeReceived,longitudeReceived,timestampReceived,android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address"),BluetoothAdapter.getDefaultAdapter().getName(),mimeReceived,formatRecived,Environment.getExternalStorageDirectory().getAbsoluteFile()+"/"+fileNameImage,UUIDReceived,sizeReceived,qualityReceived,priorityReceived);
                                new DbFunctions().insertIntoSentLogTBL(mydatabase,UUIDReceived,mmSocket.getRemoteDevice().getAddress(),1);
                                imageBackCount++;
                                String preambleString="Preamble::MessageType:" + Constants.MESSAGE_IMAGE_BACK_ACK+"::UUIDForMessage---"+UUIDReceived+"::";
                                Cursor cursorForMac=mydatabase.rawQuery("SELECT sourceMacAddr from MESSAGE_TBL where UUID='"+UUIDReceived+"'",null);
                                String sourceMac="";
                                Log.d("CTwRC","Inside case MESSAGE_IMAGE_BACK");
                                while(cursorForMac.moveToNext())
                                {
                                    sourceMac=cursorForMac.getString(0);
                                }
                                if(sourceMac.equals(mmSocket.getRemoteDevice().getAddress()))
                                {
                                    new DbFunctions().insertInc(mydatabase,-1.0);
                                }
                                else
                                {
                                    new DbFunctions().insertInc(mydatabase,-0.5);
                                }
                                Log.d("CTwRC","Preamble string for acknowledgement is:"+preambleString);

                                writePreamble(preambleString);
                                Log.d("CTwRC","File received");
                                if(imageBackCount==numImagesBack) {
                                        new DbFunctions().unsetConnectedLogTBL(mydatabase, mmSocket.getRemoteDevice().getAddress());
                                }

                            } catch (Exception e) {
                                Log.d("CTwRC", "Exception occured in case MESSAGE_IMAGE in switch:" + e);
                            }
                            break;
                        case Constants.MESSAGE_IMAGE_ACK:
                        {
                            //"Preamble::MessageType:"+Constants.MESSAGE_IMAGE_ACK+"::"+UUIDReceived+"::";
                            //fileNameImage:"+fileNameImage

                            Log.d("CTwRC","Inside MESSAGE_IMAGES_ACK");
                            UUID=new String(preamble).split("::")[2];
                            Log.d("CTwRC","UUID acknowledged is:"+UUID);
                            new DbFunctions().insertIntoSentLogTBL(mydatabase,UUID,mmSocket.getRemoteDevice().getAddress(),0);
                        }
                            break;
                        case Constants.MESSAGE_IMAGES_TOTAL:
                        {
                            Log.d("CTwRC","Inside MESSAGE_IMAGES_TOTAL");
                            numImages=Integer.parseInt(new String(preamble).split("::")[2].split(":")[1]);
                            Log.d("CTwRC","Number of images to be received are:"+numImages);
                            if(numImages==0)
                            {
                                new DbFunctions().sendMessage(mydatabase, mmSocket.getRemoteDevice().getAddress(), mmSocket.getRemoteDevice().getName(), this, Constants.MESSAGE_IMAGE_BACK);
                            }
                        }
                        break;
                        case Constants.MESSAGE_IMAGES_BACK_TOTAL:
                        {
                            Log.d("CTwRC","Inside MESSAGE_IMAGES_BACK_TOTAL");
                            numImagesBack=Integer.parseInt(new String(preamble).split("::")[2].split(":")[1]);
                            Log.d("CTwRC","Number of images to be received are:"+numImagesBack);
                            if(numImagesBack==0)
                            {
                                new DbFunctions().unsetConnectedLogTBL(mydatabase, mmSocket.getRemoteDevice().getAddress());
                            }
                        }
                        break;
                        case Constants.MESSAGE_IMAGE_BACK_ACK:
                        {
                            //fileNameImage:"+fileNameImage
                            //Exception occured in reading:java.lang.NumberFormatException: Invalid int: "DA"
                            Log.d("CTwRC","Inside MESSAGE_IMAGE_BACK_ACK");
                            Log.d("CTWRC","");
                            UUID=new String(preamble).split("::")[2].split("---")[1];
                            Log.d("CTwRC","UUID acknowledged inside MESSAGE_IMAGE_BACK_ACK is:"+UUID);
                            Cursor cursorForMac=mydatabase.rawQuery("SELECT sourceMacAddr from MESSAGE_TBL where UUID='"+UUID+"'",null);
                            String sourceMac="";
                            Log.d("CTwRC","Inside case MESSAGE_IMAGE_BACK_ACK");
                            while(cursorForMac.moveToNext())
                            {
                                sourceMac=cursorForMac.getString(0);
                            }
                            if(sourceMac.equals(android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address")))
                            {
                                new DbFunctions().insertInc(mydatabase,1.0);
                            }
                            else
                            {
                                new DbFunctions().insertInc(mydatabase,0.5);
                            }
                            Log.d("CTwRC","UUID acknowledged is:"+UUID);
                            new DbFunctions().insertIntoSentLogTBL(mydatabase,UUID,mmSocket.getRemoteDevice().getAddress(),0);
                            if(imageBackAckCount==numAcksRequired)
                                new DbFunctions().unsetConnectedLogTBL(mydatabase, mmSocket.getRemoteDevice().getAddress());
                        }
                        break;
                        case Constants.MESSAGE_INCENT_REQ:

                            int flag=0;
                            String sourceMac=new String(preamble).split("::")[4];
                            //Reputation metric code here
                            Cursor noOfEntries = mydatabase.rawQuery("SELECT * from USER_RATING_MAP_TBL where MacAd='" + sourceMac + "'", null);
                            double rating=5.0;
                            int flagForRatingPresent=0;
                            double percentInc=1.0;
                            if (noOfEntries.getCount() > 0) {
                                flagForRatingPresent=1;
                                while (noOfEntries.moveToNext()) {
                                    rating=noOfEntries.getDouble(1);
                                    if(rating>=4.0)
                                        percentInc=1;
                                    else if(rating>=3.0 && rating<4.0)
                                        percentInc=0.75;
                                    else if(rating>=2.0 && rating<3.0)
                                        percentInc=0.50;
                                    else {
                                        percentInc=0.0;
                                        flag=0;
                                        }
                                }

                            }
                            //Reputation metric code done

                            double incentivePresent=0;
                            double incentiveRequired=Double.parseDouble(new String(preamble).split("::")[2].split(":")[1]);
                            UUID=new String(preamble).split("::")[3];
                            String addedTags="";
                            double incentDest = 0.0;
                            if(new String(preamble).contains("addedTags")) {
                                addedTags = new String(preamble).split("::")[4].split(":")[1];

                                if (addedTags != null && addedTags.length() > 0) {
                                    String addedTagsSplit[] = addedTags.split(",");
                                    Cursor selfTags;

                                    for (int adI = 0; adI < addedTagsSplit.length; adI++) {
                                        selfTags = mydatabase.rawQuery("SELECT belongs_to from TSR_TBL where SI='" + addedTagsSplit[adI] + "' and belongs_to='SELF'", null);
                                        while (selfTags.moveToNext()) {
                                            String belongs_to = selfTags.getString(0);
                                            if (belongs_to.equals("SELF")) {
                                                incentDest += 0.2;
                                            }
                                        }
                                    }

                                }

                                incentDest = Math.min(incentDest, 1);
                                Log.d("CTwRC","incentDest value is:"+incentDest);

                            }
                            incentiveRequired=incentiveRequired*percentInc+incentDest;
                            Log.d("CTwRC","incentRequired value is:"+incentDest);
                                //incentiveRequired=incentiveRequired+1;

                            Cursor cursorIncent=mydatabase.rawQuery("SELECT incentive from INCENTIVES_TBL",null);
                            double sumIncents=0.0;
                            while(cursorIncent.moveToNext())
                            {
                                incentivePresent=cursorIncent.getDouble(0);
                                Cursor cursorForReqsCurIt=mydatabase.rawQuery("SELECT SUM(incentive) FROM INCENT_UUID_MAC_TBL",null);
                                while(cursorForReqsCurIt.moveToNext())
                                {
                                    sumIncents=cursorForReqsCurIt.getDouble(0);
                                }
                                int flagRec=0;
                                if(new String(preamble).contains("flag"))
                                {
                                    flagRec=Integer.parseInt(new String(preamble).split("::")[4].split(":")[1]);
                                }
                                Log.d("CTwRC","The value of incentiveRequired being inserted into INCENT_UUID_MAC_TBL is:"+incentiveRequired);
                                if(flagRec==1)
                                mydatabase.execSQL("INSERT OR IGNORE INTO INCENT_UUID_MAC_TBL VALUES('"+mmSocket.getRemoteDevice().getAddress()+"','"+UUID+"',"+incentiveRequired+",1)");
                                else
                                    mydatabase.execSQL("INSERT OR IGNORE INTO INCENT_UUID_MAC_TBL VALUES('"+mmSocket.getRemoteDevice().getAddress()+"','"+UUID+"',"+incentiveRequired+",0)");
                            }



                            Log.d("CTwRC","IncentivePrsent, sum Incents and incentiveRequired are:"+incentivePresent+","+sumIncents+","+incentiveRequired);
                            if((incentivePresent-sumIncents-incentiveRequired)>=0)
                                flag=1;
                            else flag=0;

                            if(percentInc==0)
                                flag=0;


                            if(flag==1) {
                                Cursor testTBLRows = mydatabase.rawQuery("SELECT * from INCENT_UUID_MAC_TBL", null);
                                Log.d("CTwRC", "number of rows inside INCENT_UUID_MAC_TBL at the end of INCENT_REQ are:" + testTBLRows.getCount());
                                String preambleString = "Preamble::MessageType:" + Constants.MESSAGE_INCENT_REP + "::flag:" + flag + "::" + UUID + "::"+incentiveRequired+"::";
                                Log.d("CTwRC", "Length of preamble string is:" + preambleString.getBytes().length);
                                Log.d("CTwRC", "Writing preamble inside INC_REQ as:" + preambleString);
                                writePreamble(preambleString);
                            }
                                break;
                        case Constants.MESSAGE_INCENT_REP:
                            int flagRep=0;

                                flagRep = Integer.parseInt(new String(preamble).split("::")[2].split(":")[1]);


                            if(flagRep==1) {
                                String UUIDRep = new String(preamble).split("::")[3];
                                sendMessage(UUIDRep,0,0.0);
                            }
                            //new DbFunctions().unsetConnectedLogTBL(mydatabase,mmSocket.getRemoteDevice().getAddress());
                           // preambleString="Preamble::MessageType:" + Constants.MESSAGE_TRANS_DONE+"::";
                           // writePreamble(preambleString);
                            break;
                        case Constants.MESSAGE_TRANS_DONE:
                            new DbFunctions().unsetConnectedLogTBL(mydatabase,mmSocket.getRemoteDevice().getAddress());
                            break;

                    }



                }
                    else
                {
                    if(new String(preamble).split("::")[0].equals("Himanshu"))
                    {
                        Log.d("CTwRC","Received Himanshu");
                    }
                }

                continue;
            }
                    }



             catch (Exception e) {
                Log.d("ConnectedThread", "Exception occured in reading:" + e);
                break;
            } finally {
                try {
                    fos.close();
                } catch (Exception e) {

                }
            }
        }

    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {


            Log.d("CTwRC", "If Oustream is null:"+(mmOutStream==null));
            mmOutStream.write(bytes);

            //   mmOutStream.flush();
           // Log.d("ConnectedThread", "Hopefully data is written!");
        } catch (Exception e) {
            Log.d("ConnectedThread", "Exception occured in writing:" + e);
        }
    }
    public void writePreamble(String preambleString)
    {
        byte preamble[]=new byte[Constants.PREAMBLE_SIZE];
         //Log.d("CT","Preamble string is:"+preambleString);
        byte preambleSomething[]=preambleString.getBytes();
        Arrays.fill(preamble,(byte)0);
        System.arraycopy(preambleSomething,0,preamble,0,preambleSomething.length);

        try {
            Log.d("CTwRC","writePreamble:: Writing preamble:"+new String(preamble));
            mmOutStream.write(preamble);
        }
        catch(Exception e)
        {
            Log.d("CTwRC","Exception in writing preamble:"+e);
        }

    }
    public void write(byte[] bytes, int some1, int some2)

    {
        try {
            mmOutStream.write(bytes, some1, some2);
        } catch (Exception e)
        {
            Log.d("CTwRC",""+e);
        }
    }


    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
            //mydatabase.close();
        } catch (IOException e) { }
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
    public static Bitmap bitmapFromStream(InputStream is){
        Bitmap decoded = BitmapFactory.decodeStream(is);
        return Bitmap.createScaledBitmap(decoded, 100, 100, true);
    }
    public  byte[] readBytes(int MessageSize)
    {
        int currentForTags=0;
        byte bufferForMessage[] = new byte[MessageSize];
        int bytesRead=0;
        try {

            currentForTags = 0;

            byte[] tempBytes;
            int noOfBytes = 4192;
            if (MessageSize != 0) {

                Log.d("CTwReq", "waiting to read bytes from remote device!!");
                while (currentForTags != MessageSize) {
                    if ((MessageSize - currentForTags < noOfBytes))
                        noOfBytes = MessageSize - currentForTags;
                    tempBytes = new byte[noOfBytes];
                    bytesRead = mmInStream.read(tempBytes);
                    System.arraycopy(tempBytes, 0, bufferForMessage, currentForTags, bytesRead);
                    currentForTags += bytesRead;
                }
            }
        }
        catch(Exception e)
        {
            Log.d("CTwRC","Exception in reading bytes:"+e);
        }
        return bufferForMessage;
    }
    public void sendMessage(String UUIDRep,int isTherePromise,double incentive)
    {
        try {
            Log.d("CTwRC","sendMessage:: Looking to send message with UUID:"+UUIDRep);
            Cursor cursorForMatchingImage = mydatabase.rawQuery("SELECT * from MESSAGE_TBL where UUID='" + UUIDRep + "'", null);
            Log.d("CTwRC","Number of entries in MesTBL with UUID "+UUIDRep+" are "+cursorForMatchingImage.getCount());
            cursorForMatchingImage.moveToFirst();
            String imagePath = cursorForMatchingImage.getString(0);

            Log.d("DbFunctions", "Imagepath current is:" + imagePath);
            String latitude = cursorForMatchingImage.getString(1);

            String longitude = cursorForMatchingImage.getString(2);
            String timestamp = cursorForMatchingImage.getString(3);
            String tagsForCurrentImage = cursorForMatchingImage.getString(4);
            String fileName = cursorForMatchingImage.getString(5);
            String mime = cursorForMatchingImage.getString(6);
            String format = cursorForMatchingImage.getString(7);
            String localMacAddr = cursorForMatchingImage.getString(8);
            String localName = cursorForMatchingImage.getString(9);
            Log.d("CTwRC","Size, quality and priority checking:");
            long size = cursorForMatchingImage.getLong(13);
            long quality = cursorForMatchingImage.getLong(14);
            long priority = cursorForMatchingImage.getLong(15);
            Log.d("CTwRC",size+":::"+quality+":::"+priority);
            java.io.RandomAccessFile raf = new java.io.RandomAccessFile(imagePath, "r");
            byte[] b = new byte[(int) raf.length()];
            Cursor cursorForNewTags=mydatabase.rawQuery("SELECT * from ADDED_TAGS_TBL where UUID='"+UUIDRep+"'",null);
            String addedTags="";
            while(cursorForNewTags.moveToNext())
            {
                addedTags=cursorForNewTags.getString(1);
            }
            if(!addedTags.equals(""))
                tagsForCurrentImage=tagsForCurrentImage+","+addedTags;
            raf.readFully(b);
            int codeForMessage = Constants.MESSAGE_IMAGE;
            String preambleString=new String();
            preambleString= "Preamble::MessageType:" + codeForMessage + "::MessageSize:" +
                    b.length + "::Lengths:" + fileName + ":" +
                    b.length + ":" + latitude + ":" + longitude + ":" +
                    timestamp + "::" + tagsForCurrentImage + "::" +
                    mime + "::" + format + "::" + localMacAddr + "::" + localName + "::" + UUIDRep + "::" + size + "::" + quality + "::" + priority + "::";
            if(isTherePromise==1)
            {
                 preambleString+="incentive:"+incentive+"::";
            }

            //Log.d("CTwRC", "Sending image" + imagePath + "with tags" + tagsForCurrentImage + "to " + remoteName + "--haha  :");
            Log.d("DbFunctions", "Preamble string is:" + preambleString);
            writePreamble(preambleString);
            Log.d("DbFunctions", "Preamble written");
            write(b);
            Log.d("DbFunctions", "Message written");
        }
        catch(Exception e)
        {
            Log.d("CTwRC","Exception occured in sendMessage function:"+e);
        }
    }


    //Function for message transfer
    public void postConnMTransfer()
    {
        Log.d("ConnectThread","postConnMTransfer fc called");
        int Role=1;
        Cursor cursorForRole=mydatabase.rawQuery("SELECT Role from ROLE_TBL where MACAd='SELF'",null);
        while(cursorForRole.moveToNext())
        {
            Role=cursorForRole.getInt(0);
        }
        //Map of messages to be sent
        class MesParams  extends Object implements Comparable<MesParams>{
            String UUID;
            String sourceMac;
            long size,quality,priority;
            double sumWeightsRemote;
            double incentive;
            String remoteMAC;
            double avgWeight=0.0; // To save the average remote weight of all the tags for the message
            int flagForDest;//0 for relay and 1 for dest

            public int compareTo(MesParams o) {
                return new Double(this.incentive).compareTo(new Double(o.incentive));
            }
        }
        ArrayList<MesParams> listMessages=new ArrayList<MesParams>();

        long sizeMax=-90,qualityMax=-90;
        double remoteSumMax=-0.9;
        Map<String,ArrayList<MesParams>> MACtoMessageRelay=new HashMap<String, ArrayList<MesParams>>();
        Map<String,ArrayList<MesParams>> MACtoMessageDest=new HashMap<String,ArrayList<MesParams>>();
        Iterator it = Constants.deviceToSocket.entrySet().iterator();
        Log.d("ConnectThread","postConn::Number of mappings in hashmaps are:"+Constants.deviceToSocket.size());
        //Iterate through all the connected devices
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //Take one device at a time
            String remoteMAC=((String)pair.getKey());
            //To check the condition that a message is not being checked again
            ArrayList<String> listOfUUIDsChecked=new ArrayList<String>();
            //To check the condition that a message to be transferred is not sent twice
            ArrayList<String> listOfImages=new ArrayList<String>();
            //To store device to difference in sum mappings
            HashMap<String, Double> hm = new HashMap<String, Double>();
            //Find messages for a particular device with local sum less than remote and haven't been previously exchanged with the device
            Cursor cursorForRemoteTSR=mydatabase.rawQuery("SELECT * from TSR_REMOTE_TBL where deviceMacAddr='"+remoteMAC+"'",null);

            Cursor cursorForMatchingImage;
            ArrayList<String> listUUIDSentOrReceived = new ArrayList<String>();

            while(cursorForRemoteTSR.moveToNext()) {

                String belongs_to=cursorForRemoteTSR.getString(4);
                //Find all the messages for the particular tag and device
                Log.d("ConnectThread","postConnMT: TSR and belongs_to in check is:"+cursorForRemoteTSR.getString(1)+" and "+belongs_to);
                cursorForMatchingImage = mydatabase.rawQuery("SELECT * from MESSAGE_TBL  where tagsForCurrentImage like '%" + cursorForRemoteTSR.getString(1) + "%'", null);

                double localSum=0.0,remoteSum=0.0;

                //Iterate through all the messages for the device and tag combo
                while (cursorForMatchingImage.moveToNext()) {///For every message

                    String sourceForMessage=cursorForMatchingImage.getString(8);
                    String tagsForCurrentImage = cursorForMatchingImage.getString(4);
                    String UUID = cursorForMatchingImage.getString(10);
                    Log.d("ConnectThread", "postConnMT::Message considered for transfer:" + UUID);
                    int sizeIndex = cursorForMatchingImage.getColumnIndex("size");
                    int qualityIndex = cursorForMatchingImage.getColumnIndex("quality");
                    int priorityIndex = cursorForMatchingImage.getColumnIndex("priority");
                    long size = cursorForMatchingImage.getLong(sizeIndex);
                    long quality = cursorForMatchingImage.getLong(qualityIndex);
                    int priority = cursorForMatchingImage.getInt(priorityIndex);
                    MesParams mesParams = new MesParams();
                    mesParams.remoteMAC = remoteMAC;
                    mesParams.UUID = UUID;mesParams.sourceMac=sourceForMessage;
                    mesParams.quality = quality;
                    mesParams.priority = priority;
                    //See if this message has been exchanged before between the two devices
                    Cursor ifSentOrReceived = mydatabase.rawQuery("SELECT * from SENT_IMAGE_LOG where UUID='" + UUID + "' and (sentTo='" + remoteMAC + "' OR receivedFrom='" + remoteMAC + "')", null);

                    if (ifSentOrReceived!=null && ifSentOrReceived.getCount() > 0) {
                        //Condition that this message has been exchanged before
                        ifSentOrReceived.moveToNext();
                        Log.d("DbFunctions", "Image already sent or received:" + ifSentOrReceived.getString(0) + "---" + ifSentOrReceived.getString(1) + "---" + ifSentOrReceived.getString(2));

                    } else
                    {
                        //Check if it exists in listMessages
                        int flagExists = 0;
                        for (MesParams tempMesParams : listMessages) {
                            if (tempMesParams.UUID.equals(mesParams.UUID) && tempMesParams.remoteMAC.equals(mesParams.remoteMAC)) {
                                if(belongs_to.equals("SELF")) {
                                    tempMesParams.flagForDest = 1;
                                    mesParams.flagForDest = 1;
                                }
                                flagExists = 1;
                                break;
                            }
                        }
                        //Condition that message is present in listMessages
                        if (flagExists == 1) {
                            if(belongs_to.equals("SELF"))
                            {
                                for (MesParams tempMesParams : listMessages) {
                                    if (tempMesParams.UUID.equals(mesParams.UUID) && tempMesParams.remoteMAC.equals(mesParams.UUID)) {
                                        tempMesParams.flagForDest = 1;
                                        mesParams.flagForDest = 1;
                                    }
                                }
                            }

                        }
                        //Condition that message is not present in listMessages
                        else{
                            if (belongs_to.equals("SELF")) {
                                mesParams.flagForDest=1;
                                listMessages.add(mesParams);
                                if (quality > qualityMax)
                                    qualityMax = quality;
                            }
                            else
                            {
                                mesParams.flagForDest=0;
                                String tagsForCurrentImageArray[] = tagsForCurrentImage.split(",");
                                //Find local and sum for all the messages
                                for (int i = 0; i < tagsForCurrentImageArray.length; i++) {
                                    if (tagsForCurrentImageArray[i].length() > 0) {
                                        Cursor cursorForTagsWeight = mydatabase.rawQuery("SELECT weight from TSR_TBL where SI='" + tagsForCurrentImageArray[i] + "'", null);
                                        if(cursorForTagsWeight!=null) {
                                            while (cursorForTagsWeight.moveToNext()) {
                                                double tempWeight = cursorForTagsWeight.getDouble(0);
                                                localSum += tempWeight;
                                            }
                                        }
                                        if(cursorForTagsWeight!=null)
                                            cursorForTagsWeight.close();
                                        /////comment-Doug, get a node identifier for TSR_REMOTE
                                        Cursor cursorForTagsWeightRemote = mydatabase.rawQuery("SELECT weight from TSR_REMOTE_TBL where SI='" + tagsForCurrentImageArray[i] + "' and deviceMacAddr='" + remoteMAC + "'", null);
                                        if(cursorForTagsWeightRemote!=null)
                                        while (cursorForTagsWeightRemote.moveToNext()) {
                                            double tempWeight = cursorForTagsWeightRemote.getDouble(0);
                                            remoteSum += tempWeight;
                                        }

                                        if(cursorForTagsWeightRemote!=null)
                                            cursorForTagsWeightRemote.close();
                                    }
                                }
                                mesParams.sumWeightsRemote = remoteSum;
                                mesParams.avgWeight=remoteSum/tagsForCurrentImageArray.length;
                                if (remoteSum > remoteSumMax) {
                                    remoteSumMax = remoteSum;
                                }
                                Log.d("DbFunctions", "localSum and remoteSum for the current message is:" + localSum + "--" + remoteSum);
                                double differenceInSums = remoteSum - localSum;
                                /////come here
                                if(differenceInSums>0) {

                                    if (sizeMax > size)
                                        sizeMax = size;
                                    if (quality > qualityMax)
                                        qualityMax = quality;
                                    mesParams.flagForDest=0;
                                    listMessages.add(mesParams);
                                }
                            }
                        }
                    }
                    if(ifSentOrReceived != null)
                        ifSentOrReceived.close();
                }//For every message
                if(cursorForMatchingImage!=null)
                    cursorForMatchingImage.close();
            }
            if(cursorForRemoteTSR != null)
                cursorForRemoteTSR.close();
        }


        //Calculate incentives now

        /*Himanshu-- Role based messages */

        //Cursor cursorForRoleMessages=mydatabase.rawQuery("SELECT * from MESSAGE_TBL where ",null);
        //Two arrays for descending order arrangement
        MesParams[] destMs,relayMsGtThres,relayMsLtThres; int destMsC=0;int relayMsC=0;

        //Calculate incentive for all messages

        Log.d("CTwRC","Total messages are:"+listMessages.size());
        for(MesParams mesParams:listMessages)
        {

            Log.d("CTwRC","UUID:"+mesParams.UUID+"sumRemoteWeights:"+mesParams.sumWeightsRemote+", Size:"+mesParams.size+", Quality:"+mesParams.quality+",Role:"+Role+",Priority:"+mesParams.priority+",SizeMax:"+sizeMax+",QualityMax:"+qualityMax+",remoteSumMax:"+remoteSumMax);
            //mesParams.incentive=3*mesParams.sumWeightsRemote*mesParams.size*mesParams.quality/(Role*mesParams.priority*sizeMax*qualityMax*remoteSumMax);
            double incentMax=2.0;
            Log.d("CTwRC","Value of 1/2*(mesParams.quality/(qualityMax*1.0)) is"+0.5*((mesParams.quality*1.0)/(qualityMax*1.0)));
            Log.d("CTwRC","Value of 1/2*(mesParams.sumWeightsRemote/(Role*mesParams.priority*remoteSumMax*1.0)) is"+1/2*(mesParams.sumWeightsRemote/(Role*mesParams.priority*remoteSumMax*1.0)));
            Log.d("CTwRC","Value of IncentMax:"+incentMax);
            mesParams.incentive=(0.5*((mesParams.quality*1.0)/(qualityMax*1.0))+0.5*((mesParams.sumWeightsRemote*1.0)/(Role*mesParams.priority*remoteSumMax*1.0)))*incentMax;
            Log.d("CTwRC","MesParams.incentive value is:"+mesParams.incentive);
            //(¼*(S/ Smax +Q/ Qmax)+ ½*(Pv/ (Ru * Pu)))* Imax
            //Without size consideration
           /* Random r = new Random();
            double randomValue = 2.0 + (2.0 - 0.47) * r.nextDouble();
            mesParams.incentive=5*mesParams.sumWeightsRemote*mesParams.quality/(Role*mesParams.priority*qualityMax*remoteSumMax);
            mesParams.incentive=2;*/
            //mesParams.incentive=3*randomValue;
            Log.d("CTwRC","Incentive for message "+mesParams.UUID+" is "+mesParams.incentive);
            if(mesParams.flagForDest==1)
                destMsC++;
            else
                relayMsC++;
        }
        destMs=new MesParams[destMsC];

        ArrayList<MesParams> relayGreaterThanThres=new ArrayList<MesParams>();
        ArrayList<MesParams> relayLessThanThres=new ArrayList<MesParams>();
        int destIn=0;int relayIn=0;
        for(MesParams tempMesParams:listMessages)
        {
            double incentPromised=0.0;

            if(tempMesParams.flagForDest==1) {
                //Check if the device transferring the message is itself a relay, if it is a relay, update incentive
                Cursor incentPaidZeroOrNot=mydatabase.rawQuery("SELECT promised from INCENT_FOR_MSG_TBL where UUID='"+tempMesParams.UUID+"'",null);
                while(incentPaidZeroOrNot.moveToNext())
                {
                    incentPromised=incentPaidZeroOrNot.getDouble(0);
                }
                if(!(incentPromised==0.0))
                {
                    tempMesParams.incentive=incentPromised;
                    Log.d("CTwRC","Promised incentive is:"+tempMesParams.incentive);
                }
                Random r = new Random();
                double randomValue = 0.11 + (0.31 - 0.11) * r.nextDouble();
                Log.d("CTwRC","Value of radomValue is:"+randomValue);
                tempMesParams.incentive=tempMesParams.incentive+randomValue;
                tempMesParams.incentive=Math.min(tempMesParams.incentive,2.0);
                Cursor cursorForRSSI=mydatabase.rawQuery("SELECT RSSI from BLUETOOTH_DEVICES_IN_RANGE WHERE BTDeviceMacAddr='"+tempMesParams.remoteMAC+"'",null);
                double RSSI=0.0;
                while(cursorForRSSI.moveToNext())
                {
                    RSSI=cursorForRSSI.getDouble(0);
                }

                double txP=Math.pow(10.0,RSSI/10.0);
                Log.d("","txP value is:");
                double incentBattery=txP/Math.pow(10.0,100/10.0)*(2.0/3);
                Log.d("CTwRC","Incentive due to battery is:"+incentBattery);
                Log.d("CTwRC","Final incentive value not yet assigned though is:"+Math.min(incentBattery+tempMesParams.incentive,2.0));
                destMs[destIn]=new MesParams();
                destMs[destIn++] = tempMesParams;
            }
            else if(tempMesParams.flagForDest==0){
                if(tempMesParams.avgWeight>=0.8)
                {
                    relayGreaterThanThres.add(tempMesParams);
                }
                else {
                    relayLessThanThres.add(tempMesParams);
                }
            }
        }

        relayMsGtThres=relayGreaterThanThres.toArray(new MesParams[relayGreaterThanThres.size()]);
        relayMsLtThres=relayLessThanThres.toArray(new MesParams[relayLessThanThres.size()]);
        //Arrange in descending order of incentive
        Arrays.sort(destMs);
        Arrays.sort(relayMsGtThres);
        Arrays.sort(relayMsLtThres);


        Log.d("CTwRC","Values of destMsC and relayMsC are:"+destMsC+"::"+relayMsC);
        if(destMsC!=0) {
            for (int i = destMsC - 1; i >= 0; i--) {
                Cursor cursorForNewTags=mydatabase.rawQuery("SELECT * from ADDED_TAGS_TBL where UUID='"+destMs[i].UUID+"'",null);
                String addedTags="";
                while(cursorForNewTags.moveToNext())
                {
                    addedTags=cursorForNewTags.getString(1);
                }
                String preambleString=new String();
                if(addedTags.equals(""))
                    preambleString = "Preamble::MessageType:" + Constants.MESSAGE_INCENT_REQ + "::IncentiveRequired:" + destMs[i].incentive + "::" + destMs[i].UUID + "::"+destMs[i].sourceMac+"::";
                else
                    preambleString = "Preamble::MessageType:" + Constants.MESSAGE_INCENT_REQ + "::IncentiveRequired:" + destMs[i].incentive + "::" + destMs[i].UUID + "::"+"addedTags:"+addedTags+"::";
                Log.d("ConnectThread", "Preamble String is:" + preambleString);
                ConnectedThreadWithRequestCodes newConnectedThread = new ConnectedThreadWithRequestCodes((BluetoothSocket) Constants.deviceToSocket.get(destMs[i].remoteMAC), context);
                //Log.d("CTwRC","Sending TSRs to"+((BluetoothSocket) pair.getValue()).getRemoteDevice().getName()+"--haha  TSRs:"+TSRsToShare);
                Log.d("ConnectThread", "asking for incentive from :" + destMs[i].remoteMAC);
                newConnectedThread.writePreamble(preambleString);

            }
        }
        else{
            Log.d("CTwRC","No messages for destination and value of destMsC and relayMsC are:"+destMsC+"::"+relayMsC);
        }

        //Sending to relays
        String preambleString="";
        Log.d("CTwRC","Size of relayMsGtThres is:"+relayMsGtThres.length);
        if(relayMsGtThres!=null && relayMsGtThres.length!=0)
        {
            for(int i=relayMsGtThres.length-1;i>=0;i--) {
                //Log.d("","Test incentive value is:"+relayMsGtThres[i].incentive);
                preambleString = "Preamble::MessageType:" + Constants.MESSAGE_INCENT_REQ + "::IncentiveRequired:" + relayMsGtThres[i].incentive*0.2 + "::" + relayMsGtThres[i].UUID + "::flag:1::";

                ConnectedThreadWithRequestCodes newConnectedThread = new ConnectedThreadWithRequestCodes((BluetoothSocket) Constants.deviceToSocket.get(relayMsGtThres[i].remoteMAC), context);
                //Log.d("CTwRC","Sending TSRs to"+((BluetoothSocket) pair.getValue()).getRemoteDevice().getName()+"--haha  TSRs:"+TSRsToShare);
                Log.d("ConnectThread", "asking for incentive from :" + relayMsGtThres[i].remoteMAC);
                newConnectedThread.writePreamble(preambleString);

            }
        }
        Log.d("CTwRC","Size of arraylist relayLessThanThres is:"+relayLessThanThres.size());
        Log.d("CTwRC","Size of array relayMsLtThres is:"+relayMsLtThres.length);
        if(relayMsLtThres!=null && relayMsLtThres.length!=0)
        {
        for(int i=relayMsLtThres.length-1;i>=0;i--) {
            ConnectedThreadWithRequestCodes newConnectedThread = new ConnectedThreadWithRequestCodes(Constants.deviceToSocket.get(relayMsLtThres[i].remoteMAC), context);
            newConnectedThread.sendMessage(relayMsLtThres[i].UUID, 1, relayMsLtThres[i].incentive);

        }
        }




    }
}
