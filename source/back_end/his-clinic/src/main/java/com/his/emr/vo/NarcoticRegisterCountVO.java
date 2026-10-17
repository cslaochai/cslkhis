package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class NarcoticRegisterCountVO implements Serializable {

    private Long total;

    private Long pendingAmpoule;

    private Long returnedAmpoule;
}
