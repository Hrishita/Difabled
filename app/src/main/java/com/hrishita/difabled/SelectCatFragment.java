package com.hrishita.difabled;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {//@link SelectCategory#newInstance} factory method to
 * create an instance of this fragment.
 */

public class SelectCatFragment extends Fragment {
    ImageView mutedeaf, normal,blind;
    String catConst;
    Button btn_continue;
    SharedPreferences prefs;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
   /* private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;*/

    public SelectCatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * //@param param1 Parameter 1.
     * //@param param2 Parameter 2.
     * @return A new instance of fragment SelectCategory.
     */
    // TODO: Rename and change types and number of parameters
   // public static SelectCategory newInstance(String param1, String param2) {
        //SelectCategory fragment = new SelectCategory();
       /* Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        //return fragment;
   // }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v=inflater.inflate(R.layout.fragment_select_cat,container,false);
        mutedeaf=v.findViewById(R.id.img_deafmute);
        blind=v.findViewById(R.id.img_blind);
        normal=v.findViewById(R.id.img_normal);
        btn_continue = v.findViewById(R.id.btn_sc_continue);

        prefs = ((PreHomeScreenActivity)getContext()).getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        mutedeaf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catConst=Constants.CATEGORY_MUTE;
                mutedeaf.setImageResource(R.drawable.deaflogo);
                normal.setImageResource(R.drawable.transnormlogo);
                blind.setImageResource(R.drawable.transblind);
            }
        });

        blind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*catConst=Constants.CATEGORY_BLIND;*/
                mutedeaf.setImageResource(R.drawable.transdeaflogo);
                normal.setImageResource(R.drawable.transnormlogo);
                blind.setImageResource(R.drawable.blindactive);
            }
        });
        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catConst=Constants.CATEGORY_NORMAL;

                mutedeaf.setImageResource(R.drawable.transdeaflogo);
                normal.setImageResource(R.drawable.normalactive);
                blind.setImageResource(R.drawable.transblind);
            }
        });

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("category", catConst);
                System.out.println(catConst);
                /*gotoPhoneAuthFragment(bundle);*/

            }
        });
        return  v;
    }

    /*private void gotoPhoneAuthFragment(Bundle bundle) {
        PhoneAuthFragment frag = PhoneAuthFragment.newInstance("","");
        frag.setArguments(bundle);
        System.out.println(catConst);

        ((PreHomeScreenActivity) getContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragcontainer,frag, "select category")
                .commit();
    }*/
}