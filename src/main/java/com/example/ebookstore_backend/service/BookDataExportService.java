package com.example.ebookstore_backend.service;

import java.io.IOException;
import java.util.List;

public interface BookDataExportService {
    
    /**
     * 按图书类型导出所有图书简介到文本文件
     * @param outputDir 输出目录路径
     * @return 导出的文件列表
     * @throws IOException 文件操作异常
     */
    List<String> exportBookDescriptionsByTag(String outputDir) throws IOException;
    
    /**
     * 清理导出的文件
     * @param outputDir 输出目录路径
     * @throws IOException 文件操作异常
     */
    void cleanExportedFiles(String outputDir) throws IOException;
}