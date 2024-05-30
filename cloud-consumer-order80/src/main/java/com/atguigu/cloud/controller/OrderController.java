package com.atguigu.cloud.controller;

import com.atguigu.cloud.entities.PayDTO;
import com.atguigu.cloud.resp.ResultData;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author cyy
 * @date 2024年5月17日 10:19
 * @description
 * 为什么要引入微服务？  硬编码写死问题
 *  ·IP或端口发生变化，支付微服务将不可用
 *  ·多个订单和支付微服务，无法实现微服务的负载均衡功能
 *  ·系统若支持高并发，需要很多订单和支付微服务，硬编码订单微服务后续维护会变得更复杂
 *  所以，在微服务开发的过程中，需要引入服务治理功能，实现微服务之间的动态注册与发现
 */
@RestController
public class OrderController{
    //public static final String PaymentSrv_URL = "http://localhost:8001";//先写死，硬编码
    public static final String PaymentSrv_URL = "http://cloud-payment-service";//服务注册中心上的微服务名称
    //一个Bug，IO错误，不知道的主机错误异常，
    // 因为cansul天生支持负载均衡，若通过微服务名字调用，默认后面有多个微服务，是集群版
    //需要在restTemplate上增加负载均衡注解 @LoadBalanced
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 一般情况下，通过浏览器的地址栏输入url，发送的只能是get请求
     * 我们底层调用的是post方法，模拟消费者发送get请求，客户端消费者
     * 参数可以不添加@RequestBody
     * @param payDTO
     * @return
     */
    @GetMapping("/consumer/pay/add")
    public ResultData addOrder(PayDTO payDTO){
        return restTemplate.postForObject(PaymentSrv_URL + "/pay/add",payDTO,ResultData.class);
    }

    @GetMapping("/consumer/pay/del/{id}")
    public void delOrder(@PathVariable("id") Integer id) {
        restTemplate.delete(PaymentSrv_URL+"/pay/del/"+id, ResultData.class,id);
    }

    @GetMapping("/consumer/pay/update")
    public void updateOrder(PayDTO payDTO) {
        restTemplate.put(PaymentSrv_URL + "/pay/update",payDTO,ResultData.class);
    }

    @GetMapping("/consumer/pay/get/{id}")
    public ResultData getPayInfo(@PathVariable("id") Integer id){
        return restTemplate.getForObject(PaymentSrv_URL + "/pay/get/"+id, ResultData.class, id);
    }

    @GetMapping("/consumer/pay/get/all")
    public ResultData getPayAll(){
        return restTemplate.getForObject(PaymentSrv_URL + "/pay/get/all", ResultData.class);
    }

    @GetMapping(value = "/consumer/pay/get/info")
    private String getInfoByConsul()
    {
        return restTemplate.getForObject(PaymentSrv_URL + "/pay/get/info", String.class);
    }

    /*
    * 负载原理 默认：轮询
    * 负载均衡算法：rest接口第几次请求数 % 服务器集群总数量 = 实际调用服务器位置下标  ，
    *   每次服务重启动后rest接口计数从1开始。
    * */
    /*@Resource
    private DiscoveryClient discoveryClient;
    @GetMapping("/consumer/discovery")
    public String discovery()
    {
        List<String> services = discoveryClient.getServices();
        for (String element : services) {
            System.out.println(element);
        }

        System.out.println("===================================");

        List<ServiceInstance> instances = discoveryClient.getInstances("cloud-payment-service");
        for (ServiceInstance element : instances) {
            System.out.println(element.getServiceId()+"\t"+element.getHost()+"\t"+element.getPort()+"\t"+element.getUri());
        }

        return instances.get(0).getServiceId()+":"+instances.get(0).getPort();
    }*/
}
