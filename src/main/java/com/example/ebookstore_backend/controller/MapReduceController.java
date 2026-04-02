package com.example.ebookstore_backend.controller;

import com.example.ebookstore_backend.service.MapReduceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * MapReduce控制器
 * 提供关键词统计相关的REST API
 */
@RestController
@RequestMapping("/api/mapreduce")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MapReduceController {

    private final MapReduceService mapReduceService;

    /**
     * 执行关键词统计MapReduce作业
     * @return 统计结果
     */
    @PostMapping("/keyword-count")
    public ResponseEntity<?> executeKeywordCount() {
        try {
            log.info("收到执行关键词统计的请求");
            
            Map<String, Integer> results = mapReduceService.executeKeywordCount();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "关键词统计完成");
            response.put("totalKeywords", results.size());
            response.put("results", results);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("执行关键词统计失败", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "执行关键词统计失败: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * 获取最近一次MapReduce作业的结果
     * @return 统计结果
     */
    @GetMapping("/keyword-count/result")
    public ResponseEntity<?> getLastJobResult() {
        try {
            log.info("收到获取最近作业结果的请求");
            
            Map<String, Integer> results = mapReduceService.getLastJobResult();
            
            if (results.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "没有找到作业结果，请先执行关键词统计");
                response.put("results", results);
                
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "成功获取作业结果");
            response.put("totalKeywords", results.size());
            response.put("results", results);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取作业结果失败", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取作业结果失败: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * 清理MapReduce临时文件
     * @return 操作结果
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<?> cleanupTempFiles() {
        try {
            log.info("收到清理临时文件的请求");
            
            mapReduceService.cleanupTempFiles();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "临时文件清理完成");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("清理临时文件失败", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "清理临时文件失败: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * 健康检查接口
     * @return 服务状态
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "MapReduce Service");
        response.put("message", "MapReduce服务运行正常");
        
        return ResponseEntity.ok(response);
    }
}