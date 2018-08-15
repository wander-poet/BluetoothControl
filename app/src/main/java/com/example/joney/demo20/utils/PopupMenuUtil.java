package com.example.joney.demo20.utils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.joney.demo20.R;

/**
 * Created by joney on 2018/5/3.
 */

public class PopupMenuUtil {

    private static final String TAG = "PopupMenuUtil";

    private boolean startstatus;           //开始状态

    private boolean pausestatus;           //暂停状态

    private boolean stopstatus;            //停止状态

    private boolean condition;            //临时状态存储

    public static PopupMenuUtil getInstance() {
        return MenuUtilHolder.INSTANCE;
    }

    private static class MenuUtilHolder {
        public static PopupMenuUtil INSTANCE = new PopupMenuUtil();
    }
    private View rootVew;
    private PopupWindow popupWindow;

    private RelativeLayout rlClick;
    private ImageView ivBtn;
    private LinearLayout llTest1, llTest2, llTest3;

    /**
     * 动画执行的 属性值数组
     */
    float animatorProperty[] = null;
    /**
     * 第一排图 距离屏幕底部的距离
     */
    int top = 0;
    /**
     * 第二排图 距离屏幕底部的距离
     */
    int bottom = 0;

    /**
     * 创建 popupWindow 内容
     *
     * @param context context
     */
    private void _createView(final Context context) {
        rootVew = LayoutInflater.from(context).inflate(R.layout.popup_menu, null);
        popupWindow = new PopupWindow(rootVew,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        //设置为失去焦点 方便监听返回键的监听
        popupWindow.setFocusable(false);

        //如果想要popupWindow 遮挡住状态栏可以加上这句代码
        //popupWindow.setClippingEnabled(false);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(false);

        if (animatorProperty == null) {
            top = dip2px(context, 310);
            bottom = dip2px(context, 210);
            animatorProperty = new float[]{bottom, 60, -30, -20 - 10, 0};
        }

        initLayout(context);
    }
    /**
     * dp转化为px
     *
     * @param context  context
     * @param dipValue dp value
     * @return 转换之后的px值
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 初始化 view
     */
    private void initLayout(Context context) {

        rlClick = (RelativeLayout) rootVew.findViewById(R.id.pop_rl_click);
        ivBtn = (ImageView) rootVew.findViewById(R.id.pop_iv_img);
        llTest1 = (LinearLayout) rootVew.findViewById(R.id.test1);
        llTest2 = (LinearLayout) rootVew.findViewById(R.id.test2);
        llTest3 = (LinearLayout) rootVew.findViewById(R.id.test3);


        rlClick.setOnClickListener(new MViewClick(0, context));

        llTest1.setOnClickListener(new MViewClick(1, context));
        llTest2.setOnClickListener(new MViewClick(2, context));
        llTest3.setOnClickListener(new MViewClick(3, context));


    }

    /**
     * 点击事件
     */
    private class MViewClick implements View.OnClickListener {

        public int index;
        public Context context;

        public MViewClick(int index, Context context) {
            this.index = index;
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            if (index == 0) {
                //加号按钮点击之后的执行
                _rlClickAction();
            } else if(index == 1){
                //开始采样
                if(pausestatus && !stopstatus) {

                    startstatus = true;
                    pausestatus = false;
                    showToast(context, "Aindex" + index);
                }
                else {
                    startstatus = true;
                }
            }
            else if (index == 2) {
                //暂停采样
                if(startstatus) {

                    pausestatus = true;
                    startstatus = false;

                    showToast(context, "Bindex" + index);
                }
            }
            else if (index == 3) {
                //停止采样
                if(startstatus| pausestatus) {

                    startstatus = false;
                    pausestatus = false;
                    stopstatus = true;

                    showToast(context, "Cindex" + index);
                    _rlClickAction();
                    showToast(context,"请及时保存采样数据！");


                }
            }
        }
    }

    Toast toast = null;

    /**
     * 防止toast 多次被创建
     *
     * @param context context
     * @param str     str
     */
    private void showToast(Context context, String str) {
        if (toast == null) {
            toast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        } else {
            toast.setText(str);
        }
        toast.show();
    }

    /**
     * 刚打开popupWindow 执行的动画
     */
    private void _openPopupWindowAction() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(ivBtn, "rotation", 0f, 135f);
        objectAnimator.setDuration(200);
        objectAnimator.start();

        _startAnimation(llTest1, 500, animatorProperty);
        _startAnimation(llTest2, 450, animatorProperty);
        _startAnimation(llTest3, 400, animatorProperty);

    }


    /**
     * 关闭 popupWindow执行的动画
     */
    public void _rlClickAction() {
        if (ivBtn != null && rlClick != null) {

            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(ivBtn, "rotation", 135f, 0f);
            objectAnimator.setDuration(300);
            objectAnimator.start();

            _closeAnimation(llTest1, 300, top);
            _closeAnimation(llTest2, 200, top);
            _closeAnimation(llTest3, 200, top);

            rlClick.postDelayed(new Runnable() {
                @Override
                public void run() {
                    _close();
                }
            }, 300);

        }
    }


    /**
     * 弹起 popupWindow
     *
     * @param context context
     * @param parent  parent
     */
    public void _show(Context context, View parent) {
        _createView(context);
        if (popupWindow != null && !popupWindow.isShowing()) {
            popupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, 0, 0);
            _openPopupWindowAction();
        }
    }

    /**
     * 关闭popupWindow
     */

    public void _close() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    /**
     * @return popupWindow 是否显示了
     */
    public boolean _isShowing() {
        if (popupWindow == null) {
            return false;
        } else {
            return popupWindow.isShowing();
        }
    }

    /**
     * 关闭 popupWindow 时的动画
     *
     * @param view     mView
     * @param duration 动画执行时长
     * @param next     平移量
     */
    private void _closeAnimation(View view, int duration, int next) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationY", 0f, next);
        anim.setDuration(duration);
        anim.start();
    }

    /**
     * 启动动画
     *
     * @param view     view
     * @param duration 执行时长
     * @param distance 执行的轨迹数组
     */
    private void _startAnimation(View view, int duration, float[] distance) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "translationY", distance);
        anim.setDuration(duration);
        anim.start();
    }

    /**
     * 向主活动返回布尔值参数
     * @param inf 类型 开始采样--1 暂停采样--2 停止采样--3
     * @return 返回各种类型布尔值状态
     */
    public boolean getStatus(int inf) {

        if (inf == 1) {
            condition = startstatus;
        }
        else if (inf == 2) {
            condition = pausestatus;
        }
        else if (inf == 3) {
            condition = stopstatus;
        }
        return condition;
    }

    /**
     * 主活动设置三种状态值
     * @param inf 0 -- 三种状态都为false  1 -- 三种状态都为true
     */
    public void setStatus(int inf) {
        if (inf == 0) {

            startstatus = false;
            pausestatus = false;
            stopstatus =  false;
        }
        else if (inf == 1) {

            startstatus = true;
            pausestatus = true;
            stopstatus  = true;
        }
    }
}
