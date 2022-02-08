package com.zf.emos.wx.service.impl;



import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zf.emos.wx.db.dao.TbDeptDao;
import com.zf.emos.wx.db.dao.TbUserDao;
import com.zf.emos.wx.db.pojo.MessageEntity;
import com.zf.emos.wx.db.pojo.TbUser;
import com.zf.emos.wx.exception.EmosException;
import com.zf.emos.wx.service.UserService;
import com.zf.emos.wx.task.MessageTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author pumpkin
 * @date 2021/12/30 0030 下午 21:19
 */
@Service
@Slf4j
@Scope("prototype")
public class UserServiceImpl implements UserService {

    @Value("${wx.app-id}")
    private String appId ;

    @Value("${wx.app-secret}")
    private String appSecret ;

    @Autowired
    private TbUserDao userDao ;
    
    @Autowired
    private TbDeptDao deptDao ;

    @Autowired
    private MessageTask messageTask ;

    /**
     * 获取用户openId
     * @param code 用户的临时授权字符串
     * @return
     */
    public String getOpenId(String code){
        String url = "https://api.weixin.qq.com/sns/jscode2session" ;
        HashMap<String, Object> param = new HashMap<>();
        param.put("appid" , appId) ;
        param.put("secret" , appSecret) ;
        param.put("js_code" , code) ;
        param.put("grant_type" , "authorization_code") ;
        String response = HttpUtil.post(url , param);
        JSONObject json = JSONUtil.parseObj(response);
        String openid = json.getStr("openid");
        if( openid == null || openid.length() == 0 ){
            throw new RuntimeException("临时登陆凭证错误") ;
        }
        return openid ;
    }

    @Override
    public int register(String registerCode, String code, String nickname, String photo) {
        //如果邀请码是000000 ，代表注册超级管理员 系统中超级管理员只能有一个
        if( registerCode.equals("000000") ){
            boolean bool = userDao.haveRootUser();
            //不存在超级管理员
            if( !bool ){
                String openId = getOpenId(code);
                HashMap<String, Object> param = new HashMap<>();
                param.put("openId" , openId) ;
                param.put("nickname" , nickname) ;
                param.put("photo" , photo) ;
                param.put("role" , "[0]") ;
                param.put("status" , 1) ;
                param.put("createTime" , new Date()) ;
                param.put("root" , true) ;
                userDao.insert(param);
                Integer id = userDao.searchIdByOpenId(openId);

                MessageEntity entity = new MessageEntity();
                entity.setSenderId(0);
                entity.setSenderName("系统消息");
                entity.setUuid(IdUtil.simpleUUID());
                entity.setMsg("欢迎您注册称为系统管理员，请及时更新你的员工个人信息。");
                entity.setSendTime(new Date());
                messageTask.sendAsync(id+"" , entity);

                return id ;
            }
            else{
                //如果已经有超级管理员
                throw new EmosException("系统中已存在超级管理员") ;
            }
        }
        //TODO 创建普通用户
        else{
            return 0;
        }
    }

    @Override
    public Set<String> searchUserPermissions(int userId) {
        Set<String> permissions = userDao.searchUserPermissions(userId);
        return permissions ;
    }

    @Override
    public Integer login(String code) {
        String openId = getOpenId(code);
        Integer id = userDao.searchIdByOpenId(openId);
        if( id == null ){
            throw new EmosException("用户不存在") ;
        }

        //从消息队列中接收消息，转移到消息列表
        messageTask.receiverAsync(id+"");
        return id ;
    }

    @Override
    public TbUser searchById(int userId) {
        TbUser tbUser = userDao.searchById(userId);
        return tbUser ;
    }

    @Override
    public String searchUserHiredate(int userId) {
        String hiredate = userDao.searchUserHiredate(userId);
        return hiredate;
    }

    @Override
    public HashMap searchUserSummary(int userId) {
        HashMap userSummary = userDao.searchUserSummary(userId);
        return userSummary;
    }

    @Override
    public ArrayList<HashMap> searchUserGroupByDept(String keyword) {
        ArrayList<HashMap> list_1 = deptDao.searchDeptMembers(keyword);
        ArrayList<HashMap> list_2 = userDao.searchUserGroupByDept(keyword);
        for (HashMap map_1 : list_1) {
            long deptId = (long) map_1.get("id");
            ArrayList<HashMap> members = new ArrayList() ;
            for (HashMap map_2 : list_2) {
                long id = (long) map_2.get("id") ;
                if(deptId == id){
                    members.add(map_2) ;
                }
            }
            map_1.put("members" , members) ;
        }
        return list_1;
    }

    @Override
    public ArrayList<HashMap> searchMembers(List param) {
        ArrayList<HashMap> list = userDao.searchMembers(param);
        return list;
    }
}
