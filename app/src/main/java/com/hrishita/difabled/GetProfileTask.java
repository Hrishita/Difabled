package com.hrishita.difabled;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GetProfileTask extends AsyncTask<String, Void, String> {
    Context context;
    ProfileReceivedListener listener;
    AlertDialog dialog;
    public interface ProfileReceivedListener{
        void takeProfile(String body);
    }
    GetProfileTask(Context context, ProfileReceivedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new AlertDialog.Builder(context).setCancelable(false).setView(R.layout.loading).create();
        //dialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        String uid = strings[0];
        String enduser = strings[1];
        String url = Constants.SERVER_ADDRESS + "/api/getprofile";
        String jsonBody = "{" +
                "\"uid\":\"" + uid + "\"," +
                "\"phone_number\":\"" + enduser + "\"" +
                "}";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);
        Request request = new Request.Builder().url(url).post(body).build();
        try {
            Response response = client.newCall(request).execute();

            return response.body().string();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        System.out.println("server response = 2" + s);
        dialog.dismiss();

        this.listener.takeProfile(s);
    }
}
