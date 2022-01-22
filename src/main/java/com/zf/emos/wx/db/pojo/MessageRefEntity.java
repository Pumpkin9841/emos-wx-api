package com.zf.emos.wx.db.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author pumpkin
 * @date 2022/1/22 0022 下午 15:00
 */

@Data
@Document(collection = "message_ref")
public class MessageRefEntity implements Serializable {
    @Id
    private String _id ;

    @Indexed
    private String messageId ;

    @Indexed
    private Integer receiverId ;

    @Indexed
    private Boolean readFlag ;

    @Indexed
    private Boolean lastFlag ;
}
