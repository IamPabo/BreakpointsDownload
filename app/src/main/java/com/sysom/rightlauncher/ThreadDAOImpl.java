package com.sysom.rightlauncher;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sysom.rightlauncher.db.DBHelper;
import com.sysom.rightlauncher.model.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口实现类
 */

public class ThreadDAOImpl implements ThreadDAO{

    private DBHelper mHelper = null;

    public ThreadDAOImpl(Context context){
        mHelper = new DBHelper(context);
    }

    @Override
    public void insertThread(ThreadInfo info) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL(
                "insert into thread_info(thread_id,url,start,end,finished) values(?,?,?,?,?)",
                new Object[]{info.getThreadId(),info.getThreadUrl(),
                        info.getStart(),info.getStop(),info.getFinished()}
        );
        db.close();
    }

    @Override
    public void deleteThread(String url, int thread_id) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL(
                "delete from thread_info where url=? and thread_id=?",
                new Object[]{url,thread_id}
        );
        db.close();
    }

    @Override
    public void updateThread(String url, int thread_id, int finished) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.execSQL(
                "update thread_info set url=?,thread_id=?,finished=?",
                new Object[]{url,thread_id,finished}
        );
        db.close();
        Log.d("ThreadDAOImpl", "updateThread: success  finished : " + finished);
    }

    @Override
    public List<ThreadInfo> getThreads(String url) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        List<ThreadInfo> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "select * from thread_info where url=?",
                new String[]{url}
        );
        while (cursor.moveToNext()){
            ThreadInfo info = new ThreadInfo();
            info.setThreadId(cursor.getColumnIndex("thread_id"));
            info.setThreadUrl(cursor.getString(cursor.getColumnIndex("url")));
            info.setStart(cursor.getColumnIndex("start"));
            info.setStop(cursor.getColumnIndex("end"));
            info.setFinished(cursor.getColumnIndex("finished"));
            list.add(info);
        }
        cursor.close();
        db.close();
        return list;
    }

    @Override
    public boolean isExists(String url, int thread_id) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "select * from thread_info where url=? and thread_id=?",
                new String[]{url,String.valueOf(thread_id)}
        );
        boolean isExists = cursor.moveToNext();
        cursor.close();
        db.close();
        return isExists;
    }
}
