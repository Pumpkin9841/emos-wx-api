package com.zf.emos.wx.controller;

import com.zf.emos.wx.common.utils.R;
import com.zf.emos.wx.controller.form.TestSayHelloForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author 周凡
 * @date 2021/12/10 0010 下午 16:44
 */
@RestController
@RequestMapping("/test")
@Api("简单的测试")
public class TestController {

    @PostMapping("sayHello")
    @ApiOperation("测试sayHello")
    public R sayHello(@Valid @RequestBody TestSayHelloForm testSayHelloForm){
        return R.ok().put("message" , "hello world" + testSayHelloForm.getName()) ;
    }

}
