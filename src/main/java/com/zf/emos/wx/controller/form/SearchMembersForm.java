package com.zf.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author pumpkin
 * @date 2022/2/8 0008 下午 23:01
 */
@Data
@ApiModel
public class SearchMembersForm {
    @NotBlank
    private String members;

}
