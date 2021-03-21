package com.hrishita.difabled;

import android.os.AsyncTask;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VideoCallInteractionTask extends AsyncTask<String, Void, Void> {
    private VideoCallInteractionInterface mInterface;
    VideoCallInteractionTask(VideoCallInteractionInterface interactionInterface) {
        this.mInterface = interactionInterface;
    }
    public interface VideoCallInteractionInterface {
        void success(Response response);
        void failure();
    }
    @Override
    protected Void doInBackground(String... strings) {

        String url = strings[0];
        String postData = strings[1];

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postData);
        Request request = new Request.Builder().url(url).post(body).build();
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
            mInterface.success(response);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
