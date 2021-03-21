package com.hrishita.difabled;

import java.io.Serializable;

public class FirebaseMessageModel implements Serializable {

    FirebaseMessageModel(String dateTime, String sender, String receiver, String message, boolean isMessageRecvd, String type) {
        this.dateTime = dateTime;
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.isMessageRecvd = isMessageRecvd;
        this.type = type;
    }
    String dateTime;
    boolean isMessageRecvd = false;

    public String getDateTime() {
        return dateTime;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getReceiver() {
        return receiver;
    }



    String sender;
    String message;
    String receiver;
    String type;
}
