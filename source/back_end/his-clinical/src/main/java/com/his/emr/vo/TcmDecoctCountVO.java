package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TcmDecoctCountVO implements Serializable {

    private Long pending;

    private Long decocted;

    private Long picked;

    private Long cancelled;
}
