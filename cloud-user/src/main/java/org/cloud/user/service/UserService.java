package org.cloud.user.service;


import org.cloud.user.pojo.User;

public interface UserService {
    User findByUserName(String username);

    void register(String username, String password);

    void update(User user);

    void updateAvatar(String avatarUrl);

    void updatePwd(String newPwd);

    void forgetPwd(String newPwd, User loginUser);
}
