package com.atguigu.cloud.service;

import com.atguigu.cloud.resp.ResultData;
import org.apache.ibatis.annotations.Param;

/**
 * @author cyy
 * @date 2024年5月30日 11:42
 * @description
 */
public interface StorageService {
    /**
     * 扣减库存
     */
    void decrease(Long productId,Integer count);
}
