package com.example.midterm.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class ImageUtils {
    public static String bitmapToBase64(Bitmap bitmap){
        if (bitmap == null) return "";
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float maxDimension = 300f;

        if(width > maxDimension || height > maxDimension){
            float ratio = Math.min(maxDimension / width, maxDimension / height);
            width = Math.round(ratio * width);
            height = Math.round(ratio * height);
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static Bitmap base64ToBitmap(String base64Str){
        if(base64Str == null || base64Str.trim().isEmpty()){
            return null;
        }
        try{
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}