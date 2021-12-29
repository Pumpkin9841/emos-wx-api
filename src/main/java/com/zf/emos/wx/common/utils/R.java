package com.zf.emos.wx.common.utils;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 周凡
 * @date 2021/12/10 0010 下午 15:43
 *
 * 虽然SpringMVC的Controller可以自动把对象转换成JSON返回给客户端，但是我们需要制定一个统一的标准，
 * 保证所有Controller返回的数据格式一致。最简便的办法就是定义封装类，来统一封装返回给客户端的数据。
 */
public class R extends HashMap<String,Object> {

    public R(){
        put("code" , HttpStatus.SC_OK) ;
        put("msg" , "success") ;
    }

    public static R ok(){
        R r = new R();
        return r ;
    }

    public static R ok(String msg){
        R r = ok();
        r.put("msg", msg);
        return r ;

    }

    public static R ok(Map<String,Object> map){
        R r = ok();
        r.putAll(map);
        return r ;
    }

    public static R error(int code , String msg){
        R r = new R();
        r.put("code" , code) ;
        r.put("msg" , msg) ;
        return r ;
    }

    public static R error(String msg){
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR , msg) ;
    }

    public static R error(){
        return error("未知异常，请联系管理员") ;
    }

    public R put( String key , Object value ){
        super.put(key , value) ;
        return this ;
    }

}
