package com.yasnosean.schoolprojectapp.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class Algorithms {

    public static String bitmapToString(Bitmap bitmap) {
        String imgString = "";

        if (bitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] decodeString = baos.toByteArray();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeByteArray(decodeString, 0, decodeString.length, options);
            int imageHeight = options.outHeight;
            int imageWidth = options.outWidth;
            int inSampleSize = 1;

            if (imageHeight > imageWidth) {
                if (imageHeight > 800 && imageWidth > 480) {
                    final int halfHeight = imageHeight / 1;
                    final int halfWidth = imageWidth / 1;

                    while ((halfHeight / inSampleSize) > 800 || (halfWidth / inSampleSize) > 480) {
                        inSampleSize *= 2;
                    }
                }
            } else {
                if (imageHeight > 480 && imageWidth > 800) {
                    final int halfHeight = imageHeight / 1;
                    final int halfWidth = imageWidth / 1;

                    while ((halfHeight / inSampleSize) > 800 || (halfWidth / inSampleSize) > 480) {
                        inSampleSize *= 2;
                    }
                }
            }

            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;

            Bitmap decoded = BitmapFactory.decodeByteArray(decodeString, 0, decodeString.length, options);
            System.out.println("SUPERMAN " + String.valueOf(decoded.getWidth()) + "x" + String.valueOf(decoded.getHeight()));
            imgString = Base64.encodeToString(decodeString, Base64.DEFAULT);
        }

        return imgString;
    }

    public static Bitmap stringToBitMap(String encodedString){
        try {
            byte [] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

}
