package org.cloud.share.service;


import org.cloud.common.pojo.PageBean;
import org.cloud.share.pojo.Share;

public interface ShareService {
    PageBean<Share> list(Integer pageNum, Integer pageSize);

    void delete(Integer id);

    void add(Share share);

}
