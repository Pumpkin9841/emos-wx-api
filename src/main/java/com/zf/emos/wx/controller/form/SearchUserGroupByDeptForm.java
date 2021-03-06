package com.zf.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * @author pumpkin
 * @date 2022/2/8 0008 下午 22:20
 */
@Data
@ApiModel
public class SearchUserGroupByDeptForm {
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,15}$")
    private String keyword ;
}
