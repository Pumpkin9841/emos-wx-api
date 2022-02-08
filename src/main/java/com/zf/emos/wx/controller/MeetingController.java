package com.zf.emos.wx.controller;

import com.zf.emos.wx.common.utils.R;
import com.zf.emos.wx.config.shiro.JwtUtils;
import com.zf.emos.wx.controller.form.SearchMyMeetingListByPageForm;
import com.zf.emos.wx.service.MeetingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author pumpkin
 * @date 2022/2/7 0007 下午 19:31
 */
@RestController
@RequestMapping("/meeting")
@Api("会议模块网络接口")
public class MeetingController {

    @Autowired
    private JwtUtils jwtUtils ;

    @Autowired
    private MeetingService meetingService ;

    @ApiOperation("查询会议列表分页数据")
    @PostMapping("/searchMyMeetingListByPage")
    public R searchMyMeetingListByPage(@Valid @RequestBody SearchMyMeetingListByPageForm form , @RequestHeader("token") String token){
        int userId = jwtUtils.getUserId(token);
        Integer page = form.getPage();
        Integer length = form.getLength();
        HashMap param = new HashMap();
        param.put("userId" , userId) ;
        param.put("start" , (long)((page-1)*length) ) ;
        param.put("length" , length) ;
        ArrayList<HashMap> list = meetingService.searchMyMeetingListByPage(param);
        return R.ok().put("result" , list) ;

    }
}
