package com.hrishita.difabled;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;

/**
 * A simple {@link Fragment} subclass.
 * Use the {//@link PhoneAuth#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhoneAuthFragment extends Fragment {

    String strCategory;
    TextInputEditText et_phoneNumber;
    Button btn_verifyNumber;
   /* private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;*/

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  //  private static final String ARG_PARAM1 = "param1";
  //  private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
   /* private String mParam1;
    private String mParam2;*/

    public PhoneAuthFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * //@param param1 Parameter 1.
     * //@param param2 Parameter 2.
     * @return A new instance of fragment PhoneAuth.
     */
    // TODO: Rename and change types and number of parameters
    public static PhoneAuthFragment newInstance(String param1, String param2) {
        PhoneAuthFragment fragment = new PhoneAuthFragment();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        return fragment;
    }

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
        View v = inflater.inflate(R.layout.fragment_phone_auth, container, false);
        et_phoneNumber = v.findViewById(R.id.phone_number_ti);
        btn_verifyNumber = v.findViewById(R.id.btn_verify_phone);
        btn_verifyNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoOtpScreen();
//                updateUI(STATE_INITIALIZED);
//                PhoneAuthProvider.getInstance().verifyPhoneNumber(et_phoneNumber.getText().toString(),60, TimeUnit.SECONDS,getContext(), mCallBacks);
            }
        });


        return v;
    }
    private void gotoOtpScreen() {
        OtpFillFragment otpFragment = new OtpFillFragment();
        Bundle bundle = new Bundle();
        System.out.println(getArguments().getString("category") + "");

        if (getArguments() != null) {
            bundle.putString("category", getArguments().getString("category"));
            bundle.putString("phone",et_phoneNumber.getText().toString());

        }
        otpFragment.setArguments(bundle);
        ((PreHomeScreenActivity) getContext())
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragcontainer, otpFragment, "otp verify")
                .addToBackStack(null)
                .commit();
    }
}