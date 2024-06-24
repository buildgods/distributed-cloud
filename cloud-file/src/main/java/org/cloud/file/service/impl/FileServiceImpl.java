package org.cloud.file.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import org.cloud.common.pojo.PageBean;
import org.cloud.common.utils.FileRemoveUtil;
import org.cloud.common.utils.ThreadLocalUtil;
import org.cloud.file.mapper.FileMapper;
import org.cloud.file.pojo.File;
import org.cloud.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private FileMapper fileMapper;
    @Override
    public void add(File file) {
        // 补充属性
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        file.setCreateUser(id);
        file.setCreateTime(LocalDateTime.now());
        file.setUpdateTime(LocalDateTime.now());
        fileMapper.add(file);
    }

    @Override
    public PageBean<File> list(Integer pageNum, Integer pageSize, Integer categoryId) {
        // 1.创建pageBean对象
        PageBean<File> pb = new PageBean<>();

        // 2.开启分页查询PageHelper
        PageHelper.startPage(pageNum, pageSize);

        // 3.调用mapper
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        List<File> as = fileMapper.list(userId,categoryId);

        // Page中提供了方法，可以获取PageHelper分页查询后得到的总记录和当前页数据
        Page<File> p = (Page<File>) as;

        // 把数据填充到PageBean对象中
        pb.setTotal(p.getTotal());
        pb.setItems(p.getResult());


        return pb;
    }

    @Override
    public File detail(Integer id) {
        return fileMapper.detail(id);
    }

    @Override
    public void delete(Integer id)  {

        // 获取整条数据
        File file = fileMapper.detail(id);
        // 文件名称
        String serverFileName = file.getServerFileName();
        // 删除文件
        // 存在文件
        if(!serverFileName.equals("")){
            // 将字符串转为数组
            String[] fileName = serverFileName.split("&");
            // 转为集合
            List<String> list = Arrays.asList(fileName);
            // 遍历集合删除文件
            Iterator<String> iterator = list.listIterator();
            while (iterator.hasNext()){
                String name = iterator.next();
                System.out.println(name);
                // 1.删除项目uploads中的文件
                if(!FileRemoveUtil.deleteFile(name,"uploads","files")) continue;
                // 2.删除kkFileView file包中的文件
                if(!FileRemoveUtil.deleteKKFile(name)) continue;

            }
            // 根据id删除整条数据
            fileMapper.delete(id);
        }
        fileMapper.delete(id);
    }

    @Override
    public void update(File file) {
        // 更新时间
        file.setUpdateTime(LocalDateTime.now());
        // 删除旧的文件
        // 旧的数据
        String oldServerFileName = fileMapper.detail(file.getId()).getServerFileName();
        System.out.println("旧数据"+oldServerFileName);
        if(!oldServerFileName.equals("")){
            // 新的数据
            String newServerFileName = file.getServerFileName();
            System.out.println("新数据"+newServerFileName);
            // 将字符串转为数组
            String[] oldArray = oldServerFileName.split("&");
            String[] newArray = newServerFileName.split("&");
            // 遍历旧数组
            // 遍历集合删除文件
            for (String name : oldArray) {
                System.out.println(name);
                if (!Arrays.asList(newArray).contains(name)) {

                    // 新的数组没有这条数据，执行删除逻辑
                    // 1.删除项目uploads中的文件
                    if (!FileRemoveUtil.deleteFile(name, "uploads", "files")) continue;
                    // 2.删除kkFileView file包中的文件
                    if (!FileRemoveUtil.deleteKKFile(name)) continue;
                }
            }
            fileMapper.update(file);
        }
        fileMapper.update(file);

    }

    @Override
    public List<File> getListByCreateUser(Integer createUser) {
        return fileMapper.getListByCreateUser(createUser);
    }
}
