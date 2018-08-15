package com.example.joney.demo20;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joney.demo20.entity.DaoMaster;
import com.example.joney.demo20.entity.DaoSession;
import com.example.joney.demo20.entity.Dataset;
import com.example.joney.demo20.entity.DatasetDao;
import com.example.joney.demo20.excel.ExcelUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.joney.demo20.R.id.historychart;

/**
 * Created by joney on 2018/1/29.
 */

public class History extends AppCompatActivity{

    private Toolbar tool_history;
    private Button  F_alldata;                      //查找所有数据
    private Button  Deleteall;                      //删除全部数据
    private Button Namechange;                      //调整字段名称
    private Button  Loadchart;                      //加载曲线
    private Button  Exportdata;
    private Button  Savephoto;                      //保存曲线图片
    private LineChart hy_linechart;
    private LineData  hy_linedata;
    private LineDataSet hy_linedataset;
    private YAxis hy_leftYAxis;
    private YAxis hy_rightYAxis;
    private Spinner chartswitch;
    private int testadd;
    private int swnumber;                           //曲线选择
    private int hy_vismax = 0;                      //粘度最大值
    private Namedialog namedialog;                  //名称修改对话框
    private Namedialog exportedit;                  //excel导出文件命名
    private boolean load_flag = false;              //数据库内采样名称显示spinner初始化标志位
    private boolean change_flag = false;            //名称修改后spinner初始化标志位
    private String spinnerstr;

    private List<Integer> max_min = new ArrayList<>();  //求取最大最小值数据集
    private TextView maxnumber;
    private TextView minnumber;
    private Data app;

    // 采样种类列表
    private Spinner spinnercategory;
    private List<String> listcategories = new ArrayList<>();
    private ArrayAdapter<String> adapterCategory;
    private String category;

    // Excel 导出
    private String[] title = {"    时间    ","名称","粘度","温度","扭矩","转速","转子"};
    private File file;
    private String filename;
    private ArrayList<ArrayList<String>> historylist;
    private String exportname;


