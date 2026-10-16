package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class QcDimensionSelectListVO implements Serializable {

    private Integer code;

    private String text;

    private String description;
}
