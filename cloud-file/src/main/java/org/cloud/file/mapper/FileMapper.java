package org.cloud.file.mapper;

import org.apache.ibatis.annotations.*;
import org.cloud.file.pojo.File;


import java.util.List;

@Mapper
public interface FileMapper {
    @Insert("insert into file(file_name,category_id,create_user,create_time,update_time,server_file_name)"
    +"values(#{fileName},#{categoryId},#{createUser},#{createTime},#{updateTime},#{serverFileName})")
    void add(File file);

    List<File> list(Integer userId, Integer categoryId);

    @Select("select * from file where id = #{id}")
    File detail(Integer id);

    @Delete("delete from file where id = #{id}")
    void delete(Integer id);

    @Update("update file set file_name = #{fileName},category_id = #{categoryId},create_user = #{createUser},update_time = #{updateTime},server_file_name = #{serverFileName} where id = #{id}")
    void update(File file);

    @Select("select * from file where create_user = #{createUser}")
    List<File> getListByCreateUser(Integer createUser);
}
