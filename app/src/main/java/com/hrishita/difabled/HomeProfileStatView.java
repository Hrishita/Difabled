package com.hrishita.difabled;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class HomeProfileStatView extends LinearLayout
{
    TextView txtNumber, txtName;

    public HomeProfileStatView(Context context) {
        super(context);
        init(null);
    }

    public HomeProfileStatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public HomeProfileStatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public HomeProfileStatView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }
    public void setTxtNumber(int number)
    {
        txtNumber.setText((number + ""));
    }
    public void setTxtName(String name)
    {
        txtName.setText(name);
    }

    private void init(AttributeSet attrs)
    {
        if(attrs!=null)
        {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.HomeProfileStatView);
            String amount = ta.getString(R.styleable.HomeProfileStatView_stat_number);
            String name = ta.getString(R.styleable.HomeProfileStatView_stat_name);
             LayoutInflater
                    .from(getContext())
                    .inflate(R.layout.home_profile_stat, this, true);

            txtNumber = (TextView) getChildAt(0);
            txtName = (TextView) getChildAt(1);

            setOrientation(VERTICAL);
            setElevation(40f);

            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            txtName.setLayoutParams(layoutParams);
            txtName.setTextAlignment(TEXT_ALIGNMENT_CENTER);
            txtNumber.setTextAlignment(TEXT_ALIGNMENT_CENTER);

            txtNumber.setLayoutParams(layoutParams);

            txtName.setText(name);
            txtNumber.setText(amount);

        }
    }

}
