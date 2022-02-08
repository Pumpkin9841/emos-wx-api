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

    private String senderPhoto = "https://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83erOibQT46XSkarhN5ca1fUia82BI6HjicYP2Blo80Oc3icdFmgQxuqXP5zvR1R1FAiaoJPreTB3uedQvQg/132" ;

    private String senderName ;

    private String msg ;

    @Indexed
    private Date sendTime ;
}
