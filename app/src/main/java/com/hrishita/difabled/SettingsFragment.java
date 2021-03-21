package com.hrishita.difabled;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {
    Context context;
    HomeActivityInterface mInterface;
    Button logout;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof HomeActivityInterface)
        {
            this.context= context;
            mInterface = (HomeActivityInterface) context;
            this.mInterface.hideAppBar();
            this.mInterface.hideBottomNav();
        }
        else
        {
            throw new ClassCastException("Implement Interface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        logout = v.findViewById(R.id.settings_logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(R.layout.loading).setCancelable(false).create();
                dialog.show();
                String phone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                FirebaseAuth.getInstance().signOut();

                FirebaseMessaging.getInstance().unsubscribeFromTopic(phone.substring(1))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });




                Intent i = new Intent(getContext(), PhoneAuthActivity.class);
                startActivity(i);

                ((HomeActivity)getContext()).finish();
                dialog.dismiss();
            }
        });
        return v;
    }
    @Override
    public void onStart() {
        super.onStart();
        if(this.mInterface!=null)
        {
            this.mInterface.hideBottomNav();
            this.mInterface.hideAppBar();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.mInterface.showAppBar();
        this.mInterface.showBottomNav();
    }
}
