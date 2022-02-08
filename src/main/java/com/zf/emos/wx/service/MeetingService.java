package com.zf.emos.wx.service;

import com.zf.emos.wx.db.pojo.TbMeeting;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author pumpkin
 * @date 2022/1/29 0029 下午 19:30
 */
public interface MeetingService {
    public void insertMeeting(TbMeeting tbMeeting) ;

    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param) ;
}
