package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.mapreduce.KeywordCountDriver;
import com.example.ebookstore_backend.service.BookDataExportService;
import com.example.ebookstore_backend.service.MapReduceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapReduceServiceImpl implements MapReduceService {

    private final BookDataExportService bookDataExportService;

    @Value("${mapreduce.input.dir:./hadoop-data/input}")
    private String inputDir;

    @Value("${mapreduce.output.dir:./hadoop-data/output}")
    private String outputDir;

    @Value("${mapreduce.keyword.file:./src/main/resources/keywords.txt}")
    private String keywordFile;

    @Override
    public Map<String, Integer> executeKeywordCount() throws Exception {
        log.info("开始执行关键词统计MapReduce作业");

        // 1. 准备输入数据：导出图书简介到文本文件
        log.info("步骤1: 导出图书简介数据");
        List<String> exportedFiles = bookDataExportService.exportBookDescriptionsByTag(inputDir);
        
        if (exportedFiles.isEmpty()) {
            log.warn("没有导出任何文件，无法执行MapReduce作业");
            return new HashMap<>();
        }
        
        log.info("成功导出 {} 个文件", exportedFiles.size());

        // 2. 清理旧的输出目录
        log.info("步骤2: 清理旧的输出目录");
        cleanOutputDirectory();

        // 3. 执行MapReduce作业
        log.info("步骤3: 执行MapReduce作业");
        log.info("输入目录: {}", inputDir);
        log.info("输出目录: {}", outputDir);
        log.info("关键词文件: {}", keywordFile);

        boolean success = KeywordCountDriver.runJob(inputDir, outputDir, keywordFile);

        if (!success) {
            log.error("MapReduce作业执行失败");
            throw new RuntimeException("MapReduce作业执行失败");
        }

        log.info("MapReduce作业执行成功");

        // 4. 读取并返回结果
        log.info("步骤4: 读取作业结果");
        Map<String, Integer> results = readJobResults();
        log.info("统计完成，共找到 {} 个关键词", results.size());

        return results;
    }

    @Override
    public Map<String, Integer> getLastJobResult() throws IOException {
        log.info("读取最近一次MapReduce作业结果");
        return readJobResults();
    }

    @Override
    public void cleanupTempFiles() throws IOException {
        log.info("清理临时文件");
        
        // 清理输入文件
        bookDataExportService.cleanExportedFiles(inputDir);
        
        // 清理输出文件
        cleanOutputDirectory();
        
        log.info("临时文件清理完成");
    }

    /**
     * 清理输出目录
     */
    private void cleanOutputDirectory() throws IOException {
        Path outputPath = Paths.get(outputDir);
        if (Files.exists(outputPath)) {
            try (Stream<Path> paths = Files.walk(outputPath)) {
                paths.sorted((a, b) -> b.compareTo(a)) // 反向排序，先删除文件再删除目录
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.warn("删除文件失败: {}", path, e);
                            }
                        });
            }
            log.info("输出目录已清理: {}", outputDir);
        }
    }

    /**
     * 读取MapReduce作业的输出结果
     */
    private Map<String, Integer> readJobResults() throws IOException {
        Map<String, Integer> results = new HashMap<>();
        
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            log.warn("输出目录不存在: {}", outputDir);
            return results;
        }

        // 查找part-r-00000文件（MapReduce的默认输出文件）
        File outputFile = new File(outputPath.toFile(), "part-r-00000");
        
        if (!outputFile.exists()) {
            log.warn("输出文件不存在: {}", outputFile.getAbsolutePath());
            return results;
        }

        log.info("读取输出文件: {}", outputFile.getAbsolutePath());

        try (BufferedReader reader = new BufferedReader(new FileReader(outputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 输出格式: keyword\tcount
                String[] parts = line.split("\t");
                if (parts.length == 2) {
                    String keyword = parts[0].trim();
                    int count = Integer.parseInt(parts[1].trim());
                    results.put(keyword, count);
                    log.debug("关键词: {}, 次数: {}", keyword, count);
                }
            }
        }

        return results;
    }
}