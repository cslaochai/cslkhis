package com.his.miniapp.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 消息标记已读入参（只允许标记自己的消息，越权判定在 service）。
 */
@Data
public class MarkReadDTO implements Serializable {

    @NotEmpty(message = "messageIds不能为空")
    private List<String> messageIds;
}
