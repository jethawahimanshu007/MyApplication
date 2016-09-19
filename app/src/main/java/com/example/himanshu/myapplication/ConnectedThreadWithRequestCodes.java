package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import afrl.phoenix.core.ClientAddress;
import afrl.phoenix.core.MetadataConstants;
import afrl.phoenix.information.BasicInformation;
import afrl.phoenix.information.InformationPacket;
import afrl.phoenix.information.messages.BinaryMessage;
import afrl.phoenix.information.messages.ByteArrayMessage;
import afrl.phoenix.information.messages.Message;
import afrl.phoenix.information.messages.StringMessage;

/**
 * Created by Himanshu on 8/3/2016.
 */
class ConnectedThreadWithRequestCodes extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    SQLiteDatabase mydatabase;


    Context context;
   /* private final Handler mHandler;

    public ConnectedThread(BluetoothSocket socket,Handler mHandlerInput) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        mHandler=mHandlerInput;
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }
*/

    public ConnectedThreadWithRequestCodes(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.d("CTwReqCode","Exception is thrown:"+e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public ConnectedThreadWithRequestCodes(BluetoothSocket socket,Context context) {
        InformationPacket<BinaryMessage> typedBinaryMessageInformationPacket = new InformationPacket<BinaryMessage>(
                new ClientAddress("HTTP", "http://123.45.60.89:8080"),
                new BasicInformation.Builder<BinaryMessage>("BYTE-BLATHER", MetadataConstants.Mime.OCTET_STREAM, MetadataConstants.Format.BIN,
                        new ByteArrayMessage(new byte[] { 0x06, 0x1A, 0x33, 0x2F })).build());
        InformationPacket<Message> genericTextMessageInformationPacketWithAttributes = new InformationPacket<Message>(
                new ClientAddress("TCP", "tcp://123.45.60.89:53999"),
                new BasicInformation.Builder<Message>("NONSENSE", MetadataConstants.Mime.TEXT, MetadataConstants.Format.TXT,
                        new StringMessage("The quick brown fox jumped over the lazy dog.", StandardCharsets.US_ASCII))
                        .addAttribute("Noisy", true).addAttribute("Latitude", 0.0).addAttribute("Longitude", 0.0)
                        .addAttribute("AUTHOR", "N/A").build());
        byte[] bytes=typedBinaryMessageInformationPacket.getBytes();

        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.context=context;
        if(context==null)
        {
            Log.d("CTwRC","context is null");
        }
        mydatabase = context.openOrCreateDatabase(Constants.DATABASE_NAME, context.MODE_PRIVATE, null);

        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.d("CTwReqCode","Exception is thrown:"+e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }
    public void run() {
        Log.d("CTwReqCodes","ConnectedThreadWithRequestCodes is running,mmSocket is:"+mmSocket);
        int flag = 0;
        int flagForPreamble=0;
        byte[] buffer=new byte[800];  // buffer store for the stream
        byte[] preamble=new byte[Constants.PREAMBLE_SIZE];
        byte[] bufferData=null;
        byte[] bufferDataFull={};
        int bytes=0; // bytes returned from read()
        int current=0;
        int bufferSize=0;
        FileOutputStream fos=null;
        int lengthFileName=0,lengthFile=0;


        // Keep listening to the InputStream until an exception occurs
        int MessageType;
        int MessageSize=0; String MessageFileName="";
        int sleepFlag=0;
        int i=0;

        while (true) {

            try {

                if(mmInStream.available()>0) {


                        int countForPreamble=0;
                        while(countForPreamble!=Constants.PREAMBLE_SIZE) {
                            bytes = mmInStream.read(preamble, 0, preamble.length - countForPreamble);
                            countForPreamble+=bytes;
                        }
                        Log.d("CTwReqCodes","Checking if read is done or not");
                        Log.d("ConnectedThread","Size of preamble received:"+bytes);
                        Log.d("CTwReqCodes","Preamble received is:"+new String(preamble));
                        if (new String(preamble).split("::")[0].equals("Preamble")) {

                            MessageType = Integer.parseInt(new String(preamble).split("::")[1].split(":")[1]);
                            MessageSize = Integer.parseInt(new String(preamble).split("::")[2].split(":")[1]);
                            if(new String(preamble).contains("Lengths")) {
                                lengthFileName = Integer.parseInt(new String(preamble).split("::")[3].split(":")[1]);
                                lengthFile =Integer.parseInt(new String(preamble).split("::")[3].split(":")[2]);
                            }
                            Log.d("CTwReqCode","MessageType and MessageSize received are:"+MessageType+"  and "+MessageSize);
                            switch(MessageType)
                            {
                                case Constants.MESSAGE_TAGS:
                                    byte[] bufferForTags=new byte[MessageSize];int bytesReadForTags=0; int currentForTags=0;
                                    if(MessageSize!=0)
                                    {

                                        Log.d("CTwReq","waiting to read tags from remote device!!");
                                        while(currentForTags!=MessageSize)
                                        {
                                            bytesReadForTags=mmInStream.read(bufferForTags, 0, MessageSize-currentForTags);
                                            currentForTags+=bytesReadForTags;
                                        }
                                        Log.d("CTwRequestCodes","The tags got from the device are :"+new String(bufferForTags));

                                        String remoteTags=new String();
                                        remoteTags=new String(bufferForTags);
                                        Log.d("CTwReq","remoteTags received::"+remoteTags);
                                        Log.d("CTwRC","Going to call getTags function");
                                        String localTags="";
                                        DbFunctions dbFunctions=new DbFunctions();
                                        localTags=dbFunctions.getTags(mydatabase);
                                        Log.d("CTwRC","After call to getTags function,value of localTags:"+localTags);
                                        String temp=new String();
                                        if(localTags.length()!=0)
                                        {
                                            Log.d("CTwRC","Went into localTags.length()!=0 if condition");
                                            String localTagsArray[]=localTags.split(",");
                                            String remoteTagsArray[]=remoteTags.split(",");
                                            Log.d("CTwRC","localTags Array and remote Tags array:"+localTagsArray+"  and  "+remoteTagsArray);
                                            for(int ltac=0 ;ltac<localTagsArray.length&& localTagsArray.length!=0 && remoteTagsArray.length!=0;ltac++)
                                            {
                                                for(int rtac=0;rtac<remoteTagsArray.length;rtac++)
                                                {
                                                    if(localTagsArray[ltac].equals(remoteTagsArray[rtac]))
                                                    {
                                                        temp+=localTagsArray[ltac]+",";
                                                    }
                                                }
                                            }
                                        }
                                        String matchingTags=new String();
                                        Log.d("CTwReqCode","temp string is:"+temp);
                                        matchingTags=temp.substring(0,temp.length()-1);
                                        Log.d("CTwRC","The matching tags are:"+matchingTags);
                                        if(matchingTags.length()!=0)
                                        {
                                            dbFunctions.insertIntoDeviceTagTbl(mydatabase,mmSocket.getRemoteDevice().getName(),mmSocket.getRemoteDevice().getAddress(),matchingTags);
                                        }
                                        String preambleString;

                                        preambleString="Preamble::MessageType:" + Constants.MESSAGE_TAGS_MATCHING + "::MessageSize:" + matchingTags.length() + "::";
                                        byte[] premable=new byte[Constants.PREAMBLE_SIZE];
                                        byte preambleSomething[]=preambleString.getBytes();
                                       Arrays.fill(preamble,(byte)0);
                                        System.arraycopy(preambleSomething,0,preamble,0,preambleSomething.length);
                                        write(preamble);
                                        byte[] matchingTagsBytes=matchingTags.getBytes();
                                        write(matchingTagsBytes);
                                    }
                                    else
                                    {
                                        Log.d("CTwRequestCodes","The device has no tags!!");
                                    }
                                    break;

                                case Constants.MESSAGE_TAGS_MATCHING:
                                    Log.d("CTwRC","Entered case MESSAGE_TAGS_MATCHING of switch");
                                    currentForTags=0;bufferForTags=new byte[MessageSize];
                                    bytesReadForTags=0;
                                    if(MessageSize!=0) {

                                        Log.d("CTwReq", "waiting to read tags from remote device!!");
                                        while (currentForTags != MessageSize) {
                                            bytesReadForTags = mmInStream.read(bufferForTags, 0, MessageSize - currentForTags);
                                            currentForTags += bytesReadForTags;
                                        }
                                        String matchingTags=new String(bufferForTags);
                                        Log.d("CTwRC","List of matching tags received from "+mmSocket.getRemoteDevice().getName()+" are "+matchingTags);
                                        DbFunctions dbFunctions=new DbFunctions();
                                        dbFunctions.insertIntoDeviceTagTbl(mydatabase,mmSocket.getRemoteDevice().getName(),mmSocket.getRemoteDevice().getAddress(),matchingTags);
                                        String tagsForDevice=dbFunctions.getTagsForDevice(mydatabase,mmSocket.getRemoteDevice().getAddress());
                                        Log.d("CTwRC","tagsForDevice "+mmSocket.getRemoteDevice().getName()+"are:"+tagsForDevice);
                                        String imagePaths=dbFunctions.getImagePaths(mydatabase,tagsForDevice);
                                        Log.d("CTwRC","Paths for image are:"+imagePaths);
                                        if(!imagePaths.equals(""))
                                        {
                                            String imagePathsArray[]=imagePaths.split(",");
                                            MessageFormat mf[]=new MessageFormat[imagePathsArray.length];
                                            for(int counter=0;counter<mf.length;counter++) {
                                                mf[counter]=new MessageFormat();
                                                java.io.RandomAccessFile raf = new java.io.RandomAccessFile(imagePathsArray[counter], "r");
                                                byte[] b = new byte[(int) raf.length()];
                                                Log.d("MainActivity", "Length of byte array from image is:" + b.length);
                                                raf.readFully(b);
                                                mf[counter].Message=b;
                                                String fileNameProcessing[]=imagePathsArray[counter].split("/");
                                                String fileName=fileNameProcessing[fileNameProcessing.length-1];
                                                byte fileNameBytes[]=fileName.getBytes();
                                                //mf[counter].fileName=fileName.getBytes();
                                                String BTAddr=BluetoothAdapter.getDefaultAdapter().getAddress();
                                                int messageLength=fileNameBytes.length+b.length;
                                                byte[] messageBytes=new byte[messageLength];
                                                System.arraycopy(fileNameBytes,0,messageBytes,0,fileNameBytes.length);
                                                System.arraycopy(b,0,messageBytes,fileNameBytes.length,b.length);
                                                String preambleString=new String();
                                                preambleString="Preamble::MessageType:" + Constants.MESSAGE_IMAGE + "::MessageSize:" + fileNameBytes.length + "::Lengths:"+fileName.length()+":"+b.length+"::";
                                                preambleString="Preamble::MessageType:" + Constants.MESSAGE_IMAGE + "::MessageSize:" + messageBytes.length + "::Lengths:"+fileName.length()+":"+b.length+"::";
                                                byte[] premable=new byte[Constants.PREAMBLE_SIZE];
                                                byte preambleSomething[]=preambleString.getBytes();
                                                Arrays.fill(preamble,(byte)0);
                                                System.arraycopy(preambleSomething,0,preamble,0,preambleSomething.length);
                                                Log.d("CTwRC","fileName to be sent is:"+fileName);
                                                write(preamble);
                                                write(messageBytes);
                                            }


                                        }

                                    }

                                    break;
                                case Constants.MESSAGE_IMAGE:
                                    try {
                                        Log.d("CTwRC", "Entered case MESSAGE_IMAGE of switch");
                                        currentForTags = 0;
                                        byte bufferForMessage[] = new byte[MessageSize];
                                        bytesReadForTags = 0;
                                        if (MessageSize != 0) {

                                            Log.d("CTwReq", "waiting to read tags from remote device!!");
                                            while (currentForTags != MessageSize) {
                                                bytesReadForTags = mmInStream.read(bufferForMessage, 0, MessageSize - currentForTags);
                                                currentForTags += bytesReadForTags;
                                            }
                                            byte[] fileNameBytes=new byte[lengthFileName];
                                            System.arraycopy(bufferForMessage,0,fileNameBytes,0,lengthFileName);
                                            byte[] fileBytes=new byte[lengthFile];
                                            System.arraycopy(bufferForMessage,lengthFileName,fileBytes,0,lengthFile-1);
                                            Log.d("CTwRC","Received image from device "+mmSocket.getRemoteDevice().getName());
                                            Log.d("CTwRC","The message file name is:"+new String(fileNameBytes));

                                        }


                                    }
                                    catch(Exception e)
                                    {
                                        Log.d("CTwRC","Exception occured in case MESSAGE_IMAGE in switch:"+e);
                                    }


                            break;
                            }

                            continue;
                        }

/*
                    if(current!=MessageSize) {
                        bytes = mmInStream.read(buffer, 0, bufferSize);
                        current += bytes;
                        bufferSize = bufferSize - bytes;
                        Log.d("ConnectedThread", "Bytes read:" + bytes);
                        Log.d("ConnectedThread","Total bytes read:"+current);
                        if(current==MessageSize)
                        {
                            Log.d("ConnectedThread","Data received fully");
                            fos.write(buffer);
                            fos.flush();
                            fos.close();
                            flagForPreamble=0;
                        }

                    }

*/
                }
                else
                {
                    i++;
                    /*if(i==10)
                    {
                        Log.d("CTwReqCode","Checked 10 times, no data available");
                        break;
                    }*/
                   // Log.d("CTwReqCode","Not available to read");
                    SystemClock.sleep(100);

                }


            } catch (Exception e) {
                Log.d("ConnectedThread", "Exception occured in reading:" + e);
                break;
            }
            finally{
                try {
                    fos.close();
                }
                catch(Exception e)
                {

                }
            }
        }

    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            Log.d("ConnectedThread","Going to write data  of "+bytes.length+" bytes inside ConnectedThread to device "+mmSocket.getRemoteDevice().getName());
            mmOutStream.write(bytes);
            //   mmOutStream.flush();
            Log.d("ConnectedThread","Hopefully data is written!");
        } catch (IOException e) {
            Log.d("ConnectedThread","Exception occured in writing:"+e);
        }
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}
