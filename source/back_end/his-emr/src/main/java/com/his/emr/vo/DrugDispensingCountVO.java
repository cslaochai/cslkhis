package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class DrugDispensingCountVO implements Serializable {

    private Long pending;

    private Long dispensed;

    private Long returned;
}
