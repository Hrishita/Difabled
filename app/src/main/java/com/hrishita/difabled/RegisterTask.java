package com.hrishita.difabled;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.appcompat.app.AlertDialog;

import java.io.FileInputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterTask extends AsyncTask<ProfileModel, Void, String> {
    private Context context;
    private AlertDialog dialog;
    private RegisterCallbackListener listener;
    RegisterTask(Context context, RegisterCallbackListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        dialog = new AlertDialog.Builder(context).setView(R.layout.loading).setCancelable(false).create();
        dialog.show();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(ProfileModel... strings) {

        final String username = strings[0].name;
        final String phone_number = strings[0].phoneNumber;
        final String uid = strings[0].uid;
        final String category = strings[0].cat;
        final String command = strings[0].command;
        final Uri profilePath = strings[0].imageUri;


        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody;
        MultipartBody.Builder multipartBuilder = new MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("phone_number", phone_number)
                .addFormDataPart("username", username)
                .addFormDataPart("category", category)
                .addFormDataPart("uid", uid);

        if(command.equals(Constants.IMAGE_UPLOAD_NEW)) {
            //take profile path
            if(profilePath!=null) {
                byte[] bytes = new byte[0];
                try{
                    FileInputStream is = (FileInputStream) context.getContentResolver().openInputStream(profilePath);
                    if (is != null) {
                        bytes = new byte[(int) is.getChannel().size()];
                        is.read(bytes);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                multipartBuilder
                        .addFormDataPart("command", command)
                        .addFormDataPart("profile_photo", profilePath.getPath(),
                                RequestBody.create(MediaType.parse("image/*"), bytes));
            }
            else {
                multipartBuilder
                        .addFormDataPart("command", Constants.IMAGE_UPLOAD_EXISTING)
                        .addFormDataPart("profile_photo", "DEFAULT");
            }
        } else if(command == Constants.IMAGE_UPLOAD_REMOVE) {
            //remove
            multipartBuilder
                    .addFormDataPart("command", Constants.IMAGE_UPLOAD_REMOVE)
                    .addFormDataPart("profile_photo", "DEFAULT");
        } else if(command == Constants.IMAGE_UPLOAD_EXISTING) {
            multipartBuilder
                    .addFormDataPart("command", Constants.IMAGE_UPLOAD_EXISTING)
                    .addFormDataPart("profile_photo", "DEFAULT");
        }
        requestBody = multipartBuilder.build();

        Request request = new Request.Builder().url(Constants.SERVER_ADDRESS + "/api/register").post(requestBody).build();
        try {
            Response r = client.newCall(request).execute();
            return r.body().string();
        } catch (Exception e) {

            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String string) {
        super.onPostExecute(string);
        dialog.dismiss();
        listener.registerCallback(string);
    }
}