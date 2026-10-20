package com.his.miniapp.dto;

import com.his.common.base.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 消息分页查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MessagePageDTO extends PageParam implements Serializable {
}
