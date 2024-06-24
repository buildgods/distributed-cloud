package org.cloud.article.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import org.cloud.article.mapper.ArticleMapper;
import org.cloud.article.pojo.Article;
import org.cloud.article.service.ArticleService;
import org.cloud.common.pojo.PageBean;
import org.cloud.common.utils.FileRemoveUtil;
import org.cloud.common.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;
    @Override
    public void add(Article article) {
        // 补充属性值
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        article.setCreateUser(id);
        articleMapper.add(article);
    }

    @Override
    public PageBean<Article> list(Integer pageNum, Integer pageSize, Integer categoryId, String state) {
        // 1.创建pageBean对象
        PageBean<Article> pb = new PageBean<>();

        // 2.开启分页查询PageHelper
        PageHelper.startPage(pageNum, pageSize);

        // 3.调用mapper
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        List<Article> as = articleMapper.list(userId,categoryId,state);

        // Page中提供了方法，可以获取PageHelper分页查询后得到的总记录和当前页数据
        Page<Article> p = (Page<Article>) as;

        // 把数据填充到PageBean对象中
        pb.setTotal(p.getTotal());
        pb.setItems(p.getResult());


        return pb;
    }

    @Override
    public Article detail(Integer id) {
        return articleMapper.detail(id);
    }

    @Override
    public void delete(Integer id) {
        // 获取整条数据
        Article article = articleMapper.detail(id);
        // 标题图片名称
        String titlePhotoName = article.getCoverImg();
        // 删除文件
        if(!titlePhotoName.equals("")){
            if(FileRemoveUtil.deleteFile(titlePhotoName,"uploads","titles")){
                // 根据id删除整条数据
                articleMapper.delete(id);
            }
        }
        // 根据id删除整条数据
        articleMapper.delete(id);

    }

    @Override
    public void update(Article article) {
        article.setUpdateTime(LocalDateTime.now());
        // 删除服务器旧的封面
        String oldTitleName = articleMapper.detail(article.getId()).getCoverImg();
        if(!oldTitleName.equals("")){
            if (FileRemoveUtil.deleteFile(oldTitleName, "uploads","titles")) {
                // 执行修改逻辑
                articleMapper.update(article);
            }
        }
        articleMapper.update(article);


    }
}
