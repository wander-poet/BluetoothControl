package com.example.joney.demo20;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;


/**
 * Created by joney on 2018/7/13.
 */

public class Spindleset extends AppCompatActivity {

    private Toolbar spindletoolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spindle);
        //toolbar栏初始设置
        spindletoolbar = (Toolbar) findViewById(R.id.spindletool);
        setSupportActionBar(spindletoolbar);
        spindletoolbar.setNavigationIcon(R.drawable.ic_arrowback);
        //返回监听
        spindletoolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();//返回
            }
        });
    }

}
