package com.sysom.rightlauncher.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sysom.rightlauncher.ThreadDAO;
import com.sysom.rightlauncher.ThreadDAOImpl;
import com.sysom.rightlauncher.model.FileInfo;
import com.sysom.rightlauncher.model.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * 下载任务类
 */

class DownloadTask {
    private Context mContext;
    private FileInfo mFileInfo;
    private ThreadDAO mDAO;
    private int mFinished = 0;
    boolean isPause = false;

    DownloadTask(Context context, FileInfo fileInfo) {
        mContext = context;
        mFileInfo = fileInfo;
        mDAO = new ThreadDAOImpl(context);
    }

    void download(){
        //读取数据库的线程信息
        List<ThreadInfo> threadInfos = mDAO.getThreads(mFileInfo.getFileUrl());
        ThreadInfo threadInfo;
        if(threadInfos.size() == 0){
            //初始化线程信息
            threadInfo = new ThreadInfo(0,mFileInfo.getFileUrl(),0,mFileInfo.getFileLenght(),0);
        }else{
            threadInfo = threadInfos.get(0);
        }
        new DownloadThread(threadInfo).start();
    }

    /**
     * 下载线程
     */
    private class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo;

        DownloadThread(ThreadInfo threadInfo) {
            mThreadInfo = threadInfo;
        }

        @Override
        public void run() {
            //向数据库插入线程信息
            if (!mDAO.isExists(mThreadInfo.getThreadUrl(), mThreadInfo.getThreadId())) {
                mDAO.insertThread(mThreadInfo);
            }
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream input = null;
            try {
                URL mUrl = new URL(mThreadInfo.getThreadUrl());
                conn = (HttpURLConnection) mUrl.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                //设置下载位置(开始的位置 + 已完成的部分)
                int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                Log.d(TAG, "run start: " + start);
                //设置下载的Byte字节范围
                conn.setRequestProperty("Range", "byte=" + start + "-" + mThreadInfo.getFinished());
                //设置文件写入位置
                File file = new File(DownloadService.DOWNLOAD_FILE_PATH, mFileInfo.getFileName());
                //读写删除
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                //发送广播更新进度
                Intent intent = new Intent(DownloadService.DOWNLOAD_ACTION_UPDATE);
                //更新已完成下载部分的值
                mFinished += mThreadInfo.getFinished();
                Log.d(TAG, "Finished value : " + mFinished);
                //开始下载
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //读取数据
                    input = conn.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    //获取当前耗时
                    long time = System.currentTimeMillis();
                    while ((len = input.read(buffer)) != -1) {
                        //写入文件
                        raf.write(buffer, 0, len);
                        //把下载进度广播发送给Activity
                        mFinished += len;
                        //时间间隔500毫秒计算一次
                        if (System.currentTimeMillis() - time >= 500) {
                            //获取当前耗时
                            time = System.currentTimeMillis();
                            //设置Intent的参数发送给ProgressBar
                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getFileLenght());
                            mContext.sendBroadcast(intent);
                        }
                        //在下载暂停时，保存下载进度
                        if (isPause) {
                            mDAO.updateThread(mThreadInfo.getThreadUrl(), mThreadInfo.getThreadId(), mFinished);
                            return;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                try {
                    if (raf != null) {
                        raf.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
