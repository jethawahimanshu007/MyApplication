package com.example.himanshu.myapplication;

/**
 * Created by Himanshu on 9/13/2016.
 */
public class MessageFormat {
    byte[] Message;
    String UUID;
    String Topic;
    String Format;
    String mime;
    String fileName;
    String sourceName;
    String sourceAddr;
    String destName;
    String destAddr;
    String timeStamp;
    String latitude;
    String longitude;
    MessageFormat(byte[] Message,String UUID,String Topic,String Format,String mime,String fileName,String sourceAddr,String sourceName,String destName,String destAddr,String timeStamp,String latitude,String longitude){
        this.Message=Message;
        this.UUID=UUID;
        this.Topic=Topic;
        this.Format=Format;
        this.mime=mime;
        this.fileName=fileName;
        this.sourceName=sourceName;
        this.sourceAddr=sourceAddr;
        this.destName=destName;
        this.destAddr=destAddr;
        this.timeStamp=timeStamp;
        this.latitude=latitude;
        this.longitude=longitude;
        // Message,UUID,Topic,Format,mime,fileName,sourceName,sourceAddr,destName,destAddr,timeStamp,latitude,longitude
    };
}
///MessageFormat Length :  Seperated by FileNameLength-FileLength
//imagePath VARCHAR, latitude REAL,longitude REAL,timestamp DATE,tagsForCurrentImage VARCHAR,fileName VARCHAR,mime VARCHAR,format VARCHAR,localMacAddr VARCHAR,localName VARCHAR,UUID VARCHAR,remoteMacAddr VARCHAR,remoteName VARCHAR