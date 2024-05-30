package com.atguigu.cloud.service;

import com.atguigu.cloud.entities.Order;

/**
 * @author cyy
 * @date 2024年5月30日 09:30
 * @description
 */
public interface OrderService {

    /**
     * 创建订单
     */
    void create(Order order);

}