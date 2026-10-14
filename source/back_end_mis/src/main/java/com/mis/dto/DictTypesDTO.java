package com.mis.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典批量查询入参（HIS：POST /system/dict/data/selectList|selectGroup）
 * dictTypes 支持单值字符串或逗号分隔多值
 */
@Data
public class DictTypesDTO implements Serializable {

    private String dictTypes;
}
