package com.example.ebookstore_backend.mapreduce;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mapper类：读取图书简介文本，统计关键词出现次数（按书籍去重）
 * 输入：文本文件的每一行
 * 输出：<书籍ID:关键词, 1>
 */
public class KeywordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    private Set<String> keywords = new HashSet<>();
    private Map<String, String> keywordOriginalCase = new HashMap<>(); // 存储原始大小写
    private String currentBookId = null;
    private Map<String, Set<String>> bookKeywords = new HashMap<>();

    /**
     * 在setup阶段加载关键词列表
     */
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        
        // 从配置中获取关键词文件路径
        String keywordFilePath = context.getConfiguration().get("keyword.file.path");
        
        if (keywordFilePath != null && !keywordFilePath.isEmpty()) {
            loadKeywords(keywordFilePath);
        } else {
            // 如果没有指定文件，使用默认关键词
            loadDefaultKeywords();
        }
        
        System.out.println("Loaded " + keywords.size() + " keywords");
    }

    /**
     * 从文件加载关键词
     */
    private void loadKeywords(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String keyword = line.trim();
                if (!keyword.isEmpty()) {
                    // 存储小写版本用于匹配，同时保存原始大小写
                    String lowerKeyword = keyword.toLowerCase();
                    keywords.add(lowerKeyword);
                    keywordOriginalCase.put(lowerKeyword, keyword);
                }
            }
        }
    }

    /**
     * 加载默认关键词（备用方案）
     */
    private void loadDefaultKeywords() {
        keywords.add("java");
        keywords.add("javascript");
        keywords.add("python");
        keywords.add("c++");
        keywords.add("programming");
        keywords.add("algorithm");
        keywords.add("data");
        keywords.add("structure");
        keywords.add("computer");
        keywords.add("science");
    }

    /**
     * Map函数：处理每一行文本
     */
    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        
        String line = value.toString();
        // 检查是否是书籍ID行
        if (line.startsWith("BOOK_ID:")) {
            // 提取书籍ID
            int endIndex = line.indexOf(" ===");
            if (endIndex > 0) {
                currentBookId = line.substring(8, endIndex).trim();
                bookKeywords.put(currentBookId, new HashSet<>());
            }
            return;
        }
        if (currentBookId == null) {
            return;
        }
        String lowerLine = line.toLowerCase();
        for (String keyword : keywords) {
            if (lowerLine.contains(keyword)) {
                bookKeywords.get(currentBookId).add(keyword);
            }
        }
    }
    
    /**
     * Cleanup函数：在所有map任务完成后，输出去重后的结果
     */
    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        // 对每本书的每个关键词只输出一次
        for (Map.Entry<String, Set<String>> entry : bookKeywords.entrySet()) {
            String bookId = entry.getKey();
            Set<String> bookKeywordSet = entry.getValue();
            
            for (String keyword : bookKeywordSet) {
                String originalKeyword = findOriginalKeyword(keyword);
                word.set(originalKeyword);
                context.write(word, one);
            }
        }
        
        super.cleanup(context);
    }

    /**
     * 查找关键词的原始大小写形式
     */
    private String findOriginalKeyword(String lowerKeyword) {
        // 从映射表中获取原始大小写
        return keywordOriginalCase.getOrDefault(lowerKeyword, lowerKeyword);
    }
}