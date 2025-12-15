package com.gameengine.recording;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 录制存储抽象接口
 * 支持文件/网络/内存等多种实现
 */
public interface RecordingStorage {
    /**
     * 打开写入器
     */
    void openWriter(String path) throws IOException;
    
    /**
     * 写入一行数据
     */
    void writeLine(String line) throws IOException;
    
    /**
     * 关闭写入器
     */
    void closeWriter();
    
    /**
     * 读取所有行
     */
    Iterable<String> readLines(String path) throws IOException;
    
    /**
     * 列举所有录制文件
     */
    List<File> listRecordings();
}
