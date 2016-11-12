package com.sysom.rightlauncher.model;

import java.io.Serializable;

/**
 * 文件信息
 */

public class FileInfo implements Serializable{
    private int fileId;
    private int fileLenght;
    private String fileName;
    private String fileUrl;
    private boolean isFinish;

    public FileInfo(int fileId,int fileLenght,String fileName,String fileUrl){
        this.fileId = fileId;
        this.fileLenght = fileLenght;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public int getFileLenght() {
        return fileLenght;
    }

    public void setFileLenght(int fileLenght) {
        this.fileLenght = fileLenght;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void setFinish(boolean finish) {
        isFinish = finish;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "fileId=" + fileId +
                ", fileLenght=" + fileLenght +
                ", fileName='" + fileName + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", isFinish=" + isFinish +
                '}';
    }
}
