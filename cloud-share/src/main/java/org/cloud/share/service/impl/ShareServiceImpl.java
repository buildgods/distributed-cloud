package org.cloud.share.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import org.cloud.common.pojo.PageBean;
import org.cloud.common.utils.ThreadLocalUtil;
import org.cloud.share.mapper.ShareMapper;
import org.cloud.share.pojo.Share;
import org.cloud.share.service.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ShareServiceImpl implements ShareService {
    @Autowired
    private ShareMapper shareMapper;
    @Override
    public PageBean<Share> list(Integer pageNum, Integer pageSize) {
        // 1.创建pageBean对象
        PageBean<Share> pb = new PageBean<>();

        // 2.开启分页查询PageHelper
        PageHelper.startPage(pageNum, pageSize);

        // 3.调用mapper
        List<Share> as = shareMapper.list();

        // Page中提供了方法，可以获取PageHelper分页查询后得到的总记录和当前页数据
        Page<Share> p = (Page<Share>) as;

        // 把数据填充到PageBean对象中
        pb.setTotal(p.getTotal());
        pb.setItems(p.getResult());


        return pb;
    }

    @Override
    public void delete(Integer id) {
        shareMapper.delete(id);
    }

    @Override
    public void add(Share share) {
        // 补充属性
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        String username = (String) map.get("username");
        share.setCreateUser(id);
        share.setCreateTime(LocalDateTime.now());
        share.setUserName(username);
        shareMapper.add(share);
    }
}
