package com.hrishita.difabled;

import android.os.AsyncTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdateLocationTask extends AsyncTask<String, Void, Void> {
    UpdateLocationInterface updateLocationInterface;
    UpdateLocationTask(UpdateLocationInterface updateLocationInterface) {
        this.updateLocationInterface = updateLocationInterface;
    }
    @Override
    protected Void doInBackground(String... strings) {
        String lat = strings[0];
        String longitude = strings[1];

        OkHttpClient client = new OkHttpClient();
        String jsonBody = "{" +
                "\"latitude\":\"" + lat + "\"," +
                "\"longitude\":\"" + longitude + "\"" +
                "};";

        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);
        Request request = new Request.Builder().url(Constants.SERVER_ADDRESS + "/api/updateLocation/").post(body).build();
        try {
            Response response = client.newCall(request).execute();
            String jsonResponse = response.body().string();
            this.updateLocationInterface.result(jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public interface UpdateLocationInterface{
        void result(String jsonBody);
    }
}
