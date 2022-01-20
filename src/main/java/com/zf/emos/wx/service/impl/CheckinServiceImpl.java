package com.zf.emos.wx.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateRange;
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
import com.zf.emos.wx.db.pojo.TbFaceModel;
import com.zf.emos.wx.exception.EmosException;
import com.zf.emos.wx.service.CheckinService;
import com.zf.emos.wx.task.EmailTask;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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

    @Autowired
    private TbUserDao userDao ;

    @Value("${emos.face.checkinUrl}")
    private String checkinUrl ;

    @Value("${emos.face.createFaceModelUrl}")
    private String createFaceModelUrl ;

    @Value("${emos.email.hr}")
    private String hrEmail ;

    @Value("${emos.code}")
    private String code ;



    @Autowired
    private EmailTask emailTask ;

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
            request.form("code" , code) ;
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
                String address = (String) param.get("address");
                String country = (String) param.get("country");
                String province = (String) param.get("province");
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
                                // 发送警告邮件
                                HashMap<String ,String> hashMap = userDao.searchNameAndDept(userId);
                                String name = hashMap.get("name");
                                String deptName = hashMap.get("dept_name");
                                deptName = deptName != null ? deptName : "" ;
                                SimpleMailMessage message = new SimpleMailMessage();
                                message.setTo(hrEmail);
                                message.setSubject("员工" + name + "身处高风险疫情地区警告");
                                message.setText(deptName + "员工" + name + "," + DateUtil.format(new Date() , "yyyy年MM月dd日") + "处于" + address + "，属于高风险地区" );
                                emailTask.sendAsync(message);
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

    @Override
    public void createFaceModel(int userId, String path) {
        HttpRequest request = HttpUtil.createPost(createFaceModelUrl);
        request.form("photo" , FileUtil.file(path)) ;
        request.form("code" , code) ;
        HttpResponse response = request.execute();
        String body = response.body();
        if( "无法识别出人脸".equals(body) || "照片中存在多张人脸".equals(body) ){
            throw new EmosException(body) ;
        }
        else{
            TbFaceModel tbFaceModel = new TbFaceModel();
            tbFaceModel.setUserId(userId);
            tbFaceModel.setFaceModel(body);
            faceModelDao.insert(tbFaceModel);
        }
    }

    @Override
    public HashMap searchTodayCheckin(int userId) {
        HashMap map = tbCheckinDao.searchTodayCheckin(userId);
        return map;
    }

    @Override
    public long searchCheckinDays(int userId) {
        long days = tbCheckinDao.searchCheckinDays(userId);
        return days;
    }

    @Override
    public ArrayList<HashMap> searchWeekCheckin(HashMap param) {
        // checkinList = { {日期:yyyy-MM-dd, status: "正常"} , ... }
        ArrayList<HashMap> checkinList = tbCheckinDao.searchWeekCheckin(param);
        ArrayList<String> holidayList = tbHolidaysDao.searchHolidaysInRange(param);
        ArrayList<String> workdayList = tbWorkdayDao.searchWorkdayInRange(param);
        DateTime startDate = DateUtil.parseDate(param.get("startDate").toString());
        DateTime endDate = DateUtil.parseDate(param.get("endDate").toString());
        DateRange range = DateUtil.range(startDate, endDate, DateField.DAY_OF_MONTH);

        ArrayList list = new ArrayList();
        range.forEach(one->{
            String date = one.toString();
            //查看今天是节假日还是工作日
            String type = "工作日" ;
            if( one.isWeekend() ){
                type = "节假日" ;
            }
            if( holidayList!=null && holidayList.contains(date) ){
                type = "节假日" ;
            }
            else if( workdayList!=null && workdayList.contains(date) ){
                type = "工作日" ;
            }
            String status = "" ;
            if( type.equals("工作日") && DateUtil.compare(one , DateUtil.date())<= 0 ){
                status = "缺勤" ;
                boolean flag = false ;
                for (HashMap<String ,String> map : checkinList) {
                    if( map.values().contains(date) ){
                        status = map.get("status") ;
                        flag = true ;
                        break ;
                    }
                }

                DateTime endTime = DateUtil.parse(DateUtil.today() + " " + systemConstants.attendanceEndTime);
                String today = DateUtil.today();
                if( date.equals(today) && DateUtil.date().isBefore(endTime) && flag == false ){
                    status = "" ;
                }
            }
            HashMap hashMap = new HashMap();
            hashMap.put("date" , date) ;
            hashMap.put("status" ,status) ;
            hashMap.put("type" , type) ;
            hashMap.put("day" , one.dayOfWeekEnum().toChinese("周")) ;
            list.add(hashMap) ;

        });
        return list;
    }

    @Override
    public ArrayList<HashMap> searchMonthCheckin(HashMap param) {
        return this.searchWeekCheckin(param) ;
    }
}
