package com.d.music;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.d.lib.common.module.mvp.base.BaseFragmentActivity;
import com.d.lib.common.module.repeatclick.ClickUtil;
import com.d.music.common.preferences.Preferences;
import com.d.music.local.fragment.MainFragment;
import com.d.music.module.events.MusicInfoEvent;
import com.d.music.module.service.MusicService;
import com.d.music.play.activity.PlayActivity;
import com.d.music.setting.activity.SettingActivity;
import com.d.music.setting.activity.SkinActivity;
import com.d.music.setting.activity.SleepActivity;
import com.d.music.utils.StatusBarCompat;
import com.nineoldandroids.view.ViewHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;
import cn.feng.skin.manager.loader.SkinManager;

/**
 * MainActivity
 * Created by D on 2017/4/28.
 */
public class MainActivity extends BaseFragmentActivity implements DrawerListener {
    @BindView(R.id.tv_song_name)
    TextView tvSongName;
    @BindView(R.id.tv_singer)
    TextView tvSinger;
    @BindView(R.id.tv_stroke)
    TextView tvStroke;
    @BindView(R.id.llyt_menu_exit)
    LinearLayout llytExit;
    @BindView(R.id.iv_play)
    ImageView ivPlay;
    @BindView(R.id.flyt_menu)
    FrameLayout flytMenu;

    private static DrawerLayout drawer;
    public static FragmentManager fManger;

    @OnClick({R.id.iv_play, R.id.flyt_menu, R.id.llyt_menu_sleep, R.id.llyt_menu_skin, R.id.llyt_menu_setting, R.id.llyt_menu_exit})
    public void onClickListener(View v) {
        if (ClickUtil.isFastDoubleClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.iv_play:
                PlayActivity.openActivity(MainActivity.this);
                break;
            case R.id.flyt_menu:
                drawer.openDrawer(Gravity.END);
                break;
            case R.id.llyt_menu_sleep:
                startActivity(new Intent(MainActivity.this, SleepActivity.class));
                break;
            case R.id.llyt_menu_skin:
                startActivity(new Intent(MainActivity.this, SkinActivity.class));
                break;
            case R.id.llyt_menu_setting:
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                break;
            case R.id.llyt_menu_exit:
                // 退出
                App.exit(getApplicationContext());
                break;
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_main;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (App.toFinish(intent)) {
            finish();
        }
    }

    @Override
    protected void init() {
        if (App.toFinish(getIntent())) {
            finish();
            return;
        }
        StatusBarCompat.compat(MainActivity.this, SkinManager.getInstance().getColor(R.color.lib_pub_color_main));
        EventBus.getDefault().register(this);
        fManger = getSupportFragmentManager();
        drawer = (DrawerLayout) findViewById(R.id.dl_drawer);
        replace(new MainFragment());
        drawer.setScrimColor(getResources().getColor(R.color.lib_pub_color_trans));
        drawer.addDrawerListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvSongName.setText(MusicService.getControl(getApplicationContext()).getCurSongName());
        tvSinger.setText(MusicService.getControl(getApplicationContext()).getCurSinger());
        Preferences p = Preferences.getInstance(getApplicationContext());
        flytMenu.setVisibility(p.getIsShowMenuIcon() ? View.VISIBLE : View.GONE);
        tvStroke.setText(p.getSignature());
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        View content = drawer.getChildAt(0);
        View menu = drawerView;
        float scale = 1 - slideOffset;
        float rightScale = 0.8f + scale * 0.2f;
        float leftScale = 1 - 0.3f * scale;

        ViewHelper.setScaleX(menu, leftScale);
        ViewHelper.setScaleY(menu, leftScale);

        ViewHelper.setTranslationX(content, -menu.getMeasuredWidth() * slideOffset);
        ViewHelper.setPivotX(content, content.getMeasuredWidth());
        ViewHelper.setPivotY(content, content.getMeasuredHeight() / 2);
        content.invalidate();
        ViewHelper.setScaleX(content, rightScale);
        ViewHelper.setScaleY(content, rightScale);
    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    @Override
    public void onThemeUpdate() {
        super.onThemeUpdate();
        // 沉浸式状态栏
        StatusBarCompat.compat(this, SkinManager.getInstance().getColor(R.color.lib_pub_color_main));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MusicInfoEvent event) {
        if (event != null && tvSongName != null && tvSinger != null) {
            tvSongName.setText(event.songName);
            tvSinger.setText(event.singer);
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        releaseResource();
        super.onDestroy();
    }

    private void releaseResource() {
        drawer = null;
        fManger = null;
    }

    @Override
    public void onBackPressed() {
        if (fManger.getBackStackEntryCount() <= 1) {
            finish();
        } else {
            popBackStack();
        }
    }

    public static void setDrawerLockMode(int lockMode) {
        if (drawer != null) {
            drawer.setDrawerLockMode(lockMode);
        }
    }

    public static void replace(Fragment fragment) {
        if (fManger != null) {
            fManger.beginTransaction().replace(R.id.framement, fragment)
                    .addToBackStack(null).commitAllowingStateLoss();
        }
    }

    public static void popBackStack() {
        if (fManger != null) {
            fManger.popBackStack();
        }
    }
}
