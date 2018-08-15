package com.example.joney.demo20;

/**
 * Created by joney on 2017/12/18.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothConnect extends AppCompatActivity {

    private Switch switchonoff;      //蓝牙开关

    private Button buttonsearch;     //设备搜索按钮

    static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private UUID uuid;

    private BluetoothAdapter bluetoothAdapter;

    public static BluetoothSocket bluetoothsocket;

    private static BluetoothSocket socket;

    private static AcceptThread acceptThread;

    private Handler handler;

    private ListView listView;

    private List<String> listDevice = new ArrayList<>();    //设备列表

    private ArrayAdapter<String> adapterDevice;

    private Toolbar btfind;         //顶部工具栏

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);
        //toolbar栏初始设置
        btfind = (Toolbar) findViewById(R.id.btfind);
        setSupportActionBar(btfind);
        btfind.setNavigationIcon(R.drawable.ic_arrowback);
        //返回监听
        btfind.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();//返回
            }
        });

        findViewById();
        onclick();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        InitBlueTooth();
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.findmenu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.find_item:
                break;
            case R.id.exit_item:
                finish();
                break;
            default:
        }
        return true;
    }

    public void findViewById() {
        buttonsearch = (Button)findViewById(R.id.search);
        switchonoff = (Switch)findViewById(R.id.switchOpenAndClose);
        listView = (ListView)findViewById(R.id.listViewShow);
        adapterDevice = new ArrayAdapter<>(BluetoothConnect.this,android.R.layout.simple_list_item_1,listDevice);
        listView.setAdapter(adapterDevice);
        listView.setOnItemClickListener(new ItemClickEvent());     //设置ListView 的单击监听
    }
    public void onclick() {
        buttonsearch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF){
                    Toast.makeText(BluetoothConnect.this,"请您先打开蓝牙开关",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(BluetoothConnect.this, "正在搜索蓝牙设备", Toast.LENGTH_SHORT).show();
                    bluetoothAdapter.startDiscovery();
                }

            }
        });
        /**
         * 蓝牙开关
         */
        switchonoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                        return;
                    } else if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                        bluetoothAdapter.enable();
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                            Toast.makeText(BluetoothConnect.this, "蓝牙已打开", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            acceptThread = new AcceptThread();
                            handler = new Handler() {
                                public void handle(Message message) {
                                    switch (message.what) {
                                        case 0:
                                            acceptThread.start();
                                    }
                                }
                            };
                        } catch (Exception e) {
                            Toast.makeText(BluetoothConnect.this, "服务监听出错", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (isChecked == false) {
                    bluetoothAdapter.disable();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF)
                        Toast.makeText(BluetoothConnect.this, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    class AcceptThread extends Thread
    {
        private final BluetoothServerSocket serverSocket;//蓝牙服务套接口
        public AcceptThread()
        {
            BluetoothServerSocket BTServerSocket = null;
            try {
                Method listenMethod = bluetoothAdapter.getClass().getMethod("listenUsingRfcommOn",new  Class[]{int.class});
                BTServerSocket = (BluetoothServerSocket)listenMethod.invoke(bluetoothAdapter,Integer.valueOf(1));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            serverSocket = BTServerSocket;
        }
        public void run()
        {
            while (true)
            {
                try {
                    socket = serverSocket.accept();
                    Log.e("MainActivity","socket = serverSocket.accept();错误");
                } catch (IOException e) {
                    break;
                }
                if(socket != null)
                {
                    bluetoothsocket = socket;
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            Message message = new Message();
            message.what = 0;
            handler.sendMessage(message);

        }
        public void cancel()
        {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e("MainActivity","注销错误");
            }
        }
    }

    class ItemClickEvent implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String str = listDevice.get(position);
            String[] values = str.split("\\|");
            String address = values[1];
            Log.i("address",values[1]);
            uuid = UUID.fromString(SPP_UUID);
            Log.i("uuid",uuid.toString());

            BluetoothDevice BtDev = bluetoothAdapter.getRemoteDevice(address);//getRemoteDevice获取BluetoothDevice对象从给定的蓝牙硬件地址
            Log.i("获取的蓝牙硬件对象","   "+BtDev);
            Method method;
            try {
                method = BtDev.getClass().getMethod("createRfcommSocket",new Class[]{int.class});
                bluetoothsocket = (BluetoothSocket) method.invoke(BtDev,Integer.valueOf(1));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            bluetoothAdapter.cancelDiscovery();//对蓝牙进行信号注销
            try {
                String data = "OK";
                bluetoothsocket.connect();//蓝牙配对
                Toast.makeText(BluetoothConnect.this,"连接",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setClass(BluetoothConnect.this, MainActivity.class);
                intent.putExtra("confirm",data);
                startActivity(intent);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(BluetoothConnect.this,"连接失败",Toast.LENGTH_SHORT).show();
            }

        }
    }


    public void InitBlueTooth() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//广播操作：指示远程设备上的键状态的改变
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//广播操作：指示本地适配器的蓝牙扫描模式已经改变。
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//广播操作：当地蓝牙适配器的状态已更改。
        registerReceiver(serachDevices,intentFilter);
    }

    private BroadcastReceiver serachDevices = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            Object[] listName = bundle.keySet().toArray();

            //显示收到的消息及细节
            for(int i = 0;i < listName.length;i++)
            {
                String keyName = listName[i].toString();
                Log.i(keyName,String.valueOf(bundle.get(keyName)));
            }
            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                //Toast.makeText(BluetoothConnect.this,"已进入",Toast.LENGTH_SHORT).show();
                BluetoothDevice BTDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String str = BTDevice.getName()+"|"+BTDevice.getAddress();
                if(listDevice.indexOf(str) == -1)//indexOf检索字符串，如果为null返回-1
                {
                    listDevice.add(str);
                }
                adapterDevice.notifyDataSetChanged();//每次改变刷新一下自己
            }

        }
    };

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(serachDevices);
        super.onDestroy();
        //android.os.Process.killProcess(Process.myPid());
        if (acceptThread != null) {
            acceptThread.cancel();
            acceptThread.destroy();
            acceptThread = null;
        }
    }

}
