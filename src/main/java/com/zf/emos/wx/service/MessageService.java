package com.zf.emos.wx.service;

import com.zf.emos.wx.db.pojo.MessageEntity;
import com.zf.emos.wx.db.pojo.MessageRefEntity;

import java.util.HashMap;
import java.util.List;

/**
 * @author pumpkin
 * @date 2022/1/22 0022 下午 16:42
 */
public interface MessageService {

    public String insertMessage(MessageEntity entity);

    public String insertRef(MessageRefEntity entity);

    public long searchUnreadCount(int userId);

    public long searchLastCount(int userId);

    public List<HashMap> searchMessageByPage(int userId, long start, int length) ;

    public HashMap searchMessageById(String id);

    public long updateUnreadMessage(String id) ;

    public long deleteMessageRefById(String id);

    public long deleteUserMessageRef(int userId);
}

