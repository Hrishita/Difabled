package com.hrishita.difabled;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SelectContactActivity extends AppCompatActivity {
//    ListView listView;
//    ArrayList<FriendsModel> arrayList = new ArrayList<>();
//    ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_select_contact);
//
//        initViews();
//
//        refreshPage();

    }
//
//    private void refreshPage() {
//        fetchContacts();
//    }
//
//    private void fetchContacts() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
//            // requesting to the user for permission.
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
//        } else {
//            //if app already has permission this block will execute.
//            readContacts();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(requestCode == 100 && permissions[0].equals(Manifest.permission.READ_CONTACTS) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            readContacts();
//        }
//    }
//
//    private void readContacts() {
//        ContentResolver contentResolver=getContentResolver();
//        Cursor cursor=contentResolver.query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
//        if (cursor.moveToFirst()){
//            do {
//                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Data._ID));
//                if(cursor.getString(cursor.getColumnIndex(ContactsContract.Data.HAS_PHONE_NUMBER)).equalsIgnoreCase("1")) {
//                    Cursor c = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
//                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
//                    if(c.moveToNext()) {
////                        FriendsModel model = new FriendsModel(cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)), c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
////                        arrayList.add(model);
//                    }
//                    c.close();
//                    cursor.close();
//                }
//
//            }while (cursor.moveToNext());
//        }
//    }
//
//
//    private void initViews() {
//        listView = findViewById(R.id.contacts_list);
//    }
}
