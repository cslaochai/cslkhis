package com.mis.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用主键入参（前端 id 序列化为字符串）
 */
@Data
public class IdDTO implements Serializable {

    private String id;
}
