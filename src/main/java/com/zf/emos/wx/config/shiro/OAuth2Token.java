package com.zf.emos.wx.config.shiro;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * @author 周凡
 * @date 2021/12/11 0011 下午 20:18
 */
public class OAuth2Token implements AuthenticationToken {
    private String token ;

    public OAuth2Token(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
