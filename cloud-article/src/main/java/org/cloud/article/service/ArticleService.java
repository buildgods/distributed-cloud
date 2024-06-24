package org.cloud.article.service;


import org.cloud.article.pojo.Article;
import org.cloud.common.pojo.PageBean;

public interface ArticleService {
    void add(Article article);

    PageBean<Article> list(Integer pageNum, Integer pageSize, Integer categoryId, String state);

    Article detail(Integer id);

    void delete(Integer id);

    void update(Article article);
}
