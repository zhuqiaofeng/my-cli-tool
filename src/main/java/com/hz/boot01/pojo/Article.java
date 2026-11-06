package com.hz.boot01.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

/**
 * ============================================================
 * Elasticsearch 文档实体类：文章
 * ============================================================
 *
 * 【整体数据流转图（添加文章）】
 *
 *   浏览器/Postman
 *       │
 *       ▼
 *   ArticleController.add()          ← 第1步：接收HTTP请求
 *       │
 *       ▼
 *   new Article(...)                 ← 第2步：创建Java对象
 *       │
 *       ▼
 *   ArticleRepository.save()         ← 第3步：调用Repository保存
 *       │
 *       ▼
 *   Spring Data Elasticsearch 自动生成实现类 ← 第4步：框架帮你生成查询
 *       │
 *       ▼
 *   通过HTTP发给ES服务器（127.0.0.1:9200） ← 第5步：ES是HTTP接口！
 *       │
 *       ▼
 *   Elasticsearch 服务器
 *   - 把文章内容分词，建立倒排索引
 *   - 存到 article 索引里
 *       │
 *       ▼
 *   返回自动生成的ID给前端
 *
 * 【ES 和 MySQL、MongoDB 的核心区别】
 * ┌─────────┬─────────────────┬─────────────────┬─────────────────┐
 * │         │ MySQL           │ MongoDB         │ Elasticsearch   │
 * ├─────────┼─────────────────┼─────────────────┼─────────────────┤
 * │ 作用    │ 存业务数据      │ 存灵活JSON数据  │ 全文检索/搜索    │
 * │ 概念    │ 表 Table        │ 集合 Collection │ 索引 Index      │
 * │ 一条数据│ 一行 Row        │ 一个文档 Document│ 一个文档 Document│
 * │ 查询方式│ 精确查询快      │ 条件查询        │ 模糊搜索、分词   │
 * │ 特点    │ 事务强          │ 灵活Schema      │ 搜索快、支持分词 │
 * │ 接口    │ JDBC/TCP        │ 二进制协议      │ HTTP RESTful    │
 * └─────────┴─────────────────┴─────────────────┴─────────────────┘
 *
 * 【ES 最厉害的是什么？】
 * 是**倒排索引**！
 * 比如你有100万篇文章，搜"Spring"：
 * - MySQL：要一行一行扫，where title like '%Spring%'，慢死
 * - ES：提前把所有文章分词，建立一个"词→文章列表"的索引
 *   直接查"Spring"这个词对应哪些文章，毫秒级出结果
 *
 * 【Text 和 Keyword 字段类型的区别】
 * - Text：会分词，适合搜内容
 *   比如"SpringBoot入门教程"会被分成"Spring"、"Boot"、"入门"、"教程"
 *   你搜"Spring"就能匹配到
 * - Keyword：不分词，适合精确匹配
 *   比如作者名"张三"，你必须搜"张三"才能匹配，搜"张"匹配不到
 * ============================================================
 */
@Document(indexName = "article")  // 对应ES里叫"article"的索引（相当于MySQL的表）
public class Article {

    /**
     * 主键ID
     * ES 自动生成一个字符串ID，比如：s2UsrKEBHNFgAP_mO9oV
     */
    @Id
    private String id;

    @Field(type = FieldType.Text)  // Text类型会分词，适合搜索
    private String title;       // 文章标题

    @Field(type = FieldType.Text)  // 内容也分词
    private String content;     // 文章内容

    @Field(type = FieldType.Keyword)  // Keyword类型不分词，精确匹配
    private String author;      // 作者

    private String createTime;  // 创建时间（学习demo用String避免类型转换问题）

    public Article() {}

    public Article(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.createTime = LocalDateTime.now().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
