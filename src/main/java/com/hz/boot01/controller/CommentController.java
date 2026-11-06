package com.hz.boot01.controller;

import com.hz.boot01.repository.CommentRepository;
import com.hz.boot01.pojo.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================
 * MongoDB 评论接口示例
 * ============================================================
 *
 * 【完整调用顺序（发评论）】
 *
 *   1. 前端发POST请求：POST /comment/add?articleId=100&content=写得好&author=张三
 *   2. SpringMVC把请求交给 add() 方法
 *   3. add()方法 new 一个 Comment 对象，把参数塞进去
 *   4. 调用 commentRepository.save(comment)
 *   5. Spring Data MongoDB 自动生成的实现类收到调用
 *   6. 它把 Comment 对象转成 JSON 格式
 *   7. 通过网络发给 MongoDB 服务器（127.0.0.1:27017）
 *   8. MongoDB 把数据存到 test 数据库的 comment 集合里
 *   9. MongoDB 返回自动生成的 _id
 *  10. Spring 把 _id 塞回 Comment 对象的 id 属性
 *  11. add()方法返回"评论成功，ID：xxx"给前端
 *
 * 【完整调用顺序（查评论）】
 *
 *   1. 前端发GET请求：GET /comment/list?articleId=100
 *   2. SpringMVC把请求交给 list() 方法
 *   3. 调用 commentRepository.findByArticleId("100")
 *   4. Spring 看到方法名 findByArticleId，自动生成查询条件
 *   5. 翻译成 MongoDB 命令：db.comment.find({articleId: "100"})
 *   6. 发给 MongoDB 服务器执行
 *   7. MongoDB 把查到的JSON文档列表返回
 *   8. Spring 自动把JSON转成 List<Comment> Java对象
 *   9. list()方法把这个List返回给前端，前端收到JSON数组
 *
 * 【控制台能看到什么？】
 * 我们在application.yml里开了 DEBUG 日志，调用接口时控制台会打印：
 *   findOne: query: { "_id" : ObjectId("xxx") }, fields: {}
 * 这就是底层真正发给MongoDB的命令！
 * ============================================================
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    /**
     * 注入CommentRepository
     * 注意：我们没有写CommentRepository的实现类
     * Spring在启动的时候自动帮我们生成了一个实现类的Bean，直接注入进来了
     */
    @Autowired
    private CommentRepository commentRepository;

    /**
     * 发评论接口
     *
     * 调用方式（Postman/Ajax）：
     *   POST http://localhost/comment/add
     *   参数：articleId=100&content=写得好&author=张三
     *
     * @param articleId 文章ID
     * @param content   评论内容
     * @param author    评论人
     * @return 保存成功的提示和自动生成的ID
     */
    @PostMapping("/add")
    public String add(String articleId, String content, String author) {
        // 第1步：创建Comment对象，构造方法里自动设置了createTime为当前时间
        Comment comment = new Comment(articleId, content, author);

        // 第2步：调用save()保存到MongoDB
        // save()执行完之后，MongoDB自动生成的ID会自动塞到comment对象的id属性里
        commentRepository.save(comment);

        // 第3步：返回结果，这时候comment.getId()已经有值了
        return "评论成功，ID：" + comment.getId();
    }

    /**
     * 查某篇文章的所有评论
     *
     * 调用方式（浏览器直接访问）：
     *   GET http://localhost/comment/list?articleId=100
     *
     * @param articleId 文章ID
     * @return 这篇文章的所有评论列表（JSON格式）
     */
    @GetMapping("/list")
    public List<Comment> list(String articleId) {
        // 调用findByArticleId方法
        // 你没写实现，但Spring自动帮你翻译成：db.comment.find({articleId: "100"})
        // 然后自动把MongoDB返回的JSON数组转成List<Comment>
        return commentRepository.findByArticleId(articleId);
    }
}
