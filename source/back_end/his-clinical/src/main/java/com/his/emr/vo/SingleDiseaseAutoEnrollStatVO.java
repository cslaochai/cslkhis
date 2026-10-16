package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SingleDiseaseAutoEnrollStatVO implements Serializable {

    private Integer scanned;

    private Integer enrolled;

    /**
     * 跳过原因清单（admissionId:原因），非字段缺失
     */
    private List<String> skipped;
}
