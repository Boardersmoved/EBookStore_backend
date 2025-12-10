package com.example.ebookstore_backend.repository;

import com.example.ebookstore_backend.entity.neo4j.TagNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TagNodeRepository extends Neo4jRepository<TagNode, String> {

    // 查找与目标标签距离在 0 到 2 之间的所有相关标签名称
    // 使用路径过滤，确保遍历路径中的所有节点都不是"图书总库"
    // 这样可以避免通过根节点关联到无关分类
    @Query("MATCH path = (t:Tag {name: $tagName})-[*0..2]-(related:Tag) " +
           "WHERE ALL(node IN nodes(path) WHERE node.name <> '图书总库') " +
           "RETURN DISTINCT related.name")
    List<String> findRelatedTags(String tagName);
}
