package com.hrishita.difabled;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CommentInteractionTask extends AsyncTask<String, Void, String> {
    Context context;
    AlertDialog dialog;
    CommentTaskCallback mCallback;
    CommentInteractionTask(Context context, CommentTaskCallback callback) {
        this.context=context;
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        dialog = new AlertDialog.Builder(context).setCancelable(false).setView(R.layout.loading).create();
        dialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        String type = strings[0];
        String uid = strings[1];
        String pid = strings[2];

        OkHttpClient client = new OkHttpClient();

        if(type.equals(Constants.COMMENT_TASK_TYPE_GET)) {
            //get comments
            String jsonBody = "{" +
                    "\"uid\":\"" + uid + "\"," +
                    "\"pid\":\"" + pid + "\"" +
                    "}";

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);
            Request request = new Request.Builder().url(Constants.SERVER_ADDRESS + "/api/getcomments/").post(body).build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e ){
                e.printStackTrace();
            }
        } else {
            //post comment
            String comment = strings[3];
            String jsonBody = "{" +
                    "\"uid\":\"" + uid + "\"," +
                    "\"pid\":\"" + pid + "\"," +
                    "\"comment\":\"" + comment + "\"" +
                    "}";

            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);
            Request request = new Request.Builder().url(Constants.SERVER_ADDRESS + "/api/postcomment/").post(body).build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e ){
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        dialog.dismiss();
        this.mCallback.finish(s);
    }
    public interface CommentTaskCallback{
        void finish(String s);
    }
}
