package com.zf.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author pumpkin
 * @date 2022/1/22 0022 下午 17:20
 */
@Data
@ApiModel
public class UpdateUnreadMesageByIdForm {
    @NotNull
    private String id ;
}
