package org.cloud.article.pojo;



import lombok.Data;

import org.cloud.article.ano.State;
import org.cloud.article.ano.UrlState;
import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Data
public class Article {
    private Integer id;//主键ID
    @NotEmpty
    @Pattern(regexp = "^.{1,200}$")
    private String title;//文章标题
    private String content;//文章内容
    private String coverImg;//封面图像
    @State
    private String state;//发布状态 已发布|草稿
    private Integer categoryId;//文章分类id
    private Integer createUser;//创建人ID
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createTime;//创建时间
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime updateTime;//更新时间
    @URL
    private String url;//文章链接地址
    @UrlState
    private String urlState;//链接地址存在状态
}
