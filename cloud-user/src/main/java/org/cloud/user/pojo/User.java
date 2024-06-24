package org.cloud.user.pojo;



import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Data
public class User {
    @NotNull //添加valid校验规则
    private Integer id;//主键ID
    private String username;//用户名
    @JsonIgnore// 忽略password，最终的JSON字符串中就没有password这个属性了
    private String password;//密码
    @NotEmpty
    @Pattern(regexp = "^\\S{1,10}$")
    private String nickname;//昵称
    @NotEmpty
    @Email
    private String email;//邮箱
    private String userPic;//用户头像地址
    private LocalDateTime createTime;//创建时间
    private LocalDateTime updateTime;//更新时间
}
