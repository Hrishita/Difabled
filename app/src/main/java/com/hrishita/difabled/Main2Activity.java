package com.hrishita.difabled;

import androidx.appcompat.app.AppCompatActivity;

public class Main2Activity extends AppCompatActivity {
//    RecyclerView recyclerView;
//    Button btnSend, btnConnect, btnDisconnect;
//    EditText et_username, et_message, et_my_username;
//    MyXmpp myXmpp = new MyXmpp();
//    private ArrayList<MessagesData> arrayList = new ArrayList<>();
//    private Adapter adapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main2);
//        recyclerView = findViewById(R.id.chat_list);
//        btnSend = findViewById(R.id.btn_send_main2);
//        et_username = findViewById(R.id.username_main2);
//        et_message = findViewById(R.id.message_main2);
//        et_my_username = findViewById(R.id.myusername_main2);
//        btnConnect = findViewById(R.id.btn_connect_main2);
//        btnDisconnect = findViewById(R.id.disconnect_main2);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
//        recyclerView.setAdapter(adapter);
//
//        btnSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String username = et_username.getText().toString();
//                String message = et_message.getText().toString();
//                myXmpp.sendMsg(username, message);
//            }
//        });
//
//        btnConnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String myname = et_my_username.getText().toString();
//                if(!myname.isEmpty()) {
//                    myXmpp.init(myname, "123Hello", new MyXmpp.MyXmpp2Main2() {
//                        @Override
//                        public void messageRecvd(String message) {
//                            final MessagesData data = new MessagesData("recvd", message);
//
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    arrayList.add(data);
//                                    adapter.notifyDataSetChanged();
//                                }
//                            });
//
//                        }
//
//                        @Override
//                        public void messageSent(String message) {
//                            Toast.makeText(Main2Activity.this, "Sent", Toast.LENGTH_SHORT).show();
//                        }
//                    },getApplicationContext());
//                    myXmpp.connectConnection();
//                }
//            }
//        });
//
//        btnDisconnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                myXmpp.disconnect();
//            }
//        });
//
//    }

}
