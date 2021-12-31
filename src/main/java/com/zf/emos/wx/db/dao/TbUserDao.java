package com.zf.emos.wx.db.dao;

import com.zf.emos.wx.db.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.Set;

@Mapper
public interface TbUserDao {
    public boolean haveRootUser() ;

    public int insert(HashMap param) ;

    public Integer searchIdByOpenId(String openId) ;

    public Set<String> searchUserPermissions(int userId) ;
}