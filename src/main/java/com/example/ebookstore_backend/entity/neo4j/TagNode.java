package com.example.ebookstore_backend.entity.neo4j;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Node("Tag")
@Data
@NoArgsConstructor
public class TagNode {

    @Id
    private String name;

    // 定义关系：SUB_CATEGORY (子分类)
    // 方向为 OUTGOING，表示 "当前标签 -> 包含 -> 子标签"
    @Relationship(type = "SUB_CATEGORY", direction = Relationship.Direction.OUTGOING)
    private Set<TagNode> children = new HashSet<>();

    public TagNode(String name) {
        this.name = name;
    }
    
    public void addChild(TagNode child) {
        this.children.add(child);
    }
}
