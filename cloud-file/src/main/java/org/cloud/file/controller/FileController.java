package org.cloud.file.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import org.cloud.common.pojo.PageBean;
import org.cloud.common.pojo.Result;
import org.cloud.common.utils.CacheClient;
import org.cloud.file.pojo.File;
import org.cloud.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.cloud.common.utils.RedisConstants.*;

@RestController
@RequestMapping("file")
public class FileController {
    @Autowired
    private FileService fileService;
    @Resource
    private CacheClient cacheClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping
    private Result add(@Validated File file){
        fileService.add(file);
        deleteAllCacheKeys();
        return Result.success();
    }
    @GetMapping()
    public Result<PageBean<File>> list(Integer pageNum, Integer pageSize,
                                       @RequestParam(required = false) Integer categoryId){
        String key = CACHE_FILE_KEY + pageNum + "_" + pageSize + "_" + (categoryId != null ? categoryId : "all");
        // 1.从redis中查询分类缓存
        String pageJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if(StrUtil.isNotBlank(pageJson)){
            // 3.存在，直接返回
            PageBean pageBean = JSONUtil.toBean(pageJson, PageBean.class);
            return Result.success(pageBean);
        }
        PageBean<File> pb = fileService.list(pageNum,pageSize,categoryId);
        // 4.写入redis中
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(pb),1, TimeUnit.HOURS);

        return Result.success(pb);
    }
    /**
     * 获取文件详情
     * @param id
     * @return
     */
    @GetMapping("/detail")
    public Result<File> detail(@RequestParam Integer id){
        File res = cacheClient
                .queryWithMutex(
                        CACHE_FILE_KEY,
                        id,
                        LOCK_FILE_KEY,
                        File.class,
                        fileService::detail,
                        CACHE_TTL, TimeUnit.MINUTES
                );
        if(res == null){
            return Result.error("文件不存在!");
        }
        return Result.success(res);
    }
    @GetMapping("/userList")
    public Result<List<File>> getListByCreateUser(@RequestParam Integer createUser){
        String key = CACHE_FILE_KEY+"all";
        // 1.从redis中查询分类缓存
        String listJson = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if(StrUtil.isNotBlank(listJson)){
            // 3.存在，直接返回
            JSONArray list = JSONUtil.parseArray(listJson);
            List<File> cl = JSONUtil.toList(list, File.class);
            return Result.success(cl);
        }
        List<File> cs = fileService.getListByCreateUser(createUser);
        // 4.写入redis中
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(cs),1, TimeUnit.HOURS);
        return Result.success(cs);
    }

    @DeleteMapping
    public Result delete(@RequestParam Integer id) throws IOException {
        fileService.delete(id);
        // 2.删除缓存
        deleteAllCacheKeys();
        return Result.success();
    }
    @PutMapping
    public Result update(@Validated File file){
        fileService.update(file);
        deleteAllCacheKeys();
        return Result.success();
    }
    private void deleteAllCacheKeys() {
        // 获取所有与文章列表相关的缓存键
        Set<String> keys = stringRedisTemplate.keys(CACHE_FILE_KEY + "*");

        if (keys != null && !keys.isEmpty()) {
            // 删除这些缓存键
            stringRedisTemplate.delete(keys);
        }
    }


}
