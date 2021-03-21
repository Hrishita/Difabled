package com.hrishita.difabled;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

public class UpdatePrecenceTask extends AsyncTask<String, Void, String>
{
    Context context;
    FirebaseFirestore firestore;
    UpdatePrecenceTask(Context context)
    {
        this.context = context;
        firestore = FirebaseFirestore.getInstance();
    }


    @Override
    protected String doInBackground(String... strings) {
        String phone = strings[0];
        String presence = strings[1];
        firestore.collection("users")
                .document(phone)
                .update("presence", presence.equals("true"))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                })
        ;
        return null;
    }
}
