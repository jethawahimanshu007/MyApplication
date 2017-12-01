package com.example.himanshu.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

//import static com.example.himanshu.myapplication.LabelApp.context;

/**
 * Created by Himanshu on 8/1/2017.
 */
//A class for storing the global Database handler
public class ConstantsClass {
    public static String IP_ADDRESS;
    public static ArrayList<String> IpAddresses;
    public static SQLiteDatabase mydatabaseLatest;
    public ConstantsClass(Context context)
    {
        mydatabaseLatest= context.openOrCreateDatabase(Constants.DATABASE_NAME, context.MODE_PRIVATE, null);
    }

}
