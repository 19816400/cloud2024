package com.atguigu.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author cyy
 * @date ${DATE} ${TIME}
 * @description
 * 微服务小口诀：建module、改pom、写yml、主启动、业务类
 */
@SpringBootApplication
//由于引入了一键生成，所以得引入一个包扫描工具
@MapperScan("com.atguigu.cloud.mapper")//不用每个接口上都写一个@Mapper
@EnableDiscoveryClient  //开启服务发现
@RefreshScope  //动态刷新，默认55s
public class Main8001 {
    public static void main(String[] args) {
        SpringApplication.run(Main8001.class,args);
    }
}