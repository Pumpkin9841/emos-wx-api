package com.zf.emos.wx.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

/**
 * @author pumpkin
 * @date 2022/1/22 0022 下午 14:53
 */

@Data
@Document(collection = "message")
public class MessageEntity implements Serializable {
    @Id
    private String _id ;

    @Indexed(unique = true)
    private String uuid ;

    @Indexed
    private Integer senderId ;

    private String senderPhoto = "https://pumpkn.xyz/upload/2021/10/NXQW%60%7BUI%5D6NYW32$%7B@1~S1Y-3c97ee1d7b8146788a008ad933217da8.png" ;

    private String senderName ;

    private String msg ;

    @Indexed
    private Date sendTime ;
}
