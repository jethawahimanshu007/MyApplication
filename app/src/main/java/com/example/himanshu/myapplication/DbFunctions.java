package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Himanshu on 9/7/2016.
 */

//This is a class consisting of all the functions for database--- Equivalent of stored procedures in MySQL
public class DbFunctions {

    // This function is used to pass the list of the Image paths of received images
    public  String[] getReceivedImages(SQLiteDatabase mydatabase) {

        String[] someArray=null;
        Cursor cursorForImages=null;
        List<String> imagepaths = new ArrayList<String>();
        int numOfImages = 0;
        //////
        try {

            //Database query for finding received images.. if the message has destination value set as NO, it means
            // that the image has come from another device
            cursorForImages = mydatabase.rawQuery("SELECT imagePath from MESSAGE_TBL where destMacAddr<>'NO' and destName<>'NO' ", null);
            if (cursorForImages == null)
                Log.d("DbFuncGetTags", "cursorForImages isnull");
        } catch (Exception e) {
            Log.d("DbFUncs", "Exception occurred, hahahaha!::" + e);
        }
        try {
            String image;

            try {

                if (cursorForImages != null)
                    while (cursorForImages.moveToNext()) {
                        image = cursorForImages.getString(0);
                        imagepaths.add(image);
                        numOfImages++;
                    }
                if (numOfImages != 0) {
                    Log.d("DbFuncs","Number of images are"+numOfImages);

                    ///Image paths are added to the someArray here
                     someArray=imagepaths.toArray(new String[0]);
                    Log.d("DbFuncs","Arraylist to array conversiton done");
                }


            } catch (Exception e) {
                Log.d("DbFunctions", "Exception occured in sqlite query!!! ::" + e);
            }

            Log.d("DbFuncs","Value of someArray is:"+someArray);
            return someArray;
        }
        catch(Exception e)
        {
                Log.d("Dbfuncs","Exception in getrece:"+e);
        }
        return someArray;
    }


    //Function to check if a device with a MACaddress is connected
    public int ifDeviceConnected(SQLiteDatabase mydatabase, String macAddr)
    {

        Cursor cursorForConnectedDevices = mydatabase.rawQuery("SELECT * from DEVICES_CURRENTLY_CONNECTED where BTDeviceMacAddr='"+macAddr+"'", null);
        if(cursorForConnectedDevices.moveToFirst())
            return 1;
        else
        return 0;
    }
    //Deletion to be done for some disconnected devices
    public void deleteConnectedDevices(SQLiteDatabase mydatabase)
    {
        mydatabase.execSQL("DELETE from DEVICES_CURRENTLY_CONNECTED");
        mydatabase.execSQL("DELETE from CONNECTED_LOG_TBL");
    }

    //Set the disconnection time for a device with a particular mac address
    public void devicesDisconnectedTime(SQLiteDatabase mydatabase,String deviceMacAddress,String deviceName)
    {
        try {
            //Database query for the same
            Cursor cursorForDisConnectedDevices = mydatabase.rawQuery("SELECT * from DEVICE_DISCONNECTED_TIME where deviceMacAddr='" + deviceMacAddress + "' and deviceName='" + deviceName + "'", null);
            String sqlQuery="";
            if (cursorForDisConnectedDevices != null) {
                sqlQuery = "UPDATE DEVICE_DISCONNECTED_TIME set currentTime=(datetime('now','localtime'))" + " WHERE deviceMacAddr='" + deviceMacAddress+"'";
            } else {
                sqlQuery = "INSERT INTO DEVICE_DISCONNECTED_TIME(deviceMacAddr,deviceName) values ('" + deviceMacAddress + "','" + deviceName + "')";
            }
            try {
                mydatabase.execSQL(sqlQuery);
            } catch (Exception e) {
                Log.d("DbFunctions", "Exception occured:" + e);
            }

        }
        catch(Exception e)
        {
            Log.d("DbFunctions","Exception occured:"+e);
        }
    }

