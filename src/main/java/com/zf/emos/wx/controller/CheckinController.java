package com.zf.emos.wx.controller;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.zf.emos.wx.common.utils.R;
import com.zf.emos.wx.config.SystemConstants;
import com.zf.emos.wx.config.shiro.JwtUtils;
import com.zf.emos.wx.controller.form.CheckinForm;
import com.zf.emos.wx.controller.form.SearchMonthCheckinForm;
import com.zf.emos.wx.exception.EmosException;
import com.zf.emos.wx.service.CheckinService;
import com.zf.emos.wx.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author pumpkin
 * @date 2021/12/31 0031 下午 23:39
 */

@RequestMapping("/checkin")
@RestController
@Api("签到模块web接口")
@Slf4j
public class CheckinController {

    @Autowired
    private JwtUtils jwtUtils ;

    @Autowired
    private CheckinService checkinService ;

    @Value("${emos.image-folder}")
    private String imageFolder ;

    @Autowired
    private SystemConstants systemConstants ;

    @Autowired
    private UserService userService ;

    @GetMapping("/validCanCheckin")
    @ApiOperation("是否可以签到")
    public R validCanCheckin(@RequestHeader("token") String token){
        int userId = jwtUtils.getUserId(token);
        String result = checkinService.validCanCheckin(userId, DateUtil.today());
        return R.ok(result) ;
    }

    @PostMapping("/checkin")
    @ApiOperation("签到")
    public R  checkin(@Valid CheckinForm checkinForm , @RequestParam("photo")MultipartFile file , @RequestHeader("token") String token){
        if( null == file ){
            return R.error("没有上传文件") ;
        }
        int userId = jwtUtils.getUserId(token) ;
        String filename = file.getOriginalFilename().toLowerCase();
        String path = imageFolder + "/" + filename;
        if( !filename.endsWith("jpg") ){
            FileUtil.del(path) ;
            return R.error("必须提交JPG格式的图片") ;
        }
        else {
            try {
                file.transferTo(Paths.get(path));
                HashMap param = new HashMap<>();
                param.put("userId" , userId) ;
                param.put("path" , path) ;
                param.put("city" , checkinForm.getCity()) ;
                param.put("address" , checkinForm.getAddress()) ;
                param.put("district" , checkinForm.getDistrict()) ;
                param.put("country" , checkinForm.getCountry()) ;
                param.put("province" , checkinForm.getProvince()) ;
                checkinService.checkin(param);
                return R.ok("签到成功") ;
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new EmosException("保存图片错误") ;
            }
            finally {
                FileUtil.del(path) ;
            }
        }

    }

    @PostMapping("/createFaceModel")
    @ApiOperation("创建人脸模型")
    public R createFaceModel(@RequestParam("photo") MultipartFile file, @RequestHeader("token") String token) {
        int userId = jwtUtils.getUserId(token);
        if (file==null) {
            return R.error("没有上传文件");
        }
        String fileName = file.getOriginalFilename().toLowerCase();
        String path = imageFolder + "/" + fileName;
        if (!fileName.endsWith(".jpg")) {
            return R.error("必须提交JPG格式图片");
        } else {
            try {
                file.transferTo(Paths.get(path));
                checkinService.createFaceModel(userId, path);
                return R.ok("人脸建模成功");
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new EmosException("保存图片错误");
            } finally {
                FileUtil.del(path);
            }
        }
    }

    @ApiOperation("查询用户当日签到数据")
    @GetMapping("/searchTodayCheckin")
    public R searchTodayCheckin(@RequestHeader("token") String token){
        int userId = jwtUtils.getUserId(token);
        HashMap map = checkinService.searchTodayCheckin(userId);
        //获取上班时间
        String attendanceTime = systemConstants.attendanceTime;
        String closingTime = systemConstants.closingTime;
        map.put("attendanceTime" ,attendanceTime) ;
        map.put("closingTime" ,closingTime ) ;
        //获取该用户本月一共签到天数
        long days = checkinService.searchCheckinDays(userId);
        map.put("checkinDays" , days) ;

        //查询用户入职日期
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        //获取本周开始日期
        DateTime startTime = DateUtil.beginOfWeek(DateUtil.date());
        if( startTime.isBefore(hiredate) ){
            startTime = hiredate ;
        }
        DateTime endTime = DateUtil.endOfWeek(DateUtil.date());

        HashMap param = new HashMap();
        param.put("startDate" , startTime) ;
        param.put("endDate" , endTime) ;
        param.put("userId" , userId) ;
        //获取本周签到信息
        ArrayList<HashMap> weekCheckin = checkinService.searchWeekCheckin(param);
        map.put("weekCheckin" , weekCheckin) ;
        return R.ok().put("result" , map) ;
    }

    @PostMapping("/searchMonthCheckin")
    @ApiOperation("查询用户月签到信息")
    public R searchMonthCheckin(@Valid @RequestBody SearchMonthCheckinForm form , @RequestHeader("token") String token){
        int userId = jwtUtils.getUserId(token);
        //获取员工入职日期
        DateTime hiredate = DateUtil.parse(userService.searchUserHiredate(userId));
        //获取查询月份，如果月份为1，前面添加0改为01，以此类推
        String month = form.getMonth() < 10 ? "0" + form.getMonth() : form.getMonth().toString();
        //获取要查询的年月 因为是月统计，所以日期设为01，表示每月第一天
        DateTime startTime = DateUtil.parse(form.getYear() + "-" + month + "-01");
        //如果入职日期的月份在要查询的月份后面，则报错
        if( startTime.isBefore(DateUtil.beginOfMonth(hiredate)) ){
            throw new EmosException("只能查询入职以后的考勤日期") ;
        }
        //能到这一步，表示要查询的月份跟入职日期同月
        if( startTime.isBefore(hiredate) ){
            startTime = hiredate ;
        }

        DateTime emdTime = DateUtil.endOfMonth(startTime);
        HashMap param = new HashMap();
        param.put("userId" , userId) ;
        param.put("startDate" , startTime.toString()) ;
        param.put("endDate" , emdTime) ;
        ArrayList<HashMap> list = checkinService.searchMonthCheckin(param);

        int sum_1=0, sum_2=0, sum_3=0 ;
        for (HashMap<String ,String> one : list) {
            String type = one.get("type");
            String status = one.get("status");
            if("工作日".equals(type) ){
                if("正常".equals(status)){
                    sum_1++;
                }
                else if( "迟到".equals(status) ){
                    sum_2++;
                }
                else if("缺勤".equals(status)){
                    sum_3++;
                }
            }
        }
        return R.ok().put("list",list).put("sum_1",sum_1).put("sum_2",sum_2).put("sum_3",sum_3) ;
    }

}
