package com.zf.emos.wx.controller;

import com.zf.emos.wx.common.utils.R;
import com.zf.emos.wx.config.shiro.JwtUtils;
import com.zf.emos.wx.controller.form.LoginForm;
import com.zf.emos.wx.controller.form.RegisterForm;
import com.zf.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author pumpkin
 * @date 2021/12/30 0030 下午 22:25
 */

@RestController
@RequestMapping("/user")
@Api("用户模块web接口")
public class UserController {
    @Autowired
    private UserService userService ;

    @Autowired
    private JwtUtils jwtUtils ;

    @Autowired
    private RedisTemplate redisTemplate ;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire ;

    @PostMapping("/register")
    @ApiOperation("注册用户")
    public R register(@Valid @RequestBody RegisterForm registerForm){
        int id = userService.register(registerForm.getRegisterCode(), registerForm.getCode(), registerForm.getNickname(), registerForm.getPhoto());
        String token = jwtUtils.createToken(id);
        Set<String> permissions = userService.searchUserPermissions(id);
        saveCacheToken(token , id);
        return R.ok("用户注册成功").put("token" , token).put("permission" , permissions) ;
    }

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public R login(@Valid @RequestBody LoginForm loginForm){
        Integer id = userService.login(loginForm.getCode());
        String token = jwtUtils.createToken(id);
        saveCacheToken(token , id);
        Set<String> permissions = userService.searchUserPermissions(id);
        return R.ok("用户登录成功").put("permission" , permissions).put("token" ,token) ;
    }

    private void saveCacheToken(String token , int userId){
        redisTemplate.opsForValue().set(token , userId+"" , cacheExpire , TimeUnit.DAYS);
    }
}
