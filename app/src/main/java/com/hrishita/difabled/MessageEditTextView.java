package com.hrishita.difabled;


import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MessageEditTextView extends RelativeLayout
{
    ImageView media;
    EditText text;
    TextView send;

    public MessageEditTextView(Context context) {
        super(context);
        init();
    }

    public MessageEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MessageEditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View v = inflate(getContext(), R.layout.message_et, this);
        text = v.findViewById(R.id.et_met);
        media = v.findViewById(R.id.img_met);
        send = v.findViewById(R.id.send_met);
        send.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getContext() instanceof MessageEditTextViewInterface)
                {
                    ((MessageEditTextViewInterface)getContext()).onMessageSent();
                }
            }
        });
        media.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getContext() instanceof MessageEditTextViewInterface)
                {
                    ((MessageEditTextViewInterface)getContext()).onMediaSelected();
                }
            }
        });
    }

    public Editable getText()
    {
        return text.getText();
    }
    public void clearText()
    {
        text.setText("");
    }
    public interface MessageEditTextViewInterface
    {
        void onMessageSent();
        void onMediaSelected();
    }

}
