package org.cloud.verify.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.util.IdUtil;
import org.cloud.common.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("verify")
public class VerifyCodeController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    static{
        System.setProperty("java.awt.headless", "true");
    }

    /**
     * 获取图形验证码
     * @return
     */
    @GetMapping("getCode")
    public Result<Map<String,String>> getCode(){
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100);
        String verify = IdUtil.simpleUUID();
        // 图形验证码写入流
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        lineCaptcha.write(os);
        String code = lineCaptcha.getCode();
        // 存入缓存 1分钟
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.set(verify, code, 1, TimeUnit.MINUTES);
        Map<String,String> map = new HashMap<>();
        map.put("uuid", verify);
        map.put("code", code);
        map.put("img", Base64.getEncoder().encodeToString(os.toByteArray()));
        return Result.success(map);

    }
}
