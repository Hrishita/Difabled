package com.hrishita.difabled;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BottomModalVideoCall extends BottomSheetDialogFragment
{
    FirebaseUser user;
    FirebaseAuth auth;
    Context context;
    HomeActivityInterface mInterface;

    List<User> arrayList = new ArrayList<>();
    List<String> phoneNumbers = new ArrayList<>();
    FirebaseFirestore firebaseFirestore;
    ProgressBar progressBar;
    InstantVideoCallAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_modal_video_call, container, false);
        progressBar = v.findViewById(R.id.bmvc_progress);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        firebaseFirestore= FirebaseFirestore.getInstance();

        RecyclerView recyclerView = v.findViewById(R.id.bmvc_rcv);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        });

        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            adapter= new InstantVideoCallAdapter(getContext(), arrayList);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        return v;
    }

    private void loadData() {
//        arrayList = client
//                .getAppDatabase()
//                .userDao()
//                .getAllUser();
        arrayList.clear();
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        database.child("").child("friend-request").child(user.getPhoneNumber()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot s : snapshot.getChildren()) {
                    //first direct child is uid of another user
                    if (s.hasChild("type") && s.child("type").getValue()!=null) {
                        if (s.child("type").getValue().toString().equals(Constants.FIREBASE_FRIEND_REQUEST_ACCEPTED + "")) {
                            phoneNumbers.add(s.getKey());
                        }
                    }
                }
                firebaseFirestore.collection("users")
                        .whereIn("phone", phoneNumbers)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isComplete())
                                {
                                    System.out.println("Total Result Size = " + task.getResult().size());

                                    for(DocumentSnapshot s : task.getResult().getDocuments())
                                    {
                                        System.out.println("Inside id = " + s.getId());

                                        String name= (String) s.get("name");
                                        String email = (String) s.get("email");
                                        String status = (String) s.get("status");
                                        String category = (String) s.get("category");
                                        String profile = (String) s.get("profile");
                                        String phone = s.getId();

                                        User u = new User();
                                        u.setPhone(phone);
                                        u.setStatus(status);
                                        u.setProfile_link(profile);
                                        u.setName(name);
                                        u.setEmail(email);
                                        u.setCategory(category);

                                        arrayList.add(u);
                                    }

                                    if(((HomeActivity)getContext()) == null) return;
                                    ((HomeActivity)getContext())
                                            .runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    progressBar.setVisibility(View.GONE);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            });
                                }
                            }
                        });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        if(context instanceof HomeActivityInterface)
        {
            this.mInterface = (HomeActivityInterface) context;
            this.mInterface.hideBottomNav();
        }
        else{
            throw new ClassCastException("Must Implement HomeActivityInterface");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.mInterface.showBottomNav();
    }
}
