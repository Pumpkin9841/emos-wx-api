package com.zf.emos.wx.service;

import java.util.Set;

/**
 * @author pumpkin
 * @date 2021/12/30 0030 下午 21:18
 */
public interface UserService {
    /**
     * 注册新用户
     * @param registerCode 邀请码
     * @param code 用户临时登陆凭证
     * @param nickname 微信昵称
     * @param photo 微信头像
     * @return 添加成功返回id
     */
    public int register( String registerCode , String code , String nickname ,String photo ) ;

    /**
     * 根据用户id查找用户的权限列表
     * @param userId 用户id
     * @return 用户权限的集合
     */
    public Set<String> searchUserPermissions(int userId) ;

    /**
     * 根据临时授权码获取openid，通过openid查找是否存在该用户
     * @param code 临时授权码
     * @return 用户的id
     */
    public Integer login(String code) ;
}
