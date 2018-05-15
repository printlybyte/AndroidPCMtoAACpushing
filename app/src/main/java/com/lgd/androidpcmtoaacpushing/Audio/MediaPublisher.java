package com.lgd.androidpcmtoaacpushing.Audio;

/**
 * Created by liuguodong on 04/03/18.
 */

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

public class MediaPublisher {
    private static final String TAG = "MediaPublisher";

    private LinkedBlockingQueue<Runnable> mRunnables = new LinkedBlockingQueue<>();
    private Thread workThread;


    private AudioGather mAudioGather;
    private AVEncoder mAVEncoder;


    private boolean isPublish;

    private volatile boolean loop;

    private ConnectRtmpServerCb rtmpConnectCb;
    public interface ConnectRtmpServerCb{
        public void onConnectRtmp(final int ret);
    }

    public static MediaPublisher newInstance( ) {
        return new MediaPublisher( );
    }

    private MediaPublisher( ){

    }

    public void setRtmpConnectCb( ConnectRtmpServerCb rtmpConnectCb){
        this.rtmpConnectCb = rtmpConnectCb;
    }


    public void initMediaPublish() {
        if (loop) {
            throw new RuntimeException(" =lgd= Media发布线程已经启动===");
        }
//        mVideoGather = VideoGather.getInstance();
        mAudioGather = AudioGather.getInstance();
        mAVEncoder = AVEncoder.newInstance();

        setListener();

        workThread = new Thread("publish-thread") {
            @Override
            public void run() {
                while (loop && !Thread.interrupted()) {
                    try {
                        Runnable runnable = mRunnables.take();
                        runnable.run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mRunnables.clear();
                Log.d(TAG, "= =lgd= Rtmp发布线程退出...");
            }
        };

        loop = true;
        workThread.start();
    }

    /**
     * 初始化视频编码器
     */
//    public void initVideoEncoder(int width,int height,int fps){
//        mAVEncoder.initVideoEncoder(width,height,fps);
//    }

    /**
     * 初始化音频编码器
     */
    public void initAudioEncoder(){
        mAVEncoder.initAudioEncoder(mAudioGather.getaSampleRate(),mAudioGather.getPcmForamt(),mAudioGather.getaChannelCount());
    }

    /**
     * 开始音频采集
     */
    public void startAudioGather() {
        mAudioGather.prepareAudioRecord();
        mAudioGather.startRecord();
    }

    /**
     * 停止音频采集
     */
    public void stopAudioGather() {
        mAudioGather.stopRecord();
    }

    /**
     * 释放
     */
    public void release() {
        mAudioGather.release();
        loop = false;
        if (workThread != null) {
            workThread.interrupt();
           // workThread = null;
        }
    }



    /**
     * 开始编码
     */
    public void startEncoder() {
        mAVEncoder.start();
    }

    /**
     * 停止编码
     */
    public void stopEncoder() {
        mAVEncoder.stop();
    }

    private void setListener() {


        mAudioGather.setCallback(new AudioGather.Callback() {
            @Override
            public void audioData(byte[] data) {
                mAVEncoder.putAudioData(data);
            }
        });

        mAVEncoder.setCallback(new AVEncoder.Callback() {
            @Override
            public void outputAudioSpecConfig(final byte[] aacSpec, final int len){
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "outputAudioSpecConfig");
                    }
                };
                try {
                    mRunnables.put(runnable);
                } catch (InterruptedException e) {
                    Log.e(TAG, " =lgd= outputAudioSpecConfig=====error: "+e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void outputAudioData(final byte[] aac, final int len,final int nTimeStamp) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "outPutAACData len"+len);
                    }
                };
                try {
                    mRunnables.put(runnable);
                } catch (InterruptedException e) {
                    Log.e(TAG, " =lgd= outputAudioData=====error: "+e.toString());
                    e.printStackTrace();
                }
            }
        });
    }
}
