package com.zf.emos.wx.service;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author pumpkin
 * @date 2021/12/31 0031 下午 23:08
 */
public interface CheckinService {
    /**
     * 验证用户当前是否能够打卡
     * @param userId 用户id
     * @param date 日期
     * @return 是否能签到的信息
     */
    public String validCanCheckin(int userId , String date) ;

    public void checkin(HashMap param) ;

    public void createFaceModel(int userId , String path) ;

    /**
     * 根据userId查询该用户今天的签到信息
     * @param userId 用户id
     * @return 用户姓名、部门、签到地址等信息的map
     */
    public HashMap searchTodayCheckin(int userId) ;

    /**
     * 根据userId查询用户签到的总天数
     * @param userId 用户id
     * @return 用户签到的总天数
     */
    public long searchCheckinDays(int userId) ;

    public ArrayList<HashMap> searchWeekCheckin(HashMap param) ;

    public ArrayList<HashMap> searchMonthCheckin(HashMap param) ;
}
