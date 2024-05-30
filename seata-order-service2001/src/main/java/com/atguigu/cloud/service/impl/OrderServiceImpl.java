package com.atguigu.cloud.service.impl;

import com.atguigu.cloud.apis.AccountFeignApi;
import com.atguigu.cloud.apis.StorageFeignApi;
import com.atguigu.cloud.entities.Order;
import com.atguigu.cloud.mapper.OrderMapper;
import com.atguigu.cloud.service.OrderService;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author cyy
 * @date 2024年5月30日 09:31
 * @description
 * 下订单->减库存->扣余额->改(订单)状态
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService
{
    @Resource
    private OrderMapper orderMapper;
    @Resource//订单微服务通过OpenFeign去调用库存微服务
    private StorageFeignApi storageFeignApi;
    @Resource//订单微服务通过OpenFeign去调用账户微服务
    private AccountFeignApi accountFeignApi;


    @Override
    //解决故障(超时异常)：库存和账户金额扣减后，订单状态并没有设置为已完结，从0到1
    //两阶段rollback全部回滚，保证了数据一致性  PhaseTwo_Rollback  底子就是个RTC
    //RTC则是指反向时间充数（Reverse Time Code），是一种通过编排时间戳和使用一系列事件对故障进行恢复和复原的技术。
    //RTC在分布式事务中指一种用时间戳和事件恢复故障的技术
    @GlobalTransactional(name = "cyy-create-order",rollbackFor = Exception.class) //AT模式
    //AT模式是指通过预编排的方式，在参与事务的各个服务之间自动插入分布式事务处理逻辑，保障各服务的事务操作最终一致性
    //@GlobalTransactional @Transactional(rollbackFor = Exception.class) //XA
    public void create(Order order) {
        //xid全局事务id的检查，重要
        String xid = RootContext.getXID();
        //1. 新建订单
        log.info("==================>开始新建订单"+"\t"+"xid_order:" +xid);
        //订单新建时默认初始订单状态是零，status：0：创建中；1：已完结
        order.setStatus(0);
        int result = orderMapper.insertSelective(order);

        //插入订单成功后获得插入mysql的实体对象
        Order orderFromDB = null;

        if(result > 0) {
            //从mysql里面查出刚插入的记录
            orderFromDB = orderMapper.selectOne(order);
            //orderFromDB = orderMapper.selectByPrimaryKey(order.getId());
            log.info("-------> 新建订单成功，orderFromDB info: "+orderFromDB);
            System.out.println();
            //扣减库存
            log.info("-------> 订单微服务开始调用Storage库存，做扣减count");
            storageFeignApi.decrease(orderFromDB.getProductId(), orderFromDB.getCount());
            log.info("-------> 订单微服务结束调用Storage库存，做扣减完成");
            System.out.println();
            //3. 扣减账号余额
            log.info("-------> 订单微服务开始调用Account账号，做扣减money");
            accountFeignApi.decrease(orderFromDB.getUserId(), orderFromDB.getMoney());
            log.info("-------> 订单微服务结束调用Account账号，做扣减完成");
            System.out.println();
            //4. 修改订单状态
            //将订单状态从零修改为1，表示已经完成   status：0：创建中；1：已完结
            log.info("-------> 修改订单状态");
            orderFromDB.setStatus(1);

            //Mybatis工具里面带条件的东西
            Example whereCondition = new Example(Order.class);
            Example.Criteria criteria = whereCondition.createCriteria();
            criteria.andEqualTo("userId",orderFromDB.getUserId());
            criteria.andEqualTo("status",0);

            int updateResult = orderMapper.updateByExampleSelective(orderFromDB, whereCondition);
            log.info("-------> 修改订单状态完成"+"\t"+updateResult);
            log.info("-------> orderFromDB info: "+orderFromDB);
        }
        System.out.println();
        log.info("==================>结束新建订单"+"\t"+"xid_order:" +xid);
    }
}
