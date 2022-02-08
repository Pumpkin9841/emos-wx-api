package com.zf.emos.wx.db.dao;

import com.zf.emos.wx.db.pojo.TbMeeting;
import org.apache.ibatis.annotations.Mapper;

import java.util.ArrayList;
import java.util.HashMap;

@Mapper
public interface TbMeetingDao {
    public int insertMeeting(TbMeeting tbMeeting) ;

    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param) ;
}