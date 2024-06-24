package org.cloud.share.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.cloud.share.pojo.Share;


import java.util.List;

@Mapper
public interface ShareMapper {
    @Select("select * from share")
    List<Share> list();

    @Delete("delete from share where id = #{id}")
    void delete(Integer id);

    @Insert("insert into share(file_name,create_user,create_time,server_file_name,user_name)"
            +"values(#{fileName},#{createUser},#{createTime},#{serverFileName},#{userName})")
    void add(Share share);
}
