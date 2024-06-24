package org.cloud.email.controller;

import org.cloud.common.pojo.Result;
import org.cloud.common.utils.CodeGeneratorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("mail")
public class SendMailController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String from;

    @GetMapping("getCode/{email}")
    public Result sendMailCode(@PathVariable String email)
    {
        if(!StringUtils.isEmpty(stringRedisTemplate.opsForValue().get(email)))
            return Result.error("验证码还没有过期呦");

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        // 生成6位验证码
        String code = CodeGeneratorUtil.generateCode(6);
        // 存入缓存中 1 分钟
        stringRedisTemplate.opsForValue().set(email, code, 1, TimeUnit.MINUTES);
        mailMessage.setFrom(from+"(验证码)");
        mailMessage.setTo(email);
        mailMessage.setText("您的验证码为："+code+"（有效期为1分钟）");
        mailMessage.setSubject("收藏网站邮箱验证码");

        javaMailSender.send(mailMessage);

        return Result.success("发送成功");


    }

}
