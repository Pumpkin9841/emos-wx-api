package com.zf.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author pumpkin
 * @date 2021/12/30 0030 下午 22:14
 */

@Data
@ApiModel
public class RegisterForm {
    @NotBlank(message = "注册码不能为空")
    @Pattern(regexp = "^[0-9]{6}$" , message = "注册码必须是6位数字")
    private String registerCode ;

    @NotBlank(message = "临时授权码不能为空")
    private String code ;

    @NotBlank(message = "微信昵称不能为空")
    private String nickname ;

    @NotBlank(message = "微信头像不能为空")
    private String photo ;
}
