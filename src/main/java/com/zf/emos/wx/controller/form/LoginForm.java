package com.zf.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author pumpkin
 * @date 2021/12/31 0031 下午 14:14
 */
@Data
@ApiModel
public class LoginForm {
    @NotBlank(message = "临时授权码不能为空")
    private String code ;
}
