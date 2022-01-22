package com.zf.emos.wx.controller;

import com.zf.emos.wx.common.utils.R;
import com.zf.emos.wx.config.shiro.JwtUtils;
import com.zf.emos.wx.controller.form.DeleteMessageRefByIdForm;
import com.zf.emos.wx.controller.form.MessageForm;
import com.zf.emos.wx.controller.form.SearchMessageByIdForm;
import com.zf.emos.wx.controller.form.UpdateUnreadMesageByIdForm;
import com.zf.emos.wx.service.MessageService;
import com.zf.emos.wx.task.MessageTask;
import io.swagger.annotations.ApiOperation;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;

/**
 * @author pumpkin
 * @date 2022/1/22 0022 下午 17:05
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private JwtUtils jwtUtils ;

    @Autowired
    private MessageService messageService ;

    @Autowired
    private MessageTask messageTask ;

    @PostMapping("/searchMessageByPage")
    @ApiOperation("获取分页信息列表")
    public R searchMessageByPage(@Valid @RequestBody MessageForm form , @RequestHeader("token") String token){
        int userId = jwtUtils.getUserId(token);
        Integer page = form.getPage();
        Integer length = form.getLength();
        long start = (page-1)*length ;
        List<HashMap> result = messageService.searchMessageByPage(userId, start, length);
        return R.ok().put("result" , result) ;
    }

    @PostMapping("/searchMessageById")
    @ApiOperation("根据id查询消息")
    public R searchMessageById(@Valid @RequestBody SearchMessageByIdForm form){
        HashMap map = messageService.searchMessageById(form.getId());
        return R.ok().put("result" , map) ;
    }

    @PostMapping("/UpdateUnreadMesageById")
    @ApiOperation("根据id跟新未读消息状态为已读")
    public R UpdateUnreadMesageById(@Valid @RequestBody UpdateUnreadMesageByIdForm form){
        long row = messageService.updateUnreadMessage(form.getId());
        return R.ok().put("result" , row==1?true:false) ;
    }

    @PostMapping("/DeleteMessageRefById")
    @ApiOperation("删除已读消息的接收人")
    public R DeleteMessageRefById(@Valid @RequestBody DeleteMessageRefByIdForm form){
        long row = messageService.deleteMessageRefById(form.getId());
        return R.ok().put("result" , row==1?true:false) ;
    }




    @GetMapping("/refreshMessage")
    @ApiOperation("刷新用户的消息")
    public R refreshMessage(@RequestHeader("token") String token) {
        int userId = jwtUtils.getUserId(token);
        //异步接收消息
        messageTask.receiverAsync(userId + "");
        //查询接收了多少条消息
        long lastRows=messageService.searchLastCount(userId);
        //查询未读数据
        long unreadRows = messageService.searchUnreadCount(userId);
        return R.ok().put("lastRows", lastRows).put("unreadRows", unreadRows);
    }



}
