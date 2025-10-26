package com.example.ebookstore_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

/**
 * 自定义分页结果类，用于Redis缓存
 * 解决 PageImpl 无法反序列化的问题
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private List<T> content;          // 数据列表
    private int pageNumber;           // 当前页码
    private int pageSize;             // 每页大小
    private long totalElements;       // 总元素数
    private int totalPages;           // 总页数
    private boolean first;            // 是否第一页
    private boolean last;             // 是否最后一页
    private boolean empty;            // 是否为空
    
    /**
     * 从Spring Data的Page对象转换
     */
    public static <T> PageResult<T> from(Page<T> page) {
        return new PageResult<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.isEmpty()
        );
    }
}