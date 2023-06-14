package com.example.nfc_cardreader_4;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView serialNumberTextView;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serialNumberTextView = findViewById(R.id.serial_number_text_view);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableNfcForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableNfcForegroundDispatch();
    }

    private void enableNfcForegroundDispatch() {
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    private void disableNfcForegroundDispatch() {
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNfcIntent(intent);
    }

    private void handleNfcIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            if (tag != null) {
                String serialNumber = readSerialNumber(tag);
                displaySerialNumber(serialNumber);
            }
        }
    }

    private String readSerialNumber(Tag tag) {
        NfcA nfcA = NfcA.get(tag);

        try {
            nfcA.connect();
            byte[] command = {0x30, 0x00};
            byte[] response = nfcA.transceive(command);

            if (response != null && response.length >= 4) {
                // Extract the serial number from the response
                byte[] serialNumberBytes = new byte[4];
                System.arraycopy(response, 0, serialNumberBytes, 0, 4);
                return bytesToHexString(serialNumberBytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (nfcA != null)
                    nfcA.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02X", bytes[i]));
            if (i < bytes.length - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    private void displaySerialNumber(String serialNumber) {
        if (serialNumber != null) {
            serialNumberTextView.setText(serialNumber);
        } else {
            serialNumberTextView.setText("Serial number not found");
        }
    }

    private String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}

