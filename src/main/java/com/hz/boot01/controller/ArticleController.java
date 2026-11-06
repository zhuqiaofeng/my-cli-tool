package com.hz.boot01.controller;

import com.hz.boot01.pojo.Article;
import com.hz.boot01.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Elasticsearch 搜索引擎接口示例
 * ============================================================
 *
 * 【完整调用顺序（添加文章）】
 *
 *   1. 前端发POST请求：POST /article/add?title=SpringBoot入门&content=xxx&author=张三
 *   2. SpringMVC把请求交给 add() 方法
 *   3. add()方法 new 一个 Article 对象
 *   4. 调用 articleRepository.save(article)
 *   5. Spring Data ES 自动生成的实现类收到调用
 *   6. 它把 Article 对象转成 JSON
 *   7. 通过 HTTP POST 发给 ES 服务器（127.0.0.1:9200）
 *      注意：ES 是 HTTP 接口！和 MySQL/MongoDB 的二进制协议不一样
 *   8. ES 服务器收到后：
 *      - 把 title 和 content 分词，建立倒排索引
 *      - 把文档存到 article 索引里
 *   9. ES 返回自动生成的 _id
 *  10. Spring 把 _id 塞回 Article 对象的 id 属性
 *  11. add()方法返回"添加成功，ID：xxx"给前端
 *
 * 【完整调用顺序（查全部文章）】
 *
 *   1. 前端发GET请求：GET /article/list
 *   2. 调用 articleRepository.findAll()
 *   3. Spring 翻译成 ES 查询：GET /article/_search
 *   4. 通过 HTTP 发给 ES 服务器
 *   5. ES 返回所有文档的 JSON 数组
 *   6. Spring 自动把 JSON 转成 List<Article> Java对象
 *   7. 返回给前端
 *
 * 【ES 最核心的概念：倒排索引】
 * 传统数据库：文章 → 内容
 *   文章1："Spring Boot 教程"
 *   文章2："MySQL 入门"
 *
 * ES 倒排索引：词 → 哪些文章有这个词
 *   "Spring" → [文章1]
 *   "Boot"   → [文章1]
 *   "教程"   → [文章1]
 *   "MySQL"  → [文章2]
 *
 * 你搜"Spring"，直接查这个词对应哪篇文章，毫秒级出结果
 * 这就是为什么 ES 搜索比 MySQL like 快那么多！
 * ============================================================
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    /**
     * 添加文章
     * 调用方式：POST /article/add?title=SpringBoot入门&content=这是SpringBoot教程&author=张三
     */
    @PostMapping("/add")
    public String add(String title, String content, String author) {
        // 创建Article对象，自动设置当前时间
        Article article = new Article(title, content, author);

        // 保存到ES，save()执行完后，ES自动生成的ID会自动塞到article对象里
        articleRepository.save(article);

        return "添加成功，ID：" + article.getId();
    }

    /**
     * 根据标题精确查
     * 调用方式：GET /article/findByTitle?title=SpringBoot入门
     */
    @GetMapping("/findByTitle")
    public List<Article> findByTitle(String title) {
        return articleRepository.findByTitle(title);
    }

    /**
     * 查全部文章
     */
    @GetMapping("/list")
    public List<Article> list() {
        List<Article> list = new ArrayList<>();
        articleRepository.findAll().forEach(list::add);
        return list;
    }
}
