package com.hrishita.difabled;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Utils {
    public static String[] convertStringToArray(String str, String delimiter) {
        if(str == null) return null;
        if(delimiter==null) return null;
        return str.split(delimiter);
    }

    public static boolean isUserSignedIn()
    {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        return user != null;
    }

}
