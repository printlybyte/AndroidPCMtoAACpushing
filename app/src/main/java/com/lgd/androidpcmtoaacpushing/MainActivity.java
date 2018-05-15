package com.lgd.androidpcmtoaacpushing;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.lgd.androidpcmtoaacpushing.Audio.AudioEncoder;
import com.lgd.androidpcmtoaacpushing.Audio.Audiox;
import com.lgd.androidpcmtoaacpushing.Audio.MediaPublisher;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements MediaPublisher.ConnectRtmpServerCb {
    private static final String TAG = "Debug";
    private Audiox audiox;
    private AudioEncoder audioEncoder;
    //    private RTMPMuxer rtmpMuxer;
//    private RtmpClient rtmpClient;
    private final Handler mHandler = new MyHandler(this);
    private MediaPublisher mediaPublisher;

    private static final String rtmpUrl = "rtmp://193.112.184.133:10085/live/asaas";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {

        mediaPublisher = MediaPublisher.newInstance();
        mediaPublisher.setRtmpConnectCb(this);
        mediaPublisher.initMediaPublish();
//        rtmpMuxer = new RTMPMuxer();
//        rtmpClient = new RtmpClient();
//        startThread();
    }


    public void srartAudio(View c) {
        mediaPublisher.startAudioGather();
        //初始化音频编码器
        mediaPublisher.initAudioEncoder();
        //启动编码
        mediaPublisher.startEncoder();

    }

    public void stopAudio(View c) {
        //停止编码 先要停止编码，然后停止采集
        mediaPublisher.stopEncoder();
        //停止音频采集
        mediaPublisher.stopAudioGather();
        //断开RTMP连接
    }


//    private void startThread() {
//        AppOperator.runOnThread(new Runnable() {
//            @Override
//            public void run() {
//                mHandler.sendEmptyMessage(7);
//                int result = getRtmpConnectState();
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                if (134 == result) {
//                    mHandler.sendEmptyMessage(8);
//                } else {
//                    mHandler.sendEmptyMessage(9);
//                }
//                Log.i(TAG, result + " 连接结果");
//            }
//
//
//        });
//
//    }

//    private int getRtmpConnectState() {
//        return rtmpMuxer.open(ConstantUtils.DEAUFUTRTMPURL, ConstantUtils.DEAUFUTWIGHT, ConstantUtils.DEAUFUTHEIGHT);
//    }


    @Override
    public void onConnectRtmp(int ret) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null) {
                return;
            }
            switch (msg.what) {
                case 1:
                    break;
                case 7:
                    mActivity.get().showToast("连接中");
                    break;
                case 8:
                    mActivity.get().showToast("连接成功");
                    break;
                case 9:
                    mActivity.get().showToast("连接失败，重连中");
                    break;

            }
        }
    }

    public void showToast(String msg) {

        Toast.makeText(this, "" + msg, Toast.LENGTH_SHORT).show();
    }

//    private void checkPermission() {
//        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            // 检查该权限是否已经获取
//            for (int i = 0; i < permissions.length; i++) {
//                int result = ContextCompat.checkSelfPermission(this, permissions[i]);
//                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    hasPermission = false;
//                    break;
//                } else
//                    hasPermission = true;
//            }
//            if (!hasPermission) {
//                // 如果没有授予权限，就去提示用户请求
//                ActivityCompat.requestPermissions(this,
//                        permissions, TARGET_PERMISSION_REQUEST);
//            }
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止编码 先要停止编码，然后停止采集
        mediaPublisher.stopEncoder();
        //停止音频采集
        mediaPublisher.stopAudioGather();
        //释放编码器
        if (mediaPublisher != null)
            mediaPublisher.release();
        mediaPublisher = null;
    }
}
