package org.cloud.article.mapper;

import org.apache.ibatis.annotations.*;
import org.cloud.article.pojo.Article;


import java.util.List;

@Mapper
public interface ArticleMapper {
    @Insert("insert into article(title,content,cover_img,state,category_id,create_user,create_time,update_time,url,url_state) "
    +"values(#{title},#{content},#{coverImg},#{state},#{categoryId},#{createUser},#{createTime},#{updateTime},#{url},#{urlState})")
    void add(Article article);

    List<Article> list(Integer userId, Integer categoryId, String state);

    @Select("select * from article where id = #{id}")
    Article detail(Integer id);

    @Delete("delete from article where id = #{id}")
    void delete(Integer id);

    @Update("update article set title = #{title},content = #{content},cover_img = #{coverImg}"
            +",state = #{state},category_id = #{categoryId},url = #{url},url_state = #{urlState},update_time = #{updateTime} where id = #{id}")
    void update(Article article);
}
