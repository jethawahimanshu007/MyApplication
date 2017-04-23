/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.himanshu.myapplication;



import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
//import com.google.api.client.googleapis.auth.oauth2.
//import com.fasterxml.jackson.core.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.common.collect.ImmutableList;

import org.apache.commons.compress.utils.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
/*import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
*/
import java.security.GeneralSecurityException;
import java.util.List;

/**
 * A sample application that uses the Vision API to label an image.
 */
@SuppressWarnings("serial")
public class LabelApp {
    /**
     * Be sure to specify the name of your application. If the application name is {@code null} or
     * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
     */
    private static final String APPLICATION_NAME = "Google-VisionLabelSample/1.0";
    public  static Context context;
    private static final int MAX_LABELS = 3;

    /**
     * Annotates an image using the Vision API.
     */
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        if (args.length != 1) {
            System.err.println("Missing imagePath argument.");
            System.err.println("Usage:");
            System.err.printf("\tjava %s imagePath\n", LabelApp.class.getCanonicalName());
            System.exit(1);
        }


       // LabelApp app = new LabelApp(getVisionService());
        //printLabels(System.out, imagePath, app.labelImage(imagePath, MAX_LABELS));
    }

    /**
     * Prints the labels received from the Vision API.
     */
    public static String printLabels(PrintStream out, String imagePath, List<EntityAnnotation> labels) {
       // out.printf("Labels for image %s:\n", imagePath);
        String finalResult="";
        for (EntityAnnotation label : labels) {
            finalResult+=label.getDescription()+",";
           //Log.d("LabelApp", "\t%s (score: %.3f)\n"+ label.getDescription()+ label.getScore());
        }
        if(finalResult.contains(","))
        {
            finalResult=finalResult.substring(0,finalResult.length()-1);
        }
        if (labels.isEmpty()) {
            Log.d("LabelApp","\tNo labels found.");
        }
        return finalResult;
    }

    /**
     * Connects to the Vision API using Application Default Credentials.
     */

    public void setContext(Context context)
    {
        Log.d("LabelApp","Function set Context called, if context is null?:"+(context==null));

        this.context=context;
    }
    public static Vision getVisionService(Context context) throws IOException, GeneralSecurityException {


        //DefaultCredentialProvider dc;
        Log.d("LabelApp","The value of GOOGLE_APPLICATION_CREDENTIALS:"+System.getProperty("GOOGLE_APPLICATION_CREDENTIALS"));
        //GoogleCredential credential = GoogleCredential.getApplicationDefault();
        GoogleCredential credential;

            Log.d("LabelApp", "Context inside LabelApp is null or not:" + (context == null));
            Log.d("LabelApp", "context.getResources().openRawResource(R.raw.credentials) is null or not:" + (context.getResources().openRawResource(R.raw.credentials) == null));

            credential = GoogleCredential.fromStream(context.getResources().openRawResource(R.raw.credentials)).createScoped(VisionScopes.all());
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

      /*  return new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
                */
        return new Vision.Builder(AndroidHttp.newCompatibleTransport(), jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();


    }

    private final Vision vision;

    /**
     * Constructs a {@link LabelApp} which connects to the Vision API.
     */
    public LabelApp(Vision vision) {
        this.vision = vision;


    }


    /**
     * Gets up to {@code maxResults} labels for an image stored at {@code path}.
     */
    public List<EntityAnnotation> labelImage(String path, int maxResults) throws IOException {
        //byte[] data = Files.readAllBytes(path);


        /* java.io.RandomAccessFile raf = new java.io.RandomAccessFile(path, "r");
        byte[] b = new byte[(int) raf.length()];
        Log.d("CTwRC", "Length of byte array from image is:" + b.length);
        raf.readFully(b);
        */
        byte[] b= IOUtils.toByteArray(new FileInputStream(saveBitmapToFile(new File(path))));
        Log.d("LabelApp","Size of the image bytes to be sent is:"+b.length);
       // byte[] b= IOUtils.toByteArray(new FileInputStream(new File(path)));
        AnnotateImageRequest request =
                new AnnotateImageRequest()
                        .setImage(new Image().encodeContent(b))
                        .setFeatures(ImmutableList.of(
                                new Feature()
                                        .setType("LABEL_DETECTION")
                                        .setMaxResults(maxResults)));
        Vision.Images.Annotate annotate =
                vision.images()
                        .annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotate.setDisableGZipContent(true);

        BatchAnnotateImagesResponse batchResponse = annotate.execute();
        assert batchResponse.getResponses().size() == 1;
        AnnotateImageResponse response = batchResponse.getResponses().get(0);
        if (response.getLabelAnnotations() == null) {
            throw new IOException(
                    response.getError() != null
                            ? response.getError().getMessage()
                            : "Unknown error getting image annotations");
        }
        return response.getLabelAnnotations();
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