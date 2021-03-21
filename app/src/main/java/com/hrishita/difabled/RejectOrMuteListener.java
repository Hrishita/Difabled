package com.hrishita.difabled;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class RejectOrMuteListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Broadcast");
        Bundle extras = intent.getExtras();
        if(extras!=null) {
            int notificationId = extras.getInt("notificationID");
            System.out.println(notificationId + "");
            String action = intent.getAction();
            if (action != null) {
                if(action.equals("com.jayshreegopalapps.xmpptest.REJECT")) {
                    //reject call
                    System.out.println("REJECT call");
                    Intent i = new Intent(context, MyService.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("video_call_id", intent.getExtras().getString("video_call_id"));
                    bundle.putString("caller", intent.getExtras().getString("caller"));
                    i.putExtras(bundle);
                    i.setAction("ACTION_STOP_MY_SERVICE");
                    context.startService(i);
                }
                else{
                    //mute call (Show notification in background)
                    System.out.println("MUTE call");
                    Intent i = new Intent(context,MyService.class);
                    i.setAction("ACTION_MUTE_MY_SERVICE");
                    context.startService(i);
                }
            }
        }
    }
}
