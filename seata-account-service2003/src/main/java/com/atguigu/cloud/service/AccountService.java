package com.atguigu.cloud.service;

import com.atguigu.cloud.resp.ResultData;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author cyy
 * @date 2024年5月30日 12:06
 * @description
 */
public interface AccountService {
    /**
     * 扣减账户余额
     * @param userId 用户id
     * @param money 本次消费金额
     */
    void decrease(@Param("userId") Long userId, @Param("money") Long money);
}
