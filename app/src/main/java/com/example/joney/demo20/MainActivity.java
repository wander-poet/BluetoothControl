package com.example.joney.demo20;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joney.demo20.entity.DaoMaster;
import com.example.joney.demo20.entity.DaoSession;
import com.example.joney.demo20.entity.Dataset;
import com.example.joney.demo20.entity.DatasetDao;
import com.example.joney.demo20.utils.PopupMenuUtil;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.example.joney.demo20.BluetoothConnect.bluetoothsocket;
import static com.example.joney.demo20.R.id.lineChart;
import static com.example.joney.demo20.R.id.torquenumber;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private AmountView mAmountView;
    private EditText edPTSelect;
    private EditText cursorvisible;
    private PopupWindow popupWindow;
    private NumberPicker numberPicker,numberPicker0;     //两个数字选择器
    private View PTSelecting_view;
    private Button submit_PTSelect; //定时时间选择

    private int minute = 0;         //定时分
    private int sec = 0;            //定时秒
    private int speednumber;        //转速
    private Button Run;             //运行按钮
    private Button Reset;           //复位按键
    private Spinner Spindle;        //转子号选择键
    private Spinner Ptselect;       //定时打印切换
    private int torque;             //扭矩值
    private int temperature;        //温度值
    private int viscosity;          //粘度值
    private int vismax = 0;           //粘度值最大值 用于Y轴坐标的动态设置
    private int spindle;            //转子号
    private float convert;          //数值类型转换
    private int connectflag;        //蓝牙socket连接标志位
    private int compar;
    private LineChart mLineChart;
    private LineData lineData;
    private LineDataSet lineDataSet;
    private YAxis leftYAxis;
    private YAxis rightYAxis;
    private LimitLine limitLine;
    private Spinner swchart;
    private int swposition;
    private TextView torquedis;
    private TextView temperaturedis;
    private TextView viscositydis;
    private android.os.Handler handler;
    private String encodeType = "utf-8";
    private boolean isRecording = false;
    private OutputStream outputStream = null;
    private ConnectedThread connectedThread;

    //采样部分
    private boolean startSampling;
    private boolean pauseSampling;
    private boolean stopSampling;

    //数据库测试  基于GreenDao ORM
    private DaoMaster  daoMaster;
    private DaoSession daoSession;
    private DatasetDao dataDao;
    private String  dataname;
    private ImageView ivImg;
    private RelativeLayout rlClick;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);        //工具栏
        setSupportActionBar(toolbar);

        Run = (Button) findViewById(R.id.running);                     //运行按键
        Reset = (Button) findViewById(R.id.reset);                     //复位按键
        Spindle = (Spinner) findViewById(R.id.spinner);                //转子选择
        Ptselect = (Spinner) findViewById(R.id.ptselect);              //定时打印时间切换
        torquedis = (TextView) findViewById(torquenumber);             //扭矩显示
        viscositydis = (TextView) findViewById(R.id.visnumber);        //粘度显示
        temperaturedis = (TextView) findViewById(R.id.tempnumber);     //温度显示
        swchart = (Spinner) findViewById(R.id.switchchart);            //图表切换
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        mLineChart = (LineChart) findViewById(lineChart);              //图表
        rlClick = (RelativeLayout) findViewById(R.id.rl_click);
        ivImg = (ImageView) findViewById(R.id.iv_img);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        //数字选择器初始化
        initNumberPicker();
        //LineChart初始化 默认为扭矩
        initLineChart0();
        initLineDataSet("扭矩",R.color.numberpicker_line2);
        //判断蓝牙socket是否连接
        if (BluetoothConnect.bluetoothsocket != null)
        {
            connectflag = 1;
            StartThread();
            Toast.makeText(this,"蓝牙设备已连接",Toast.LENGTH_SHORT).show();
        }
        if (BluetoothConnect.bluetoothsocket == null)
        {
            connectflag = 0;
            Toast.makeText(this,"蓝牙设备尚未连接",Toast.LENGTH_SHORT).show();
        }
        onclick();
        //导航栏
        navView.setCheckedItem(R.id.nav_home);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.nav_home:                      //返回主页
                        mDrawerLayout.closeDrawers();
                        break;
                    case R.id.nav_bluetooth:                 //蓝牙连接界面
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, BluetoothConnect.class);
                        startActivity(intent);

                        break;
                    case R.id.nav_history:                   //历史记录界面
                        Intent intent1 = new Intent();
                        intent1.setClass(MainActivity.this,History.class);
                        startActivity(intent1);
                        break;
                    case R.id.nav_about:                     //关于
                        Intent intent2 = new Intent();
                        intent2.setClass(MainActivity.this,About.class);
                        startActivity(intent2);
                        break;
                    default:
                }
                return true;
            }
        });

        /**
         * 转子号调整监听
         */
        Spindle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (connectflag == 1) {
                    if (position == 0) {
                        sendMessage("30xxxx");
                    }
                    if (position == 1) {
                        sendMessage("31xxxx");
                    }
                    if (position == 2) {
                        sendMessage("32xxxx");
                    }
                    if (position == 3) {
                        sendMessage("33xxxx");
                    }
                    if (position == 4) {
                        sendMessage("34xxxx");
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /**
         * 定时打印
         */
        Ptselect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (connectflag == 1) {
                    if (position == 0) {
                        sendMessage("6 1xxx");
                    }
                    if (position == 1) {
                        sendMessage("6 1xxx");
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        /**
         * 三种图标之间的切换显示监听
         */
        swchart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0) {
                    swposition = position;
                    initLineChart0();
                    initLineDataSet("扭矩",R.color.numberpicker_line2);
                }
                if(position == 1) {
                    swposition = position;
                    leftYAxis.removeLimitLine(limitLine);
                    initLineChart1();
                    initLineDataSet("温度",Color.RED);
                }
                if(position == 2) {
                    swposition = position;
                    leftYAxis.removeLimitLine(limitLine);
                    initLineChart2();
                    initLineDataSet("粘度",Color.BLUE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        edPTSelect = (EditText) findViewById(R.id.edPTSelect);      //显示定时打印时间编辑框
        edPTSelect.setText( minute + " 分 " + sec + " 秒 ");
        mAmountView = (AmountView) findViewById(R.id.amount_view);  //转速调节框
        mAmountView.setmaxnumber(100);                              //设定调节框最大值
        cursorvisible = (EditText) findViewById(R.id.etAmount);     //转速调节中间的文本框

        /**
         * 转速调整框光标点击显示
         */
        cursorvisible.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    cursorvisible.setCursorVisible(true);          // 再次点击显示光标
                }
                return false;
            }
        });

        /**
         * 转速调节监听
         */
        mAmountView.setOnAmountChangeListener(new AmountView.OnAmountChangeListener() {
            @Override
            public void onAmountChange(View view, int amount) {
                Toast.makeText(getApplicationContext(), "Amount " + amount, Toast.LENGTH_SHORT).show();
                speednumber = mAmountView.getAmount();
                if (speednumber <= 100 && speednumber > 0) {
                    if (connectflag == 1) {

                        if (speednumber == 100)
                            sendMessage("2100xx");
                        else if (speednumber <= 9 && speednumber >= 0) {
                            sendMessage("200" + speednumber + "" + "xx");
                        } else if (speednumber <= 99 && speednumber >= 10) {
                            sendMessage("20" + speednumber + "" + "xx");
                        }
                    }
                }
            }
        });

        /**
         * 选择定时打印时间监听
         */
        edPTSelect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // 设置初始值
                numberPicker.setValue(sec);
                numberPicker0.setValue(minute);

                // 强制隐藏键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                // 填充布局并设置弹出窗体的宽,高
                popupWindow = new PopupWindow(PTSelecting_view,
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                // 设置弹出窗体可点击
                popupWindow.setFocusable(true);
                // 设置弹出窗体动画效果
                popupWindow.setAnimationStyle(R.style.AnimBottom);
                // 触屏位置如果在选择框外面则销毁弹出框
                popupWindow.setOutsideTouchable(true);
                // 设置弹出窗体的背景
                popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                popupWindow.showAtLocation(PTSelecting_view,
                        Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

                // 设置背景透明度
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                lp.alpha = 0.5f;
                getWindow().setAttributes(lp);

                // 添加窗口关闭事件
                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

                    @Override
                    public void onDismiss() {
                        WindowManager.LayoutParams lp = getWindow().getAttributes();
                        lp.alpha = 1f;
                        getWindow().setAttributes(lp);
                    }

                });
            }

        });

        /**
         * 确定所选择的为定时还是打印
         */
        submit_PTSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                minute  = sec*10+minute*20;
                minute = numberPicker0.getValue();                  //获取设定的分钟
                sec = numberPicker.getValue();                      //获取设定的秒数
                edPTSelect.setText(minute + " 分 "+ sec + " 秒");   //对Edittext设置文本显示
                popupWindow.dismiss();                              //关闭选择窗口
                if (( minute>=0 && minute<=9 )&&( sec>=0 && sec<=9 ))
                    sendMessage("4x0"+minute+""+"0"+sec+"");
                else if (( minute>=10 && minute<=59 )&&( sec>=0 && sec<=9 ))
                    sendMessage("4x"+minute+""+"0"+sec+"");
                else if (( minute>=0 && minute<=9 )&&( sec>=10 && sec<=59 ))
                    sendMessage("4x0"+minute+""+sec+"");
                else if (( minute>=10 && minute<=59 )&&( sec>=10 && sec<=59 ))
                    sendMessage("4x"+minute+sec);
            }
        });

        Handler wenTimeHandler = new Handler() {                    //温度定时更新
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 0) {
                    temperaturedis.setText(temperature+" ℃");
                    sendEmptyMessageDelayed(0,200);
                }
            }
        };
        Handler nianTimeHandler = new Handler() {                   //粘度定时更新
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 0) {
                    viscositydis.setText(viscosity+" mPa·s");
                    sendEmptyMessageDelayed(0,200);
                }
            }
        };
        Handler niuTimeHandler = new Handler() {                    //扭矩定时更新
            public void handleMessage(android.os.Message msg) {
                if (msg.what == 0) {
                    torquedis.setText(torque+" %");
                    sendEmptyMessageDelayed(0,200);
                }
            }
        };
