package com.his.system.vo;

import lombok.Data;

import java.util.List;

/**
 * 字典类型及其数据分组出参
 */
@Data
public class DictTypeGroupVO {

    /**
     * 字典类型编码
     */
    private String dictType;

    /**
     * 该类型下的字典数据
     */
    private List<SysDictDataVO> dataList;
}
