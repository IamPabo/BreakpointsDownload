package com.sysom.rightlauncher.model;

/**
 * 线程信息
 */

public class ThreadInfo{
    private int threadId;
    private String threadUrl;
    private int start;
    private int end;
    private int finished;

    public ThreadInfo(int threadId, String threadUrl, int start, int end, int finished) {
        this.threadId = threadId;
        this.threadUrl = threadUrl;
        this.start = start;
        this.end = end;
        this.finished = finished;
    }

    public ThreadInfo() {
        super();
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public String getThreadUrl() {
        return threadUrl;
    }

    public void setThreadUrl(String threadUrl) {
        this.threadUrl = threadUrl;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStop() {
        return end;
    }

    public void setStop(int end) {
        this.end = end;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }
}
