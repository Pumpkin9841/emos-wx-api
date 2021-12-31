package com.zf.emos.wx.db.dao;

import com.zf.emos.wx.db.pojo.TbHolidays;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbHolidaysDao {
    public Integer searchTodayIsHolidays() ;
}