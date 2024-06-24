package org.cloud.user.controller;



import org.cloud.common.pojo.Result;
import org.cloud.common.utils.CacheClient;
import org.cloud.user.pojo.User;
import org.cloud.user.service.UserService;
import org.cloud.common.utils.JwtUtil;
import org.cloud.common.utils.Md5Util;
import org.cloud.common.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.cloud.common.utils.RedisConstants.*;

@RestController
@RequestMapping("/user")
@Validated// 开启校验
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CacheClient cacheClient;

    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{1,10}$") String username, @Pattern(regexp = "^\\S{5,16}$") String password){
        //查询用户
        User u = userService.findByUserName(username);
        if(u == null){
            //没有占用
            //注册
            userService.register(username,password);
            return Result.success();
        }else{
            return Result.error("用户名己被占用");
        }
    }

    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{1,10}$") String username, @RequestParam(name = "password") String password,@RequestParam(name = "uuid") String uuid,@RequestParam(name = "code") String code){
        // 根据用户名查询用户
        User loginUser = userService.findByUserName(username);
        // 判断用户是否存在
        if(loginUser == null)
            return Result.error("用户名不存在");
        // 判断密码是否正确 loginUser 对象中的密码是密文
        if(Md5Util.getMD5String(password).equals(loginUser.getPassword())) {
            Map<String,Object> claims = new HashMap<>();
            claims.put("id", loginUser.getId());
            claims.put("username", loginUser.getUsername());
            String token = JwtUtil.genToken(claims);
            // 存储token到redis中
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.set(token, token,1, TimeUnit.HOURS);
            // 判断验证码是否输入正确
            if (!Objects.equals(stringRedisTemplate.opsForValue().get(uuid), code))
            {
                return Result.error("验证码不正确或验证码过期了");
            }
            return Result.success(token);
        }


        return Result.error("密码错误");


    }
    @GetMapping("/userInfo")
    public Result<User> userInfo(@RequestHeader(name = "Authorization") String token){
        // 根据用户名查询用户
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User res = cacheClient
                .queryWithMutex(
                        CACHE_USER_KEY,
                        username,
                        LOCK_USER_KEY,
                        User.class,
                        userService::findByUserName,
                        CACHE_TTL, TimeUnit.MINUTES
                );
        if(res == null){
            return Result.error("用户不存在!");
        }
        return Result.success(res);

    }

    /**
     * @RequestBody 这个注解可以将请求的JSON格式转换为实体对象
     * @Validated 添加这个注解，才会开启校验
     * @param user
     * @return
     */
    @PutMapping("/update")
    public Result update(@RequestBody @Validated User user){
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        userService.update(user);
        System.out.println(CACHE_USER_KEY+username);
        stringRedisTemplate.delete(CACHE_USER_KEY+username);
        return Result.success();

    }

    /**
     * @PatchMapping 局部更新数据
     * @RequestParam 用于queryString的请求格式
     * @URL url合法地址的校验
     * @param avatarUrl
     * @return
     */
    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam String avatarUrl){
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        userService.updateAvatar(avatarUrl);
        stringRedisTemplate.delete(CACHE_USER_KEY+username);
        return Result.success();
    }

    /**
     * 登录后修改密码
     * @param params 注意还可以将JSON转化为map来接收
     * @return
     */
    @PatchMapping ("/updatePwd")
    public Result updatePwd(@RequestBody Map<String,String> params,@RequestHeader("Authorization") String token){
        // 1.校验参数
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");
        if(!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd) || !StringUtils.hasLength(rePwd)){
            return Result.error("缺少必要的参数");
        }
        // 原密码是否正确
        Map<String,Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User loginUser = userService.findByUserName(username);
        if(!loginUser.getPassword().equals(Md5Util.getMD5String(oldPwd))){
            return Result.error("原密码填写不正确");
        }
        // 新输入的密码和重新输入的密码是否一致
        if (!rePwd.equals(newPwd)){
            return Result.error("两次填写的新密码不一样");
        }
        // 完成密码的更新
        userService.updatePwd(newPwd);
        stringRedisTemplate.delete(CACHE_USER_KEY+username);
        // 删除redis中对应的token
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.getOperations().delete(token);
        return Result.success();
    }

    /**
     * 没有登录，修改密码
     * @param username
     * @param newPwd
     * @param rePwd
     * @param code
     * @param email
     * @return
     */
    @PutMapping("forgetPwd")
    public Result forgetPwd(@Pattern(regexp = "^\\S{1,10}$") String username, @RequestParam(name = "password") String newPwd,@RequestParam(name = "repassword") String rePwd,@RequestParam(name = "code") String code,@RequestParam(name="email") String email){
        if(StringUtils.isEmpty(stringRedisTemplate.opsForValue().get(email)))
            return Result.error("验证码过期了，请重新获取");
        // 判断邮箱验证码是否正确
        if(!stringRedisTemplate.opsForValue().get(email).equals(code)){
            return Result.error("邮箱验证码不正确");
        }
        // 根据用户名查询用户
        User loginUser = userService.findByUserName(username);
        // 判断用户是否存在
        if(loginUser == null)
            return Result.error("用户名不存在");
        // 新输入的密码和重新输入的密码是否一致
        if (!rePwd.equals(newPwd)){
            return Result.error("两次填写的新密码不一样");
        }
        // 判断新密码和旧密码是否一样
        if(loginUser.getPassword().equals(Md5Util.getMD5String(newPwd))){
            return Result.error("新密码与旧密码相同");
        }
        // 完成密码更新
        userService.forgetPwd(newPwd,loginUser);

        return Result.success("修改成功");
    }
}
