package com.example.mohamed.tp;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.provider.Telephony.Carriers.PASSWORD;

/**
 * Created by MOHAMED on 19/12/2017.
 */


public class MainActivity extends Activity {

    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;

    public static final String SMS_URI = "content://sms";

    public static final String ADDRESS = "address";
    public static final String PERSON = "person";
    public static final String DATE = "date";
    public static final String READ = "read";
    public static final String STATUS = "status";
    public static final String TYPE = "type";
    public static final String BODY = "body";
    public static final String SEEN = "seen";
    private static MainActivity ins;

    public static MainActivity  getInstance(){
        return ins;
    }
    public static class Receiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {



            Toast.makeText(context, "Action", Toast.LENGTH_SHORT).show();
            if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
                Bundle bundle = intent.getExtras();
                if(bundle != null){
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for(int i = 0; i<pdus.length; i++)
                        messages[i]= SmsMessage.createFromPdu((byte[])pdus[i]);

                    ContentResolver contentResolver = context.getContentResolver();
                    for(SmsMessage message : messages)
                    {


                        MainActivity .getInstance().displayMessage(message.getMessageBody());


                        putSmsToDatabase( contentResolver, message );
                    }
                }
            }

        }



    }

    public static  class Receiver2 extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    public static class Receiver3 extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    public static  class SmsSendService   extends Service{

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
    public void displayMessage(String text){
         TextView tv;
        tv = (TextView) findViewById(R.id.tv);

        tv.setText(text);
    }
    public String getWord()
    {
        EditText editText = (EditText)findViewById(R.id.edtText);

        return editText.getText().toString();

    }


   public void deleteSMS(Context context, String message, String number) {
        try {

            Uri uriSms = Uri.parse("content://sms/inbox");
            Cursor c = context.getContentResolver().query(uriSms,
                    new String[] { "_id", "thread_id", "address",
                            "person", "date", "body" }, null, null, null);

            if (c != null && c.moveToFirst()) {
                do {
                    long id = c.getLong(0);
                    long threadId = c.getLong(1);
                    String address = c.getString(2);
                    String body = c.getString(5);

                    if (message.equals(body) && address.equals(number)) {

                        context.getContentResolver().delete(
                                Uri.parse("content://sms/" + id), null, null);
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ins = this;
        setContentView(R.layout.main_activity);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            getPermissionToReadSMS();
        }
      SmsManager smsManager = SmsManager.getDefault();

        Receiver receiver = new Receiver();
        registerReceiver(receiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));

        Button b = (Button)findViewById(R.id.bouton);
        Button supprimeSMS = (Button)findViewById(R.id.suppSMS);


        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SmsManager smsManager = SmsManager.getDefault();

                Receiver receiver = new Receiver();
                registerReceiver(receiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
            }
        });

        supprimeSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteSMS(getApplicationContext(),"Thxokk","+33651903191");
            }
        });


    }





    @TargetApi(Build.VERSION_CODES.M)
    public void getPermissionToReadSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_SMS)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_SMS},
                    READ_SMS_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private static void putSmsToDatabase(ContentResolver contentResolver, SmsMessage sms )
    {
        String word;
        // Create SMS row
        ContentValues values = new ContentValues();
        values.put( ADDRESS, sms.getOriginatingAddress() );
        values.put( DATE, sms.getTimestampMillis() );
        values.put( READ, 0);
        values.put( STATUS, sms.getStatus() );
        values.put( TYPE, 1);
        values.put( SEEN, 0);
        word = MainActivity .getInstance().getWord();
        // ajoute le mot clé au message reçu
        values.put(BODY,sms.getMessageBody() + word);

        //On sauvegarde le message dans le téléphone
        contentResolver.insert( Uri.parse( SMS_URI ), values );
    }

}