    //数据库测试  基于GreenDao ORM
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private DatasetDao dataDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        //工具栏初始化
        tool_history = (Toolbar)findViewById(R.id.history);
        setSupportActionBar(tool_history);
        tool_history.setNavigationIcon(R.drawable.ic_arrowback);
        tool_history.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();                        //返回
            }
        });
        app = (Data)getApplication();
        getDataDao();
        findViewById();
        init_spinner();
        init_historychart0();
        init_historydataset("扭矩",R.color.numberpicker_line2);
        onclick();


    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.historytool, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.spindle_number:
                Intent intent = new Intent();
                intent.setClass(History.this,Spindleset.class);
                startActivity(intent);
                break;
            default:
        }
        return true;
    }

    /**
     * 获取dataDao
     */
    public void getDataDao() {
        //创建数据
        DaoMaster.DevOpenHelper devOpenHelper = new DaoMaster.DevOpenHelper(History.this,"ND.db",null);
        daoMaster = new DaoMaster(devOpenHelper.getWritableDatabase());
        daoSession = daoMaster.newSession();
        dataDao = daoSession.getDatasetDao();

    }

    public void findViewById(){

        F_alldata = (Button)findViewById(R.id.id_search_all);
        Deleteall = (Button)findViewById(R.id.id_deleteall);
        hy_linechart = (LineChart)findViewById(historychart);
        chartswitch = (Spinner)findViewById(R.id.hy_chartsw);
        Loadchart = (Button)findViewById(R.id.hy_loadchart);
        Namechange = (Button)findViewById(R.id.id_namechange);
        Exportdata = (Button)findViewById(R.id.export_excel);
        Savephoto = (Button)findViewById(R.id.save_photo);
        spinnercategory = (Spinner) findViewById(R.id.spinner_category);
        adapterCategory = new ArrayAdapter<>(History.this,android.R.layout.simple_spinner_item,listcategories);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnercategory.setAdapter(adapterCategory);
        maxnumber = (TextView)findViewById(R.id.hy_max);
        minnumber = (TextView)findViewById(R.id.hy_min);

    }

    /**
     * 初始化采样名称spinner
     */
    private void init_spinner() {
        try{
            List<Dataset> dataList = dataDao.queryBuilder().list();
            if (dataList != null && !load_flag) {
                String namecmp = "";
                String nametmp = "";
                for (int i = 0; i < dataList.size(); i++) {
                    Dataset ds = dataList.get(i);
                    //获取名称
                    namecmp = ds.getNAME();
                    if (!(namecmp.equals(nametmp))) {
                        listcategories.add(ds.getNAME()+"|"+ds.getTime());
                        nametmp = namecmp;
                        adapterCategory.notifyDataSetChanged();
                    }

                }
                load_flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 初始化LineChart --扭矩
     */
    //显示扭矩
    private void init_historychart0() {
        //显示边界
        hy_linechart.setDrawBorders(true);
        //获取X轴
        XAxis xAxis = hy_linechart.getXAxis();
        //设置X轴位置
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //设置X轴坐标的最小间隔
        xAxis.setGranularity(1f);
        //获取Y轴
        hy_leftYAxis = hy_linechart.getAxisLeft();
        hy_rightYAxis = hy_linechart.getAxisRight();
        //左侧 右侧Y轴值设置
        hy_leftYAxis.setAxisMinimum(0f);
        hy_leftYAxis.setAxisMaximum(100f);
        hy_rightYAxis.setAxisMinimum(0f);
        hy_rightYAxis.setAxisMaximum(100f);
        //左侧Y轴百分数显示
        hy_leftYAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return (int) value + "%";
            }
        });
        //右侧Y轴不显示
        hy_rightYAxis.setEnabled(false);


        //描述标签设置
        Description description = new Description();
        description.setEnabled(false);
        hy_linechart.setDescription(description);
    }

    /**
     * 初始化LineChart --温度
     */
    private void init_historychart1() {

        //显示边界
        hy_linechart.setDrawBorders(true);
        //获取X轴
        XAxis xAxis = hy_linechart.getXAxis();
        //设置X轴位置
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //设置X轴坐标的最小间隔
        xAxis.setGranularity(1f);
        //获取Y轴
        hy_leftYAxis = hy_linechart.getAxisLeft();
        hy_rightYAxis = hy_linechart.getAxisRight();
        //左侧 右侧Y轴值设置
        hy_leftYAxis.setAxisMinimum(0f);
        hy_leftYAxis.setAxisMaximum(100f);
        hy_rightYAxis.setAxisMinimum(0f);
        hy_rightYAxis.setAxisMaximum(100f);
        //左侧Y轴百分数显示
        hy_leftYAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return (int) value + "℃";
            }
        });
        //右侧Y轴不显示
        hy_rightYAxis.setEnabled(false);


        //描述标签设置
        Description description = new Description();
        description.setEnabled(false);
        hy_linechart.setDescription(description);
    }

    /**
     * 初始化LineChart --粘度
     */
    private void init_historychart2() {

        //显示边界
        hy_linechart.setDrawBorders(true);
        //获取X轴
        XAxis xAxis = hy_linechart.getXAxis();
        //设置X轴位置
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //设置X轴坐标的最小间隔
        xAxis.setGranularity(1f);
        //获取Y轴
        hy_leftYAxis = hy_linechart.getAxisLeft();
        hy_rightYAxis = hy_linechart.getAxisRight();
        //左侧 右侧Y轴值设置
        hy_leftYAxis.setAxisMinimum(0);
        hy_leftYAxis.setAxisMaximum(120001);
        hy_rightYAxis.setAxisMinimum(0);
        hy_rightYAxis.setAxisMaximum(120001);
        //左侧Y轴百分数显示
        hy_leftYAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return (int) value + "mPa·s";
            }
        });
        //右侧Y轴不显示
        hy_rightYAxis.setEnabled(false);


        //描述标签设置
        Description description = new Description();
        description.setEnabled(false);
        hy_linechart.setDescription(description);
    }

    /**
     * 初始化折线
     * @param name  折线名称
     * @param color 折现颜色
     */
    private void init_historydataset(String name, int color) {

        hy_linedataset = new LineDataSet(null, name);
        //设置曲线宽度
        hy_linedataset.setLineWidth(1.5f);
        //设置圆点半径
        hy_linedataset.setCircleRadius(1.5f);
        //设置曲线颜色
        hy_linedataset.setColor(color);
        //设置曲线上每个数据点圈颜色
        hy_linedataset.setCircleColor(color);
        hy_linedataset.setHighLightColor(color);
        //设置曲线填充
        hy_linedataset.setDrawFilled(true);
        hy_linedataset.setFillColor(color);
        hy_linedataset.setAxisDependency(YAxis.AxisDependency.LEFT);
        //设置显示值的字体大小
        hy_linedataset.setValueTextSize(10f);
        //线模式为圆滑曲线（默认折线）
        hy_linedataset.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //添加一个空的 LineData
        hy_linedata = new LineData();
        hy_linechart.setData(hy_linedata);
        hy_linechart.invalidate();

        //设置曲线值为整数
        hy_linedataset.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                int IValue = (int) value;
                return String.valueOf(IValue);
            }
        });
    }

    /**
     * 动态添加数据
     * @param number  添加的数据值
     */
    public void hy_dataadd(int number) {

        //最开始的时候才添加 lineDataSet（一个lineDataSet 代表一条线）
        if (hy_linedataset.getEntryCount() == 0) {
            hy_linedata.addDataSet(hy_linedataset);
        }
        hy_linechart.setData(hy_linedata);


        Entry entry = new Entry(hy_linedataset.getEntryCount(), number);

        hy_linedata.addEntry(entry, 0);
        //通知数据已经改变
        hy_linedata.notifyDataChanged();
        hy_linechart.notifyDataSetChanged();
        //设置在曲线图中显示的最大数量
        hy_linechart.setVisibleXRangeMaximum(10);
        //移到某个位置
        hy_linechart.moveViewToX(hy_linedata.getEntryCount() - 5);
    }

    public void onclick() {

        /**
         * 修改当前所选参数名称
         */
        Namechange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                namedialog = new Namedialog(History.this);
                namedialog.setYesOnclickListener("确定", new Namedialog.onYesOnclickListener() {
                    @Override
                    public void onYesClick() {
                        String oldname = (String)spinnercategory.getSelectedItem();
                        String[] values = oldname.split("\\|");
                        String address = values[0];
                        String editname = namedialog.geteditname();
                        if (!(editname == null || "".equals(editname))) {
                        List<Dataset> namelist = dataDao.queryBuilder().where(DatasetDao.Properties.NAME.eq(address)).list();
                        for (int i = 0; i < namelist.size(); i++) {

                            Dataset nc = namelist.get(i);

                            nc.setNAME(editname);
                            dataDao.update(nc);
                        }
                            Toast.makeText(History.this, "修改成功！", Toast.LENGTH_LONG).show();
                            namedialog.dismiss();

                    } else {
                           Toast.makeText(History.this,"名称不能为空，请重新输入！",Toast.LENGTH_LONG).show();
                        }
                    }

                });
                namedialog.setNoOnclickListener("取消", new Namedialog.onNoOnclickListener() {
                    @Override
                    public void onNoClick() {
                        namedialog.dismiss();
                    }
                });
                namedialog.show();
            }
        });
        /**
         * 查询并显示数据库内存储的所有数据
         */
        F_alldata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    List<Dataset> dataList = dataDao.queryBuilder().list();
                    if (dataList != null) {
                        String searchAllInfo = "";
                        for (int i = 0; i < dataList.size(); i++) {
                            Dataset ds = dataList.get(i);
                            searchAllInfo += "id：" + ds.getMeasureId() + "时间：" + ds.getTime() + "名称：" + ds.getNAME() + "粘度：" + ds.getVIS() + "温度：" + ds.getTEM() + "扭矩：" + ds.getTOR() + "转速：" + ds.getVlo() + "转子：" + ds.getSpi() + "\n";
                        }
                        TextView tvSearchInfo = (TextView) findViewById(R.id.id_search_all_info);
                        tvSearchInfo.setText(searchAllInfo);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        /**
         * 导出EXCEL
         */
        Exportdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportedit = new Namedialog(History.this);
                exportedit.setTitle("导出文件命名");

                exportedit.setYesOnclickListener("确定", new Namedialog.onYesOnclickListener() {
                    @Override
                    public void onYesClick() {
                        //编辑导出文件名称
                        exportname = exportedit.geteditname().toString();
                        exportExcel();
                        exportedit.dismiss();
                    }
                });
                exportedit.setNoOnclickListener("取消", new Namedialog.onNoOnclickListener() {
                    @Override
                    public void onNoClick() {
                        exportedit.dismiss();
                    }
                });
                exportedit.show();
            }
        });
        /**
         * 删除数据库中当前所选数据
         */
        Deleteall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String inputText1 = (String)spinnercategory.getSelectedItem();
                    String[] values = inputText1.split("\\|");
                    String address = values[0];
                    dataDao.queryBuilder().where(DatasetDao.Properties.NAME.eq(address)).buildDelete().executeDeleteWithoutDetachingEntities();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /**
         * 曲线加载
         */
        Loadchart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    max_min.clear();                  //数据集清空
                    String inputText = (String)spinnercategory.getSelectedItem();
                    String[] values = inputText.split("\\|");
                    String address = values[0];
                    String searchAllInfo = "";
                    Toast.makeText(History.this,address,Toast.LENGTH_LONG).show();
                    List<Dataset> dataList = dataDao.queryBuilder().where(DatasetDao.Properties.NAME.eq(address)).list();
                    for (int i = 0; i < dataList.size(); i++) {
                        Dataset ds = dataList.get(i);
                        if (swnumber == 0) {
                            searchAllInfo = ds.getTOR();
                            testadd = Integer.valueOf(searchAllInfo);
                            max_min.add(testadd);
                        }
                        if (swnumber == 1) {
                            searchAllInfo = ds.getTEM();
                            testadd = Integer.valueOf(searchAllInfo);
                            max_min.add(testadd);
                        }
                        if (swnumber == 2) {
                            searchAllInfo = ds.getVIS();
                            testadd = Integer.valueOf(searchAllInfo);
                            if (testadd >= hy_vismax) {
                                hy_leftYAxis.setAxisMaximum(testadd * 3 / 2);
                                hy_vismax = testadd;
                            } else {
                                hy_leftYAxis.setAxisMaximum(testadd * 3 / 2);
                            }
                            max_min.add(testadd);
                        }
                        hy_dataadd(testadd);
                        Max_Min();
                       // maxnumber.setText(app.getSP_1());

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        /**
         *三种图表之间的切换显示监听
        */
        chartswitch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    swnumber = position;

                    init_historychart0();
                    init_historydataset("扭矩", R.color.numberpicker_line2);
                }
                if (position == 1) {
                    swnumber = position;
                    init_historychart1();
                    init_historydataset("温度", Color.RED);
                }
                if (position == 2) {
                    swnumber = position;
                    init_historychart2();
                    init_historydataset("粘度", Color.BLUE);
                }
                Max_Min_clear();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /**
         * 保存图片
         */
        Savephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取输入框名称信息
                String prename = (String)spinnercategory.getSelectedItem();
                String[] values = prename.split("\\|");
                String address = values[0];
                if (swnumber == 0) {
                    category = "扭矩";
                } else if (swnumber == 1) {
                    category = "温度";
                } else if (swnumber == 2) {
                    category = "粘度";
                }
                if (hy_linechart.saveToPath(address + category + System.currentTimeMillis(), "")) {
                    Toast.makeText(History.this, "图片保存成功", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(History.this, "图片保存失败", Toast.LENGTH_SHORT).show();
            }
        });

        /**
         * 采样数据种类选择
         */
        spinnercategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Max_Min_clear();
                spinnerstr = (String)spinnercategory.getSelectedItem();
                Toast.makeText(History.this,spinnerstr,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * 求取所显示曲线最大值最小值
     */
    public void Max_Min() {

        int min = 0;
        int max = 0;
        min = max = max_min.get(0);
        for (int i = 0; i < max_min.size();i++) {
            if (max_min.get(i) > max) {
                max = max_min.get(i);
            }
            if (max_min.get(i) < min) {
                min = max_min.get(i);
            }
        }

        maxnumber.setText(max+"");
        minnumber.setText(min+"");


    }
    /**
     * 清空最大值最小值显示
     */
    public void Max_Min_clear() {
        maxnumber.setText("");
        minnumber.setText("");
    }
    /**
     * 导出EXCEL
     */
    public void exportExcel() {
        file = new File(getSDPath() + "/粘度计导出数据");

        makeDir(file);

        //判断输入文件名称是否为空并进行下面的处理
        if (!(exportname == null || "".equals(exportname))) {
            ExcelUtils.initExcel(file.toString() + "/" + exportname + ".xls", title);
            filename = getSDPath() + "/粘度计导出数据/" + exportname + ".xls";
            ExcelUtils.writeObjListToExcel(getRecordData(), filename, this);
        }
        else {
            Toast.makeText(History.this, "请输入导出文件名称！", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 将数据库数据转换
     * @return 返回指定采样名称的List接口
     */
    private  ArrayList<ArrayList<String>> getRecordData() {
        historylist = new ArrayList<>();
        String prename = (String)spinnercategory.getSelectedItem();
        String[] values = prename.split("\\|");
        String address = values[0];
        List<Dataset> dataList = dataDao.queryBuilder().where(DatasetDao.Properties.NAME.eq(address)).list();
        for (int i = 0; i <dataList.size(); i++) {
            Dataset hyset = dataList.get(i);
            ArrayList<String> beanList = new ArrayList<String>();
            beanList.add(hyset.getTime());
            beanList.add(hyset.getNAME());
            beanList.add(hyset.getVIS());
            beanList.add(hyset.getTEM());
            beanList.add(hyset.getTOR());
            beanList.add(hyset.getVlo());
            beanList.add(hyset.getSpi());
            historylist.add(beanList);
        }
        return historylist;
    }

    /**
     * 获取手机存储路径
     * @return  返回字符串形式路径
     */
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        String dir = sdDir.toString();
        return dir;

    }
    public  void makeDir(File dir) {
        if (!dir.getParentFile().exists()) {
            makeDir(dir.getParentFile());
        }
        dir.mkdir();
    }
        //Dialog执行
//        public void dialognameedit() {
//
//        final EditText editText = new EditText(this);
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("名称修改");
//        builder.setView(editText);
//            builder.setView(editText);
//        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String oldname = Inputcategory.getText().toString();              //获取输入框中原名称
//                String editname = editText.getText().toString();                  //获取要修改的名称
//                List<Dataset> namelist = dataDao.queryBuilder().where(DatasetDao.Properties.NAME.eq(oldname)).list();
//                for(int i = 0; i<namelist.size(); i++){
//
//                    Dataset nc = namelist.get(i);
//                    nc.setNAME(editname);
//                    dataDao.update(nc);
//                }
//                Toast.makeText(History.this, "更新成功", Toast.LENGTH_SHORT).show();
//
//            }
//        });
//        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//        builder.create().show();
//    }
}
///**
// * 显示数据库内的采样数据
// */
//        Storage_show.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try{
//                    List<Dataset> dataList = dataDao.queryBuilder().list();
//                    if (dataList != null) {
//                        String searchAllInfo = "";
//                        String namecmp = "";
//                        String nametmp = "";
//                        for (int i = 0; i < dataList.size(); i++) {
//                            Dataset ds = dataList.get(i);
//                            //获取名称
//                            namecmp = ds.getNAME();
//                            if (!(namecmp.equals(nametmp))) {
//                                searchAllInfo += "名称："+ ds.getNAME() + "时间：" + ds.getTime() + "\n";
//                                nametmp = namecmp;
//                            }
//
//                        }
//                        TextView tvSearchInfo = (TextView) findViewById(R.id.id_search_all_info);
//                        tvSearchInfo.setText(searchAllInfo);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });