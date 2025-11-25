package com.example.ebookstore_backend.repository;

import com.example.ebookstore_backend.entity.neo4j.TagNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TagNodeRepository extends Neo4jRepository<TagNode, String> {

    // 查找与目标标签距离在 0 到 2 之间的所有相关标签名称
    // (t)-[*0..2]-(related) 表示无向遍历，即父类和子类都会被查出来
    @Query("MATCH (t:Tag {name: $tagName})-[*0..2]-(related:Tag) RETURN DISTINCT related.name")
    List<String> findRelatedTags(String tagName);
}
