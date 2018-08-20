package com.d.music.module.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.d.lib.common.utils.log.ULog;
import com.d.music.App;
import com.d.music.MainActivity;
import com.d.music.R;
import com.d.music.common.Constants;
import com.d.music.common.preferences.Preferences;
import com.d.music.module.events.MusicInfoEvent;
import com.d.music.module.greendao.bean.MusicModel;
import com.d.music.module.greendao.db.AppDB;
import com.d.music.module.greendao.util.AppDBUtil;
import com.d.music.module.media.controler.MediaControler;
import com.d.music.module.media.controler.MediaPlayerManager;
import com.d.music.play.activity.PlayActivity;
import com.d.music.setting.activity.ModeActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * MusicService
 * Created by D on 2017/4/29.
 */
public class MusicService extends Service {
    public static boolean progressLock = false;
    private static boolean isRunning;

    private MediaControler control;
    private MusicBinder binder;
    private NotificationManager manager;
    private int notification_ID;
    private WeakHandler handler = new WeakHandler(this);
    private ControlBroadcast broadcast;

    public static boolean isRunning() {
        return isRunning;
    }

    public static void startService(Context context) {
        if (context == null || isRunning) {
            return;
        }
        // 开启service服务
        Intent intent = new Intent(context, MusicService.class);
        context.startService(intent);
    }

