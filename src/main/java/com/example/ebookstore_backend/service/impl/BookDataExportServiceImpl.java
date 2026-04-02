package com.example.ebookstore_backend.service.impl;

import com.example.ebookstore_backend.entity.Book;
import com.example.ebookstore_backend.entity.Tag;
import com.example.ebookstore_backend.repository.BookRepository;
import com.example.ebookstore_backend.repository.TagRepository;
import com.example.ebookstore_backend.service.BookDataExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookDataExportServiceImpl implements BookDataExportService {

    private final BookRepository bookRepository;
    private final TagRepository tagRepository;

    @Override
    @Transactional(readOnly = true)
    public List<String> exportBookDescriptionsByTag(String outputDir) throws IOException {
        log.info("开始按标签导出图书简介到目录: {}", outputDir);
        
        // 创建输出目录
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
            log.info("创建输出目录: {}", outputDir);
        }
        
        List<String> exportedFiles = new ArrayList<>();
        
        // 获取所有标签
        List<Tag> allTags = tagRepository.findAll();
        log.info("找到 {} 个标签", allTags.size());
        
        if (allTags.isEmpty()) {
            log.warn("没有找到任何标签，创建默认分类文件");
            // 如果没有标签，将所有书籍导出到一个默认文件
            String defaultFile = exportBooksWithoutTag(outputDir);
            if (defaultFile != null) {
                exportedFiles.add(defaultFile);
            }
        } else {
            // 按标签导出
            for (Tag tag : allTags) {
                String fileName = exportBooksByTag(tag, outputDir);
                if (fileName != null) {
                    exportedFiles.add(fileName);
                }
            }
            
            // 导出没有标签的图书
            String noTagFile = exportBooksWithoutTag(outputDir);
            if (noTagFile != null) {
                exportedFiles.add(noTagFile);
            }
        }
        
        log.info("导出完成，共生成 {} 个文件", exportedFiles.size());
        return exportedFiles;
    }

    /**
     * 按标签导出图书简介
     */
    private String exportBooksByTag(Tag tag, String outputDir) throws IOException {
        List<Book> books = bookRepository.findAll().stream()
                .filter(book -> book.getTags().contains(tag))
                .collect(Collectors.toList());
        
        if (books.isEmpty()) {
            log.info("标签 '{}' 下没有图书", tag.getName());
            return null;
        }
        
        // 清理标签名称，移除特殊字符
        String sanitizedTagName = tag.getName().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_");
        String fileName = sanitizedTagName + ".txt";
        String filePath = Paths.get(outputDir, fileName).toString();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Book book : books) {
                if (book.getDescription() != null && !book.getDescription().trim().isEmpty()) {
                    // 写入图书ID、标题和简介（用于去重统计）
                    writer.write("BOOK_ID:" + book.getId() + " === " + book.getTitle() + " ===");
                    writer.newLine();
                    writer.write(book.getDescription());
                    writer.newLine();
                    writer.newLine();
                }
            }
        }
        
        log.info("导出标签 '{}' 的 {} 本图书到文件: {}", tag.getName(), books.size(), fileName);
        return fileName;
    }

    /**
     * 导出没有标签的图书
     */
    private String exportBooksWithoutTag(String outputDir) throws IOException {
        List<Book> booksWithoutTag = bookRepository.findAll().stream()
                .filter(book -> book.getTags() == null || book.getTags().isEmpty())
                .collect(Collectors.toList());
        
        if (booksWithoutTag.isEmpty()) {
            log.info("没有找到无标签的图书");
            return null;
        }
        
        String fileName = "Uncategorized.txt";
        String filePath = Paths.get(outputDir, fileName).toString();
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Book book : booksWithoutTag) {
                if (book.getDescription() != null && !book.getDescription().trim().isEmpty()) {
                    // 写入图书ID、标题和简介（用于去重统计）
                    writer.write("BOOK_ID:" + book.getId() + " === " + book.getTitle() + " ===");
                    writer.newLine();
                    writer.write(book.getDescription());
                    writer.newLine();
                    writer.newLine();
                }
            }
        }
        
        log.info("导出 {} 本无标签图书到文件: {}", booksWithoutTag.size(), fileName);
        return fileName;
    }

    @Override
    public void cleanExportedFiles(String outputDir) throws IOException {
        Path outputPath = Paths.get(outputDir);
        if (Files.exists(outputPath) && Files.isDirectory(outputPath)) {
            Files.walk(outputPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("删除文件: {}", path.getFileName());
                        } catch (IOException e) {
                            log.error("删除文件失败: {}", path.getFileName(), e);
                        }
                    });
            log.info("清理导出文件完成");
        }
    }
}