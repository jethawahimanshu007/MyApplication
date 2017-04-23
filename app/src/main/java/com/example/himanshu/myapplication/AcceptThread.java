package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

/**
 * Created by Himanshu on 8/27/2016.
 */

    class AcceptThread extends Thread {
    Context context;
    SQLiteDatabase mydatabase;
        private static  UUID MY_UUID ;
        private final BluetoothServerSocket mmServerSocket;
        BluetoothSocket mmSocket = null;
        private  InputStream mmInStream;
        public  OutputStream mmOutStream;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
            //UUID MY_UUID= UUID.randomUUID();
            Log.d("UUID","UUID is:"+MY_UUID);
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("BTcheck", MY_UUID);
            } catch (Exception e) { }
            mmServerSocket = tmp;
        }

    public AcceptThread(Context context) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final
        BluetoothServerSocket tmp = null;
        BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        this.context=context;
        //UUID MY_UUID= UUID.randomUUID();
        Log.d("UUID","UUID is:"+MY_UUID);
        try {
            // MY_UUID is the app's UUID string, also used by the client code
            tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("BTcheck", MY_UUID);
        } catch (Exception e) { }
        mmServerSocket = tmp;
    }

    public AcceptThread(Context context, SQLiteDatabase mydatabase) {
        // Use a temporary object that is later assigned to mmServerSocket,
        // because mmServerSocket is final

        BluetoothServerSocket tmp = null;
        BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        Cursor cursorForUUIDs=mydatabase.rawQuery("SELECT * from UUID_TBL where isUsed=0",null);
        while(cursorForUUIDs.moveToNext())
        {
            MY_UUID=UUID.fromString(cursorForUUIDs.getString(0));


            break;
        }
        this.context=context;
        this.mydatabase=mydatabase;
        //UUID MY_UUID= UUID.randomUUID();
        Log.d("UUID","UUID is:"+MY_UUID);
        try {
            // MY_UUID is the app's UUID string, also used by the client code
             MY_UUID =
                    UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
            tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("BTcheck", MY_UUID);
        } catch (Exception e) { }
        mmServerSocket = tmp;
    }
    public void run() {

            Log.d("AcceptClass-MainAc","Started listening");
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    Log.d("AcceptThread","Going to blocking mode with call to accept");
                    mmSocket = mmServerSocket.accept();
                    mmInStream=mmSocket.getInputStream();
                    mmOutStream=mmSocket.getOutputStream();
                    mydatabase.execSQL("UPDATE UUID_TBL set MacAddress='"+mmSocket.getRemoteDevice().getAddress()+"' AND isUsed=1 where UUID='"+MY_UUID+"'");

                    Log.d("AcceptThread","Connection is accepted maybe!!!!! The value of connected socket returned is:"+mmSocket);
                    new DbFunctions().setConnectedLogTBL(mydatabase,mmSocket.getRemoteDevice().getAddress());

                    try {
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
                        int MessageType;
                        int MessageSize = 0;
                        String MessageFileName = "";
                        int sleepFlag = 0;
                        int i = 0;
                        /*
                        while (true) {

                            try {

                                if (mmInStream.available() > 0) {

                                    int countForPreamble = 0;
                                    while (countForPreamble != Constants.PREAMBLE_SIZE) {
                                        bytes = mmInStream.read(preamble, 0, preamble.length - countForPreamble);
                                        countForPreamble += bytes;
                                    }


                                    Log.d("CTwReqCodes", "Preamble received is:" + new String(preamble));
                                    if(preamble==null)
                                    {
                                        Log.d("CTwRC","preable is null");
                                    }
                                    if (new String(preamble).split("::")[0].equals("Preamble")) {

                                        MessageType = Integer.parseInt(new String(preamble).split("::")[1].split(":")[1]);
                                        MessageSize = Integer.parseInt(new String(preamble).split("::")[2].split(":")[1]);
                                        if (new String(preamble).contains("Lengths")) {
                                            fileNameImage = new String(preamble).split("::")[3].split(":")[1];
                                            fileNameLength = Integer.parseInt(new String(preamble).split("::")[3].split(":")[2]);
                                            latitudeReceived=new String(preamble).split("::")[3].split(":")[3];
                                            longitudeReceived=new String(preamble).split("::")[3].split(":")[4];
                                            timestampReceived=new String(preamble).split("::")[3].split(":")[5]+new String(preamble).split("::")[3].split(":")[6]+new String(preamble).split("::")[3].split(":")[7];
                                            tagsReceived=new String(preamble).split("::")[4];
                                            mimeReceived=new String(preamble).split("::")[5];
                                            formatRecived=new String(preamble).split("::")[6];
                                            macAddrReceived=new String(preamble).split("::")[7];
                                            deviceNameReceived=new String(preamble).split("::")[8];
                                            UUIDReceived=new String(preamble).split("::")[9];
                                        }
                                        Log.d("CTwReqCode", "MessageType and MessageSize received are:" + MessageType + "  and " + MessageSize);


                                        switch (MessageType) {
                                            case Constants.MESSAGE_TAGS:
                                                byte[] bufferForTags = new byte[MessageSize];
                                                int bytesReadForTags = 0;
                                                int currentForTags = 0;
                                                if (MessageSize != 0) {

                                                    Log.d("CTwReq", "waiting to read tags from remote device!!");
                                                    while (currentForTags != MessageSize) {
                                                        bytesReadForTags = mmInStream.read(bufferForTags, 0, MessageSize - currentForTags);
                                                        currentForTags += bytesReadForTags;
                                                    }
                                                    Log.d("CTwRequestCodes", "The tags got from the device are :" + new String(bufferForTags));

                                                    String remoteTags = new String();
                                                    remoteTags = new String(bufferForTags);
                                                    Log.d("CTwReq", "remoteTags received::" + remoteTags);
                                                    Log.d("CTwRC", "Going to call getTags function");
                                                    String localTags = "";
                                                    DbFunctions dbFunctions = new DbFunctions();
                                                    localTags = dbFunctions.getTags(mydatabase);
                                                    Log.d("CTwRC", "After call to getTags function,value of localTags:" + localTags);
                                                    String splitByCommas[] = localTags.split(",");
                                                    String[] unique = new HashSet<String>(Arrays.asList(splitByCommas)).toArray(new String[0]);
                                                    localTags = TextUtils.join(",", unique);

                                                    String temp = new String();
                                                    if (localTags.length() != 0) {
                                                        Log.d("CTwRC", "Went into localTags.length()!=0 if condition");
                                                        String localTagsArray[] = localTags.split(",");
                                                        String remoteTagsArray[] = remoteTags.split(",");

                                                        Log.d("CTwRC", "localTags Array and remote Tags array:" + Arrays.toString(localTagsArray) + "  and  " + Arrays.toString(remoteTagsArray));
                                                        for (int ltac = 0; ltac < localTagsArray.length && localTagsArray.length != 0 && remoteTagsArray.length != 0; ltac++) {
                                                            for (int rtac = 0; rtac < remoteTagsArray.length; rtac++) {
                                                                if (localTagsArray[ltac].equals(remoteTagsArray[rtac])) {
                                                                    temp += localTagsArray[ltac] + ",";
                                                                }
                                                            }
                                                        }
                                                    }
                                                    String matchingTags = new String();
                                                    Log.d("CTwReqCode", "temp string is:" + temp);
                                                    if (!temp.equals("")) {
                                                        matchingTags = temp.substring(0, temp.length() - 1);
                                                        splitByCommas = matchingTags.split(",");
                                                        unique = new HashSet<String>(Arrays.asList(splitByCommas)).toArray(new String[0]);
                                                        matchingTags = TextUtils.join(",", unique);

                                                        Log.d("CTwRC", "The matching tags are:" + matchingTags);
                                                        if (matchingTags.length() != 0) {
                                                            dbFunctions.insertIntoDeviceTagTbl(mydatabase, mmSocket.getRemoteDevice().getName(), mmSocket.getRemoteDevice().getAddress(), matchingTags);
                                                        }
                                                        String preambleString;

                                                        preambleString = "Preamble::MessageType:" + Constants.MESSAGE_TAGS_MATCHING + "::MessageSize:" + matchingTags.length() + "::";
                                                        byte[] premable = new byte[Constants.PREAMBLE_SIZE];
                                                        byte preambleSomething[] = preambleString.getBytes();
                                                        Arrays.fill(preamble, (byte) 0);
                                                        System.arraycopy(preambleSomething, 0, preamble, 0, preambleSomething.length);
                                                        write(preamble);
                                                        byte[] matchingTagsBytes = matchingTags.getBytes();
                                                        write(matchingTagsBytes);
                                                    }
                                                } else {
                                                    Log.d("CTwRequestCodes", "The device has no tags!!");
                                                }
                                                break;

                                            case Constants.MESSAGE_TAGS_MATCHING:
                                                Log.d("CTwRC", "Entered case MESSAGE_TAGS_MATCHING of switch");
                                                currentForTags = 0;
                                                bufferForTags = new byte[MessageSize];
                                                bytesReadForTags = 0;
                                                if (MessageSize != 0) {

                                                    Log.d("CTwReq", "waiting to read tags from remote device!!");
                                                    while (currentForTags != MessageSize) {
                                                        bytesReadForTags = mmInStream.read(bufferForTags, 0, MessageSize - currentForTags);
                                                        currentForTags += bytesReadForTags;
                                                    }
                                                    String matchingTags = new String(bufferForTags);
                                                    Log.d("CTwRC", "List of matching tags received from " + mmSocket.getRemoteDevice().getName() + " are " + matchingTags);
                                                    DbFunctions dbFunctions = new DbFunctions();
                                                    dbFunctions.insertIntoDeviceTagTbl(mydatabase, mmSocket.getRemoteDevice().getName(), mmSocket.getRemoteDevice().getAddress(), matchingTags);
                                                    String tagsForDevice = dbFunctions.getTagsForDevice(mydatabase, mmSocket.getRemoteDevice().getAddress());
                                                    Log.d("CTwRC", "tagsForDevice is:" + tagsForDevice);
                                                    String tagsForDeviceArray[] = tagsForDevice.split(",");
                                                    for(int index=0;index<tagsForDeviceArray.length;index++)
                                                    {
                                                        String imageAttrs = dbFunctions.getImagePaths(mydatabase, tagsForDeviceArray[i]);
                                                        Log.d("CTwRC", "If imageAttrs is null:" + (imageAttrs == null));
                                                        Log.d("CTwRC","imageAttrs are:"+imageAttrs);
                                                        if (!imageAttrs.split("<<").equals("")) {
                                                            String imageAttrsArray[] = imageAttrs.split("<<");

                                                            //String imagePathsArray[] ;

                                                            ArrayList<String> imagesArrayList = new ArrayList<String>();
                                                            for (int counter = 0; counter < imageAttrsArray.length; counter++) {
                                                                Log.d("ctr","imageAttrsArray[counter]"+imageAttrsArray[counter]);
                                                                String imageAttsPerImage = imageAttrsArray[counter];
                                                                String imagePath = imageAttrsArray[counter].split(":::")[0];
                                                                String latitude = imageAttrsArray[counter].split(":::")[1];
                                                                String longitude = imageAttrsArray[counter].split(":::")[2];
                                                                String timestamp = imageAttrsArray[counter].split(":::")[3];
                                                                Log.d("ConnTwRC","timestamp is:"+timestamp);
                                                                String tagsForCurrentImage = imageAttrsArray[counter].split(":::")[4];
                                                                Log.d("ConnTwRC","tagsForCurrentimage is:"+tagsForCurrentImage);


                                                                if(!imagesArrayList.contains(imagePath))
                                                                    imagesArrayList.add(imagePath);
                                                                else
                                                                    continue;

                                                                Log.d("CTwRC", "imageAttrs are:" + imagePath + latitude + longitude + timestamp);

                                                                java.io.RandomAccessFile raf = new java.io.RandomAccessFile(imagePath, "r");
                                                                byte[] b = new byte[(int) raf.length()];
                                                                Log.d("CTwRC", "Length of byte array from image is:" + b.length);
                                                                raf.readFully(b);


                                                                String fileNameProcessing[] = imagePath.split("/");

                                                                String fileName = fileNameProcessing[fileNameProcessing.length - 1];
                                                                Log.d("CTwRC", "Filename is:" + fileName);
                                                                Log.d("CTwrc", "filename split by dot has entries:" + fileName.split("\\.").length);
                                                                String mime = "images/" + fileName.split("\\.")[1];
                                                                String format = fileName.split("\\.")[1];
                                                                String localMacAddr = BluetoothAdapter.getDefaultAdapter().getAddress();
                                                                String localName = BluetoothAdapter.getDefaultAdapter().getName();
                                                                String UUID=localMacAddr+"-"+format;
                                                                Log.d("CTwRC","UUID:"+UUID);
                                                                String preambleString;
                                                                preambleString = "Preamble::MessageType:" + Constants.MESSAGE_IMAGE + "::MessageSize:" +
                                                                        b.length + "::Lengths:" + fileName + ":" +
                                                                        b.length + ":" + latitude + ":" + longitude + ":" +
                                                                        timestamp + "::" + tagsForCurrentImage + "::" +
                                                                        mime + "::" + "::" + format + "::" + localMacAddr + "::" + localName + "::"+UUID+"::";
                                                                Log.d("CTwRC", "Preamble to be sent for image file is:" + preambleString);
                                                                byte preambleSomething[] = preambleString.getBytes();
                                                                Arrays.fill(preamble, (byte) 0);
                                                                System.arraycopy(preambleSomething, 0, preamble, 0, preambleSomething.length);
                                                                Log.d("CTwRC", "fileName to be sent is:" + fileName);
                                                                write(preamble);
                                                                write(b);
                                                            }


                                                        }
                                                    }

                                                }

                                                break;
                                            case Constants.MESSAGE_IMAGE:
                                                try {
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
                                                    dbFunctions.insertIntoDeviceReceiptTbl(mydatabase,mmSocket.getRemoteDevice().getName(),mmSocket.getRemoteDevice().getAddress(),tagsReceived,latitudeReceived,longitudeReceived,timestampReceived,deviceNameReceived,macAddrReceived,mimeReceived,formatRecived,Environment.getExternalStorageDirectory().getAbsoluteFile()+"/"+fileNameImage,UUIDReceived);

                                                    Log.d("CTwRC","File received");


                                                } catch (Exception e) {
                                                    Log.d("CTwRC", "Exception occured in case MESSAGE_IMAGE in switch:" + e);
                                                }
                                                break;
                                            case Constants.MESSAGE_TEST:
                                                try {
                                                    byte[] bufferForTest = new byte[MessageSize];
                                                    int bytesReadForTest = 0;
                                                    int currentForTest = 0;
                                                    if (MessageSize != 0) {

                                                        Log.d("CTwReq", "waiting to read tags from remote device!!");

                                                        while (currentForTest != MessageSize) {
                                                            bytesReadForTest = mmInStream.read(bufferForTest, 0, MessageSize - currentForTest);

                                                            currentForTest += bytesReadForTest;
                                                            Log.d("CTwRC", "Total Bytes read for test:" + currentForTest);
                                                        }

                                                        Log.d("CTwRC", "Total bytes read:" + currentForTest);


                                                        FileOutputStream out = null;
                                                        try {
                                                            out = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/imageNew.jpg");
                                                            out.write(bufferForTest);


                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        } finally {
                                                            try {
                                                                if (out != null) {
                                                                    out.close();
                                                                }
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                        try {

                                                        } catch (Exception e) {
                                                            Log.d("ConnectedThread", "Exception occured" + e);
                                                        }
                                                    }
                                                }
                                                catch(Exception e)
                                                {

                                                }
                                                break;
                                            case Constants.MESSAGE_TSRS:
                                            {
                                                byte remoteBytesRead[] = readBytes(MessageSize);
                                                String remoteTSRs = new String(remoteBytesRead);

                                                Log.d("CTwRC", "Remote TSRs received:" + remoteTSRs);
                                                //Process received TSRs
                                                new DbFunctions().saveRemoteTSRs(mydatabase, mmSocket.getRemoteDevice(), remoteTSRs);
                                                Log.d("CTwRC","Debug 1");
                                                //Send its own TSRs
                                                String TSRsToShare = new DbFunctions().calculateTSRsPre(mydatabase);
                                                Log.d("CTwRC","Debug 2");
                                                Log.d("ConnectThread", "new TSRs are:" + TSRsToShare);
                                                byte[] byteArrayTSRsToShare = TSRsToShare.getBytes();
                                                String preambleString = "Preamble::MessageType:" + Constants.MESSAGE_TSRS_BACK + "::MessageSize:" + byteArrayTSRsToShare.length + "::";
                                                Log.d("ConnectThread", "Preamble String is:" + preambleString);
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
                                            }
                                            break;
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
                        }*/
                        ConnectedThreadWithRequestCodes newConnectedThread=new ConnectedThreadWithRequestCodes(mmSocket,context);
                        newConnectedThread.start();
                    }

                    catch(Exception e)
                    {
                        Log.d("AcceptThread","Exception is thrown!! LOL::"+e);
                        AcceptThread at=new AcceptThread(context,mydatabase);
                        at.start();
                    }
                    AcceptThread at=new AcceptThread(context,mydatabase);
                    at.start();


                    //Thread.sleep(500);

                } catch (Exception e) {
                    Log.d("AcceptThread","Exception is thrown, lol..:"+e);
                    break;
                }
                // If a connection was accepted
                if (mmSocket != null) {
                    // Do work to manage the connection (in a separate thread)
                    //manageConnectedSocket(socket);
                    //mmServerSocket.close();
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (Exception e) { }
        }

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
        // Log.d("CT","Preamble string is:"+preambleString);
        byte preambleSomething[]=preambleString.getBytes();
        Arrays.fill(preamble,(byte)0);
        System.arraycopy(preambleSomething,0,preamble,0,preambleSomething.length);
        try {
            mmOutStream.write(preamble);
        }
        catch(Exception e)
        {
            Log.d("CTwRC","Exception in writing preamble");
        }

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
    }

