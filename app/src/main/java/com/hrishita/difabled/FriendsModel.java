package com.hrishita.difabled;


import java.io.Serializable;

class FriendsModel implements Serializable {
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    private String chat_id;

    FriendsModel(User user, String chat_id) {
        this.user= user;
        this.chat_id= chat_id;
    }

}
