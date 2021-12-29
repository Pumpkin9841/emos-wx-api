package com.zf.emos.wx.config.shiro;

import org.springframework.stereotype.Component;

/**
 * @author 周凡
 * @date 2021/12/12 0012 下午 12:39
 */
@Component
public class ThreadLocalToken {
    private ThreadLocal<String> local = new ThreadLocal<>() ;

    public void setToken(String token){
        local.set(token);
    }

    public String getToken(){
        return local.get() ;
    }

    public void clear(){
        local.remove();
    }
}
