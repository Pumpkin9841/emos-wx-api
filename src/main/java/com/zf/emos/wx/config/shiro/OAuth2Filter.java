package com.zf.emos.wx.config.shiro;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;


import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 周凡
 * @date 2021/12/12 0012 下午 13:08
 */
@Component
@Scope("prototype")
public class OAuth2Filter extends AuthenticatingFilter {
    @Autowired
    private ThreadLocalToken threadLocalToken ;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire ;

    @Autowired
    private JwtUtils jwtUtils ;

    @Autowired
    private RedisTemplate redisTemplate ;

    /**
     * 拦截请求之后，用于把令牌字符串封装成令牌对象
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws Exception
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = getHttpServletRequestToken(request);
        if( StringUtils.isBlank(token) ){
            return null ;
        }
        return new OAuth2Token(token);
    }

    /**
     * 拦截请求，判断请求是否应该被shiro处理
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest rq = (HttpServletRequest) request;
        //小程序的Ajax请求提交application/json数据的时候，会先发出options请求
        //这里放行options请求，不需要shiro处理
        if(rq.getMethod().equals(RequestMethod.OPTIONS.name())){
            return true ;
        }
        //除了options请求之后，所有请求都应该被shiro处理
        return false;
    }

    /**
     * 处理所有应该被shiro所处理的请求
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        resp.setHeader("Content-Type" , "text/html;charset=UTF-8");
        //允许跨域
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));

        return false;
    }

    public String getHttpServletRequestToken(HttpServletRequest request){
        //从请求头中获取token
        String token = request.getHeader("token");
        //如果请求头中没有token，则从请求体中获取token
        if(StringUtils.isBlank(token)){
            token = request.getParameter("token") ;
        }
        return token ;
    }
}
