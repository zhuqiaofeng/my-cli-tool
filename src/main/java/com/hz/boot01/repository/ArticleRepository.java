package com.hz.boot01.repository;

import com.hz.boot01.pojo.Article;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * ============================================================
 * Elasticsearch 数据访问层
 * ============================================================
 *
 * 【和 MongoDB Repository 几乎一模一样！】
 * 都是继承一个父接口，不用写实现类，Spring 自动帮你生成
 *
 * 【继承 ElasticsearchRepository 后，你自动获得这些方法】
 * - save()          → 保存/更新文档
 * - findById()      → 根据ID查
 * - findAll()       → 查全部
 * - deleteById()    → 根据ID删
 * - count()         → 统计数量
 *
 * 【方法名命名规则也和 MongoDB 一样】
 * findByAuthor("张三")  → where author = "张三"
 * findByTitleLike("Spring")  → where title like "%Spring%"
 *
 * 【为什么要放在 repository 包？】
 * 和 MongoDB 一样：
 * MyBatis 的 @MapperScan("com.hz.boot01.dao") 会扫描 dao 包
 * 如果把这个接口放dao包，MyBatis会把它当成MyBatis Mapper
 * 和Spring Data ES自动生成的Bean冲突，启动报错！
 * 所以单独放在 repository 包，和 MongoDB 的 CommentRepository 放一起
 * ============================================================
 */
public interface ArticleRepository extends ElasticsearchRepository<Article, String> {

    /**
     * 根据标题精确查
     * 你不用写实现，Spring 自动帮你翻译成 ES 查询
     */
    List<Article> findByTitle(String title);
}
