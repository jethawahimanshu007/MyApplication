package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;


import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import android.content.Context;
import android.widget.Toast;



/**
 * Created by Himanshu on 8/2/2016.
 */

//This is a client which will connect to server in acceptThread
class ConnectThread extends Thread {


    private static final UUID MY_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    int flagSent=0;
    Uri uri;
    public final BluetoothSocket mmSocket[];
    public final BluetoothDevice mmDevice[];
    public int deviceClasses[];
    public SQLiteDatabase mydatabase;
  //HImanshu--  private final Handler mHandlerInput;
    BluetoothAdapter mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    public Context context;


     BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SQLiteDatabase mydatabase=context.openOrCreateDatabase(Constants.DATABASE_NAME, context.MODE_PRIVATE, null);
            String action = intent.getAction();
            DbFunctions dbFunctions=new DbFunctions();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Log.d("ReceiverBroadcast","Something connected");

                Log.d("ConnectThread","Inside connectThreads receiver broadcast");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mydatabase.execSQL("INSERT OR IGNORE INTO DEVICES_CURRENTLY_CONNECTED(BTDeviceMacAddr) VALUES( '"+device.getAddress()+"')");

                Log.d("ReceiverBroadcast","The connected device is:"+device.getName());
            }
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("ReceiverBroadcast","The disconnected device is:"+device.getName());
                mydatabase.execSQL("DELETE FROM DEVICES_CURRENTLY_CONNECTED where BTDeviceMacAddr='"+device.getAddress()+"'");
                mydatabase.execSQL("UPDATE UUID_TBL set isUsed=0 and MacAddress='NO' where MacAddress='"+device.getAddress()+"'" );
                dbFunctions.devicesDisconnectedTime(mydatabase,device.getAddress(),device.getName());

            }
        }
    };
    public ConnectThread(String deviceStrings[],Context context) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final

        int lengthOfArray=deviceStrings.length;
        BluetoothSocket tmp = null;
        mmDevice = new BluetoothDevice[lengthOfArray];
        mmSocket=new BluetoothSocket[lengthOfArray];
        deviceClasses=new int[lengthOfArray];
        for(int i=0;i<mmDevice.length;i++)
        {
            BluetoothDevice device= BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceStrings[i].split("--")[0]);
            mmDevice[i]=device;
            deviceClasses[i]=Integer.parseInt(deviceStrings[i].split("--")[1]);
        }
        this.context=context;
        mydatabase = context.openOrCreateDatabase(Constants.DATABASE_NAME, context.MODE_PRIVATE, null);
        for(int i=0;i<mmDevice.length;i++)
        {
            try {

                tmp = mmDevice[i].createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (Exception e) { Log.d("ConnectThread","MaybeException"); }
            mmSocket[i] = tmp;
        }
    }


    public void run() {

        Log.d("ConnectThread","Entered the run function of ConnectThread");

        mBluetoothAdapter.cancelDiscovery();
        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_ACL_CONNECTED");
        context.registerReceiver(receiver,filter);

        Log.d("ConnectThread","Stopped discovery inside connectThread, length of mmSocket is"+mmSocket.length);

            int flagToConnect=0;
            Log.d("ConnectThread","mmSocket.length:"+mmSocket.length);
            for(int i=0;i<mmSocket.length;i++)
            {
                flagToConnect=0;
                //Log.d("ConnectThread","The device address and name is:"+mmDevice[i].getAddress()+"::"+mmDevice[i].getName());
                //if((mmDevice[i].getAddress().equals("18:3B:D2:E9:CC:9B")||mmDevice[i].getAddress().equals("BC:F5:AC:5D:5D:50"))||mmDevice[i].getAddress().equals("D0:87:E2:4E:7A:2B")) {


                    if(deviceClasses[i]==268)
                    {
                        try {
                            mmSocket[i].connect();
                        }
                        catch(Exception e)
                        {
                            Log.d("ConnectThread","Connection to laptop failed because:"+e);
                        }
                    }

                        //Dell--Tablet--Phone
                    if (new DbFunctions().ifDeviceConnected(mydatabase, mmDevice[i].getAddress()) == 0 && ((mmDevice[i].getAddress().trim().equals("18:3B:D2:EA:15:62")) || (mmDevice[i].getAddress().trim().equals("D0:87:E2:4E:7A:2B")) || (mmDevice[i].getAddress().trim().equals("18:3B:D2:E9:CC:9B")))) {
                      /*For tablet*/ /* if (new DbFunctions().ifDeviceConnected(mydatabase, mmDevice[i].getAddress()) == 0 && ((mmDevice[i].getAddress().trim().equals("18:3B:D2:EA:15:62")))) {/
                      /*For Dell*/  /*if (new DbFunctions().ifDeviceConnected(mydatabase, mmDevice[i].getAddress()) == 0 &&  (mmDevice[i].getAddress().trim().equals("18:3B:D2:E9:CC:9B"))) {*/
                        String macAddress = android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address");
                        if (macAddress.equals("18:3B:D2:EA:15:62") && (mmDevice[i].getAddress().trim().equals("18:3B:D2:E9:CC:9B")))
                            flagToConnect = 1;
                        if (macAddress.equals("D0:87:E2:4E:7A:2B") && (mmDevice[i].getAddress().trim().equals("18:3B:D2:EA:15:62")))
                            flagToConnect = 1;
                            if(flagToConnect==1){
                        Log.d("ConnectThread", "Trying to connect to device:" + mmDevice[i].getName());
                        try {
                            mmSocket[i].connect();
                            mydatabase.execSQL("INSERT OR IGNORE INTO DEVICES_CURRENTLY_CONNECTED(BTDeviceMacAddr) VALUES( '" + mmDevice[i].getAddress() + "')");
                            mydatabase.execSQL("INSERT OR IGNORE INTO TSR_SHARE_DONE_TBL VALUES('" + mmDevice[i].getAddress() + "',0)");
                            if(Constants.deviceToSocket.get(mmDevice[i].getAddress())==null)
                            Constants.deviceToSocket.put(mmDevice[i].getAddress(), mmSocket[i]);
                            new DbFunctions().setConnectedLogTBL(mydatabase, mmDevice[i].getAddress());

                            try {
                                ConnectedThreadWithRequestCodes newConnectedThreadToRead = new ConnectedThreadWithRequestCodes(mmSocket[i], context);
                                newConnectedThreadToRead.start();
                            } catch (Exception e) {
                                Log.d("ConnectThread", "Exception is thrown!! LOL::" + e);
                            }
                            if (i == mmSocket.length - 1) {
                                Log.d("ConnectThread", "All the devices have been tried for connection establishment");
                                postConnection();
                                // postConnMTransfer();
                            }

                        } catch (Exception e) {
                            Log.d("ConnectThread", "Error in connecting mostly:" + e);
                            if (i == mmSocket.length - 1) {
                                Log.d("ConnectThread", "All the devices have been tried for connection establishment");
                                postConnection();
                                // postConnMTransfer();
                            }
                        }

                    }
                    }
                else
                    {
                        if(i==mmSocket.length-1)
                        {
                            Log.d("ConnectThread","All the devices have been tried for connection establishment");
                            postConnection();
                            //postConnMTransfer();
                        }
                    }
                //}
            }



    }

    public void postConnection()
    {

        mydatabase.execSQL("UPDATE CONNECT_ALL_ATTEMPT_TBL SET doneOrNot=0");
        Iterator it = Constants.deviceToSocket.entrySet().iterator();
        //Log.d("ConnectThread","postConnect-notMT::Number of items in hashmap are:"+Constants.deviceToSocket.size());
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());

            String Role=new String();
            //Share TSRs with newly connected device
            Log.d("ConnectThread","Calculating TSRs before sending");
            String TSRsToShare = new DbFunctions().calculateTSRsPre(mydatabase);
            Cursor cursorForRole=mydatabase.rawQuery("SELECT Role from ROLE_TBL where MACAd='SELF'",null);
            while(cursorForRole.moveToNext())
            {
                Role=cursorForRole.getString(0);
            }
          //  Log.d("ConnectThread", "new TSRs are:" + TSRsToShare);
            byte[] byteArrayTSRsToShare = TSRsToShare.getBytes();

            String ratingsDevices=new String();
            int countR=0;
            Cursor cursorForDeviceRatings=mydatabase.rawQuery("SELECT * from USER_RATING_MAP_TBL",null);
            int noOfRows=cursorForDeviceRatings.getCount();
            if(noOfRows==0)
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
            String preambleString = "Preamble::MessageType:" + Constants.MESSAGE_TSRS + "::MessageSize:" + byteArrayTSRsToShare.length + "::Role:"+Role+"::"+ratingsDevices+"::";

            //Log.d("ConnectThread", "Preamble String is:" + preambleString);
            ConnectedThreadWithRequestCodes newConnectedThread = new ConnectedThreadWithRequestCodes((BluetoothSocket) pair.getValue(), context);
            Log.d("CTwRC","Sending TSRs to"+((BluetoothSocket) pair.getValue()).getRemoteDevice().getName()+"--haha  TSRs:"+TSRsToShare);
            newConnectedThread.writePreamble(preambleString);
            newConnectedThread.write(byteArrayTSRsToShare);
            //it.remove();     // avoids a ConcurrentModificationException
            //Log.d("ConnectThread","postConnect-notMT::Number of items in hashmap after it.remove are:"+Constants.deviceToSocket.size());
        }
        mydatabase.execSQL("UPDATE CONNECT_ALL_ATTEMPT_TBL SET doneOrNot=1");

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
        class MesParams  implements Comparable<MesParams>{
            String UUID;
            String sourceMac;
            long size,quality,priority;
            double sumWeightsRemote;
            double incentive;
            String remoteMAC;
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
                while (cursorForMatchingImage.moveToNext()) {

                    String sourceForMessage=cursorForMatchingImage.getString(8);

                    String tagsForCurrentImage=cursorForMatchingImage.getString(4);
                    String UUID=cursorForMatchingImage.getString(10);
                    Log.d("ConnectThread","postConnMT::Message considered for transfer:"+UUID);
                    int sizeIndex=cursorForMatchingImage.getColumnIndex("size"); int qualityIndex=cursorForMatchingImage.getColumnIndex("quality"); int priorityIndex=cursorForMatchingImage.getColumnIndex("priority");
                    long size=cursorForMatchingImage.getLong(sizeIndex); long quality=cursorForMatchingImage.getLong(qualityIndex);int priority=cursorForMatchingImage.getInt(priorityIndex);
                    MesParams mesParams=new MesParams();
                    mesParams.remoteMAC=remoteMAC; mesParams.sourceMac=sourceForMessage;
                    mesParams.UUID=UUID; mesParams.quality=quality;mesParams.priority=priority;
                    if(sizeMax>size)
                        sizeMax=size;
                    if(quality>qualityMax)
                        qualityMax=quality;

                    if(belongs_to.equals("SELF"))
                    {

                        if(MACtoMessageRelay!=null && MACtoMessageRelay.get(remoteMAC)!=null && MACtoMessageRelay.get(remoteMAC).contains(UUID))
                        {
                            //move message from relay map to destination map
                            ArrayList<MesParams> relayList=MACtoMessageRelay.get(remoteMAC);
                            relayList.remove(mesParams);
                            MACtoMessageRelay.put(remoteMAC,relayList);

                            ArrayList<MesParams> destList=MACtoMessageRelay.get(remoteMAC);
                            mesParams.flagForDest=1;
                            destList.add(mesParams);
                            MACtoMessageDest.put(remoteMAC,destList);
                        }

                    }
                    if(listOfUUIDsChecked.contains(UUID))
                        continue;
                    else{
                        listOfUUIDsChecked.add(UUID);
                    }
                    String tagsForCurrentImageArray[]=tagsForCurrentImage.split(",");
                    //Find local and sum for all the messages
                    for(int i=0;i<tagsForCurrentImageArray.length;i++)
                    {
                        if(tagsForCurrentImageArray[i].length()>0)
                        {
                            Cursor cursorForTagsWeight=mydatabase.rawQuery("SELECT weight from TSR_TBL where SI='"+tagsForCurrentImageArray[i]+"'",null);
                            if(cursorForTagsWeight.moveToFirst()) {
                                double tempWeight = cursorForTagsWeight.getDouble(0);
                                localSum+=tempWeight;
                            }

                            /////comment-Doug, get a node identifier for TSR_REMOTE
                            Cursor cursorForTagsWeightRemote=mydatabase.rawQuery("SELECT weight from TSR_REMOTE_TBL where SI='"+tagsForCurrentImageArray[i]+"' and deviceMacAddr='"+remoteMAC+"'",null);
                            if(cursorForTagsWeightRemote.moveToFirst()) {
                                double tempWeight = cursorForTagsWeightRemote.getDouble(0);
                                remoteSum+=tempWeight;
                            }

                        }
                    }
                    mesParams.sumWeightsRemote=remoteSum;
                    if(remoteSum>remoteSumMax)
                    {
                        remoteSumMax=remoteSum;
                    }
                    Log.d("DbFunctions","localSum and remoteSum for the current message is:"+localSum+"--"+remoteSum);
                    double differenceInSums=remoteSum-localSum;
                    /////come here
                    hm.put(UUID,differenceInSums);
                    //See if this message has been exchanged before between the two devices
                    Cursor ifSentOrReceived = mydatabase.rawQuery("SELECT * from SENT_IMAGE_LOG where UUID='" + cursorForMatchingImage.getString(10) + "' and (sentTo='" + remoteMAC + "' OR receivedFrom='" + remoteMAC + "')", null);
                    if (ifSentOrReceived.getCount()>0) {
                        //Condition that this message has been exchanged before
                        if (!listUUIDSentOrReceived.contains(cursorForMatchingImage.getString(10))) {
                            listUUIDSentOrReceived.add(cursorForMatchingImage.getString(10));
                            Log.d("DbFunctions", "Image already sent or received:" + ifSentOrReceived.getString(0) + "---" + ifSentOrReceived.getString(1) + "---" + ifSentOrReceived.getString(2));
                        }
                    } else {
                        //condition that this message has not been exchanged before
                        if(belongs_to.equals("SELF")) {

                            Log.d("ConnectThread",mesParams.UUID+" is added to dest list");
                            ArrayList destList=new ArrayList<MesParams>();
                            if(MACtoMessageDest.get(remoteMAC)!=null) {
                                destList = MACtoMessageDest.get(remoteMAC);
                            }
                                mesParams.flagForDest = 1;
                                destList.add(mesParams);
                                MACtoMessageDest.put(remoteMAC, destList);
                                listMessages.add(mesParams);
                                listOfImages.add(UUID);

                        }
                        if(hm.get(UUID)>=0)
                        {
                            if(listOfImages.contains(UUID))
                            {
                                //This image is included in the list of messages to be sent

                            }
                            else
                            {
                                //This image is not included in the list of messages to be sent,so add it
                                if(!(belongs_to.equals("SELF")))
                                {
                                    ArrayList<MesParams> relayList=MACtoMessageRelay.get(remoteMAC);
                                    mesParams.flagForDest=1;
                                    relayList.add(mesParams);
                                    MACtoMessageRelay.put(remoteMAC, relayList);
                                    listMessages.add(mesParams);
                                }
                                listOfImages.add(UUID);
                            }
                        }
                    }
                }
            }
        } ///List of all the messages to be transferred formed-- two maps from mac to message for dest and relay

        //Calculate incentives now


        //Variables to store size of each message
        //Iterate through dest hashmap to find max size
        it = MACtoMessageDest.entrySet().iterator();

        HashMap<String,MesParams> UUIDtoParams=new HashMap<String,MesParams>();
        ArrayList<MesParams> allMessages=new ArrayList<MesParams>();

        //Two arrays for descending order arrangement
        MesParams[] destMs,relayMs; int destMsC=0;int relayMsC=0;

        //Calculate incentive for all messages

        for(MesParams mesParams:allMessages)
        {
            mesParams.incentive=mesParams.sumWeightsRemote*mesParams.size*mesParams.quality/(Role*mesParams.priority*sizeMax*qualityMax*remoteSumMax);
            if(mesParams.flagForDest==1)
                destMsC++;
            else
                relayMsC++;
        }
        destMs=new MesParams[destMsC];
        relayMs=new MesParams[relayMsC];

        //Arrange in descending order of incentive
        Arrays.sort(destMs);
        Arrays.sort(relayMs);

       /* while(it.hasNext())
        {
            Map.Entry pair = (Map.Entry) it.next();
            MesParams mesParams=(MesParams) pair.getValue();
            mesParams.incentive=mesParams.sumWeightsRemote*mesParams.size*mesParams.quality/(Role*mesParams.priority*sizeMax*qualityMax*remoteSumMax);
            if(!allMessages.contains(mesParams))
            {
                allMessages.add(mesParams);
            }
        }

        //Iterate through relay hashmap to find max size
        it = MACtoMessageRelay.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry pair = (Map.Entry) it.next();
            MesParams mesParams=(MesParams) pair.getValue();
            mesParams.incentive=mesParams.sumWeightsRemote*mesParams.size*mesParams.quality/(Role*mesParams.priority*sizeMax*qualityMax*remoteSumMax);
        }*/
        //Sending to destinations--temporary
        /*
        Log.d("ConnectThread","listMessages.size():"+listMessages.size());
        for(MesParams tempMesParams:listMessages)
        {

            String preambleString = "Preamble::MessageType:" + Constants.MESSAGE_INCENT_REQ+"::IncentiveRequired:"+tempMesParams.incentive+"::UUID:"+tempMesParams.UUID+"::";
            Log.d("ConnectThread", "Preamble String is:" + preambleString);
            ConnectedThreadWithRequestCodes newConnectedThread = new ConnectedThreadWithRequestCodes((BluetoothSocket) Constants.deviceToSocket.get(tempMesParams.remoteMAC), context);
            //Log.d("CTwRC","Sending TSRs to"+((BluetoothSocket) pair.getValue()).getRemoteDevice().getName()+"--haha  TSRs:"+TSRsToShare);
            Log.d("ConnectThread","asking for incentive from :"+tempMesParams.remoteMAC);
            newConnectedThread.writePreamble(preambleString);
        }
        */

        Log.d("ConnectThread","Value of destMsc and relayMsc before transferring is:"+destMsC+"::"+relayMsC);
        for(int i=destMsC-1;i>=0;i++)
        {
            String Preamble;
            Log.d("ConnectThread","Value of sourceMac is:"+destMs[i].sourceMac);
            String preambleString = "Preamble::MessageType:" + Constants.MESSAGE_INCENT_REQ+"::IncentiveRequired:"+destMs[i].incentive+"::UUID:"+destMs[i].UUID+"::"+destMs[i].sourceMac+"::";
            Log.d("ConnectThread", "Preamble String is:" + preambleString);
            ConnectedThreadWithRequestCodes newConnectedThread = new ConnectedThreadWithRequestCodes((BluetoothSocket) Constants.deviceToSocket.get(destMs), context);
            //Log.d("CTwRC","Sending TSRs to"+((BluetoothSocket) pair.getValue()).getRemoteDevice().getName()+"--haha  TSRs:"+TSRsToShare);
            Log.d("ConnectThread","asking for incentive from :"+destMs[i]);
            newConnectedThread.writePreamble(preambleString);

        }
        //Sending to relays
        for(int i=relayMsC-1;i>0;i++)
        {
            ConnectedThreadWithRequestCodes newConnectedThread = new ConnectedThreadWithRequestCodes((BluetoothSocket) Constants.deviceToSocket.get(destMs), context);
            newConnectedThread.sendMessage(relayMs[i].UUID,1,relayMs[i].incentive);
        }




    }
    public void cancel() {
        try {
            //mmSocket.close();
        } catch (Exception e) { }
    }





}



