package org.cloud.share.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.cloud.common.pojo.PageBean;
import org.cloud.common.pojo.Result;
import org.cloud.share.pojo.Share;
import org.cloud.share.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

import static org.cloud.common.utils.RedisConstants.*;


@RestController
@RequestMapping("/share")
public class ShareController {
    @Autowired
    private ShareService shareService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @GetMapping()
    public Result<PageBean<Share>> list(Integer pageNum, Integer pageSize){
        String key = CACHE_SHARE_KEY+"all";
//        // 1.从redis中查询分类缓存
        String pageJson = stringRedisTemplate.opsForValue().get(key);
//        // 2.判断是否存在
        if(StrUtil.isNotBlank(pageJson)){
            // 3.存在，直接返回
            PageBean pageBean = JSONUtil.toBean(pageJson, PageBean.class);
            return Result.success(pageBean);
        }
        PageBean<Share> pb = shareService.list(pageNum,pageSize);
        // 4.写入redis中
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(pb),1, TimeUnit.HOURS);
        return Result.success(pb);
    }
    @DeleteMapping
    public Result delete(@RequestParam Integer id) {
        shareService.delete(id);
        stringRedisTemplate.delete(CACHE_SHARE_KEY+"all");
        return Result.success();
    }
    @PostMapping
    private Result add(@Validated Share share){
        shareService.add(share);
        stringRedisTemplate.delete(CACHE_SHARE_KEY+"all");
        return Result.success();
    }
}
