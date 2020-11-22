  package com.example.app;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

  public class show extends AppCompatActivity {
      private LeDeviceListAdapter mLeDeviceListAdapter;
      /**
       * 搜索BLE终端
       */
      private int seconds = 0;
      private BluetoothAdapter mBluetoothAdapter;
      private TextView Content;
      private TextView Time;
      private TextView dao;
      private boolean mScanning;
      private Handler mHandler;
      private String nowadress = "";
      private Timer timer;
      private TimerTask timerTask;
      ImageView start;
      // Stops scanning after 10 seconds.
      private static final long SCAN_PERIOD = 14400000;
      private boolean run = false;

      //    private final Handler handler = new Handler();
      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_show);
          init();
          run = true;
//        handler.postDelayed(task, 1000);
          mHandler = new Handler();
          // Use this check to determine whether BLE is supported on the device.  Then you can
          // selectively disable BLE-related features.
          if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
              Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
              finish();
          }
          final BluetoothManager bluetoothManager =
                  (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
          mBluetoothAdapter = bluetoothManager.getAdapter();
          // Checks if Bluetooth is supported on the device.
          if (mBluetoothAdapter == null) {
              Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
              finish();
              return;
          }
          //开启蓝牙
          mBluetoothAdapter.enable();
          start.setOnClickListener(this::onClick);
      }

      private void init() {
          countDownTimer.start();
          Content = findViewById(R.id.content);
          Time = findViewById(R.id.time);
          dao = findViewById(R.id.daojishi);
          start=findViewById(R.id.imageView2);
      }

      ////    private final Runnable task = new Runnable() {
//        @Override
//        public void run() {
//            // TODO Auto-generated method stub
//            if (run) {
//                onResume();
//                handler.postDelayed(this, 5000);
//            }
//        }
//    };
      @Override
      protected void onResume() {
          super.onResume();

          // Initializes list view adapter.
          mLeDeviceListAdapter = new LeDeviceListAdapter(this);
//        Collections.sort(mLeDeviceListAdapter.getlist(), new LeDeviceListAdapter.sortById());
//        System.out.println("Asd"+mLeDeviceListAdapter.getlist().size());
//        if(mLeDeviceListAdapter.getlist().size()>0) {
//            System.out.println("Asd"+ mLeDeviceListAdapter.getlist().get(0).bluetoothAddress);
//            Content.setText("你目前所在位置:" + mLeDeviceListAdapter.getlist().get(0).bluetoothAddress);
//        }
          scanLeDevice(true);
      }

      private void scanLeDevice(final boolean enable) {
          if (enable) {
              // Stops scanning after a pre-defined scan period.
              mHandler.postDelayed(new Runnable() {
                  @Override
                  public void run() {
                      mScanning = false;
                      mBluetoothAdapter.stopLeScan(mLeScanCallback);
                      invalidateOptionsMenu();
                  }
              }, SCAN_PERIOD);

              mScanning = true;
              mBluetoothAdapter.startLeScan(mLeScanCallback);
          } else {
              mScanning = false;
              mBluetoothAdapter.stopLeScan(mLeScanCallback);
          }
          invalidateOptionsMenu();
      }

      // Device scan callback.
      private BluetoothAdapter.LeScanCallback mLeScanCallback =
              new BluetoothAdapter.LeScanCallback() {//对设备列表进行刷新的接口

                  @Override
                  public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {//每扫描到一个设备刷新数据

                      final iBeaconClass.iBeacon ibeacon = iBeaconClass.fromScanData(device, rssi, scanRecord);
                      runOnUiThread(() -> {
                          mLeDeviceListAdapter.addDevice(ibeacon);
                          Collections.sort(mLeDeviceListAdapter.getlist(), new LeDeviceListAdapter.sortById());
                          if (mLeDeviceListAdapter.getlist().size() > 0) {
                              if (!mLeDeviceListAdapter.getlist().get(0).bluetoothAddress.equals(nowadress)) {//顶部蓝牙设备变化
                                  System.out.println(nowadress + "asd" + mLeDeviceListAdapter.getlist().get(0).bluetoothAddress);
                                  starttime();//记录停留时间
                                  nowadress = mLeDeviceListAdapter.getlist().get(0).bluetoothAddress;
                              }
                              Content.setText("你目前所在位置信标ID为:" + nowadress.substring(12, 14) + nowadress.substring(15, 17));
                          }
                          mLeDeviceListAdapter.notifyDataSetChanged();
                      });
                  }
              };

      private void starttime() {
          seconds = 0;
          if (timer != null) {
              timer.cancel();
              timer = null;
          }
          if (timerTask != null) {
              timerTask = null;
          }
          timerTask = new TimerTask() {
              @Override
              public void run() {
                  Message msg = new Message();
                  msg.what = 0;
                  handler.sendMessage(msg);
              }
          };
          timer = new Timer();
          timer.schedule(timerTask, 0, 1000);
      }

      //这是接收回来处理的消息
      @SuppressLint("HandlerLeak")
      private Handler handler = new Handler() {
          @SuppressLint("SetTextI18n")
          public void handleMessage(Message msg) {
              seconds++;
              Time.setText("停留时间为:" + formatTime(seconds));
          }

      };

      @SuppressLint("DefaultLocale")
      private String formatTime(long seconds) {
          return String.format(" %02d时%02d分%02d秒", seconds / 3600, seconds % 3600 / 60, seconds % 60);
      }

      /**
       * CountDownTimer 实现倒计时
       */
      private CountDownTimer countDownTimer = new CountDownTimer(4 * 60 * 60 * 1000, 1000) {
          @Override
          public void onTick(long millisUntilFinished) {
              String value = formatTime(millisUntilFinished/1000);
              dao.setText(value);
          }

          @Override
          public void onFinish() {
              Intent intent=new Intent(show.this, start.class);
              startActivity(intent);
          }
      };
      public void onClick(View v) {
          switch (v.getId()) {
              case R.id.imageView2:
                  // 此处添加事件
                  countDownTimer.cancel();
                  Intent intent=new Intent(show.this, start.class);
                  startActivity(intent);
                  break;
              default:
                  break;
          }
      }
  }