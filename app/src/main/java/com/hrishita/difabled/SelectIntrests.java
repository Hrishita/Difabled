package com.hrishita.difabled;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

import androidx.fragment.app.Fragment;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.chip.Chip;

import java.util.HashSet;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class SelectIntrests extends Fragment {

    FlexboxLayout relativeLayout;
    SparseBooleanArray array = new SparseBooleanArray();
    Set<String> set = new HashSet();
    Button btn_continue;

    public SelectIntrests() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_select_intrests, container, false);
        relativeLayout = v.findViewById(R.id.rl_si);
        btn_continue = v.findViewById(R.id.btn_interest_continue);

        for(int i = 0;i < Constants.categories.length;i++) {
            Chip chip = new Chip(getContext());
            chip.setBackgroundColor(Color.WHITE);
            chip.setText(Constants.categories[i]);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_selector_text));
            chip.setChipBackgroundColor(getResources().getColorStateList(R.color.chip_selector));
            chip.setCheckable(true);
            final int finalI = i;
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        array.put(finalI, true);
                    }
                    else {
                        array.put(finalI, false);
                    }
                }
            });

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 20;
            params.rightMargin = 20;
            params.topMargin = 20;
            params.bottomMargin = 20;
            chip.setLayoutParams(params);
            relativeLayout.addView(chip);
        }

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = ((PreHomeScreenActivity)getContext()).getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                for(int i = 0;i < array.size();i++) {
                    int key = array.keyAt(i);
                    set.add(Constants.categories[key]);
                }
                preferences.edit().putStringSet("interests", set).putBoolean("interest", true).commit();
                gotoHome();
            }
        });

        return v;
    }

    private void gotoHome() {
        Intent i = new Intent(getContext(), HomeActivity.class);
        startActivity(i);
        ((PreHomeScreenActivity)getContext()).finish();
    }

}
