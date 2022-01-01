package com.zf.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author pumpkin
 * @date 2022/1/1 0001 下午 18:36
 */
@Data
@ApiModel
public class CheckinForm {
    private String address ;
    private String country ;
    private String province ;
    private String city ;
    private String district ;
}
