package com.zf.emos.wx.service;

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
}
