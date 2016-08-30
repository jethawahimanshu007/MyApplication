package com.example.himanshu.myapplication;

import android.bluetooth.BluetoothSocket;
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

/**
 * Created by Himanshu on 8/3/2016.
 */
class ConnectedThreadWithRequestCodes extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
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
    public void run() {
        Log.d("CTwReqCodes","ConnectedThreadWithRequestCodes is running,mmSocket is:"+mmSocket);
        int flag = 0;
        int flagForPreamble=0;
        byte[] buffer=new byte[800];  // buffer store for the stream
        byte[] preamble=new byte[Constants.PREAMBLE_SIZE];
        byte[] bufferData=null;
        byte[] bufferDataFull={};
        int bytes; // bytes returned from read()
        int current=0;
        int bufferSize=0;
        FileOutputStream fos=null;


        // Keep listening to the InputStream until an exception occurs
        int MessageType;
        int MessageSize=0; String MessageFileName="";
        int sleepFlag=0;
        int i=0;
        if(mmInStream!=null)
        {
            Log.d("CTwReqCode","InStream is not null, still no data read!!");
        }
        while (true) {


            try {

                if(mmInStream.available()>0) {

                    sleepFlag=0;
                    //FlagForPreamble to check if preamble has been received or not

                    if(flagForPreamble==0) {

                        //////////CULPRIT part
                        bytes = mmInStream.read(preamble, 0, preamble.length);
                        Log.d("CTwReqCodes","Checking if read is done or not");
                        Log.d("ConnectedThread","Size of preamble received:"+bytes);
                        if (new String(preamble).split("::")[0].equals("Preamble")) {
                            flagForPreamble = 1;
                            MessageType = Integer.parseInt(new String(preamble).split("::")[1].split(":")[1]);
                            MessageSize = Integer.parseInt(new String(preamble).split("::")[2].split(":")[1]);
                            switch(MessageType)
                            {
                                case Constants.MESSAGE_TAGS:
                                    byte[] bufferForTags=new byte[MessageSize];int bytesReadForTags=0; int currentForTags=0;
                                    if(MessageSize!=0)
                                    {

                                        while(current!=MessageSize)
                                        {
                                            bytesReadForTags=mmInStream.read(bufferForTags, 0, MessageSize-currentForTags);
                                            currentForTags+=bytesReadForTags;
                                        }
                                        Log.d("CTwRequestCodes","The tags got from the device are :"+new String(bufferForTags));
                                    }
                                    break;
                            }
                            continue;
                        }

                    }
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


                }
                else
                {
                    i++;
                    if(i==10)
                    {
                        Log.d("CTwReqCode","Checked 10 times, no data available");
                        break;
                    }
                   // Log.d("CTwReqCode","Not available to read");
                    SystemClock.sleep(100);
                        /*if(sleepFlag==10)
                        {
                            if(bufferDataFull==null)
                            {
                                Log.d("ConnectedThread","bufferDataFull is null");
                                break;
                            }
                            else {
                                fos.write(bufferDataFull);
                                bufferData = null;
                                bufferDataFull = null;
                                break;
                            }
                        }
                        sleepFlag+=1;
                        Log.d("ConnectedThread","Not available, sleeping for 100 ms");
                        SystemClock.sleep(100);
                        */
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
            Log.d("ConnectedThread","Going to write data  of "+bytes.length+" bytes inside ConnectedThread");
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
