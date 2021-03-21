package com.hrishita.difabled;

import android.os.AsyncTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LikePostTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... strings) {
        String uid = strings[0];
        String postId = strings[1];
        String action = strings[2];

        OkHttpClient client = new OkHttpClient();
        String url = Constants.SERVER_ADDRESS + "/api/likepost/";


        String jsonBody = "{" +
                "\"uid\":\"" + uid + "\"," +
                "\"action\":\"" + action + "\"," +
                "\"pid\":\"" + postId + "\"" +
                "}";

        System.out.println(jsonBody);

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);

        Request request = new Request.Builder().url(url).post(body).build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if(s==null) {
            System.out.println("Something Went Wrong While Liking Post");
        }
    }
}
