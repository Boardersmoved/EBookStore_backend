package com.example.ebookstore_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 100)
    private String author;

    @Column(name = "isbn", length = 20, unique = true)
    private String isbn;
    
    @Lob
    @Column(name = "cover_image_base64")
    private String coverImageBase64;

    @Column(nullable = false, precision = 10, scale = 2) // 10位总精度，小数点后2位
    private BigDecimal price;

    @Lob // 用于存储大文本对象
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sales", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer sales = 0;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "is_available")
    private Boolean isAvailable = true; // 新增字段：是否上架（true=上架，false=下架）

    // 多对多关系: 一本书可以有多个标签，一个标签可以用于多本书
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "book_tags", // 中间表名
            joinColumns = @JoinColumn(name = "book_id"), // 当前实体(Book)在中间表的外键
            inverseJoinColumns = @JoinColumn(name = "tag_id") // 对方实体(Tag)在中间表的外键
    )
    private Set<Tag> tags = new HashSet<>();
}
//```
//        * **`@Entity`**: 声明这是一个JPA实体。
//        * **`@Table(name = "...")`**: 指定对应的数据库表名。
//        * **`@Id` 和 `@GeneratedValue`**: 定义主键及其生成策略。
//        * **`@Column`**: 定义字段与数据库列的映射，可以指定长度、是否可空等。
//        * **`@ManyToMany`**: 定义多对多关系。`WorkspaceType.EAGER` 表示加载Book时会立即加载其关联的Tag。`@JoinTable` 配置了中间表。
//        * **Lombok注解 (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`)**: 自动生成相应的Java代码。