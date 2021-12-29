package com.zf.emos.wx.exception;

import lombok.Data;

/**
 * @author 周凡
 * @date 2021/12/10 0010 下午 15:10
 */

/**
 * 如果继承Exception，必须手动显式处理，要么上抛，要么捕获
 * 如果继承RuntimeException这个类，RuntimeException类型的异常可以被虚拟机隐式处理，这就省去了很多手动处理异常的麻烦
 * */
@Data
public class EmosException extends RuntimeException{
    private String msg ;
    private int code ;

    public EmosException() {
    }

    public EmosException( String msg ){
        super(msg);
        this.msg = msg ;
    }

    public EmosException( String msg , Throwable e){
        super(msg , e);
        this.msg = msg ;
    }

    public EmosException( String msg , int code ){
        super(msg);
        this.msg = msg;
        this.code = code ;
    }

    public EmosException( String msg , int code , Throwable e ){
        super(msg , e);
        this.msg = msg ;
        this.code = code ;
    }



}
