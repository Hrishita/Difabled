package com.hrishita.difabled;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    public static boolean isUserProfileCreated(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String pro = preferences.getString("profile", "");
        return !pro.equals("");
    }

    public static void saveProfile(Context context, String name, String type) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        preferences.edit().putString("name", name).putString("type", type).commit();
    }


}
