package com.atguigu.cloud.config;

import feign.Logger;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author cyy
 * @date 2024年5月19日 22:05
 * @description
 */
@Configuration
public class FeignConfig
{
    @Bean
    public Retryer myRetryer()
    {
        return Retryer.NEVER_RETRY; //Feign默认配置是不走重试策略的

        //最大请求次数为3(1+2)，初始间隔时间为100ms，重试间最大间隔时间为1s
        //return new Retryer.Default(100,1,3);
    }

    /**
     *
     * 对Feign接口的调用情况进行监控和输出
     * @return
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        /*
        * NONE：默认的，不显示任何日志；
          BASIC：仅记录请求方法、URL、响应状态码及执行时间；
          HEADERS：除了 BASIC 中定义的信息之外，还有请求和响应的头信息；
          FULL：除了 HEADERS 中定义的信息之外，还有请求和响应的正文及元数据。
        * */
        return Logger.Level.FULL;
    }
}
