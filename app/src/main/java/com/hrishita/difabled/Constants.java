package com.hrishita.difabled;

import android.os.Environment;

public class Constants {
    public static String CATEGORY_NORMAL = "0";
    public static String CATEGORY_MUTE = "1";
    public static String CATEGORY_DEAF = "2";
    public static String CATEGORY_MUTE_DEAF = "3";
    public static String CATEGORY_BLIND="4";

    public static String SUCCESS = "success";
    public static String FAILURE = "failure";

    public static int MAX_IMAGE_SIZE = 4096;

    public static String SERVER_ADDRESS = "https://difabledwebservicefinal.herokuapp.com/";
//    public static String SERVER_ADDRESS = "http://192.168.0.108:3000";

    public static String ACTION_LIKE = "like";
    public static String ACTION_UNLIKE = "unlike";

    public static String COMMENT_TASK_TYPE_GET = "get-comments";
    public static String COMMENT_TASK_TYPE_POST = "post-comment";

    public static Integer FIREBASE_FRIEND_REQUEST_RECVD = 0;
    public static Integer FIREBASE_FRIEND_REQUEST_SENT = 1;
    public static Integer FIREBASE_FRIEND_REQUEST_ACCEPTED = 2;


    public static String IMAGE_UPLOAD_EXISTING = "existing";
    public static String IMAGE_UPLOAD_REMOVE = "remove";
    public static String IMAGE_UPLOAD_NEW = "new";

    public static String[] categories = {"art",
            "business",
            "food",
            "talent",
            "sports",
            "Science & Tech",
            "Influencer",
            "Politics",
            "Others",
            "Fashion & Lifestyle",
            "DIY"
    };

    public static String NOTIFICATION_TYPE_POST = "post";
    public static String NOTIFICATION_TYPE_USER = "user";

    public static String MESSAGE_TYPE_TEXT = "0";
    public static String MESSAGE_TYPE_IMAGE = "1";

    public static String ROOT_DIR = Environment.getExternalStorageDirectory() + "";
}
