package com.sysom.rightlauncher.service;

import android.content.Context;
import android.content.Intent;

import com.sysom.rightlauncher.ThreadDAO;
import com.sysom.rightlauncher.ThreadDAOImpl;
import com.sysom.rightlauncher.model.FileInfo;
import com.sysom.rightlauncher.model.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * 下载任务类
 */

public class DownloadTask {
    private Context mContext;
    private FileInfo mFileInfo;
    private ThreadDAO mDAO;
    private int mFinished = 0;
    public boolean isPause = false;

    public DownloadTask(Context context, FileInfo fileInfo) {
        mContext = context;
        mFileInfo = fileInfo;
        mDAO = new ThreadDAOImpl(context);
    }

    public void download(){
        //读取数据库的线程信息
        List<ThreadInfo> threadInfos = mDAO.getThreads(mFileInfo.getFileUrl());
        ThreadInfo threadInfo = null;
        if(threadInfos.size() == 0){
            //初始化线程信息
            threadInfo = new ThreadInfo(0,mFileInfo.getFileUrl(),0,mFileInfo.getFileLenght(),0);
        }else{
            threadInfo = threadInfos.get(0);
        }
        new DownloadThread(threadInfo);
    }

    /**
     * 下载线程
     */
    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo;

        public DownloadThread(ThreadInfo threadInfo) {
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
                conn.setRequestProperty("Range", "byte=" + start + "-" + mThreadInfo.getStop());
                //设置文件写入位置
                File file = new File(DownloadService.DOWNLOAD_FILE_PATH, mFileInfo.getFileName());
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(start);
                Intent intent = new Intent(DownloadService.DOWNLOAD_ACTION_UPDATE);
                mFinished += mThreadInfo.getFinished();
                //开始下载
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //读取数据
                    input = conn.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = input.read(buffer)) != -1) {
                        //写入文件
                        raf.write(buffer, 0, len);
                        //把下载进度广播发送给Activity
                        mFinished += len;
                        if (System.currentTimeMillis() - time >= 500) {
                            time = System.currentTimeMillis();
                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getFileLenght());
                            mContext.sendBroadcast(intent);
                        }
                        if (isPause) {
                            mDAO.updateThread(mThreadInfo.getThreadUrl(), mThreadInfo.getThreadId(), mFinished);
                            return;
                        }
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
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


            //在下载暂停时，保存下载进度

        }
    }
}
