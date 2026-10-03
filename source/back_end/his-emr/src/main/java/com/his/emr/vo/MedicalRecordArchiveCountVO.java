package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MedicalRecordArchiveCountVO implements Serializable {

    private Integer total;

    private Integer pending;

    private Integer archived;

    private Integer sealed;
}
