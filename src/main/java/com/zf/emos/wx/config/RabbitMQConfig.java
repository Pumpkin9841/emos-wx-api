package com.zf.emos.wx.config;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author pumpkin
 * @date 2022/1/22 0022 下午 17:34
 */
@Configuration
public class RabbitMQConfig {
    @Bean
    public ConnectionFactory getFactory(){
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.88.128");
        connectionFactory.setPort(5672);
        return connectionFactory ;
    }
}