    //Insert into TSR table some more tags
    public void insertIntoTSRTbl(SQLiteDatabase mydatabase, String tags)
    {
        String tagsArray[]=tags.split(",");
        Log.d("DbFunctions","Debug inside insertIntoTSRTbl, tags here are:"+tags);
        for(int i=0;i<tagsArray.length;i++) {
            Log.d("DbFunctions","insertIntoTSRTbl, value of a tag is"+tagsArray[i]);
            Cursor cursorForDisConnectedDevices = mydatabase.rawQuery("SELECT * from TSR_TBL where SI='"+tagsArray[i]+"'", null);
            if (!cursorForDisConnectedDevices.moveToFirst()) {
                Log.d("DbFunctions","This tag is not present in TSR_TBL, so inserting it");
                mydatabase.execSQL("INSERT OR IGNORE INTO TSR_TBL(belongs_to,SI,weight,updated_by) VALUES ('SELF','"+tagsArray[i]+"',0.5,'SELF')");
            }
        }

    }

    ///Find out the latest timestamps in all the timestamps of disconnected devices
    public String latestTimeStamp(ArrayList<String> times)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStampLatest="";
        try {
            Date leastDate = dateFormat.parse("1900-02-01 01:01:01");
            long leastDateTime=leastDate.getTime();

        for(String timeTemp:times)
        {
            Date timeTempDate= dateFormat.parse(timeTemp);
            if(timeTempDate.getTime()-leastDateTime>0)
            {
                timeStampLatest=timeTemp;
            }
        }
        }
        catch(Exception e)
        {
            Log.d("DbFunctions","Exception in latestTimestamp:"+e);
        }
        return timeStampLatest;
    }

    //ChitChat logic
    public void saveRemoteTSRs(SQLiteDatabase mydatabase, BluetoothDevice bluetoothDevice,String remoteTSRs)
    {

        try {
            ////Logic here is that firstly put into TSR_REMOTE and TSR_TBL
            ///Then scan through all the connected devices and see their SIs and then calculate delta for each one of them
            ///Put delta value in
            int Psi = 1;
            double delta = 0.0;
            double newWeight = 0.0;
            double constant=3600000;
            //SI to delta value
            HashMap<String, Double> hm = new HashMap<String, Double>();
            String currentTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String TSRs[] = remoteTSRs.split(",");
            Log.d("DbFunctions","RemoteTSRs received are:"+remoteTSRs);
            ///This for loop inserts or updates TSRs in TSR_REMOTE_TBL and inserts only new TSRs into TSR_TBL
            if(remoteTSRs!=null && remoteTSRs.length()!=0) {
                Log.d("DbFunctions", "TSRs is not null");
                for (int i = 0; i < TSRs.length; i++) {
                    String tempTSRVals[] = TSRs[i].split("\\[\\[");
                   // String tempTSRVals[] = TSRs[i].split("\\[\\[");
                    //Log.d("");
                    String tempRemoteSI = tempTSRVals[0];
                    String tempRemoteWeightSI = tempTSRVals[1];
                    String tempRemoteBelongsTo = tempTSRVals[2];
                    Log.d("DbFunctions","saveRemoteTSRs-- Before DbFunctions insert into TSR_REMOTE_TBL:");
                    mydatabase.execSQL("INSERT OR IGNORE INTO TSR_REMOTE_TBL(deviceMacAddr,SI,weight,belongs_to) VALUES('" + bluetoothDevice.getAddress() + "','" + tempRemoteSI + "'," + Double.parseDouble(tempRemoteWeightSI) + ",'" + tempRemoteBelongsTo + "')");
                    Log.d("DbFunctions","saveRemoteTSRs-- After DbFunctions insert into TSR_REMOTE_TBL, SI and remoteMAC inserted are:"+tempRemoteSI+"::"+bluetoothDevice.getAddress());
                    mydatabase.execSQL("UPDATE TSR_REMOTE_TBL set weight=" + tempRemoteWeightSI + " where deviceMacAddr='" + bluetoothDevice.getAddress() + "' and SI='" + tempRemoteSI + "'");
                    ///Here insertion of new TSRs into TSR_TBL is done
                    Cursor cursorForTSRs = mydatabase.rawQuery("SELECT * from TSR_TBL where SI='" + tempRemoteSI + "'", null);
                    int flag=0;
                   ///If SI is not there in local device, insert it into TSR_TBL
                    if(cursorForTSRs.getCount()==0) {

                        //belongs_to VARCHAR,SI varchar,time DATE DEFAULT (datetime('now','localtime')), weight real,updated_by
                        String sqlQuery = "INSERT OR IGNORE INTO TSR_TBL(belongs_to,SI,weight,updated_by) VALUES('" + bluetoothDevice.getAddress() + "','" + tempRemoteSI + "'," + Double.parseDouble(tempRemoteWeightSI) + ",'" + bluetoothDevice.getAddress() + "')";

                        mydatabase.execSQL(sqlQuery);
                    }

                }

                ///find out currently connected devices to apply Growth function
                Cursor cursorForConnectedDevices = mydatabase.rawQuery("SELECT * from DEVICES_CURRENTLY_CONNECTED", null);

                    while (cursorForConnectedDevices.moveToNext()) {

                        //deviceMacAddr VARCHAR, deviceName VARCHAR,currentTime DATE DEFAULT (datetime('now','localtime')))

                        String deviceMacAddr = cursorForConnectedDevices.getString(0);


                        String connectEstablishTime = cursorForConnectedDevices.getString(1);


                        ///Find out TSRs for this device from REMOTE_TSR_TBL
                        Cursor cursorForRemoteDevicetags = mydatabase.rawQuery("SELECT deviceMacAddr, SI, weight FROM TSR_REMOTE_TBL where deviceMacAddr='" + deviceMacAddr + "'",null);

                        //Log.d("Column name exists or not:"+);

                        while (cursorForRemoteDevicetags.moveToNext()) {

                            //deviceMacAddr VARCHAR, SI VARCHAR, weight real, time DEFAULT (datetime('now','localtime'))),belongs_to VARCHAR
                            String belongs_to_remote = cursorForRemoteDevicetags.getString(0);
                            Log.d("DbFunctions","belongs_to_remote:"+belongs_to_remote);
                            String SI = cursorForRemoteDevicetags.getString(1);
                           // String weight = Double.toString(cursorForRemoteDevicetags.getDouble(2));
                            String weight="0.5";

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {

                                //Calculation for growth function
                                Log.d("DbFunctions","saveRemoteTSRs-- currentTimestamp,connectEstablisthment time is:"+currentTimeStamp+"--"+connectEstablishTime);

                                long diffTime = dateFormat.parse(currentTimeStamp).getTime() - dateFormat.parse(connectEstablishTime).getTime();
                                Log.d("DbFunctions","saveRemoteTSRs-- Tc -Tsv value is:"+diffTime);
                                double multi = Double.parseDouble(weight) * diffTime;
                                //This is to find out if SI belongs to SPu or not
                                Cursor cursorForLocalTSR = mydatabase.rawQuery("SELECT belongs_to from TSR_TBL where SI='" + SI + "'", null);
                                cursorForLocalTSR.moveToFirst();
                                String belongs_to_local = cursorForLocalTSR.getString(0);
                                if (belongs_to_local.equals("SELF") && belongs_to_remote.equals("SELF"))
                                    Psi = 1;
                                else if (belongs_to_local.equals("SELF") && !belongs_to_remote.equals("SELF"))
                                    Psi = 2;
                                else if (!belongs_to_local.equals("SELF") && belongs_to_remote.equals("SELF"))
                                    Psi = 3;
                                else if (!belongs_to_local.equals("SELF") && !belongs_to_remote.equals("SELF"))
                                    Psi = 4;

                                Log.d("DbFunctions","saveRemoteTSRs-- Psi value is:"+Psi);
                                ///Calculating delta
                                ///Calculating delta
                                Log.d("DbFunctions","saveRemoteTSRs-- value of weight before calculating delta is:"+Double.parseDouble(weight));
                                delta+= (Double.parseDouble(weight) * (diffTime)) / ((Psi)*(constant));
                                Log.d("DbFunctions","Delta for SI"+SI+"is:"+delta);
                                if (hm.containsKey(SI)) {
                                    double valueForSI = hm.get(SI);
                                    valueForSI += delta;
                                    hm.remove(SI);
                                    hm.put(SI, valueForSI);
                                } else
                                    hm.put(SI, delta);
                                //mydatabase.execSQL("UPDATE TSR_TBL set weight="+newWeight+" where SI='"+SI+"'");
                            } catch (Exception e) {
                                Log.d("", "Exception in parsing date");
                            }

                        }

                    }
                    Set<Map.Entry<String, Double>> set = hm.entrySet();

                    for (Map.Entry<String, Double> me : set) {

                        String SI = me.getKey();
                        double weight = 0.0;
                        double delta1 = me.getValue();
                        Log.d("DbFunctions","Debug 1");
                        Cursor cursorForTSRTBL = mydatabase.rawQuery("SELECT * FROM TSR_TBL where SI='" + SI + "'", null);
                        while (cursorForTSRTBL.moveToNext()) {
                            weight = Double.parseDouble(cursorForTSRTBL.getString(3));
                            Log.d("DBFunctions","Weight already inside TSR_TBL for SI"+SI+"is:"+weight);
                        }
                        if (weight + delta > 1) {
                            Log.d("DbFunctions","The value of weight and delta is"+weight+";;;"+delta);
                            weight = 1;
                        }
                        else weight = weight + delta;
                        Log.d("DbFunctions","New weight for SI "+SI+"is:"+weight);
                        mydatabase.execSQL("UPDATE TSR_TBL set weight=" + weight + " where SI='" + SI + "'");
                    }


                    ///Apply final step of minimum of 1 and w+delta

            }
        }
        catch(Exception e)
        {
            Log.d("DbFunctions","Exception occured:"+e);
        }

    }

    //This function is used to add a messsage to a database table
    public void insertIntoMSGTBL(SQLiteDatabase mydatabase,String imagePath,String latitude,String longitude,String timestamp,String tagsForCurrentImage,String fileName,String mime,String format,String localMacAddr,String localName,String UUID,long size, long quality,int priority)
    {
        String query="INSERT OR IGNORE INTO MESSAGE_TBL VALUES('"+imagePath+"',"+latitude+","+longitude+",'"+timestamp+"','"+tagsForCurrentImage+"','"+fileName+"','"+mime+"','"+format+"','"+localMacAddr+"','"+ localName+"','"+ UUID+"','NO','NO',"+size+","+quality+","+priority+")";
        try {
            mydatabase.execSQL(query);
        }
        catch(Exception e)
        {
            Log.d("DbFunctions","Exception inserting into MSG_TBL:"+e);
        }
    }

    //This function is used to send a message
    public int sendMessage(SQLiteDatabase mydatabase,String remoteMAC,String remoteName, ConnectedThreadWithRequestCodes ct,int codeForMessage)
    {
        Log.d("DbFunctions","Inside sendMessage");
        HashMap<String, Double> hm = new HashMap<String, Double>();
        Cursor cursorForRemoteTSR=mydatabase.rawQuery("SELECT * from TSR_REMOTE_TBL where deviceMacAddr='"+remoteMAC+"'",null);
        ArrayList<String> listOfImages=new ArrayList<>();
        int number=0;
        String preambleString;
        Cursor cursorForMatchingImage;
        ArrayList<String> listUUIDSentOrReceived = new ArrayList<String>();
        while(cursorForRemoteTSR.moveToNext()) {
            //Log.d("DbFunctions", "Inside sendMessage,TSRs are there for remote");
            //Log.d("DbFunctions", "TSR current is:" + cursorForRemoteTSR.getString(1));
            cursorForMatchingImage = mydatabase.rawQuery("SELECT * from MESSAGE_TBL  where tagsForCurrentImage like '%" + cursorForRemoteTSR.getString(1) + "%'", null);

            double localSum=0.0,remoteSum=0.0;

            while (cursorForMatchingImage.moveToNext()) {
                String tagsForCurrentImage=cursorForMatchingImage.getString(4);
                String tagsForCurrentImageArray[]=tagsForCurrentImage.split(",");
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

                Log.d("DbFunctions","localSum and remoteSum for the current message is:"+localSum+"--"+remoteSum);
                double differenceInSums=remoteSum-localSum;
                String UUID=cursorForMatchingImage.getString(10);
                /////come here

                hm.put(UUID,differenceInSums);
                Cursor ifSentOrReceived = mydatabase.rawQuery("SELECT * from SENT_IMAGE_LOG where UUID='" + cursorForMatchingImage.getString(10) + "' and (sentTo='" + remoteMAC + "' OR receivedFrom='" + remoteMAC + "')", null);
                if (ifSentOrReceived.moveToNext()) {
                    if (!listUUIDSentOrReceived.contains(cursorForMatchingImage.getString(10))) {
                        listUUIDSentOrReceived.add(cursorForMatchingImage.getString(10));
                        Log.d("DbFunctions", "Image already sent or received:" + ifSentOrReceived.getString(0) + "---" + ifSentOrReceived.getString(1) + "---" + ifSentOrReceived.getString(2));
                    }
                } else {
                    if(hm.get(UUID)>=0)
                    number++;
                }
            }

        }
        preambleString = "";
        if (codeForMessage == Constants.MESSAGE_IMAGE) {
            preambleString = "Preamble::MessageType:" + Constants.MESSAGE_IMAGES_TOTAL + "::Number:" + number + "::";

        } else if (codeForMessage == Constants.MESSAGE_IMAGE_BACK) {
            preambleString = "Preamble::MessageType:" + Constants.MESSAGE_IMAGES_BACK_TOTAL + "::Number:" + number + "::";
        }
        ct.writePreamble(preambleString);

        cursorForRemoteTSR=mydatabase.rawQuery("SELECT * from TSR_REMOTE_TBL where deviceMacAddr='"+remoteMAC+"'",null);
        while(cursorForRemoteTSR.moveToNext()) {
                    cursorForMatchingImage = mydatabase.rawQuery("SELECT * from MESSAGE_TBL  where tagsForCurrentImage like '%" + cursorForRemoteTSR.getString(1) + "%'", null);
            while(cursorForMatchingImage.moveToNext()) {
                 if (listOfImages.contains(cursorForMatchingImage.getString(0)))
                    listOfImages.add(cursorForMatchingImage.getString(0));
                else {

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
                         String UUID = cursorForMatchingImage.getString(10);

                     if(!listUUIDSentOrReceived.contains(UUID)) {
                         try {
                             if (hm.get(UUID) < 0) {
                                 Log.d("DbFunctions", "RemoteSum is less than local, so not sending message!");
                             }
                             else {
                                 java.io.RandomAccessFile raf = new java.io.RandomAccessFile(imagePath, "r");
                                 byte[] b = new byte[(int) raf.length()];

                                 raf.readFully(b);
                                 preambleString = "Preamble::MessageType:" + codeForMessage + "::MessageSize:" +
                                         b.length + "::Lengths:" + fileName + ":" +
                                         b.length + ":" + latitude + ":" + longitude + ":" +
                                         timestamp + "::" + tagsForCurrentImage + "::" +
                                         mime + "::" + format + "::" + localMacAddr + "::" + localName + "::" + UUID + "::";
                                 Log.d("CTwRC", "Sending image" + imagePath + "with tags" + tagsForCurrentImage + "to " + remoteName + "--haha  :");
                                 Log.d("DbFunctions", "Preamble string is:" + preambleString);
                                 ct.writePreamble(preambleString);
                                 Log.d("DbFunctions", "Preamble written");
                                 ct.write(b);
                                 Log.d("DbFunctions", "Message written");
                             }
                         } catch (Exception e) {
                             Log.d("DbFunctions", "Exception occured in accessing file:" + e);
                         }

                     }
                 }

            }
        }
        return number;

    }

    //This function is used to insert the record of a received message
    public void insertIntoMsgTblRemoteMsg(SQLiteDatabase mydatabase,String deviceName,String deviceMacAddr,String tags,String latitude, String longitude,String timestamp,String destDeviceName,String destDeviceAddr,String mime,String format,String fileName,String UUIDReceived,long size, long quality,long priority )
    {
        try {
            String fileNameProcessing[] = fileName.split("/");
            String fileNameFinal = fileNameProcessing[fileNameProcessing.length - 1];
            String sqlStatement = "";
            sqlStatement = "INSERT INTO DEVICE_IMAGE_RECEIPT values('" + deviceName + "','" + deviceMacAddr + "','"
                    + tags + "','" + latitude + "','" + longitude + "','" + timestamp +
                    "','" +destDeviceName+"','"+destDeviceAddr+"','"+mime+"','"+format+  "','"+fileName +"','"+UUIDReceived+"')";
            sqlStatement = "INSERT OR IGNORE INTO MESSAGE_TBL values('" + fileName + "','" + latitude + "','"
                    + longitude + "','" + timestamp + "','" + tags + "','" + fileNameFinal +
                    "','" +mime+"','"+format+"','"+deviceMacAddr+"','"+deviceName+  "','"+UUIDReceived +"','"+destDeviceAddr+"','"+destDeviceName+"',"+size+","+quality+","+priority+")";

            mydatabase.execSQL(sqlStatement);
        }
        catch(Exception e)
        {
            Log.d("dbFunctions","Exception occured in inserting into DEVICE_IMAGE_RECEIPT"+e);
        }
    }
    //This function is used to record that a device with a MAC address is connected-- if value 0, means connected
    public void setConnectedLogTBL(SQLiteDatabase mydatabase,String MacAddr)
    {
        mydatabase.execSQL("INSERT OR IGNORE INTO CONNECTED_LOG_TBL values('"+MacAddr+"',0)");
    }
    //This function is used to record that a device with a MAC address is disconnected-- if value 1, means disconnected
    public void unsetConnectedLogTBL(SQLiteDatabase mydatabase,String MacAddr)
    {
        mydatabase.execSQL("UPDATE CONNECTED_LOG_TBL set doneOrNot=1 where MacAddr='"+MacAddr+"'");
        Log.d("DbFunctions","Done transaction with device-"+MacAddr);
    }

    //This function is used to insert into a log table that the message is either sent from this device to another device,
    //or a message is received from another device, or a message is
    public void insertIntoSentLogTBL(SQLiteDatabase mydatabase, String UUID,String macAddr,int sentOrReceived)
    {
        if(sentOrReceived==0) {
            mydatabase.execSQL("INSERT OR IGNORE INTO SENT_IMAGE_LOG values('" + UUID + "','"+macAddr+"','NO')");
        }
        else
        {
            mydatabase.execSQL("INSERT OR IGNORE INTO SENT_IMAGE_LOG values('" + UUID + "','NO','"+macAddr+"')");
        }
    }

    //This is to execute the decay function of ChitChat
    public String calculateTSRsPre(SQLiteDatabase mydatabase)
    {
        int Beta=2;
        Log.d("DbFunctions","Inside calculateTSRsPre");
        String currentTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String finalMapSItoTSR=new String();
        ArrayList<String> connectedDevices=new ArrayList<String>();
        Cursor cursorForConnectedDevices = mydatabase.rawQuery("SELECT BTDeviceMacAddr from DEVICES_CURRENTLY_CONNECTED",null);

        while (cursorForConnectedDevices.moveToNext()) {
            connectedDevices.add(cursorForConnectedDevices.getString(0));

        }
        Cursor cursorForTSR = mydatabase.rawQuery("SELECT * from TSR_TBL", null);


            /// For each individual SIDs
            while(cursorForTSR.moveToNext()) {

                String belongs_to=cursorForTSR.getString(0);
                String SI=cursorForTSR.getString(1);
                String time=cursorForTSR.getString(2);
                String weight=cursorForTSR.getString(3);
                String updated_by=cursorForTSR.getString(4);
                Log.d("DbFunctions","calculateTSRsPre-- SI in picture:"+SI);
                //Log.d("DbFunctions","TSR is:"+SI);

                Cursor cursorForRemoteTSRs=mydatabase.rawQuery("SELECT * from TSR_REMOTE_TBL where SI='"+SI+"' and weight>0",null);
                int flagForPresent=0;
                while(cursorForRemoteTSRs.moveToNext())
                {
                    String macAddr=cursorForRemoteTSRs.getString(0);
                    ///Check if any device with weight>0 for this SI exists.. If it exists, that means weight will remain unchanged
                    if(connectedDevices.contains(macAddr))
                    {
                        ///This means that there is some device with positive TSR exists which is still connected

                        weight=weight;
                        flagForPresent=1;
                        finalMapSItoTSR+=SI+"[["+weight+"[["+belongs_to+",";
                        Log.d("DbFunctions","calculateTSRsPre-- Device "+macAddr+" has the weight"+weight+" for SI "+SI);
                        break;
                    }
                }

                if(flagForPresent==0)
                {
                    ///flag for if a device with SI w>0 currently connected or not

                    ArrayList<String> timeStamps=new ArrayList<String>();


                    Cursor cursorForQuery=mydatabase.rawQuery("SELECT * from TSR_REMOTE_TBL where SI='"+SI+"' and weight>0",null);

                            ///No device with w for SI>0 currenly connected.. So find out the disconnected devices with this SI and its weight>0 and its latest timestamp
                                Log.d("DbFunctions","calculateTSRsPre-- No other device connected at this point has a SI "+SI);
                    //SELECT strftime('%s','now') - strftime('%s','2004-01-01 02:34:56')
                                cursorForQuery=mydatabase.rawQuery("SELECT a.* from DEVICE_DISCONNECTED_TIME a JOIN TSR_REMOTE_TBL b ON a.deviceMacAddr=b.deviceMacAddr and b.SI='"+SI+"' and (strftime('%s','now')-strftime('%s',a.currentTime)>=5)",null);
                                //Add all the timestamps
                                ///////////Current point
                                while(cursorForQuery.moveToNext())
                                {
                                    timeStamps.add(cursorForQuery.getString(3));
                                }

                            //Found out the latest time of disconnection
                            String timeLatestDisconnected="";
                            if(timeStamps.size()!=0)
                                timeLatestDisconnected=latestTimeStamp(timeStamps);
                            ///Applying the formula now
                            Log.d("DbFunctions","calculateTSRsPre--latest disconnection time:"+timeLatestDisconnected);
                            //calculate w-0.5
                            double wLessPoint5=Double.parseDouble(weight)-0.5;
                            //Calculating difference between timestamps
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            long differenceInTime=0;
                            try {
                                Date Tc=dateFormat.parse(currentTimeStamp);
                                Log.d("DbFunctions","Tc is:"+Tc.toString());
                                if(timeLatestDisconnected==null)
                                    differenceInTime=0;
                                Date Tdi=dateFormat.parse(timeLatestDisconnected);
                                Log.d("DbFunctions","Tc and Tdi are:"+Tc.toString()+"--"+Tdi.toString());
                                differenceInTime=Tc.getTime()-Tdi.getTime();
                                Log.d("DbFunctions","difference in time, tc-tdi is:"+differenceInTime);
                            }catch(Exception e)
                            {
                                Log.d("DbFunctions","Exception occured:"+e);
                            }
                            ////tc==tdi condition
                            if(differenceInTime==0)
                            {
                                finalMapSItoTSR += SI + "[[" + weight + "[[" + belongs_to + ",";
                            }
                            else {
                                double mulByBeta = Beta * differenceInTime;
                                double newWeight = 0.0;
                                ///SI belongs to SPu condition
                                if (belongs_to.equals("SELF")) {
                                    newWeight = wLessPoint5 / (mulByBeta) + 0.5;

                                } else {
                                    ///SI does not belong to SPu condition
                                    newWeight = Double.parseDouble(weight) / (mulByBeta);
                                }
                                mydatabase.execSQL("UPDATE TSR_TBL set weight="+newWeight+" WHERE SI='"+SI+"'");
                                finalMapSItoTSR += SI + "[[" + newWeight + "[[" + belongs_to + ",";
                            }




                }
                else
                {
                    finalMapSItoTSR+=SI+"[["+weight+"[["+belongs_to+",";
                }
                //Log.d("DbFuncs","Connected device is:"+cursorForTSR.getString(0));
            }

        if(!finalMapSItoTSR.equals(""))
            finalMapSItoTSR=finalMapSItoTSR.substring(0,finalMapSItoTSR.length()-1);

        Log.d("DbFunctions","finalMapSItoTSR in function calculateTSRSpre is:"+finalMapSItoTSR);
        return finalMapSItoTSR;
    }

    ///This is to increment the incentive value by the parameter "value"
    public void insertInc(SQLiteDatabase mydatabase,double value)
    {
        Log.d("DbFunctions","InsertInc called with value of value:"+value);
        mydatabase.execSQL("UPDATE INCENTIVES_TBL set incentive=incentive+"+value);
    }

    ///This function is to give a list of image titles of the messages created by myself
    public  String[] getOwnImages(SQLiteDatabase mydatabase) {
        String[] someArray=null;
        Cursor cursorForImages=null;
        List<String> imagepaths = new ArrayList<String>();
        int numOfImages = 0;
        //////
        try {
            cursorForImages = mydatabase.rawQuery("SELECT imagePath from MESSAGE_TBL where destMacAddr='NO' and destName='NO' ", null);
            if (cursorForImages == null)
                Log.d("DbFuncGetTags", "cursorForImages isnull");
        } catch (Exception e) {
            Log.d("DbFUncs", "Exception occurred, hahahaha!::" + e);
        }
        try {
            String image;

            try {

                if (cursorForImages != null)
                    while (cursorForImages.moveToNext()) {
                        image = cursorForImages.getString(0);
                        imagepaths.add(image);
                        Log.d("Dbfuncs","An image is:"+image);
                        numOfImages++;
                    }
                if (numOfImages != 0) {
                    Log.d("DbFuncs","Number of images are"+numOfImages);
                    someArray=imagepaths.toArray(new String[0]);
                    Log.d("DbFuncs","Arraylist to array conversiton done");
                }


            } catch (Exception e) {
                Log.d("DbFunctions", "Exception occured in sqlite query!!! ::" + e);
            }

            Log.d("DbFuncs","Value of someArray is:"+someArray);
            //////
            return someArray;
        }
        catch(Exception e)
        {
            Log.d("Dbfuncs","Exception in getrece:"+e);
        }
        return someArray;
    }

    //This function is used to calculate the distance between two points which are represented by latitude and longitude parameters
    public static double calculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    //This function is used to delete the transactions that are older than the five latest transactions
    public void deleteLeastRecent(SQLiteDatabase mydatabase)
    {
        Cursor noRows=mydatabase.rawQuery("SELECT * from LAST_FIVE_TRANS",null);
        int noRowsCount=noRows.getCount();
        Cursor minTime=mydatabase.rawQuery("SELECT min(time) from LAST_FIVE_TRANS",null);
        Log.d("DbFunctions","Number of rows in LAST_FIVE_TRANS are:"+minTime.getCount());
        if(minTime!=null && noRowsCount>5) {
            Log.d("DbFunctions","Record Need to be deleted");
            minTime.moveToFirst();
            String minTimeS = minTime.getString(0);
            mydatabase.execSQL("DELETE from LAST_FIVE_TRANS where time='"+minTimeS+"'");
        }
    }
}


