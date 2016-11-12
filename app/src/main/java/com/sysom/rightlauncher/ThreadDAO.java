package com.sysom.rightlauncher;

import com.sysom.rightlauncher.model.ThreadInfo;

import java.util.List;

/**
 * 数据库访问接口
 */

public interface ThreadDAO {
    /**
     * 插入线程
     */
    public void insertThread(ThreadInfo info);

    /**
     * 删除线程
     */
    public void deleteThread(String url,int thread_id);

    /**
     * 更新下载进度
     */
    public void updateThread(String url,int thread_id,int finished);

    /**
     * 查询文件的线程信息
     */
    public List<ThreadInfo> getThreads(String url);

    /**
     * 线程是否存在
     */
    public boolean isExists(String url,int thread_id);
}
