package com.hrishita.difabled;

import java.io.Serializable;

public class NotificationModel implements Serializable
{
    String type;
    User user;
    String postId;
    String text;
    NotificationModel(String type, User user, String postId, String text)
    {
        this.type = type;
        this.user = user;
        this.postId = postId;
        this.text = text;
    }
}
