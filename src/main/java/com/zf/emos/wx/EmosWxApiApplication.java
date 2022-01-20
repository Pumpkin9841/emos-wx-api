package com.zf.emos.wx;

import cn.hutool.core.util.StrUtil;
import com.zf.emos.wx.config.SystemConstants;
import com.zf.emos.wx.db.dao.SysConfigDao;
import com.zf.emos.wx.db.pojo.SysConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

@SpringBootApplication
@ServletComponentScan
@Slf4j
@EnableAsync //开启异步
public class EmosWxApiApplication {

    @Autowired
    private SystemConstants systemConstants ;

    @Autowired
    private SysConfigDao sysConfigDao ;

    @Value("${emos.image-folder}")
    private String imageFolder ;

    public static void main(String[] args) {

        SpringApplication.run(EmosWxApiApplication.class, args);
    }

    //让SpringBoot项目启动后自动执行的方法
    @PostConstruct
    public void init(){
        List<SysConfig> sysConfigs = sysConfigDao.selectAllParam();
        sysConfigs.forEach(one->{
            String paramKey = one.getParamKey();
            //改成驼峰命名
            paramKey = StrUtil.toCamelCase(paramKey);
            String paramValue = one.getParamValue();
            try {
                Field field = systemConstants.getClass().getDeclaredField(paramKey);
                field.set(systemConstants , paramValue);
            } catch (Exception e) {
                log.error("执行异常" , e);
            }
        });
        new File(imageFolder).mkdirs() ;
    }

}
