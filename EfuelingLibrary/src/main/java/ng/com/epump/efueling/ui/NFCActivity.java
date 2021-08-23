package ng.com.epump.efueling.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import ng.com.epump.efueling.R;
import ng.com.epump.efueling.models.Utility;

public class NFCActivity extends AppCompatActivity {
    NfcAdapter mNfcAdapter;
    IntentFilter[] filters;
    String[][] techList;
    Intent mIntent;
    String cardId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        initNfcAdapter();

        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        ndef.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        ndef.addCategory(Intent.CATEGORY_DEFAULT);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        techDetected.addCategory(Intent.CATEGORY_DEFAULT);
        filters = new IntentFilter[]{tagDetected, techDetected, ndef};
        techList = new String[][] { new String[] { NfcF.class.getName() } };
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

    private void initNfcAdapter() {
        NfcManager nfcManager = (NfcManager)getSystemService(Context.NFC_SERVICE);
        mNfcAdapter = nfcManager.getDefaultAdapter();
        turnNFCOn();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mIntent = intent;
        processIntent();
    }

    private void turnNFCOn(){

        if (mNfcAdapter != null) {
            if (!mNfcAdapter.isEnabled()) {

                AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
                alertbox.setTitle("Info");
                alertbox.setMessage("Turn on NFC");
                alertbox.setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                            startActivity(intent);
                        }
                    }
                });
                alertbox.setNegativeButton("Close", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(1);
                        finish();
                    }
                });
                alertbox.show();

            }
        }
        else{
            AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
            alertbox.setTitle("NFC");
            alertbox.setCancelable(false);
            alertbox.setMessage("Device not capable of NFC communication");
            alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setResult(-1);
                    finish();
                }
            });
            alertbox.show();
        }
    }

    private void processIntent() {
        Tag nfcTag = mIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (nfcTag != null){
            cardId = Utility.bytesToHexString(nfcTag.getId());
            showPinInput();
        }
        else{
            setResult(-1);
            finish();
        }
    }

    private void enableNfcForegroundDispatch() {
        try {
            Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            PendingIntent nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            mNfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, filters, techList);
        } catch (IllegalStateException ex) {
            Log.e("TAG", "Error enabling NFC foreground dispatch", ex);
        }
    }

    private void disableNfcForegroundDispatch() {
        try {
            mNfcAdapter.disableForegroundDispatch(this);
        } catch (IllegalStateException ex) {
            Log.e("TAG", "Error disabling NFC foreground dispatch", ex);
        }
    }

    private void showPinInput(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter PIN");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        input.setTextSize(24);

        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString();
                String pin = Utility.padPassword(password);
                Intent returndata = new Intent();
                returndata.putExtra("Card_ID", cardId);
                returndata.putExtra("PIN", pin);
                setResult(0, returndata);
                finish();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                setResult(1);
                finish();
            }
        });

        builder.show();
    }
}