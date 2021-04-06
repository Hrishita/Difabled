package com.hrishita.difabled;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebViewActivity extends AppCompatActivity {
    WebView webView;
    private String mVideoCallId;
    String remoteUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        mVideoCallId = getIntent().getStringExtra("video_call_id");
        if(getIntent().getAction().equals("RECEIVER")) {
            System.out.println("s is not null");
            remoteUser = getIntent().getStringExtra("caller");
            informCallReceived();
        }
        else{
            System.out.println(getIntent().getData());
        }


//        if (WebViewFeature.isFeatureSupported(WebViewFeature.START_SAFE_BROWSING)) {
//            WebViewCompat.startSafeBrowsing(getApplicationContext(), new ValueCallback<Boolean>() {
//                @Override
//                public void onReceiveValue(Boolean value) {
//                    if(value == true) {
//                        Toast.makeText(WebViewActivity.this, "Safe Browsing", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//        }


        webView = findViewById(R.id.web_view);

        setUpWebViewDefaults();
        webView.loadUrl("https://difabledwebservicefinal.herokuapp.com/api/connect?video_call_id=" + mVideoCallId);
//        webView.loadUrl("http://192.168.43.34:3000");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("getUserMedia, WebView", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());

                return true;
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Below isn't necessary, however you might want to:
                        // 1) Check what the site is and perhaps have a blacklist
                        // 2) Have a pop up for the user to explicitly give permission
                        if(request.getOrigin().toString().equals("https://difabledwebservicefinal.herokuapp.com/") ||
                                request.getOrigin().toString().equals("https://difabledwebservicefinal.herokuapp.com/")) {
                            System.out.println("correct site");
                            request.grant(request.getResources());
                        } else {
                            System.out.println("incorrect site");
                            request.deny();
                        }
                    }
                });
            }
        });


    }

    private void informCallReceived() {
//        VideoCallInteractionTask task = new VideoCallInteractionTask(new VideoCallInteractionTask.VideoCallInteractionInterface() {
//            @Override
//            public void success(Response response) {
//                try {
//                    JSONObject object = new JSONObject(response.body().string());
//                    if(object.getString("type").equals("success")) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(WebViewActivity.this, "Success", Toast.LENGTH_SHORT).show();
//
//                            }
//                        });
//                    }
//                    else{
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(WebViewActivity.this, "Failure", Toast.LENGTH_SHORT).show();
//
//                            }
//                        });
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override
//            public void failure() {
//                Toast.makeText(WebViewActivity.this, "Failure", Toast.LENGTH_SHORT).show();
//            }
//        });
//        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
//
        String mNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
//
//        String jsonBody = "{" +
//                "\"video_call_id\":\"" + getIntent().getStringExtra("video_call_id") + "\"," +
//                "\"answering_party\":\"" + mNumber + "\"" +
//                "}";
//
//        task.execute("https://difabledwebservicefinal.herokuapp.com/api/answerCall", jsonBody);

        VideoCallReceivedInformTask task = new VideoCallReceivedInformTask();
        task.execute(mNumber, mVideoCallId, remoteUser);
    }

    private void setUpWebViewDefaults() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//        }
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        // Enable remote debugging via chrome://inspect
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView.clearCache(true);
        webView.clearHistory();
        webView.setWebViewClient(new WebViewClient());

    }
    public static class VideoCallReceivedInformTask extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... strings) {
            String answering_party = strings[0];
            String videocallid = strings[1];
            String receiver = strings[2];
//        String uid = strings[2];

            OkHttpClient client = new OkHttpClient();
            String url = "https://fcm.googleapis.com/fcm/send";

            String jsonBody = "{" +
                    "\"to\": \"/topics/"+ receiver.substring(1) +"\",\n" +
                    "\"priority\": \"high\",\n" +
                    "\"data\": {\n" +
                    "\"receiver\": \""+receiver+"\",\n" +
                    "\"video_call_id\": \""+videocallid+"\",\n" +
                    "\"type\": \"" + VIDEO_CALL_CONSTANTS.VIDEO_CALL_ANSWER + "\"\n" +
                    "}," +
                    "\"ttl\":\"0s\"" +
                    "}";

//            String jsonBody = "{" +
//                    "\"video_call_id\":\"" + videocallid + "\"," +
//                    "\"answering_party\":\"" + answering_party + "\"" +
//                    "}";
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonBody);

            System.out.println(jsonBody + " both reject");

            Request request = new Request.Builder()
                    .addHeader("Authorization", "key=AAAAs9qrN6Q:APA91bFaMrQ-tnqvhNxH2QhJ4f1Og1wmICZm5V0aMnHwf4wU9MqS3Yeh1uc5yahScYvaSuPyF-D_s1P56Pj2mpjVKB5I-lAUcSje9VsFOQjXVaQtl8I10eaUo3JMmPuT-h9OUe2EZEa8")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .url(url)
                    .build();

            try {
                Response res =  client.newCall(request).execute();
                System.out.println(res.body().string() + " both reject 2");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
