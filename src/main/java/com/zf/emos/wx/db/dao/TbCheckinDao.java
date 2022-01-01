package com.zf.emos.wx.db.dao;

import com.zf.emos.wx.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;

@Mapper
public interface TbCheckinDao {
    public Integer haveCheckin(HashMap param) ;

    public void insert(TbCheckin entity);


}