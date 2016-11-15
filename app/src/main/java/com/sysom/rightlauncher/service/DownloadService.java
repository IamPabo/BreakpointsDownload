package com.sysom.rightlauncher.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sysom.rightlauncher.model.FileInfo;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * 下载服务
 */

public class DownloadService extends Service{
    private static final String TAG = "DownloadService";

    public static final String DOWNLOAD_FILE_PATH =
            Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/downloads/";
    public static final String DOWNLOAD_START = "download_start";
    public static final String DOWNLOAD_STOP = "download_stop";
    public static final String DOWNLOAD_ACTION_UPDATE = "update_download ";
    private static final int MSG_INIT = 0;
    private DownloadTask mTask;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //判断点击状态
        if(DOWNLOAD_START.equals(intent.getAction())){
            //获取传过来的FileInfo
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            //启动子线程初始化FileInfo
            //所有网络操作要在子线程完成防止主线程发生堵塞
            new mThread(fileInfo).start();
            Log.i(TAG,"download start"+fileInfo.toString());
        }else if(DOWNLOAD_STOP.equals(intent.getAction())){
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            if(mTask != null){
                mTask.isPause = true;
            }
            Log.i(TAG,"download stop");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_INIT:
                    //获取传过来的FileInfo对象
                    FileInfo info = (FileInfo) msg.obj;
                    Log.i(TAG, "handleMessage: " + info.toString());
                    mTask = new DownloadTask(DownloadService.this,info);
                    mTask.download();
                    break;
            }
        }
    };

    class mThread extends Thread{
        private FileInfo info;
        //初始化
        mThread(FileInfo info){
            this.info = info;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            try {
                //转换URL
                URL mUrl = new URL(info.getFileUrl());
                Log.d(TAG, "run: "+ mUrl);
                //打开链接
                conn = (HttpURLConnection) mUrl.openConnection();
                //设置链接时间 3000毫秒
                conn.setConnectTimeout(3000);
                //设置请求方式为 get
                conn.setRequestMethod("GET");
                int length;
                //网络链接响应OK
                //添加依赖包（HttpStatus.SC_OK） compile 'org.jbundle.util.osgi.wrapped:org.jbundle.util.osgi.wrapped.org.apache.http.client:4.1.2'
                if(conn.getResponseCode() != HttpURLConnection.HTTP_OK){
                    return;
                }
                //获取文件长度
                length = conn.getContentLength();
                if(length <= 0){
                    return;
                }
                //创建本地文件
                File mFile = new File(DOWNLOAD_FILE_PATH);
                //判断目录是否存在
                //不存在创建目录
                if(!mFile.exists()) mFile.mkdir();
                //目录下创建文件（目录文件，文件名）
                File file = new File(mFile,info.getFileName());
                //设置文件 r read(读) w write(写) d delete（删除）
                raf = new RandomAccessFile(file,"rwd");
                //设置文件长度
                raf.setLength(length);
                //更新FileInfo长度信息
                info.setFileLenght(length);
                //用Handler发送初始化后的FileInfo对象，回归主线程操作
                mHandler.obtainMessage(MSG_INIT,info).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    //关闭HttpsURLConnection和RandomAccessFile,防止内存泄露
                    if(conn != null) {
                        conn.disconnect();
                    }
                    if(raf != null) {
                        raf.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
