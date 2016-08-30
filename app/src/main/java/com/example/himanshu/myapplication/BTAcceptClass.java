package com.example.himanshu.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by Himanshu on 8/8/2016.
 */
public class BTAcceptClass {
    Handler mHandler;
    BTAcceptClass(Handler mHandlerInput)
    {
        mHandler=mHandlerInput;
    }
    public synchronized  void start()
    {
        AcceptThread at=new AcceptThread();
        at.start();

        Message msg = mHandler.obtainMessage(Constants.MESSAGE_READ);
        Bundle bundle=new Bundle();
        bundle.putString("FoundDevice","New device is connected, I guess");
        //mHandler.sendMessage(msg);
    }
}
