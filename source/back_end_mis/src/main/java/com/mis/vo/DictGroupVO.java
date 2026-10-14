package com.mis.vo;

import com.mis.entity.MssDictData;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 字典分组出参（HIS：selectGroup，[{dictType, dataList}]）
 */
@Data
public class DictGroupVO implements Serializable {

    private String dictType;

    private List<MssDictData> dataList;

    public DictGroupVO() {
    }

    public DictGroupVO(String dictType, List<MssDictData> dataList) {
        this.dictType = dictType;
        this.dataList = dataList;
    }
}
