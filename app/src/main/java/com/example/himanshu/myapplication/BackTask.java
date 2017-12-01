package com.example.himanshu.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import org.apache.commons.net.util.SubnetUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Himanshu on 7/29/2017.
 */

//This file handles integration--- i.e. transfer file from DTNPhotoshare to ROGER
    //The discovery part is commented out because it is no longer required!
public class BackTask extends AsyncTask<ContextHandler,Void,Void> {

    String TAG="BackTask";
    String NOMAC="00:00:00:00:00:00";
    int TIMEOUT_SCAN = 3600; // seconds
    int TIMEOUT_SHUTDOWN = 10; // seconds
    long ip=0;
    long start = 0;
    long end = 0;
    int pt_move = 2;
    long size=end-start+1;
    ExecutorService mPool;
    ExecutorService multiConnectPool;
    private WifiInfo info;
    public int speed = 0;
    public String ssid = null;
    public String bssid = null;
    public String carrier = null;
    public String macAddress = NOMAC;
    public String netmaskIp = "0.0.0.0";
    public String gatewayIp = "0.0.0.0";
    Handler handler;


    protected Void doInBackground(ContextHandler... urls) {


        try {
            ConstantsClass.IpAddresses = new ArrayList<String>();
            getWifiInfo(urls[0].context);
            Context context = urls[0].context;
            //ConstantsClass.mydatabaseLatest = context.openOrCreateDatabase(Constants.DATABASE_NAME, context.MODE_PRIVATE, null);
            handler = urls[0].handler;
            //executeCode();
            ConstantsClass.mydatabaseLatest.execSQL("CREATE TABLE IF NOT EXISTS ROGER_IP_ADD(IpAdd VARCHAR, PRIMARY KEY(IpAdd))");
            Cursor cursorForROgerIP = ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from ROGER_IP_ADD", null);
            String IpAddress = new String();
            while (cursorForROgerIP.moveToNext()) {
                IpAddress = cursorForROgerIP.getString(0);
            }

            Log.d("BackTask", "Total addresses to try connection for:" + ConstantsClass.IpAddresses.size());
            multiConnectPool = Executors.newFixedThreadPool(10);
            Log.d("BackTask", "Trying to connect to Ip address:" + IpAddress);
            multiConnectPool.execute(new ConnectRunnable(IpAddress));

        }
        catch(Exception e)
        {
            Log.d("BackTask"," Exception occcured in BackTask:"+e);
        }
     return null;
    }

