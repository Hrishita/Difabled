package com.hrishita.difabled;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class VideoCallStartTask extends AsyncTask<String, String, String> {
    Context context;
    VideoCallStatusCallback mCallback;
    AlertDialog dialog;
    public VideoCallStartTask(Context context, VideoCallStatusCallback callback) {
        this.context = context;
        this.mCallback = callback;
    }

    public interface VideoCallStatusCallback{
        void success(JSONObject s);
        void failure(JSONObject s);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new AlertDialog.Builder(context).setCancelable(false).setView(R.layout.video_call_status_view).create();
        dialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        String myPhoneNumber = strings[0];
        String receiverPhoneNumber = strings[1];
        String id = strings[2];

        publishProgress("connecting");

        OkHttpClient client = new OkHttpClient();
        String jsonBody = "{" +
                "\"caller\":\"" + myPhoneNumber + "\"," +
                "\"receiver\":\"" + receiverPhoneNumber + "\"," +
                "\"id\":\"" + id + "\"" +
                "}";
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);
        Request request = new Request.Builder().url("https://webrtctestdev.herokuapp.com/api/callUser").post(body).build();

        try {
            Response response = client.newCall(request).execute();
            String responseJson = response.body().string();
            JSONObject json = new JSONObject(responseJson);
            String response_type = json.getString("type");
            if(response_type.equals("success")) {
                mCallback.success(json);
                publishProgress("Success");
            }
            else if(response_type.equals("failure")) {
                mCallback.failure(json);
                publishProgress("Failure");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        TextView textView = dialog.findViewById(R.id.video_call_status_text_view);
        textView.setText(values[0]);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        dialog.dismiss();
    }
}
