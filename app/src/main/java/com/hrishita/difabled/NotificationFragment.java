package com.hrishita.difabled;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment {

    HomeActivityInterface mInterface;
    Context context;
    RecyclerView recyclerView;
    List<NotificationModel> arrayList = new ArrayList<>();
    NotificationAdapter adapter;
    FirebaseUser user;
    ArrayList<String> phoneNumbers = new ArrayList<>();

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof HomeActivityInterface)
        {
            this.context = context;
            this.mInterface = (HomeActivityInterface) context;
            this.mInterface.hideAppBar();
        }
        else
        {
            throw new ClassCastException("Implement Interface");
        }
    }
    private void loadData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference().child("friend-request").child(user.getPhoneNumber()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    phoneNumbers.clear();
                    for(DataSnapshot s : snapshot.getChildren())
                    {
                        if(s.child("type").exists() && s.child("type").getValue()!=null)
                        {
                            try {
                                if (s.child("type").getValue().toString().equals(Constants.FIREBASE_FRIEND_REQUEST_RECVD + "")) {
                                    phoneNumbers.add(s.getKey());
//                                    arrayList.add(new NotificationModel(Constants.NOTIFICATION_TYPE_USER, s.getKey()))
                                }
                            }
                            catch (NullPointerException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    if(!phoneNumbers.isEmpty())
                        loadFriendRequestName();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        arrayList.add(new NotificationModel(Constants.NOTIFICATION_TYPE_USER,1,1, "Devarsh mavani and 10 others liked your post"));
//        arrayList.add(new NotificationModel(Constants.NOTIFICATION_TYPE_USER,1,1, "Devarsh mavani and 10 others liked your post"));
//        arrayList.add(new NotificationModel(Constants.NOTIFICATION_TYPE_USER,1,1, "Devarsh mavani and 10 others liked your post"));
//        arrayList.add(new NotificationModel(Constants.NOTIFICATION_TYPE_USER,1,1, "Devarsh mavani and 10 others liked your post"));
//        arrayList.add(new NotificationModel(Constants.NOTIFICATION_TYPE_USER,1,1, "Devarsh mavani and 10 others liked your post"));
//        arrayList.add(new NotificationModel(Constants.NOTIFICATION_TYPE_USER,1,1, "Devarsh mavani and 10 others liked your post"));
//        arrayList.add(new NotificationModel(Constants.NOTIFICATION_TYPE_USER,1,1, "Devarsh mavani and 10 others liked your post"));
//        arrayList.add(new NotificationModel(Constants.NOTIFICATION_TYPE_USER,1,1, "Devarsh mavani and 10 others liked your post"));
//        arrayList.add(new NotificationModel(Constants.NOTIFICATION_TYPE_USER,1,1, "Devarsh mavani and 10 others liked your post"));
//        arrayList.add(new NotificationModel(Constants.NOTIFICATION_TYPE_USER,1,1, "Devarsh mavani and 10 others liked your post"));
//        arrayList.add(new NotificationModel(Constants.NOTIFICATION_TYPE_USER,1,1, "Devarsh mavani and 10 others liked your post"));
    }

    private void loadFriendRequestName() {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users")
                .whereIn("phone", phoneNumbers)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful() && task.isComplete())
                        {
                            if(task.getResult()!=null) {
                                arrayList.clear();
                                for (DocumentSnapshot s : task.getResult().getDocuments()) {
                                    if(s.get("name")!=null) {
                                        User user = new User();
                                        user.setPhone(s.get("phone").toString());
                                        user.setCategory(s.get("category").toString());
                                        user.setEmail(s.get("handler").toString());
                                        user.setProfile_link(s.get("profile").toString());
                                        user.setStatus(s.get("status").toString());
                                        user.setName(s.get("name").toString());
                                        user.setId(s.get("phone").toString());
                                        arrayList.add(new NotificationModel(Constants.NOTIFICATION_TYPE_USER, user, "-1", s.get("name").toString() + " Sent you friend request"));
                                    }
                                }
                                if(getContext()!=null)
                                {
                                    ((HomeActivity)getContext())
                                            .runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    adapter= new NotificationAdapter(getContext(), arrayList);
                                                    recyclerView.setAdapter(adapter);
                                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_notification, container, false);
        recyclerView = v.findViewById(R.id.notifications_rcv);
        user = FirebaseAuth.getInstance().getCurrentUser();
        loadData();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.mInterface.showAppBar();
    }
}
