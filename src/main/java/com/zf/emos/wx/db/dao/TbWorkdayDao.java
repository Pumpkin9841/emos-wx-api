package com.zf.emos.wx.db.dao;

import com.zf.emos.wx.db.pojo.TbWorkday;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbWorkdayDao {

    public Integer searchTodayIsWorkdays() ;

    public ArrayList<String> searchWorkdayInRange(HashMap param);
}