//        Handler zhuanziTimeHandler = new Handler() {                    //转子定时更新
//            public void handleMessage(android.os.Message msg) {
//                if (msg.what == 0) {
//
//                    sendEmptyMessageDelayed(0,200);
//                }
//            }
//        };
//        Handler speedTimeHandler = new Handler() {                    //转速定时更新
//            public void handleMessage(android.os.Message msg) {
//                if (msg.what == 0) {
//
//                    sendEmptyMessageDelayed(0,200);
//                }
//            }
//        };

            wenTimeHandler.sendEmptyMessageDelayed(0,200);
            nianTimeHandler.sendEmptyMessageDelayed(0,200);
            niuTimeHandler.sendEmptyMessageDelayed(0,200);
//        zhuanziTimeHandler.sendEmptyMessageDelayed(0,200);
//        speedTimeHandler.sendEmptyMessageDelayed(0,200);




        //在类里声明一个Handler
//        Handler mTimeHandler = new Handler() {
//            public void handleMessage(android.os.Message msg) {
//                if (msg.what ==  0) {
//                    torquenumber.setText(TimeHelper.formatter_m.format(new Date(System.currentTimeMillis())));
//                    sendEmptyMessageDelayed(0, 1000);
//                }
//            }
//        };
        getDataDao();

        rlClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenuUtil.getInstance()._show(context, ivImg);
            }
        });

    }

    /**
     * 当popupWindow 正在展示的时候 按下返回键 关闭popupWindow 否则关闭activity
     */
    @Override
    public void onBackPressed() {
        if (PopupMenuUtil.getInstance()._isShowing()) {
            PopupMenuUtil.getInstance()._rlClickAction();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 获取当前系统时间后进行格式转换
     * @return  返回转换格式后的时间
     */
    public static String getStringdate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * dataDao对象获取
     */
    public void getDataDao() {
        //创建数据
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(MainActivity.this,"ND.db",null);
        daoMaster = new DaoMaster(devOpenHelper.getWritableDatabase());
        daoSession = daoMaster.newSession();
        dataDao = daoSession.getDatasetDao();

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;

    }

    //工具栏右上角
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.datasave:
                samplingsave();
                break;
            case R.id.quit:
                finish();
                break;
            default:
        }
        return true;
    }

    /**
     * 主界面采样数据保存
     */
    public void samplingsave() {

        final Namedialog Datasave = new Namedialog(MainActivity.this);

        Datasave.setTitle("采样数据保存");
        Datasave.setYesOnclickListener("保存数据",  new Namedialog.onYesOnclickListener(){
            @Override
            public void onYesClick() {

                String dataname = Datasave.geteditname();
                //对一段采样后的数据命名
                if (!(dataname == null || "".equals(dataname))) {
                    List<Dataset> namelist = dataDao.queryBuilder().where(DatasetDao.Properties.NAME.eq("Temporary")).list();
                    for (int i = 0; i < namelist.size(); i++) {
                        Dataset nc = namelist.get(i);
                        nc.setNAME(dataname);
                        dataDao.update(nc);
                    }
                    Toast.makeText(MainActivity.this, "数据保存成功", Toast.LENGTH_SHORT).show();
                    Datasave.dismiss();
                }
                else {
                    Toast.makeText(MainActivity.this,"请输入保存名称！",Toast.LENGTH_SHORT).show();
                }
            }
        });
        Datasave.setNoOnclickListener("取消保存", new Namedialog.onNoOnclickListener() {
            @Override
            public void onNoClick() {
                Datasave.dismiss();
            }
        });
        Datasave.show();

    }

    /**
     * 初始化时间调节滚动框布局
     */
    private void initNumberPicker() {
        PTSelecting_view = LayoutInflater.from(this).inflate(R.layout.popupwindow, null);
        //秒钟数字选择器参数设置
        submit_PTSelect = (Button) PTSelecting_view.findViewById(R.id.submit_PTSelect);
        numberPicker = (NumberPicker) PTSelecting_view.findViewById(R.id.numberPicker);
        numberPicker.setMaxValue(59);       //可选最大值
        numberPicker.setMinValue(0);        //可选最小值
        numberPicker.setFocusable(false);
        numberPicker.setFocusableInTouchMode(false);
        //设置NumberPicker，中间的EditView不可以点击修改
        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setNumberPickerDividerColor(numberPicker);

        //分钟数字选择器参数设置
        numberPicker0 = (NumberPicker) PTSelecting_view.findViewById(R.id.numberPicker0);
        numberPicker0.setMaxValue(60);
        numberPicker0.setMinValue(0);
        numberPicker0.setFocusable(false);
        numberPicker0.setFocusableInTouchMode(false);
        numberPicker0.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setNumberPickerDividerColor(numberPicker0);
    }

    /**
     * 自定义滚动框分隔线颜色
     */
    private void setNumberPickerDividerColor(NumberPicker number) {
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    //设置分割线的颜色值
                    pf.set(number, new ColorDrawable(ContextCompat.getColor(this, R.color.numberpicker_line2)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    /**
     * 点击事件监听
     */
    public void onclick() {
        /**
         * 运行按钮监听
         */
        Run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (connectflag == 0) {
//                    if (swposition == 0) {
//                        torque = (int) ((Math.random()) * 90);
//                        addEntry(torque);
//                    }
//                    if (swposition ==1) {
//                       temperature = (int) ((Math.random()) * 90);
//                       addEntry(temperature);
//                    }
//                    if (swposition ==2) {
//                       viscosity = (int) ((Math.random()) * 90);
//                        if(viscosity>=vismax) {
//                            leftYAxis.setAxisMaximum(viscosity * 3 / 2);
//                            vismax = viscosity;
//                        }
//                        else {
//                            leftYAxis.setAxisMaximum(vismax * 3 / 2);
//                        }
//                        addEntry(viscosity);
//                }
//                    //dataname = "1号粘液";
                      sendMessage("5xxxxx");
//                    String currenttime = getStringdate();      //获取当前系统时间并进行格式转换
//                    Dataset dt = new Dataset(null,currenttime,"1号粘液",viscosity+"",temperature+"",torque+"",speednumber+"",spindle+"");
//                    long end = dataDao.insert(dt);
//                    String msg = "";
//                    if (end > 0) {
//                        msg = "001新增成功~";
//                    } else {
//                        msg = "新增失败~";
//                    }
//                }
            }
        });
        /**
         * 复位按钮监听
         */
        Reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sendMessage("1xxxxx");
//                mLineChart.clear();
//                leftYAxis.removeLimitLine(limitLine);
//                   mLineChart.setData(new LineData());
//                mLineChart.invalidate();
                  PopupMenuUtil.getInstance().setStatus(0);
            }
        });
