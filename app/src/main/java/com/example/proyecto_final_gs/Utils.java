package com.example.proyecto_final_gs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class Utils {

    public static void goToActivity(Context context, Class<?> target, Bundle bundle, boolean finish){
        Intent i = new Intent(context, target);
        if(bundle!=null)
            i.putExtras(bundle);
        context.startActivity(i);
        if(finish)
            ((Activity) context).finish();
    }

    public static void loadImageWithGlide(Context context, Uri uri, ImageView image){
        Glide.with(context)
                .load(uri)
                .apply(RequestOptions.circleCropTransform()).into(image);
    }

    public static void loadImageWithGlideSize(Context context, Uri uri, ImageView image, int sizex, int sizey){
        Glide.with(context)
                .load(uri)
                .override(sizex,sizey)
                .apply(RequestOptions.circleCropTransform()).into(image);

    }

}
