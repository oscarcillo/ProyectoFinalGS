package com.example.proyecto_final_gs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class Utils {

    public static void goToActivity(Context context, Class<?> target, Bundle bundle, boolean finish){
        Intent i = new Intent(context, target);
        if(bundle!=null)
            i.putExtras(bundle);
        context.startActivity(i);
        if(finish)
            ((Activity) context).finish();
    }

}
