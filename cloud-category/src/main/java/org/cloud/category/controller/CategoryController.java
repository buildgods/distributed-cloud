package org.cloud.category.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import org.cloud.category.pojo.Category;
import org.cloud.category.service.CategoryService;
import org.cloud.common.pojo.Result;
import org.cloud.common.utils.CacheClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.cloud.common.utils.RedisConstants.*;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CacheClient cacheClient;

    @PostMapping
    public Result add(@RequestBody @Validated(Category.Add.class) Category category){
        categoryService.add(category);
        // 2.删除缓存
        stringRedisTemplate.delete(CACHE_CATEGORY_KEY+"all");
        return Result.success();
    }

    @GetMapping
    public Result<List<Category>> list(){
        String key = CACHE_CATEGORY_KEY+"all";
        // 1.从redis中查询分类缓存
        String listJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if(StrUtil.isNotBlank(listJson)){
            // 3.存在，直接返回
            JSONArray list = JSONUtil.parseArray(listJson);
            List<Category> cl = JSONUtil.toList(list, Category.class);
            return Result.success(cl);
        }
        List<Category> cs = categoryService.list();
        // 4.写入redis中
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(cs),1, TimeUnit.HOURS);
        return Result.success(cs);
    }
    @GetMapping("/detail")
    public Result<Category> detail(@RequestParam Integer id){
        Category c = cacheClient
                .queryWithMutex(
                        CACHE_CATEGORY_KEY,
                        id,
                        LOCK_CATEGORY_KEY,
                        Category.class,
                        categoryService::findById,
                        CACHE_TTL,TimeUnit.MINUTES
                );
        if(c == null){
            return Result.error("分类不存在!");
        }
        return Result.success(c);
    }

    @PutMapping
    public Result update(@RequestBody @Validated(Category.Update.class) Category category){
        // 1.更新数据库
        categoryService.update(category);
        // 2.删除缓存
        stringRedisTemplate.delete(CACHE_CATEGORY_KEY+category.getId());
        stringRedisTemplate.delete(CACHE_CATEGORY_KEY+"all");
        return Result.success();
    }
    @DeleteMapping
    public Result delete(@RequestParam @Validated(Category.Delete.class) Integer id){
        // 1.更新数据库
        categoryService.delete(id);
        // 2.删除缓存
        stringRedisTemplate.delete(CACHE_CATEGORY_KEY+id);
        stringRedisTemplate.delete(CACHE_CATEGORY_KEY+"all");
        return Result.success();
    }

}
