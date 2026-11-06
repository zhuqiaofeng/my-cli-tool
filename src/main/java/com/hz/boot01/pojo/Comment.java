package com.hz.boot01.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * ============================================================
 * MongoDB 文档实体类：评论
 * ============================================================
 *
 * 【整体数据流转图（发评论）】
 *
 *   浏览器/Postman
 *       │
 *       ▼
 *   CommentController.add()          ← 第1步：接收HTTP请求
 *       │
 *       ▼
 *   new Comment(...)                 ← 第2步：创建Java对象
 *       │
 *       ▼
 *   CommentRepository.save()         ← 第3步：调用Repository保存
 *       │
 *       ▼
 *   Spring Data MongoDB 自动生成实现类 ← 第4步：框架帮你写好SQL
 *       │
 *       ▼
 *   MongoDB 服务器（27017端口）       ← 第5步：真正存到数据库
 *       │
 *       ▼
 *   返回保存后的ID给前端
 *
 * 【和 MySQL 实体类的区别】
 * ┌─────────┬─────────────────┬─────────────────┐
 * │         │ MySQL           │ MongoDB         │
 * ├─────────┼─────────────────┼─────────────────┤
 * │ 注解    │ @Table          │ @Document       │
 * │ 概念    │ 表 Table        │ 集合 Collection │
 * │ 一条数据│ 一行 Row        │ 一个文档 Document│
 * │ 主键    │ @Id 自增        │ @Id 自动生成ObjectId│
 * │ 字段    │ 必须固定        │ 可以不固定，灵活 │
 * └─────────┴─────────────────┴─────────────────┘
 * ============================================================
 */
@Document(collection = "comment")  // 指定这个类对应MongoDB里叫"comment"的集合（相当于MySQL的表）
public class Comment {

    /**
     * 主键ID
     * MongoDB 会自动生成一个24位的字符串ID，比如：6aec7a63213f2a6e7531181b
     * 你不用手动赋值，save()之后ID会自动塞进这个对象里
     */
    @Id
    private String id;

    private String articleId;    // 文章ID：这条评论属于哪篇文章
    private String content;      // 评论内容：比如"写得好"
    private String author;       // 评论人：比如"张三"
    private LocalDateTime createTime;  // 评论时间：自动取当前时间

    // 构造方法、getter、setter
    public Comment() {}

    /**
     * 构造方法：创建评论的时候自动设置当前时间
     */
    public Comment(String articleId, String content, String author) {
        this.articleId = articleId;
        this.content = content;
        this.author = author;
        this.createTime = LocalDateTime.now();  // 创建评论时自动记录时间
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getArticleId() { return articleId; }
    public void setArticleId(String articleId) { this.articleId = articleId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
