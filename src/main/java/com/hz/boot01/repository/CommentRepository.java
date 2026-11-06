package com.hz.boot01.repository;

import com.hz.boot01.pojo.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * ============================================================
 * MongoDB 数据访问层（DAO）
 * ============================================================
 *
 * 【最神奇的地方】
 * 你有没有发现：这个接口没有写实现类？
 * 对！Spring Data MongoDB 会在运行时自动帮你生成实现类！
 *
 * 【它是怎么做到的？】
 * 1. 你继承了 MongoRepository<Comment, String>
 *    - 第一个参数：这个Repository管哪个实体类（Comment）
 *    - 第二个参数：主键是什么类型（String）
 *
 * 2. 继承之后，你自动就有了这些方法：
 *    - save()          → 保存/更新
 *    - findById()      → 根据ID查
 *    - findAll()       → 查全部
 *    - deleteById()    → 根据ID删
 *    这些方法都不用你写，框架已经帮你实现好了！
 *
 * 3. 你自己定义的方法，比如 findByArticleId()
 *    Spring 会根据方法名自动帮你生成查询！
 *    findByArticleId → 意思是 where articleId = ?
 *    你只要按照规则起名，不用写任何SQL！
 *
 * 【方法名命名规则举例】
 *   findByAuthor("张三")        → where author = "张三"
 *   findByContentLike("写得")   → where content like "%写得%"
 *   findByArticleIdAndAuthor(1, "张三") → where articleId=1 and author="张三"
 *
 * 【为什么要放在 repository 包？】
 * MyBatis 的 @MapperScan("com.hz.boot01.dao") 会扫描 dao 包下所有接口
 * 如果把这个Repository放在dao包，MyBatis会把它当成MyBatis的Mapper
 * 和Spring Data MongoDB自动生成的Bean冲突，启动报错！
 * 所以我们单独建了一个 repository 包，专门放MongoDB的Repository
 * ============================================================
 */
public interface CommentRepository extends MongoRepository<Comment, String> {

    /**
     * 根据文章ID查这篇文章的所有评论
     *
     * 【注意】这个方法你不用写实现！
     * Spring 看到方法名叫 findByArticleId
     * 就自动翻译成 MongoDB 查询：db.comment.find({articleId: "100"})
     *
     * @param articleId 文章ID
     * @return 这篇文章的所有评论列表
     */
    List<Comment> findByArticleId(String articleId);
}
