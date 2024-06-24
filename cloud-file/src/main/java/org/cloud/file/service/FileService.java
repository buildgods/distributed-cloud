package org.cloud.file.service;



import org.cloud.common.pojo.PageBean;
import org.cloud.file.pojo.File;

import java.io.IOException;
import java.util.List;

public interface FileService {
    void add(File file);

    PageBean<File> list(Integer pageNum, Integer pageSize, Integer categoryId);


    File detail(Integer id);

    void delete(Integer id) throws IOException;

    void update(File file);

    List<File> getListByCreateUser(Integer createUser);
}
