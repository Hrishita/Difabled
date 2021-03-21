package com.hrishita.difabled;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadPostAsyncTask extends AsyncTask<UploadPostModel, Void, String> {
    AlertDialog dialog;
    Context context;
    UploadPostCallback callback;

    UploadPostAsyncTask(Context context, UploadPostCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new AlertDialog.Builder(context).setView(R.layout.loading).setCancelable(false).create();
        dialog.show();
    }

    @Override
    protected String doInBackground(UploadPostModel[] postModels) {
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("phone_number", postModels[0].phone_number)
                .addFormDataPart("uid", postModels[0].uid)
                .addFormDataPart("caption", postModels[0].caption)
                .addFormDataPart("payment", postModels[0].paymentLink)
                ;

        for(int i = 0;i<postModels[0].locations.length;i++) {
            try {
                FileInputStream fis = (FileInputStream) new FileInputStream(postModels[0].locations[i]);
                if (fis != null) {
                    byte[] bytes = new byte[(int) fis.getChannel().size()];
                    fis.read(bytes);
                    multipartBodyBuilder.addFormDataPart("images", postModels[0].locations[i], RequestBody.create(MediaType.parse("image/*"), bytes));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        RequestBody body = multipartBodyBuilder.build();

        Request request = new Request.Builder().url(Constants.SERVER_ADDRESS + "/api/uploadpost/").post(body).build();

        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        dialog.dismiss();
        this.callback.uploadpostresult(s);
    }

    interface UploadPostCallback{
        void uploadpostresult(String s);
    }

}
