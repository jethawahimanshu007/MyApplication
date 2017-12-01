package com.example.himanshu.myapplication;

import android.content.Context;
import android.os.Handler;


/**
 * Created by Himanshu on 8/30/2017.
 */

//Used in some classes for passing handler so that classes can communicate with each other
public class ContextHandler {
    Context context;
    Handler handler;
    ContextHandler(Context context,Handler handler){
        this.context=context;
        this.handler=handler;
    }
}
