package com.zf.emos.wx.task;

import com.rabbitmq.client.*;
import com.zf.emos.wx.db.pojo.MessageEntity;
import com.zf.emos.wx.db.pojo.MessageRefEntity;
import com.zf.emos.wx.exception.EmosException;
import com.zf.emos.wx.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pumpkin
 * @date 2022/1/22 0022 下午 17:48
 */
@Component
@Slf4j
public class MessageTask {
    @Autowired
    private ConnectionFactory factory ;

    @Autowired
    private MessageService messageService ;

    //同步发送消息
    public void send(String topic , MessageEntity entity){
        String id = messageService.insertMessage(entity);
        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()){
            channel.queueDeclare(topic,true,false,false,null) ;
            HashMap header = new HashMap();
            header.put("messageId" ,id) ;
            AMQP.BasicProperties build = new AMQP.BasicProperties().builder().headers(header).build();
            channel.basicPublish("",topic,build,entity.getMsg().getBytes());
            log.debug("消息发送成功");
        }catch (Exception e){
            log.error("执行异常",e);
            throw new EmosException("向MQ发送消息失败") ;
        }
    }

    //异步发送消息
    @Async
    public void sendAsync(String topic , MessageEntity entity){
        send(topic,entity);
    }

    /**
     * 同步接收消息
     * @param topic 队列名
     * @return i 接收消息的数量
     */
    public int receiver(String topic){
        int i = 0 ;
        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()){
            channel.queueDeclare(topic,true,false,false,null) ;
            //队列中有多好消息是未知的，所以死循环，直到没有消息
            while(true){
                //false表示不自动应答
                GetResponse response = channel.basicGet(topic, false);
                if(response != null){
                    AMQP.BasicProperties properties = response.getProps();
                    Map<String, Object> headers = properties.getHeaders();//获取附加属性
                    String messageId = headers.get("messageId").toString();
                    //获取消息正文
                    byte[] body = response.getBody();
                    String message = new String(body) ;
                    log.debug("接收到消息" + message);
                    MessageRefEntity entity = new MessageRefEntity();
                    entity.setMessageId(messageId);
                    entity.setReceiverId(Integer.parseInt(topic));
                    entity.setLastFlag(true);
                    entity.setReadFlag(false);
                    messageService.insertRef(entity) ;
                    //数据保存后，才发送ACK应答，让topic删除这条消息
                    long delivertyTag = response.getEnvelope().getDeliveryTag() ;
                    channel.basicAck(delivertyTag ,false);
                    i++ ;
                }
                else{
                    break ;
                }
            }

        }catch (Exception e){
            log.error("执行异常",e);

        }
        return i ;
    }

    /**
     * 异步接收消息
     * @param topic
     */
    @Async
    public void receiverAsync(String topic){
        receiver(topic) ;
    }

    /**
     * 同步删除消息队列
     * @param topic
     */
    public void deleteQueue(String topic){
        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()){
            channel.queueDelete(topic) ;
            log.debug("消息队列删除成功");
        }catch (Exception e){
            log.error("执行异常",e);
            throw new EmosException("删除队列失败") ;
        }
    }

    /**
     * 异步删除队列
     * @param topic
     */
    @Async
    public void deleteQueueAsync(String topic){
        deleteQueue(topic);
    }
}