    /**
     * 睡眠定时
     *
     * @param start: true: 开始定时睡眠 false: 取消睡眠设置
     * @param time:  定时时间
     */
    public static void timing(Context context, boolean start, long time) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 4681,
                new Intent(Constants.PlayFlag.PLAYER_CONTROL_TIMING), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (start) {
            if (am != null) {
                am.set(AlarmManager.RTC, System.currentTimeMillis() + time, pendingIntent);
            }
        } else {
            if (am != null) {
                am.cancel(pendingIntent);
            }
        }
    }

    private static class WeakHandler extends Handler {
        WeakReference<MusicService> weakReference;

        WeakHandler(MusicService service) {
            weakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService theService = weakReference.get();
            if (theService != null && MusicService.isRunning()) {
                switch (msg.what) {
                    case 1:
                        MediaPlayerManager mediaManager = theService.control.getMediaManager();
                        if (mediaManager != null
                                && theService.control.getStatus() == Constants.PlayStatus.PLAY_STATUS_PLAYING) {
                            // 获取当前音乐播放的位置
                            int currentPosition = mediaManager.getCurrentPosition();
                            // 获取当前音乐播放总时间
                            int duration = mediaManager.getDuration();
                            Intent intent = new Intent();
                            intent.setAction(Constants.PlayFlag.MUSIC_CURRENT_POSITION);
                            intent.putExtra("currentPosition", currentPosition);
                            intent.putExtra("duration", duration);
                            if (!progressLock) {
                                // 给PlayerActivity发送广播
                                theService.sendBroadcast(intent);
                            }
                        }
                        theService.handler.sendEmptyMessageDelayed(1, 1000);
                        break;
                }
            }
            super.handleMessage(msg);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        isRunning = true;
        if (control == null) {
            control = MediaControler.getIns(getApplicationContext());
        }
        binder = new MusicBinder();
        broadcast = new ControlBroadcast();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.PlayFlag.PLAYER_CONTROL_PLAY_PAUSE);
        filter.addAction(Constants.PlayFlag.PLAYER_CONTROL_PREV);
        filter.addAction(Constants.PlayFlag.PLAYER_CONTROL_NEXT);
        filter.addAction(Constants.PlayFlag.PLAYER_CONTROL_EXIT);
        filter.addAction(Constants.PlayFlag.PLAYER_CONTROL_TIMING);
        filter.addAction(Constants.PlayFlag.PLAYER_RELOAD);
        filter.addAction(Constants.PlayFlag.MUSIC_SEEK_TO_TIME);
        registerReceiver(broadcast, filter);

        notification_ID = 6671;
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Observable.create(new ObservableOnSubscribe<List<MusicModel>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<MusicModel>> e) throws Exception {
                List<MusicModel> list = AppDBUtil.getIns(getApplicationContext()).optMusic().queryAll(AppDB.MUSIC);
                if (list == null) {
                    list = new ArrayList<>();
                }
                e.onNext(list);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<MusicModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<MusicModel> list) {
                        Preferences p = Preferences.getIns(getApplicationContext());
                        boolean play = Constants.PlayerMode.mode == Constants.PlayerMode.PLAYER_MODE_NOTIFICATION
                                || (p.getIsAutoPlay() && list.size() > 0);
                        control.init(list, p.getLastPlayPosition(), play);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 刷新通知栏
     *
     * @param bitmap:     图标
     * @param songName:   歌曲名
     * @param artistName: 歌手
     * @param status:     无/播放/暂停
     */
    private void updateNotification(Bitmap bitmap, String songName, String artistName, int status) {
        Intent intent;
        if (Constants.PlayerMode.mode == Constants.PlayerMode.PLAYER_MODE_NOTIFICATION) {
            intent = new Intent(this, ModeActivity.class);
        } else if (Constants.PlayerMode.mode == Constants.PlayerMode.PLAYER_MODE_MINIMALIST) {
            intent = new Intent(this, PlayActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        PendingIntent pintent = PendingIntent.getActivity(this, 0, intent, 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            Notification.Builder builder = new Notification.Builder(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                builder.setPriority(Notification.PRIORITY_HIGH);
            }
            builder.setSmallIcon(R.drawable.module_common_ic_launcher); // 设置图标
            builder.setTicker(""); // 手机状态栏的提示
            builder.setContentIntent(pintent); // 点击后的意图

            RemoteViews rv = getRemoteViews(bitmap, songName, artistName, status);
            builder.setContent(rv);
            Notification notification;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification = builder.build(); // 4.1以上
            } else {
                notification = builder.getNotification();
            }
            manager.notify(notification_ID, notification);
            startForeground(notification_ID, notification); // 自定义的notification_ID不能为0
        } else {
            // API Level >= 4 (Android 1.6) && API Level < 16 (Android 4.1)
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.module_common_ic_launcher)
                    .setTicker("")
                    .setWhen(System.currentTimeMillis());

            RemoteViews rv = getRemoteViews(bitmap, songName, artistName, status);
            builder.setContent(rv);
            Notification notification;
            notification = builder.getNotification();
            manager.notify(notification_ID, notification);
            startForeground(notification_ID, notification); // 自定义的notification_ID不能为0
        }
    }

    @android.support.annotation.NonNull
    private RemoteViews getRemoteViews(Bitmap bitmap, String songName, String artistName, int status) {
        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.module_play_notification);
        if (bitmap != null) {
            rv.setImageViewBitmap(R.id.image, bitmap);
        } else {
            rv.setImageViewResource(R.id.image, R.drawable.module_play_ic_notification);
        }
        if (status == Constants.PlayStatus.PLAY_STATUS_PLAYING) {
            rv.setImageViewResource(R.id.tv_play_pause, R.drawable.module_play_ic_notification_pause);
        } else if (status == Constants.PlayStatus.PLAY_STATUS_PAUSE) {
            rv.setImageViewResource(R.id.tv_play_pause, R.drawable.module_play_ic_notification_play);
        }
        rv.setTextViewText(R.id.title, songName);
        rv.setTextViewText(R.id.text, artistName);

        // 此处action不能是一样的 如果一样的 接受的flag参数只是第一个设置的值
        Intent pauseIntent = new Intent(Constants.PlayFlag.PLAYER_CONTROL_PLAY_PAUSE);
        pauseIntent.putExtra("flag", Constants.PlayFlag.PLAY_FLAG_PLAY_PAUSE);
        PendingIntent pausePIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, 0);
        rv.setOnClickPendingIntent(R.id.play_pause, pausePIntent);

        Intent nextIntent = new Intent(Constants.PlayFlag.PLAYER_CONTROL_NEXT);
        nextIntent.putExtra("flag", Constants.PlayFlag.PLAY_FLAG_NEXT);
        PendingIntent nextPIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
        rv.setOnClickPendingIntent(R.id.next, nextPIntent);

        Intent preIntent = new Intent(Constants.PlayFlag.PLAYER_CONTROL_PREV);
        preIntent.putExtra("flag", Constants.PlayFlag.PLAY_FLAG_PRE);
        PendingIntent prePIntent = PendingIntent.getBroadcast(this, 0, preIntent, 0);
        rv.setOnClickPendingIntent(R.id.prev, prePIntent);

        Intent exitIntent = new Intent(Constants.PlayFlag.PLAYER_CONTROL_EXIT);
        exitIntent.putExtra("flag", Constants.PlayFlag.PLAY_FLAG_EXIT);
        PendingIntent exitPIntent = PendingIntent.getBroadcast(this, 0, exitIntent, 0);
        rv.setOnClickPendingIntent(R.id.exit, exitPIntent);
        return rv;
    }

    /**
     * 取消通知栏
     */
    public void cancleNotification() {
        // Can not work beacauseof "startForeground"!
        manager.cancel(notification_ID);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeMessages(1);
        handler.sendEmptyMessageDelayed(1, 1000);
        return super.onStartCommand(intent, flags, startId);
    }

    public class MusicBinder extends Binder {
        public void updateNotificationTask(Bitmap bitmap, String title, String name, int playStatus) {
            updateNotification(bitmap, title, name, playStatus);
        }

        public MediaControler getinstanceMusicControlUtils() {
            return control;
        }
    }

    private class ControlBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {
                case Constants.PlayFlag.PLAYER_CONTROL_PLAY_PAUSE:
                case Constants.PlayFlag.PLAYER_CONTROL_NEXT:
                case Constants.PlayFlag.PLAYER_CONTROL_PREV:
                case Constants.PlayFlag.PLAYER_CONTROL_EXIT:
                    final int flag = intent.getIntExtra("flag", -1);
                    ULog.d("flags" + flag + "");
                    switch (flag) {
                        case Constants.PlayFlag.PLAY_FLAG_PLAY_PAUSE:
                            if (control.getStatus() == Constants.PlayStatus.PLAY_STATUS_PLAYING) {
                                control.pause();
                            } else if (control.getStatus() == Constants.PlayStatus.PLAY_STATUS_PAUSE) {
                                control.start();
                            }
                            break;
                        case Constants.PlayFlag.PLAY_FLAG_NEXT:
                            control.next();
                            break;
                        case Constants.PlayFlag.PLAY_FLAG_PRE:
                            control.prev();
                            break;
                        case Constants.PlayFlag.PLAY_FLAG_EXIT:
                            App.exit(getApplicationContext()); // 退出应用
                            break;
                    }
                    break;
                case Constants.PlayFlag.PLAYER_CONTROL_TIMING:
                    App.exit(getApplicationContext()); // 退出应用
                    break;
                case Constants.PlayFlag.PLAYER_RELOAD:
                    updateNotif(Constants.PlayStatus.PLAY_STATUS_PLAYING); // 正在播放
                    break;
                case Constants.PlayFlag.MUSIC_SEEK_TO_TIME:
                    final MediaPlayerManager mediaManager = control.getMediaManager();
                    if (mediaManager != null) {
                        int currentPosttion = intent.getIntExtra("progress", 0);
                        if (currentPosttion / 1000 >= mediaManager.getDuration() / 1000) {
                            control.next();
                        } else {
                            control.seekTo(currentPosttion);
                        }
                    }
                    progressLock = false; // 解锁
                    break;
            }
        }
    }

    private void updateNotif(int status) {
        switch (status) {
            case Constants.PlayStatus.PLAY_STATUS_STOP:
                updateNotification(null, control.getSongName(), control.getArtistName(), status);
                // 取消通知栏
                cancleNotification();
                break;
            case Constants.PlayStatus.PLAY_STATUS_PLAYING:
            case Constants.PlayStatus.PLAY_STATUS_PAUSE:
                // 正在播放/暂停
                // 更新Notification的显示
                updateNotification(null, control.getSongName(), control.getArtistName(), status);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onEventMainThread(MusicInfoEvent event) {
        if (event != null && isRunning && event.isUpdateNotif) {
            updateNotif(event.status); // 正在播放
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        unregisterReceiver(broadcast);
        isRunning = false;
        stopForeground(true);
        super.onDestroy();
    }
}