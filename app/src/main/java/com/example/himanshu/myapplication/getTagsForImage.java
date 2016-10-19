package com.example.himanshu.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;


import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by Himanshu on 10/3/2016.
 */
public class getTagsForImage extends Thread{
    public static String picturePath;
    public static Handler handler;
    public getTagsForImage(String picturePath,Handler handler)
    {
        this.picturePath=picturePath;
        this.handler=handler;
    }
    public static void getKeywords() {
        try {
            //LabelApp labelApp = new LabelApp(LabelApp.getVisionService());
            //labelApp.labelImage(picturePath,5);
        }
        catch(Exception e)
        {

        }
        //Path imagePath = Paths.get(args[0]);

        //LabelApp app = new LabelApp(getVisionService());
        //printLabels(System.out, imagePath, app.labelImage(imagePath, MAX_LABELS));


        try {

            VisualRecognition service = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20);
            service.setApiKey("98f4f46c48e79c8916102d410183b4739a72a6a7");

            System.out.println("Classify an image");
            ClassifyImagesOptions options = new ClassifyImagesOptions.Builder()
                    .images(saveBitmapToFile(new File(picturePath)))
                    .build();
            VisualClassification result = service.classify(options).execute();
            String resultString = result.toString();
            Log.d("getTags", "resultString:" + resultString);
            String resultStringArray[] = resultString.split("\n");
            String finalOP = "";


            for (int i = 0; i < resultStringArray.length; i++) {
                if (resultStringArray[i].contains("class\"")) {
                    finalOP += resultStringArray[i].split(":")[1];
                    Log.d("getTags", "finalOP:" + finalOP);
                    finalOP.replace("\"", "");

                }
            }
            if (finalOP.length() != 0)
                finalOP = finalOP.substring(0, finalOP.length() - 1);
            Log.d("getTags", "FinalOP:" + finalOP);

            Log.d("getTags", "Going to send message");
            Message msgObj = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putString("message", finalOP);
            msgObj.setData(b);
            handler.sendMessage(msgObj);

        }
        catch(Exception e)
        {
            Log.d("getTagsForImage","Exception occured:"+e);
            Message msgObj = handler.obtainMessage();
            Bundle b = new Bundle();
            b.putString("message", "Internet Issue");
            msgObj.setData(b);
            handler.sendMessage(msgObj);
        }
    }
    public void run()
    {
        getKeywords();
    }
    public static File saveBitmapToFile(File file){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE=75;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }
}
