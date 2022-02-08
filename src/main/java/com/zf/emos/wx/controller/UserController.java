package com.zf.emos.wx.controller;

import cn.hutool.json.JSONUtil;
import com.zf.emos.wx.common.utils.R;
import com.zf.emos.wx.config.shiro.JwtUtils;
import com.zf.emos.wx.controller.form.LoginForm;
import com.zf.emos.wx.controller.form.RegisterForm;
import com.zf.emos.wx.controller.form.SearchMembersForm;
import com.zf.emos.wx.controller.form.SearchUserGroupByDeptForm;
import com.zf.emos.wx.exception.EmosException;
import com.zf.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @GetMapping("/searchUserSummary")
    @ApiOperation("查询用户信息")
    public R searchUserSummary(@RequestHeader("token") String token){
        int userId = jwtUtils.getUserId(token);
        HashMap userSummary = userService.searchUserSummary(userId);
        return R.ok().put("result" , userSummary) ;
    }

    @PostMapping("/searchUserGroupByDept")
    @ApiOperation("查询员工列表，按部门分组排列")
    @RequiresPermissions(value = {"ROOT" , "EMPLOYEE:SELECT"} ,logical = Logical.OR)
    public R searchUserGroupByDept(@Valid @RequestBody SearchUserGroupByDeptForm searchUserGroupByDeptForm , @RequestHeader("token") String token){
        ArrayList<HashMap> maps = userService.searchUserGroupByDept(searchUserGroupByDeptForm.getKeyword());
        return R.ok().put("result" , maps) ;
    }

    @PostMapping("/searchMembers")
    @ApiOperation("查询成员")
    @RequiresPermissions(value = {"ROOT", "MEETING:INSERT", "MEETING:UPDATE"}, logical = Logical.OR)
    public R searchMembers(@Valid @RequestBody SearchMembersForm form) {
        if (!JSONUtil.isJsonArray(form.getMembers())) {
            throw new EmosException("members不是JSON数组");
        }
        List param = JSONUtil.parseArray(form.getMembers()).toList(Integer.class);
        ArrayList list = userService.searchMembers(param);
        return R.ok().put("result", list);
    }
}
