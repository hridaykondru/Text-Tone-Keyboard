package com.hriday.project;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

public class DeviceAdmin extends DeviceAdminReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        //context.stopService(new Intent(context, CallSer2.class));



        //Intent myIntent = new Intent(context, CallSer2.class);
        //myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //context.startService(myIntent);
    }

    public void onEnabled(Context context, Intent intent) {
    };

    public void onDisabled(Context context, Intent intent) {
    };
}