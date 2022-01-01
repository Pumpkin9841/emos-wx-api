package com.zf.emos.wx.db.dao;

import com.zf.emos.wx.db.pojo.TbFaceModel;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TbFaceModelDao {
    public String searchFaceModel(int userId);
    public void insert(TbFaceModel tbFaceModel) ;
    public int deleteFaceModel(int userId) ;
}