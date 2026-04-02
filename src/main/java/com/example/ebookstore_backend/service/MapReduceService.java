package com.example.ebookstore_backend.service;

import java.io.IOException;
import java.util.Map;

public interface MapReduceService {
    
    /**
     * 执行关键词统计MapReduce作业
     * @return 统计结果，键为关键词，值为出现次数
     * @throws Exception MapReduce执行异常
     */
    Map<String, Integer> executeKeywordCount() throws Exception;
    
    /**
     * 获取最近一次MapReduce作业的结果
     * @return 统计结果
     * @throws IOException 读取结果文件异常
     */
    Map<String, Integer> getLastJobResult() throws IOException;
    
    /**
     * 清理MapReduce相关的临时文件
     * @throws IOException 文件操作异常
     */
    void cleanupTempFiles() throws IOException;
}