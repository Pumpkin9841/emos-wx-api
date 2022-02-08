package com.zf.emos.wx.service.impl;

import cn.hutool.json.JSONArray;
import com.zf.emos.wx.db.dao.TbMeetingDao;
import com.zf.emos.wx.db.pojo.TbMeeting;
import com.zf.emos.wx.exception.EmosException;
import com.zf.emos.wx.service.MeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author pumpkin
 * @date 2022/1/29 0029 下午 19:31
 */
@Service
@Slf4j
public class MeetingServiceImpl implements MeetingService {

    @Autowired
    private TbMeetingDao tbMeetingDao ;

    @Override
    public void insertMeeting(TbMeeting tbMeeting) {
        int row = tbMeetingDao.insertMeeting(tbMeeting);
        if( row != 1 ){
            throw new EmosException("会议添加失败") ;
        }
        //TODO 开启审批工作流
    }

    @Override
    public ArrayList<HashMap> searchMyMeetingListByPage(HashMap param) {
        ArrayList<HashMap> hashMaps = tbMeetingDao.searchMyMeetingListByPage(param);
        String date = null ;
        ArrayList resultList = new ArrayList();
        HashMap resultMap = null ;
        JSONArray jsonArray = null ;
        for (HashMap map : hashMaps) {
            String temp = map.get("date").toString();
            if( !temp.equals(date) ){
                date = temp ;
                resultMap = new HashMap() ;
                resultMap.put("date" , date) ;
                jsonArray = new JSONArray() ;
                resultMap.put("list" , jsonArray) ;
                resultList.add(resultMap);
            }
            jsonArray.put(map) ;
        }
        return resultList;
    }
}
