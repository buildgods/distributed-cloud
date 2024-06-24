package org.cloud.article.controller;



import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.cloud.article.pojo.Article;
import org.cloud.article.service.ArticleService;
import org.cloud.common.pojo.PageBean;
import org.cloud.common.pojo.Result;
import org.cloud.common.utils.CacheClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.cloud.common.utils.RedisConstants.*;
import static org.cloud.common.utils.RedisConstants.CACHE_TTL;


@RestController
@RequestMapping("/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;
    @Resource
    private CacheClient cacheClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping
    public Result add(@Validated Article article){
        articleService.add(article);
        deleteAllCacheKeys();
        return Result.success();

    }

    @GetMapping
    public Result<PageBean<Article>> list(Integer pageNum, Integer pageSize,
                                          @RequestParam(required = false) Integer categoryId,
                                          @RequestParam(required = false) String state){
        String key = CACHE_ARTICLE_KEY + pageNum + "_" + pageSize + "_" + (categoryId != null ? categoryId : "all") + "_" + (state != null ? state : "all");
//        // 1.从redis中查询分类缓存
        String pageJson = stringRedisTemplate.opsForValue().get(key);
//        // 2.判断是否存在
        if(StrUtil.isNotBlank(pageJson)){
            // 3.存在，直接返回
            PageBean pageBean = JSONUtil.toBean(pageJson, PageBean.class);
            return Result.success(pageBean);
        }
        PageBean<Article> pb = articleService.list(pageNum,pageSize,categoryId,state);
        // 4.写入redis中
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(pb),1, TimeUnit.HOURS);
        return Result.success(pb);

    }

    /**
     * 获取文章详情
     * @param id
     * @return
     */
    @GetMapping("/detail")
    public Result<Article> detail(@RequestParam Integer id){
        Article res = cacheClient
                .queryWithMutex(
                        CACHE_ARTICLE_KEY,
                        id,
                        LOCK_ARTICLE_KEY,
                        Article.class,
                        articleService::detail,
                        CACHE_TTL, TimeUnit.MINUTES
                );
        if(res == null){
            return Result.error("文件不存在!");
        }
        return Result.success(res);
    }

    @DeleteMapping
    public Result delete(@RequestParam Integer id){
        articleService.delete(id);
        // 2.删除缓存
        deleteAllCacheKeys();
        return Result.success();
    }
    @PutMapping
    public Result update(@Validated Article article){
        articleService.update(article);
        deleteAllCacheKeys();
        return Result.success();
    }
    private void deleteAllCacheKeys() {
        // 获取所有与文章列表相关的缓存键
        Set<String> keys = stringRedisTemplate.keys(CACHE_ARTICLE_KEY + "*");

        if (keys != null && !keys.isEmpty()) {
            // 删除这些缓存键
            stringRedisTemplate.delete(keys);
        }
    }
}
