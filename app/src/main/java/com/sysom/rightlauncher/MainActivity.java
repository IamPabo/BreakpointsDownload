package com.sysom.rightlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.sysom.rightlauncher.model.FileInfo;
import com.sysom.rightlauncher.service.DownloadService;

import static com.sysom.rightlauncher.service.DownloadService.DOWNLOAD_ACTION_UPDATE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";
    private static final String DOWNLOAD_URL = "http://downmobile.kugou.com/Android/KugouPlayer/7840/KugouPlayer_219_V7.8.4.apk";

    private ProgressBar mProgressBar;
    private FileInfo mFileInfo;
    private mBraodcastReceiver mBraodcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        Button btnStop = (Button) findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(this);
        Button btnStart = (Button) findViewById(R.id.btn_start);
        btnStart.setOnClickListener(this);
        mFileInfo = new FileInfo(0,0,"haozip",DOWNLOAD_URL);
        mProgressBar.setMax(100);
        IntentFilter intentFliter = new IntentFilter();
        intentFliter.addAction(DOWNLOAD_ACTION_UPDATE);
        mBraodcastReceiver = new mBraodcastReceiver();
        registerReceiver(mBraodcastReceiver,intentFliter);
    }

    public void onClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.btn_stop:
                //点击停止
                Log.d(TAG, "onClick: ");
                intent.setAction(DownloadService.DOWNLOAD_STOP);
                intent.putExtra("fileInfo",mFileInfo);
                intent.setClass(this,DownloadService.class);
                startService(intent);
                break;
            case R.id.btn_start:
                //点击开始
                intent.setAction(DownloadService.DOWNLOAD_START);
                intent.putExtra("fileInfo",mFileInfo);
                intent.setClass(this,DownloadService.class);
                startService(intent);
                break;
        }
    }

    class mBraodcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: " + action);
            if(action.equals(DOWNLOAD_ACTION_UPDATE)){
                mProgressBar.setProgress(intent.getIntExtra("finished",0));
                mFileInfo.setFinish(intent.getIntExtra("finished",0));
                Log.d(TAG, "progress: " + intent.getIntExtra("finished",0));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBraodcastReceiver != null){
            unregisterReceiver(mBraodcastReceiver);
        }
    }
}