    private void writeMessages(SQLiteDatabase mydatabaseLatest,Socket sock) {
        Cursor allMessages = ConstantsClass.mydatabaseLatest.rawQuery("SELECT imagePath,fileName,size,UUID from MESSAGE_TBL", null);
        InputStream is= new InputStream() {
            @Override
            public int read() throws IOException {
                return 0;
            }
        };
        try {
            is = sock.getInputStream();
        }
        catch(Exception e)
        {
            Log.d("BackTask","Exception getting inputStream");
        }
        //Send total number of messages
        int totalNumberOfMessages = allMessages.getCount();
        byte totalNoByteArray[] = ByteBuffer.allocate(4).putInt(totalNumberOfMessages).array();
        Log.d("BackTask", "Sending totalNoOfMessages:" + totalNumberOfMessages);
        writeByteArray(totalNoByteArray,sock);

        ///Write preamble size, preamble and message
        while (allMessages.moveToNext()) {
            //Write preamble size and preamble first

            String filePath = allMessages.getString(0);
            String fileName = allMessages.getString(1);
            String UUID=allMessages.getString(3);
            File myFile = new File(filePath);
            byte[] mybytearray = new byte[(int) myFile.length()];
            int size = mybytearray.length;
            Preamble preamble = new Preamble(size, fileName,UUID.replace(":","+"));
            try {
                byte preambleBytes[] = Serializer.serialize(preamble);

                //Write size of preamble byte array
                byte preambleBytesSize[] = ByteBuffer.allocate(4).putInt(preambleBytes.length).array();
                writeByteArray(preambleBytesSize,sock);

                //Write preamble
                writeByteArray(preambleBytes,sock);

                //Read flag for message send-- send if 1-- This is to avoid duplication--
                // if a laptop does not have this message, it sends 1
                byte[] flagBytes=new byte[4];
                readByteArray(is,flagBytes);
                ByteBuffer flagByteBuffer=ByteBuffer.wrap(flagBytes);
                int flagForSending=flagByteBuffer.getInt();

                //Send only if flag received==1
                if(flagForSending==1) {
                    //Write message
                    FileInputStream fis = new FileInputStream(myFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    bis.read(mybytearray, 0, mybytearray.length);
                    writeByteArray(mybytearray, sock);

                    //Get metadata byte array
                    String metadata = messageMetadata(UUID, ConstantsClass.mydatabaseLatest);
                    byte[] metadataBytes = metadata.getBytes();
                    byte metadataBytesSize[] = ByteBuffer.allocate(4).putInt(metadataBytes.length).array();

                    //Write metadata size
                    writeByteArray(metadataBytesSize, sock);

                    //Write actual metadata
                    writeByteArray(metadataBytes, sock);
                }
            } catch (Exception e) {
                Log.d("BackTask", "Exception occured");
            }
        }
    }
    public static void writeByteArray(byte[] bytes,Socket sock)
    {
        try {
            Log.d("BackTask","Writing number of bytes:"+bytes.length);
            OutputStream os = sock.getOutputStream();
            System.out.println("Sending...");
            os.write(bytes, 0, bytes.length);
            os.flush();
            Log.d("BackTask","Sent");
        }
        catch(Exception e)
        {
            Log.d("BackTask","Exception:"+e);
        }
    }

    public static void readByteArray(InputStream is, byte byteArray[])
    {
        int bytesRead;
        int current = 0;

        try{
            bytesRead = is.read(byteArray,0,byteArray.length);
            current= bytesRead;


            while(current!=byteArray.length) {
                bytesRead =is.read(byteArray, current, (byteArray.length-current));
                if(bytesRead >= 0) current += bytesRead;
            }

        }
        catch(Exception e)
        {
            System.out.println("Exception:"+e);
        }
        // System.out.println("Done reading "+byteArray.length+" bytes for the image" );
    }

    public void executeCode()
    {
        int THREADS = 10;
        mPool= Executors.newFixedThreadPool(THREADS);
        Log.v(TAG, "start=" + getIpFromLongUnsigned(start) + " (" + start
                + "), end=" + getIpFromLongUnsigned(end) + " (" + end
                + "), length=" + size);

        if (ip <= end && ip >= start) {
            Log.i(TAG, "Back and forth scanning");
            // gateway
            launch(start);

            // hosts
            long pt_backward = ip;
            long pt_forward = ip + 1;
            long size_hosts = size - 1;

            for (int i = 0; i < size_hosts; i++) {
                // Set pointer if of limits
                if (pt_backward <= start) {
                    pt_move = 2;
                } else if (pt_forward > end) {
                    pt_move = 1;
                }
                // Move back and forth
                if (pt_move == 1) {
                    launch(pt_backward);
                    pt_backward--;
                    pt_move = 2;
                } else if (pt_move == 2) {
                    launch(pt_forward);
                    pt_forward++;
                    pt_move = 1;
                }
            }

        }
        else {
            Log.i(TAG, "Sequential scanning");
            for (long i = start; i <= end; i++) {
                launch(i);
            }
        }



        mPool.shutdown();
        try {
            if(mPool.awaitTermination(TIMEOUT_SCAN,TimeUnit.SECONDS)) {
                Log.d("BackTask","Discovery done!!");
                final Message msg = new Message();
                final Bundle b = new Bundle();
                b.putCharSequence("message", "Discovery done!");
                msg.setData(b);
                handler.sendMessage(msg);
            }
            if(!mPool.awaitTermination(TIMEOUT_SCAN, TimeUnit.SECONDS)){
                mPool.shutdownNow();
                Log.e(TAG, "Shutting down pool");
                if(!mPool.awaitTermination(TIMEOUT_SHUTDOWN, TimeUnit.SECONDS)){
                    Log.e(TAG, "Pool did not terminate");
                }
            }
        } catch (InterruptedException e){
            Log.e(TAG, e.getMessage());
            mPool.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
        }

    }
    public static String getIpFromLongUnsigned(long ip_long) {
        String ip = "";
        for (int k = 3; k > -1; k--) {
            ip = ip +
                    ((ip_long >> k * 8) & 0xFF) + ".";
        }
        return ip.substring(0, ip.length() - 1);
    }

    class CheckRunnable implements Runnable {
        private String addr;


        CheckRunnable(String addr) {
            this.addr = addr;
        }
        public void run() {
            try{


                InetAddress h = InetAddress.getByName(addr);
                String hardwareAddress=new String();
                // Arp Check #1
                hardwareAddress = getHardwareAddress(addr);
                if(!NOMAC.equals(hardwareAddress)) {
                    //Log.d(TAG, "found using arp #1 " + addr);
                    ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO WIFI_NODES_TBL VALUES('"+addr+"')");
                    if(!ConstantsClass.IpAddresses.contains(addr))
                    ConstantsClass.IpAddresses.add(addr);
                    return;
                }

                // Native InetAddress check
                if (h.isReachable(200)) {
                    //Log.d(TAG, "found using InetAddress ping "+addr);
                    ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO WIFI_NODES_TBL VALUES('"+addr+"')");
                    if(!ConstantsClass.IpAddresses.contains(addr))
                        ConstantsClass.IpAddresses.add(addr);
                    return;
                }

                // Arp Check #2
                hardwareAddress = getHardwareAddress(addr);
                if(!NOMAC.equals(hardwareAddress)){
                    ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO WIFI_NODES_TBL VALUES('"+addr+"')");
                    //Log.d(TAG, "found using arp #2 "+addr);
                    if(!ConstantsClass.IpAddresses.contains(addr))
                        ConstantsClass.IpAddresses.add(addr);
                    return;
                }

                // Arp Check #3
                hardwareAddress = getHardwareAddress(addr);
                if(!NOMAC.equals(hardwareAddress)){
                    //Log.d(TAG, "found using arp #2 "+addr);
                    ConstantsClass.mydatabaseLatest.execSQL("INSERT OR IGNORE INTO WIFI_NODES_TBL VALUES('"+addr+"')");
                    if(!ConstantsClass.IpAddresses.contains(addr))
                        ConstantsClass.IpAddresses.add(addr);
                    return;
                }
            }
            catch(Exception e)
            {
                Log.d("","Exception occured:"+e);
            }
        }
    }
    private void launch(long i) {
        if(!mPool.isShutdown()) {
            mPool.execute(new CheckRunnable(getIpFromLongUnsigned(i)));
        }
    }
    public String getHardwareAddress(String ip) {
        String hw = "00:00:00:00:00:00";
        BufferedReader bufferedReader = null;
        String MAC_RE = "^%s\\s+0x1\\s+0x2\\s+([:0-9a-fA-F]+)\\s+\\*\\s+\\w+$";
        int BUF = 8 * 1024;
        try {
            if (ip != null) {
                String ptrn = String.format(MAC_RE, ip.replace(".", "\\."));
                Pattern pattern = Pattern.compile(ptrn);
                bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"), BUF);
                String line;
                Matcher matcher;
                while ((line = bufferedReader.readLine()) != null) {
                    matcher = pattern.matcher(line);
                    if (matcher.matches()) {
                        hw = matcher.group(1);
                        break;
                    }
                }
            } else {
                Log.e(TAG, "ip is null");
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't open/read file ARP: " + e.getMessage());
            return hw;
        } finally {
            try {
                if(bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return hw;
    }

    public boolean getWifiInfo(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            info = wifi.getConnectionInfo();
            // Set wifi variables
            speed = info.getLinkSpeed();
            ssid = info.getSSID();
            bssid = info.getBSSID();
            macAddress = info.getMacAddress();

            gatewayIp = getIpFromIntSigned(wifi.getDhcpInfo().gateway);
            // broadcastIp = getIpFromIntSigned((dhcp.ipAddress & dhcp.netmask)
            // | ~dhcp.netmask);
            netmaskIp = getIpFromIntSigned(wifi.getDhcpInfo().netmask);
            SubnetUtils.SubnetInfo subnetInfo=new SubnetUtils(gatewayIp,netmaskIp).getInfo();
            ip=getUnsignedLongFromIp(gatewayIp);
            start=getUnsignedLongFromIp(subnetInfo.getLowAddress());
            end=getUnsignedLongFromIp(subnetInfo.getHighAddress());
            size=end-start+1;
            Log.d(TAG,"Start and end ips are:"+getIpFromLongUnsigned(start)+" and "+getIpFromLongUnsigned(end));

            return true;
        }
        return false;
    }

    public long getUnsignedLongFromIp(String ip_addr) {
        String[] a = ip_addr.split("\\.");
        return (Integer.parseInt(a[0]) * 16777216 + Integer.parseInt(a[1]) * 65536
                + Integer.parseInt(a[2]) * 256 + Integer.parseInt(a[3]));
    }
    int IpToCidr(String ip) {
        double sum = -2;
        String[] part = ip.split("\\.");
        for (String p : part) {
            sum += 256D - Double.parseDouble(p);
        }
        return 32 - (int) (Math.log(sum) / Math.log(2d));
    }
    public static String getIpFromIntSigned(int ip_int) {
        String ip = "";
        for (int k = 0; k < 4; k++) {
            ip = ip + ((ip_int >> k * 8) & 0xFF) + ".";
        }
        return ip.substring(0, ip.length() - 1);
    }

    class ConnectRunnable implements Runnable {
        private String addr;

        ConnectRunnable(String addr) {
            this.addr = addr;
        }

        public void run() {
            try {
                Socket sock = new Socket(addr, 1149);
                writeMessages(ConstantsClass.mydatabaseLatest, sock);
            }
            catch(Exception e)
            {
                Log.d(TAG,"Exception:"+e);
            }
        }
    }


    //Get all the message metadata for a given UUID
    public String messageMetadata(String UUIDRep,SQLiteDatabase mydatabaseLatest)
    {
        String metadata=new String();
        try {
            Cursor cursorForMatchingImage = ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from MESSAGE_TBL where UUID='" + UUIDRep + "'", null);


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

            Cursor cursorForRateImage=ConstantsClass.mydatabaseLatest.rawQuery("SELECT * from RATE_PARAMS_TBL where UUID='"+UUIDRep+"'",null);
            String rateData=new String();
            double RatingForTags=5.0,Confidence=5.0,Quality=5.0;
            if(cursorForRateImage.getCount()!=0) {
                cursorForRateImage.moveToFirst();
                //rating REAL,confi REAL,qua REAL
                RatingForTags = cursorForRateImage.getDouble(1);
                Confidence = cursorForRateImage.getDouble(2);
                Quality = cursorForRateImage.getDouble(3);

            }
            rateData="Rating For Tags:"+RatingForTags+"\r\n"+"Confidence on rating for tags:"+Confidence+"\r\n"+"Rating for quality of Message:"+Quality;
            //long size = cursorForMatchingImage.getLong(13);
            //long quality = cursorForMatchingImage.getLong(14);
            //long priority = cursorForMatchingImage.getLong(15);

            metadata+="File name:"+fileName+ "\r\n"+"Timestamp:"+timestamp+"\r\n"+"Tags:"+tagsForCurrentImage+"\r\n";
            metadata+="Latitude:"+latitude+"\r\n"+"Longitude:"+longitude+"\r\n"+"Format:"+format+"\r\n"+"Source:"+localMacAddr+"\r\n";
            metadata+=rateData;
            Log.d("BackTask","metadata value is:"+metadata);

        }
        catch(Exception e)
        {
            Log.d("CTwRC","Exception occured in sendMessage function:"+e);
        }
        return metadata;
    }

}

class Preamble implements Serializable
{
    int dataSize;
    String dataFileName;
    String UUID;
    static final long serialVersionUID=40L;
    Preamble(int size, String fileName,String UUIDIn)
    {
        dataSize=size;dataFileName=fileName;UUID=UUIDIn;
    }
}




