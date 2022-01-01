package com.zf.emos.wx.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.zf.emos.wx.config.SystemConstants;
import com.zf.emos.wx.db.dao.*;
import com.zf.emos.wx.db.pojo.TbCheckin;
import com.zf.emos.wx.exception.EmosException;
import com.zf.emos.wx.service.CheckinService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author pumpkin
 * @date 2021/12/31 0031 下午 23:11
 */
@Service
@Scope("prototype")
@Slf4j
public class CheckinServiceImpl implements CheckinService {

    @Autowired
    private TbHolidaysDao tbHolidaysDao ;

    @Autowired
    private TbWorkdayDao tbWorkdayDao ;

    @Autowired
    private TbCheckinDao tbCheckinDao ;

    @Autowired
    private SystemConstants systemConstants ;

    @Autowired
    private TbFaceModelDao faceModelDao ;

    @Autowired
    private TbCityDao tbCityDao ;

    @Value("${emos.face.checkinUrl}")
    private String checkinUrl ;

    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl ;

    @Override
    public String validCanCheckin(int userId, String date) {
        //true: 当前日期为节假日  否则 false
        boolean bool1 = tbHolidaysDao.searchTodayIsHolidays() != null ? true : false;
        //true: 当前日期为工作日
        boolean bool2 = tbWorkdayDao.searchTodayIsWorkdays() != null ? true : false;

        String type = "工作日" ;
        if(DateUtil.date().isWeekend()){
            type = "节假日" ;
        }
        if( bool1 ){
            type = "节假日" ;
        }
        else if( bool2 ){
            type = "工作日" ;
        }

        if( type.equals("节假日") ){
            return "节假日不需要考勤" ;
        }
        //工作日
        else{
            DateTime now = DateUtil.date();
            String start = DateUtil.today() + " " + systemConstants.attendanceStartTime;
            String end = DateUtil.today() + " " + systemConstants.attendanceEndTime;
            DateTime attendStart = DateUtil.parse(start);
            DateTime attendEnd = DateUtil.parse(end);
            if( now.isBefore(attendStart) ){
                return "没有到上班考勤时间" ;
            }
            else if( now.isAfter(attendEnd) ){
                return "考勤时间已经结束" ;
            }
            //若在上班考勤时间内，还需要判断该用户当天是否已经考勤过了
            else {
                HashMap<String, Object> map = new HashMap<>();
                map.put("userId" , userId) ;
                map.put("date" , date) ;
                map.put("start" , start) ;
                map.put("end" , end) ;
                Integer integer = tbCheckinDao.haveCheckin(map);
                boolean b = integer != null ? true : false;
                return b ? "请勿重复考勤" : "可以考勤" ;

            }
        }

    }

    @Override
    public void checkin(HashMap param) {
        //判断能否签到
        DateTime date = DateUtil.date(); //当前时间
        DateTime d1 = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceTime); //上班时间
        DateTime d2 = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceEndTime); //截至打卡时间
        int status = 1 ; //正常签到
        if( date.compareTo(d1) <= 0 ){
            status = 1 ;
        }
        else if( date.compareTo(d1) > 0 && date.compareTo(d2) <= 0 ){
            status = 2 ; //迟到
        }
        //查询签到的人脸模型
        Integer userId = (Integer) param.get("userId");
        String faceModel = faceModelDao.searchFaceModel(userId);
        if( faceModel == null ){
            throw new EmosException("不存在人脸模型") ;
        }
        else{
            String path = (String) param.get("path"); //拍照的路径
            HttpRequest request = HttpUtil.createPost(checkinUrl);
            request.form("photo" , FileUtil.file(path) , "targetModel" , faceModel) ;
            HttpResponse response = request.execute();
            if( response.getStatus() != 200 ){
                log.error("人脸识别服务异常");
                throw new EmosException("人脸识别服务异常") ;
            }
            String body = response.body();
            if( "无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body)){
                throw new EmosException(body) ;
            }
            else if("False".equals(body)){
                throw new EmosException("签到无效，非本人签到") ;
            }
            else if("True".equals(body)){
                //这里获取签到地区新冠疫情风险等级
                int risk = 1 ; //低风险
                String city = (String) param.get("city");
                String district = (String) param.get("district");
                if(!StrUtil.isBlank(city) && !StrUtil.isBlank(district)){
                    String code = tbCityDao.searchCode(city);
                    String url = "http://m."+ code + ".bendibao.com/news/yqdengji/?qu=" + district ;
                    try {
                        Document document = Jsoup.connect(url).get(); //获取url网页的html
                        Elements elements = document.getElementsByClass("list-content"); //获取html中类名为list-content的元素
                        if( elements.size() > 0 ){
                            Element element = elements.get(0);
                            String text = element.select("p:last-child").text();
                            if("高风险".equals(text)){
                                risk = 3 ;
                                //TODO 发送警告邮件
                            }
                            else if("中风险".equals(text)) {
                                risk = 2 ;
                            }
                        }

                    } catch (Exception e) {
                        log.error("执行异常" , e);
                        throw new EmosException("获取风险等级失败") ;
                    }
                }
                // 保存签到记录
                String address = (String) param.get("address");
                String country = (String) param.get("country");
                String province = (String) param.get("province");
                TbCheckin tbCheckin = new TbCheckin();
                tbCheckin.setUserId(userId);
                tbCheckin.setAddress(address);
                tbCheckin.setCountry(country);
                tbCheckin.setProvince(province);
                tbCheckin.setCity(city);
                tbCheckin.setDistrict(district);
                tbCheckin.setStatus((byte)status);
                tbCheckin.setRisk(risk);
                tbCheckin.setDate(DateUtil.today());
                tbCheckin.setCreateTime(date);
                tbCheckinDao.insert(tbCheckin);
            }
        }

    }
}
