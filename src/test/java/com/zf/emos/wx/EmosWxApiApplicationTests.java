package com.zf.emos.wx;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.zf.emos.wx.db.pojo.MessageEntity;
import com.zf.emos.wx.db.pojo.MessageRefEntity;
import com.zf.emos.wx.db.pojo.TbMeeting;
import com.zf.emos.wx.service.MeetingService;
import com.zf.emos.wx.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class EmosWxApiApplicationTests {

    @Autowired
    private MessageService messageService ;

    @Autowired
    private MeetingService meetingService ;

    @Test
    void contextLoads() {
        for (int i = 0; i < 100; i++) {
            MessageEntity messageEntity = new MessageEntity();
            messageEntity.setUuid(IdUtil.simpleUUID());
            messageEntity.setSenderId(0);
            messageEntity.setSenderName("系统消息");
            messageEntity.setMsg("这是第 " + i + " 条测试消息");
            messageEntity.setSendTime(new Date());
            String id = messageService.insertMessage(messageEntity);

            MessageRefEntity refEntity = new MessageRefEntity();
            refEntity.setMessageId(id);
            refEntity.setReceiverId(9);
            refEntity.setLastFlag(true);
            refEntity.setReadFlag(false);
            messageService.insertRef(refEntity) ;
        }
    }

    @Test
    void createMeetingTest(){
        for (int i = 0; i < 100; i++) {
            TbMeeting meeting = new TbMeeting();
            meeting.setId((long)i);
            meeting.setUuid(IdUtil.simpleUUID());
            meeting.setTitle("测试会议"+i);
            meeting.setCreatorId(9L); //ROOT用户ID
            meeting.setDate(DateUtil.today());
            meeting.setPlace("线上会议室");
            meeting.setStart("08:30");
            meeting.setEnd("10:30");
            meeting.setType((short) 1);
            meeting.setMembers("[9]");
            meeting.setDesc("会议研讨Emos项目上线测试");
            meeting.setInstanceId(IdUtil.simpleUUID());
            meeting.setStatus((short)3);
            meeting.setCreateTime(new Date());
            meetingService.insertMeeting(meeting);
        }
    }

}
