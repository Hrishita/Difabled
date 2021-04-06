package com.hrishita.difabled;

import android.os.AsyncTask;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VideoCallAsyncTask extends AsyncTask<String, Void, Void> {
    private VideoCallResultInterface videoCallResultInterface;
    VideoCallAsyncTask(VideoCallResultInterface videoCallResultInterface) {
        this.videoCallResultInterface = videoCallResultInterface;
    }
    @Override
    protected Void doInBackground(String... strings) {
        String id = strings[0];
        String my_number = strings[1];
        String receiver_number = strings[2];

        OkHttpClient client = new OkHttpClient();
        String jsonBody = "{" +
                "\"caller\":\"" + my_number + "\"," +
                "\"receiver\":\"" + receiver_number + "\"," +
                "\"id\":\"" + id + "\"" +
                "}";
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);
        Request request = new Request.Builder().url("https://difabledwebservicefinal.herokuapp.com/api/callUser").post(body).build();

        try {
            Response response = client.newCall(request).execute();
            String responseJson = response.body().string();
            JSONObject json = new JSONObject(responseJson);
            String response_type = json.getString("type");
            if(response_type.equals("success")) {
                videoCallResultInterface.success(json);
            }
            else if(response_type.equals("failure")) {
                videoCallResultInterface.failure(json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public interface VideoCallResultInterface{
        void success(JSONObject message);
        void failure(JSONObject message);
    }
}