//        Savedata.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String times = getStringdate();
//                Toast.makeText(MainActivity.this,times,Toast.LENGTH_SHORT).show();
//            }
//        });


    }
    public void popupeditdialog() {

    }
    /**
     * 初始化LineChart--扭矩
     */
    private void initLineChart0() {
        //显示边界
        mLineChart.setDrawBorders(true);
        //获取X轴
        XAxis xAxis = mLineChart.getXAxis();
        //设置X轴位置
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //设置X轴坐标的最小间隔
        xAxis.setGranularity(1f);
        //获取Y轴
        leftYAxis = mLineChart.getAxisLeft();
        rightYAxis = mLineChart.getAxisRight();
        //左侧 右侧Y轴值设置
        leftYAxis.setAxisMinimum(0f);
        leftYAxis.setAxisMaximum(100f);
        rightYAxis.setAxisMinimum(0f);
        rightYAxis.setAxisMaximum(100f);
        //左侧Y轴百分数显示
        leftYAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return (int) value + "%";
            }
        });
        //右侧Y轴不显示
        rightYAxis.setEnabled(false);


        //描述标签设置
        Description description = new Description();
        description.setEnabled(false);
        mLineChart.setDescription(description);
    }

    /**
     * 初始化LineChart--温度
     */
    private void initLineChart1() {

        //显示边界
        mLineChart.setDrawBorders(true);
        //获取X轴
        XAxis xAxis = mLineChart.getXAxis();
        //设置X轴位置
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //设置X轴坐标的最小间隔
        xAxis.setGranularity(1f);
        //获取Y轴
        leftYAxis = mLineChart.getAxisLeft();
        rightYAxis = mLineChart.getAxisRight();
        //左侧 右侧Y轴值设置
        leftYAxis.setAxisMinimum(0f);
        leftYAxis.setAxisMaximum(100f);
        rightYAxis.setAxisMinimum(0f);
        rightYAxis.setAxisMaximum(100f);
        //左侧Y轴百分数显示
        leftYAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return (int) value + "℃";
            }
        });
        //右侧Y轴不显示
        rightYAxis.setEnabled(false);


        //描述标签设置
        Description description = new Description();
        description.setEnabled(false);
        mLineChart.setDescription(description);
    }

    /**
     * 初始化LineChart--粘度
     */
    private void initLineChart2() {

        //显示边界
        mLineChart.setDrawBorders(true);
        //获取X轴
        XAxis xAxis = mLineChart.getXAxis();
        //设置X轴位置
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //设置X轴坐标的最小间隔
        xAxis.setGranularity(1f);
        //获取Y轴
        leftYAxis = mLineChart.getAxisLeft();
        rightYAxis = mLineChart.getAxisRight();
        //左侧 右侧Y轴值设置
        leftYAxis.setAxisMinimum(0);
        leftYAxis.setAxisMaximum(120001);
        rightYAxis.setAxisMinimum(0);
        rightYAxis.setAxisMaximum(120001);
        //左侧Y轴百分数显示
        leftYAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return (int) value + "mPa·s";
            }
        });
        //右侧Y轴不显示
        rightYAxis.setEnabled(false);


        //描述标签设置
        Description description = new Description();
        description.setEnabled(false);
        mLineChart.setDescription(description);
    }
    /**
     * 初始化折线
     * @param name
     * @param color
     */
    private void initLineDataSet(String name, int color) {

        lineDataSet = new LineDataSet(null, name);
        //设置曲线宽度
        lineDataSet.setLineWidth(1.5f);
        //设置圆点半径
        lineDataSet.setCircleRadius(1.5f);
        //设置曲线颜色
        lineDataSet.setColor(color);
        //设置曲线上每个数据点圈颜色
        lineDataSet.setCircleColor(color);
        lineDataSet.setHighLightColor(color);
        //设置曲线填充
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(color);
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        //设置显示值的字体大小
        lineDataSet.setValueTextSize(10f);
        //线模式为圆滑曲线（默认折线）
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //添加一个空的 LineData
        lineData = new LineData();
        mLineChart.setData(lineData);
        mLineChart.invalidate();

        //设置曲线值为整数
        lineDataSet.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                int IValue = (int) value;
                return String.valueOf(IValue);
            }
        });
    }
    /**
     * 动态添加数据
     * @param number
     */
    public void addEntry(int number) {

        //最开始的时候才添加 lineDataSet（一个lineDataSet 代表一条线）
        if (lineDataSet.getEntryCount() == 0) {
            lineData.addDataSet(lineDataSet);
        }
        mLineChart.setData(lineData);


        Entry entry = new Entry(lineDataSet.getEntryCount(), number);
        if(number >= 80 && number != 0 && swposition ==0){
            //添加警戒线
            limitLine = new LimitLine(80,"扭矩过大，请调整转速");
            limitLine.setLineWidth(1f);
            limitLine.setTextSize(12f);
            limitLine.setLineColor(Color.RED);
            limitLine.setTextColor(Color.GRAY);
            leftYAxis.addLimitLine(limitLine);
        }
        else if(swposition == 0)
            leftYAxis.removeLimitLine(limitLine);

        lineData.addEntry(entry, 0);
        //通知数据已经改变
        lineData.notifyDataChanged();
        mLineChart.notifyDataSetChanged();
        //设置在曲线图中显示的最大数量
        mLineChart.setVisibleXRangeMaximum(10);
        //移到某个位置
        mLineChart.moveViewToX(lineData.getEntryCount() - 5);
    }

    public void sendMessage(String str) {

        try {
            outputStream = BluetoothConnect.bluetoothsocket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "输出流失败", Toast.LENGTH_SHORT);
        }

        byte[] msgBuffer = null;

        try {
            msgBuffer = str.getBytes(encodeType);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e("decode 错误","编码异常",e);
        }

        try {
//            outputStream.write(msgBuffer);
//            setTitle("成功发送命令");
//            Log.i("成功发送命令","  "+str);
            for (byte b : msgBuffer) {
                outputStream.write(b);
                Thread.sleep(8);
            }

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(),"发送命令失败",Toast.LENGTH_SHORT);
        }
        catch (InterruptedException e) {
        }


    }

    private void StartThread() {
        connectedThread = new ConnectedThread();
        handler = new MyHandler();
        connectedThread.Start();
    }

    class ConnectedThread extends  Thread
    {
        private InputStream inputStream = null;
        private long wait;
        private Thread thread;

        public ConnectedThread()
        {
            isRecording = false;
            this.wait = 50;
            thread = new Thread(new ReadRunnable());
        }

        public void Stop()
        {
            isRecording = false;
        }
        public void Start()
        {
            isRecording = true;
            State state = thread.getState();
            if(state == State.NEW)
            {
                thread.start();
            }else thread.resume();
        }

        private class ReadRunnable implements Runnable
        {

            @Override
            public void run() {
                while(isRecording)
                {
                    try {
                        inputStream = bluetoothsocket.getInputStream();

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(),"创建input流失败",Toast.LENGTH_SHORT).show();
                    }
                    int length =128;
                    byte[] temp = new byte[length];                   //创建一个byte数组进行获取
                    if(inputStream != null)
                    {
                        try {
                            int len = inputStream.read(temp,0,length-1);
                            int cmp = temp[len-1];                    //取出每次接收的字符串最后一位值
                            Log.i("temp[len-1]",cmp+"");
                            if((len <= 6 && len > 0)| cmp != 120 )    //120为X的ASCLL码值
                            {
                                Thread.sleep(20);
                                byte[] bluetoothbuff1 = new byte[len];
                                System.arraycopy(temp,0,bluetoothbuff1,0,bluetoothbuff1.length);
                                String readStr1 = new String(bluetoothbuff1,encodeType);
                                //对出现截断的数据进行二次读取
                                int len_left = inputStream.read(temp,0,length-1);
                                byte[] bluetoothbuff2 = new byte[len_left];
                                System.arraycopy(temp,0,bluetoothbuff2,0,bluetoothbuff2.length);
                                String readStr2 = new String(bluetoothbuff2,encodeType);
                                String readStr = readStr1 + readStr2;
                                Log.i("Twice",readStr);
                                handler.obtainMessage(01,len,-1,readStr).sendToTarget();
                            } else {
                                byte[] bluetoothbuff = new byte[len];
                                System.arraycopy(temp, 0, bluetoothbuff, 0, bluetoothbuff.length);
                                String readStr = new String(bluetoothbuff, encodeType);
                                Log.d("One",readStr);
                                handler.obtainMessage(01, len, -1, readStr).sendToTarget();
                            }
                            Thread.sleep(wait);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            handler.sendEmptyMessage(00);
                        }
                    }
                }
            }
        }

    }

    private class MyHandler extends android.os.Handler
    {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what)
            {
                case 00:
                    isRecording = false;

                    break;
                case 01:

                    //获取功能键状态
                    startSampling = PopupMenuUtil.getInstance().getStatus(1);
                    pauseSampling = PopupMenuUtil.getInstance().getStatus(2);
                    stopSampling  = PopupMenuUtil.getInstance().getStatus(3);

                    //获取字符并且拆分
                    String info = (String)msg.obj;
                    Log.i("内容",info);
                    info = info +"";
                    String[] meter = info.split("\\|");              //数据解析拆分

                    //扭矩部分
                    convert = Float.parseFloat(meter[0]);
                    torque = (int)convert;
                    if (swposition == 0 && startSampling) {  //加入图表更新
                        addEntry(torque);
                    }
                    //粘度部分
                    String addresses = meter[1];
                    convert = Float.parseFloat(addresses);
                    viscosity = (int)convert;
                    if (swposition == 2 && startSampling) {  //Y轴粘度刻度值的实时变换
                        if(viscosity>=vismax) {
                            leftYAxis.setAxisMaximum(viscosity * 3 / 2);
                            vismax = viscosity;
                        }
                        else {
                            leftYAxis.setAxisMaximum(vismax * 3 / 2);
                        }
                        addEntry(viscosity);
                    }

                    //转速部分
                    convert = Float.parseFloat(meter[2]);
                    speednumber = (int)convert;
                    AmountView.etAmount.setText(speednumber+"");
                    convert = Float.parseFloat((meter[3]));          //温度
                    temperature = (int)convert;
                    if (swposition ==1 && temperature<=100 && startSampling) {
                        addEntry(temperature);
                        Log.i("温度",temperature+"");
                    }

                    //转子部分
                    convert = Float.parseFloat(meter[4]);
                    spindle = (int)convert;
                    Spindle.setSelection(spindle);

                    //定时打印时间设置部分
                    convert = Float.parseFloat(meter[5]);            //分
                    minute =(int)convert;
                    convert = Float.parseFloat(meter[6]);            //秒
                    sec = (int)convert;
                    edPTSelect.setText(minute + " 分 "+ sec + " 秒");

                    //数据库写入操作
                    if ( startSampling ) {
                        String currenttime = getStringdate();      //获取当前系统时间并进行格式转换
                        Dataset dt = new Dataset(null, currenttime, "Temporary", viscosity + "", temperature + "", torque + "", speednumber + "", spindle + "");
                        long end = dataDao.insert(dt);
                    }
                    //Log.i("内容",meter[1]);
                    break;
                default:
                    break;
            }
        }
    }
}
