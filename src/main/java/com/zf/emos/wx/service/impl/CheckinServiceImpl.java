package com.zf.emos.wx.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.zf.emos.wx.config.SystemConstants;
import com.zf.emos.wx.db.dao.TbCheckinDao;
import com.zf.emos.wx.db.dao.TbHolidaysDao;
import com.zf.emos.wx.db.dao.TbWorkdayDao;
import com.zf.emos.wx.service.CheckinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author pumpkin
 * @date 2021/12/31 0031 下午 23:11
 */
@Service
@Scope("prototype")
@Slf4j
public class CheckinServiceImpl implements CheckinService {

    @Autowired
    private TbHolidaysDao tbHolidaysDao ;

    @Autowired
    private TbWorkdayDao tbWorkdayDao ;

    @Autowired
    private TbCheckinDao tbCheckinDao ;

    @Autowired
    private SystemConstants systemConstants ;

    @Override
    public String validCanCheckin(int userId, String date) {
        //true: 当前日期为节假日  否则 false
        boolean bool1 = tbHolidaysDao.searchTodayIsHolidays() != null ? true : false;
        //true: 当前日期为工作日
        boolean bool2 = tbWorkdayDao.searchTodayIsWorkdays() != null ? true : false;

        String type = "工作日" ;
        if(DateUtil.date().isWeekend()){
            type = "节假日" ;
        }
        if( bool1 ){
            type = "节假日" ;
        }
        else if( bool2 ){
            type = "工作日" ;
        }

        if( type.equals("节假日") ){
            return "节假日不需要考勤" ;
        }
        //工作日
        else{
            DateTime now = DateUtil.date();
            String start = DateUtil.today() + " " + systemConstants.attendanceStartTime;
            String end = DateUtil.today() + " " + systemConstants.attendanceEndTime;
            DateTime attendStart = DateUtil.parse(start);
            DateTime attendEnd = DateUtil.parse(end);
            if( now.isBefore(attendStart) ){
                return "没有到上班考勤时间" ;
            }
            else if( now.isAfter(attendEnd) ){
                return "考勤时间已经结束" ;
            }
            //若在上班考勤时间内，还需要判断该用户当天是否已经考勤过了
            else {
                HashMap<String, Object> map = new HashMap<>();
                map.put("userId" , userId) ;
                map.put("date" , date) ;
                map.put("start" , start) ;
                map.put("end" , end) ;
                Integer integer = tbCheckinDao.haveCheckin(map);
                boolean b = integer != null ? true : false;
                return b ? "请勿重复考勤" : "可以考勤" ;

            }
        }


        return null;
    }
}
