package com.his.emr.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SignCaStatusVO implements Serializable {

    private String caMode;

    /** external 模式下的适配器名，internal 为 null */
    private String providerName;

    private Boolean available;

    private String hint;
}
