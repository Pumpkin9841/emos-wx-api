package com.zf.emos.wx.db.dao;

import com.zf.emos.wx.db.pojo.TbWorkday;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbWorkdayDao {
    public Integer searchTodayIsWorkdays() ;
}