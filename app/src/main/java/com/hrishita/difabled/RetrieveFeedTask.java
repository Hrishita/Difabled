package com.hrishita.difabled;

import android.os.AsyncTask;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RetrieveFeedTask extends AsyncTask<String, Void, Void> {

    protected Void doInBackground(String... id) {
        try {
//            users[0].getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//                @Override
//                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    OkHttpClient client = new OkHttpClient();
                    RequestBody requestBody = new MultipartBody.Builder()
                            .setType(MultipartBody.FORM)
                            .addFormDataPart("username", id[0])
                            .build();

                    Request request = new Request.Builder().url(Constants.SERVER_ADDRESS + "/api/createUser").post(requestBody).build();
                    try {
                        client.newCall(request).execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                }
//            });
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}