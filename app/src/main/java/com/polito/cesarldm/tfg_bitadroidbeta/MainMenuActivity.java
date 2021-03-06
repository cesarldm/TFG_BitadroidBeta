package com.polito.cesarldm.tfg_bitadroidbeta;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.polito.cesarldm.tfg_bitadroidbeta.services.GPSService;
import com.polito.cesarldm.tfg_bitadroidbeta.vo.ChannelConfiguration;

import info.plux.pluxapi.bitalino.BITalinoDescription;
import info.plux.pluxapi.bitalino.BITalinoState;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainMenuActivity extends AppCompatActivity  implements View.OnClickListener{

    //UI Elements
    RelativeLayout rl;
    Button btnStartRec,btnShowRec,btnEcg;
    FloatingActionButton btnScanDev;
    TextView tvDeviceName,tvDeviceMac,tvBattery;
    ImageView ivBattery,ivLogo;
    //Device selected by user
    private BluetoothDevice device;
    private BITalinoState deviceState;
    private BITalinoDescription deviceDesc;
    final int BLUETOOTH_INTENT=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        rl=(RelativeLayout)findViewById(R.id.rl_MMA);
        rl.setOnClickListener(this);
        tvDeviceName=(TextView)findViewById(R.id.tv_MM_name);
        tvDeviceMac=(TextView)findViewById(R.id.tv_MM_mac);
        tvBattery=(TextView)findViewById(R.id.tv_battery_info);
        ivBattery=(ImageView)findViewById(R.id.imageViewBattery);
        ivLogo=(ImageView)findViewById(R.id.imageViewBitaText);
        btnStartRec=(Button) findViewById(R.id.btn_MM_start_recordings);
        btnShowRec=(Button) findViewById(R.id.btn_MM_show_recordings);
        btnScanDev=(FloatingActionButton) findViewById(R.id.btn_MM_scan);
        btnEcg=(Button) findViewById(R.id.btn_MM_ECG);
        btnScanDev.setOnClickListener(this);
        btnStartRec.setOnClickListener(this);
        btnShowRec.setOnClickListener(this);
        btnEcg.setOnClickListener(this);
        runtimeLocationPermissions();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.my_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton) {
            Intent infoIntent=new Intent (this, InfoActivity.class);
            startActivity(infoIntent);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStart(){
        super.onStart();


    }
    @Override
    protected void onResume(){
        super.onResume();
        if(device!=null){
            tvDeviceName.setText(device.getName());
            tvDeviceMac.setText(device.getAddress());
            rl.setBackgroundResource(R.drawable.bitalinofound);
        }else{
            tvDeviceName.setText("Device not selected ");
            tvDeviceMac.setText(null);
            ivLogo.setImageDrawable(null);
            rl.setBackgroundResource(R.drawable.bitalinonotfound);
            ivBattery.setImageDrawable(null);
            tvBattery.setText(null);
        }

    }
    @Override
    protected void onPause(){
        super.onPause();

    }
    @Override
    protected void onStop(){
        super.onStop();

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();

    }
    @Override
    public void onBackPressed(){
       finish();
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_MM_scan:
                Intent bthIntent=new Intent(this,SelectDevicesActivity.class);
                startActivityForResult(bthIntent,BLUETOOTH_INTENT);
                overridePendingTransition(R.animator.enter,R.animator.exit);
                break;
            case R.id.rl_MMA:
                Intent imageIntent=new Intent(this,SelectDevicesActivity.class);
                startActivityForResult(imageIntent,BLUETOOTH_INTENT);
                overridePendingTransition(R.animator.enter,R.animator.exit);
                break;
            case R.id.btn_MM_start_recordings:
                Intent configIntent=new Intent(this,SelectConfigActivity.class);
                configIntent.putExtra("Device",device);
                startActivity(configIntent);
                break;
            case R.id.btn_MM_show_recordings:
                Intent recordIntent=new Intent(this,SelectRecordingActivity.class);
                recordIntent.putExtra("Device",device);
                startActivity(recordIntent);
                break;
            case R.id.btn_MM_ECG:
                Intent ccintent=new Intent (this, CtreateHMConfigActivity.class);
                ccintent.putExtra("Device",device);
                startActivity(ccintent);
                /**Intent hMIntent =new Intent(this,HeartMonitorActivity.class);
                ChannelConfiguration heartMonitor=new ChannelConfiguration("Heart Monitor",new int[]
                        {1},new int[] {1},100,new String[] {"ECG"},new String[] {"ECG"});
                hMIntent.putExtra("Device",device);
                hMIntent.putExtra("Config",heartMonitor);
                startActivity(hMIntent);
                **/
        }

    }
    /**
     * RECEIVES THE BLUETOOTH DEVICE SELECTED BY THE USER
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == SelectDevicesActivity.RESULT_OK){
                ivLogo.setImageResource(R.drawable.bitalogo);
                rl.setBackgroundResource(R.drawable.bitalinofound);
                Bundle b=data.getExtras();
                device=b.getParcelable("result");
                deviceDesc=b.getParcelable("Desc");
                if(data.hasExtra("State")){
                    deviceState=b.getParcelable("State");
                    displayBatteryInfo(deviceState.getBattery());
                }else{
                    ivBattery.setImageResource(R.drawable.ic_battery_unknown);
                }
                //connect

            }
            if (resultCode == SelectDevicesActivity.RESULT_CANCELED) {
                device=null;
                deviceDesc=null;
                toastMessageShort("No Bitalino device selected");
            }
        }
    }

    private void displayBatteryInfo(int battery) {

        if(battery<=500){
           ivBattery.setImageResource(R.drawable.ic_battery_0);
        }else if(battery<=525) {
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        }else if(battery<=562){
            ivBattery.setImageResource(R.drawable.ic_battery_50);
        }else if(battery<=593){
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        }else{
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
        int currentBat=((battery-500)*100)/124;
        if(currentBat<0)currentBat=0;
        if(currentBat>100)currentBat=100;
        tvBattery.setText(currentBat+"%");
    }

    private void runtimeLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Android Marshmallow and above permission check
            if (this.checkSelfPermission(ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.permission_check_dialog_title))
                        .setMessage(getString(R.string.permission_check_dialog_message))
                        .setPositiveButton(getString(R.string.permission_check_dialog_positive_button), null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                requestPermissions(new String[]{ACCESS_FINE_LOCATION}, 101);
                            }
                        });
                builder.show();
            }
        }if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //Android Marshmallow and above permission check
                if (this.checkSelfPermission(ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.permission_check_dialog_title))
                            .setMessage(getString(R.string.permission_check_dialog_message))
                            .setPositiveButton(getString(R.string.permission_check_dialog_positive_button), null)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @TargetApi(Build.VERSION_CODES.M)
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 102);
                                }
                            });
                    builder.show();
                }
            }if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Android Marshmallow and above permission check
            if (this.checkSelfPermission(READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.permission_check_dialog_title_READ))
                        .setMessage(getString(R.string.permission_check_dialog_message_READ))
                        .setPositiveButton(getString(R.string.permission_check_dialog_positive_button), null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 103);
                            }
                        });
                builder.show();
            }
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainMenuActivity", "Fine location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_denied_dialog_title))
                            .setMessage(getString(R.string.permission_denied_dialog_message))
                            .setPositiveButton(getString(R.string.permission_denied_dialog_positive_button), null)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {

                                }
                            });
                    builder.show();
                }
                break;
            case 102:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainMenuActivity", "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_denied_dialog_title))
                            .setMessage(getString(R.string.permission_denied_dialog_message))
                            .setPositiveButton(getString(R.string.permission_denied_dialog_positive_button), null)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {

                                }
                            });
                    builder.show();
                }
                break;
            case 103:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("MainMenuActivity", "external read permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.permission_denied_dialog_title))
                            .setMessage(getString(R.string.permission_denied_dialog_message_READ))
                            .setPositiveButton(getString(R.string.permission_denied_dialog_positive_button), null)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialogInterface) {

                                }
                            });
                    builder.show();
                }
                break;

            default:
                return;
        }
    }
    /**
     * TOAST METHODS
     */
    public void toastMessageShort(String a) {
        Toast.makeText(this, a, Toast.LENGTH_SHORT).show();

    }

    public void toastMessageLong(String a) {
        Toast.makeText(this, a, Toast.LENGTH_LONG).show();

    }
}
