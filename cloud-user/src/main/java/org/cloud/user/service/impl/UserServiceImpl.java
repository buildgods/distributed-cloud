package org.cloud.user.service.impl;


import org.cloud.common.utils.FileRemoveUtil;
import org.cloud.user.mapper.UserMapper;
import org.cloud.user.pojo.User;
import org.cloud.user.service.UserService;
import org.cloud.common.utils.Md5Util;
import org.cloud.common.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public User findByUserName(String username) {
        return userMapper.findByUserName(username);
    }

    @Override
    public void register(String username, String password) {
        //加密
        String md5String = Md5Util.getMD5String(password);
        //添加
        userMapper.add(username,md5String);

    }

    @Override
    public void update(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void updateAvatar(String avatarUrl) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        String username = (String) map.get("username");
        // 根据用户名查询用户数据
        User user = userMapper.findByUserName(username);
        // 获取旧的头像
        String oldAvatarUrl = user.getUserPic();
        // 判断用户是否有头像
        if (!oldAvatarUrl.equals("")){
            // 删除旧头像
            if(FileRemoveUtil.deleteFile(oldAvatarUrl,"uploads","avatars"))
            {
                userMapper.updateAvatar(avatarUrl,id);
            }
        }
        userMapper.updateAvatar(avatarUrl,id);


    }

    @Override
    public void updatePwd(String newPwd) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updatePwd(Md5Util.getMD5String(newPwd),id);
    }

    @Override
    public void forgetPwd(String newPwd, User loginUser) {
        Integer id = loginUser.getId();
        userMapper.updatePwd(Md5Util.getMD5String(newPwd),id);
    }
}
