package com.his.system.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SignCaProbeOutboundVO implements Serializable {

    private String providerName;

    private Boolean csrPrinted;

    private Boolean certReturned;

    private String conclusion;
}
