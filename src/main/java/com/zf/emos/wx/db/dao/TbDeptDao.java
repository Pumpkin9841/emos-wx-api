package com.zf.emos.wx.db.dao;

import com.zf.emos.wx.db.pojo.TbDept;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbDeptDao {
    public ArrayList<HashMap> searchDeptMembers(String keyword) ;
}