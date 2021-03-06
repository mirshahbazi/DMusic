package com.d.lib.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.d.lib.common.R;
import com.d.lib.common.utils.Util;
import com.d.lib.common.view.loading.LoadingLayout;

/**
 * Default Status: 默认态
 * Created by D on 2017/5/7.
 */
public class DSLayout extends FrameLayout {
    /*********** 默认态类型 ************/
    public final static int STATE_LOADING = 0x10;//默认态：loading态
    public final static int STATE_EMPTY = 0x11;//默认态：无数据
    public final static int STATE_NET_ERROR = 0x12;//默认态：网络错误

    /***************** 默认态居中类型 ****************/
    private final static int CENT_TYPE_MAIN = 1;
    private final static int CENT_TYPE_LOCAL = 2;
    private final static float[] AJUST_HEIGHT = new float[]{0, 50, 70};

    private int layoutId;
    private int centerType;//居中类型
    private float adjustHeightT;//校正高度
    private float adjustHeightB;//校正高度
    private int resIdEmpty, resIdNetError;

    private LinearLayout llytDsl;
    private ImageView ivIcon;
    private TextView tvDesc;
    private Button button;
    private LoadingLayout ldlLoading;

    public DSLayout(Context context) {
        this(context, null);
    }

    public DSLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DSLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.lib_pub_DSLayout);
        layoutId = typedArray.getResourceId(R.styleable.lib_pub_DSLayout_lib_pub_dsl_layout, R.layout.lib_pub_layout_ds);
        centerType = typedArray.getInteger(R.styleable.lib_pub_DSLayout_lib_pub_dsl_ceterType, 0);
        adjustHeightT = typedArray.getDimension(R.styleable.lib_pub_DSLayout_lib_pub_dsl_adjustHeightT, 0);
        adjustHeightB = typedArray.getDimension(R.styleable.lib_pub_DSLayout_lib_pub_dsl_adjustHeightB, 0);
        resIdEmpty = typedArray.getResourceId(R.styleable.lib_pub_DSLayout_lib_pub_dsl_emptyDrawable, R.drawable.lib_pub_ic_no_data);
        resIdNetError = typedArray.getResourceId(R.styleable.lib_pub_DSLayout_lib_pub_dsl_netErroDrawable, R.drawable.lib_pub_ic_network_err);
        typedArray.recycle();
        init(context);
    }

    protected void init(Context context) {
        View root = LayoutInflater.from(context).inflate(layoutId, this);
        View vT = root.findViewById(R.id.v_dsl_t);
        View vB = root.findViewById(R.id.v_dsl_b);

        llytDsl = (LinearLayout) root.findViewById(R.id.llyt_dsl);
        ivIcon = (ImageView) root.findViewById(R.id.iv_dsl_icon);
        tvDesc = (TextView) root.findViewById(R.id.tv_dsl_desc);
        button = (Button) root.findViewById(R.id.btn_dsl);

        ldlLoading = (LoadingLayout) root.findViewById(R.id.ldl_loading);

        ViewGroup.LayoutParams paramsT = vT.getLayoutParams();
        ViewGroup.LayoutParams paramsB = vB.getLayoutParams();
        switch (centerType) {
            case CENT_TYPE_MAIN:
                paramsB.height = Util.dip2px(context, AJUST_HEIGHT[CENT_TYPE_MAIN]);
                break;
            case CENT_TYPE_LOCAL:
                paramsB.height = Util.dip2px(context, AJUST_HEIGHT[CENT_TYPE_LOCAL]);
                break;
            default:
                //do nothing, default center 0/0
                break;
        }
        if (adjustHeightT != 0 || adjustHeightB != 0) {
            //优先级大于并覆盖centerType
            paramsT.height = (int) adjustHeightT;
            paramsB.height = (int) adjustHeightB;
        }
        vT.setLayoutParams(paramsT);//设置顶部校正高度
        vB.setLayoutParams(paramsB);//设置底部校正高度
    }

    private void showLoading() {
        setVisibility(VISIBLE);
        ldlLoading.setVisibility(VISIBLE);
        llytDsl.setVisibility(GONE);
    }

    private void showEmpty() {
        setVisibility(VISIBLE);
        ldlLoading.setVisibility(GONE);
        llytDsl.setVisibility(VISIBLE);
        ivIcon.setImageResource(resIdEmpty);
        tvDesc.setText(getResources().getString(R.string.lib_pub_no_data));
        button.setVisibility(GONE);
    }

    private void showNetError() {
        setVisibility(VISIBLE);
        ldlLoading.setVisibility(GONE);
        llytDsl.setVisibility(VISIBLE);
        ivIcon.setImageResource(resIdNetError);
        tvDesc.setText(getResources().getString(R.string.lib_pub_net_error));
        button.setText(getResources().getString(R.string.lib_pub_retry));
        button.setVisibility(VISIBLE);
    }

    /**
     * 设置图片
     */
    public DSLayout icon(int resId) {
        ivIcon.setImageResource(resId);
        return this;
    }

    /**
     * 设置图片
     */
    public DSLayout icon(Drawable drawable) {
        ivIcon.setImageDrawable(drawable);
        return this;
    }

    /**
     * 设置图片
     */
    public DSLayout icon(String url) {
        Glide.with(getContext())
                .load(url)
                .apply(new RequestOptions().dontTransform().dontAnimate())
                .into(ivIcon);
        return this;
    }

    /**
     * 设置Gif图
     */
    public DSLayout gif(int resId) {
        Glide.with(getContext())
                .asGif()
                .load(resId)
                .into(ivIcon);
        return this;
    }

    /**
     * 设置Gif图
     */
    public DSLayout gif(String url) {
        Glide.with(getContext())
                .asGif()
                .load(url)
                .into(ivIcon);
        return this;
    }

    /**
     * 设置提示文本
     */
    public DSLayout desc(CharSequence text) {
        tvDesc.setText(text);
        return this;
    }

    /**
     * 设置按钮文本、显示状态
     */
    public DSLayout button(CharSequence text, int visibility) {
        button.setText(text);
        button.setVisibility(visibility);
        return this;
    }

    /**
     * setState
     */
    public DSLayout setState(int state) {
        switch (state) {
            case GONE:
            case VISIBLE:
            case INVISIBLE:
                setVisibility(state);
                break;
            case STATE_LOADING:
                showLoading();
                break;
            case STATE_EMPTY:
                showEmpty();
                break;
            case STATE_NET_ERROR:
                showNetError();
                break;
        }
        return this;
    }
}
