package com.example.sunion_nfc_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    private static NfcAdapter mNfcAdapter;
    private static String LOG_TAG = "NFCDEMO";

    private TextView textView;
    private String DEBUG_TAG = "SUNION-DEBUG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(DEBUG_TAG,"onCreate");
        setContentView(R.layout.activity_main);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        textView = (TextView) findViewById(R.id.textView);
        Log.i(DEBUG_TAG,"onCreate finish");
        Log.i(DEBUG_TAG,"onCreate getIntent");
        Intent newIntent = getIntent();
        doAction(newIntent);
        Log.i(DEBUG_TAG,"onCreate getIntent finish");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            Intent intent = new Intent(this,
                    this.getClass()).addFlags(
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, intent, PendingIntent.FLAG_MUTABLE);
            try {
                mNfcAdapter.enableForegroundDispatch(
                        this, pendingIntent, null, new String[][]{
                                new String[]{NfcA.class.getName()}});
            } catch (IllegalStateException ex) {
                Log.d(LOG_TAG, "Error: Could not enable the NFC foreground" +
                        "dispatch system. The activity was not in foreground.");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            try {
                mNfcAdapter.disableForegroundDispatch(this);
            } catch (IllegalStateException ex) {
                Log.d(LOG_TAG, "Error: Could not disable the NFC foreground" +
                        "dispatch system. The activity was not in foreground.");
            }
        }
    }

    private void receiveTextAppend(String s){
        SpannableStringBuilder spn = new SpannableStringBuilder(s+'\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.append(spn);
    }

    private void doAction(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Log.i(DEBUG_TAG,"ACTION_NDEF_DISCOVERED");
            Parcelable[] rawMessages =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                    receiveTextAppend("ACTION_NDEF_DISCOVERED:"+messages[i].toString());
                    Log.i(DEBUG_TAG,messages[i].toString());
                    NdefRecord[] records = messages[i].getRecords();
                    for (int j = 0; j < records.length; j++) {
                        receiveTextAppend("Record Type:"+new String(records[j].getType(), StandardCharsets.UTF_8) + "Data:"+new String(records[j].getPayload(), StandardCharsets.UTF_8));
                    }
                }
                // Process the messages array.
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Log.i(DEBUG_TAG,"ACTION_TECH_DISCOVERED");
            Parcelable[] rawMessages =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];
                    receiveTextAppend("ACTION_TECH_DISCOVERED:"+messages[i].toString());
                    Log.i(DEBUG_TAG,messages[i].toString());
                    NdefRecord[] records = messages[i].getRecords();
                    for (int j = 0; j < records.length; j++) {
                        // RTD_TEXT = T = mimeType="text/plain"
                        receiveTextAppend("Record Type:"+new String(records[j].getType(), StandardCharsets.UTF_8) + "Data:"+new String(records[j].getPayload(), StandardCharsets.UTF_8));
                    }
                }
                // Process the messages array.
            }
        } else {
            // skip android.intent.action.MAIN or other not support tag
            Log.i(DEBUG_TAG,"A New Intent Come not ACTION_TECH_DISCOVERED or ACTION_NDEF_DISCOVERED is " + intent.getAction());
            receiveTextAppend("A New Intent Come");
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(DEBUG_TAG,"onNewIntent");
        doAction(intent);
        Log.i(DEBUG_TAG,"onNewIntent finish");
    }
}