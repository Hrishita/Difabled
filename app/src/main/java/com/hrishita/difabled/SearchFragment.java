package com.hrishita.difabled;


import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {
    HomeActivityInterface mInterface;
    Context context;

    RecyclerView recyclerView;
    List<User> arrayList = new ArrayList<>();
    com.hrishita.difabled.SearchFriendsAdapter adapter;
    ImageView back;
    EditText searchBox;
    FirebaseFirestore db;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        db= FirebaseFirestore.getInstance();
        searchBox = v.findViewById(R.id.search_edit_text);
        back = v.findViewById(R.id.search_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity)getContext())
                        .getSupportFragmentManager()
                        .popBackStack();
            }
        });

        recyclerView = v.findViewById(R.id.search_friends_rcv);

        adapter = new com.hrishita.difabled.SearchFriendsAdapter(getContext(), arrayList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                arrayList.clear();
                db.collection("users")
                        .whereEqualTo("name", s.toString())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful())
                                {
                                    System.out.println("Size : " + task.getResult().size());
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        System.out.println(document.getData().toString());
                                        User u = new User();
                                        String phone = document.getId();
                                        String name = (String) document.get("name");
                                        String profile = (String) document.get("profile");
                                        String status = (String) document.get("status");
                                        String email = (String) document.get("handler");
                                        String category = (String) document.get("category");
                                        System.out.println(profile);
                                        u.setPhone(phone);
                                        u.setEmail(email);
                                        u.setCategory(category);
                                        u.setName(name);
                                        u.setProfile_link(profile);
                                        u.setStatus(status);
                                        arrayList.add(u);
                                    }
                                    if(getContext() == null)
                                         return;
                                    ((HomeActivity)getContext())
                                            .runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    adapter.notifyDataSetChanged();
                                                }
                                            });
                                }
                            }
                        });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return v;
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof HomeActivityInterface)
        {
            mInterface = (HomeActivityInterface) context;
            mInterface.hideAppBar();
        }
        else
        {
            throw new ClassCastException("Must Implement Interface");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mInterface.showBottomNav();
        mInterface.showAppBar();
    }
}
