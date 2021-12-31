package com.zf.emos.wx.config;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author pumpkin
 * @date 2021/12/31 0031 下午 22:05
 */

@Data
@Component
public class SystemConstants {
    public String attendanceStartTime;
    public String attendanceTime;
    public String attendanceEndTime;
    public String closingStartTime;
    public String closingTime;
    public String closingEndTime;
}
