package com.zf.emos.wx.controller;

import cn.hutool.core.date.DateUtil;
import com.zf.emos.wx.common.utils.R;
import com.zf.emos.wx.config.shiro.JwtUtils;
import com.zf.emos.wx.service.CheckinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author pumpkin
 * @date 2021/12/31 0031 下午 23:39
 */

@RequestMapping("/checkin")
@RestController
@Api("签到模块web接口")
@Slf4j
public class CheckinController {

    @Autowired
    private JwtUtils jwtUtils ;

    @Autowired
    private CheckinService checkinService ;

    @GetMapping("/validCanCheckin")
    @ApiOperation("是否可以签到")
    public R validCanCheckin(@RequestHeader("token") String token){
        int userId = jwtUtils.getUserId(token);
        String result = checkinService.validCanCheckin(userId, DateUtil.today());
        return R.ok(result).put("hello" , "ok") ;
    }
}
