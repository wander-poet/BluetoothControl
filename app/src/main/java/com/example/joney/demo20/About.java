package com.example.joney.demo20;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;


/**
 * Created by joney on 2018/1/29.
 */

public class About extends AppCompatActivity{

    private Toolbar tool_about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        //工具栏初始化
        tool_about = (Toolbar)findViewById(R.id.about);
        setSupportActionBar(tool_about);
        tool_about.setNavigationIcon(R.drawable.ic_arrowback);
        tool_about.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();                //返回
            }
        });
    }

}
