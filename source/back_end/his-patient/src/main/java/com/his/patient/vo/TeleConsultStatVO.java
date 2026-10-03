package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 互联网医院 / 远程会诊统计（服务端 group by 出，不让前端数当前页）。
 */
@Data
public class TeleConsultStatVO implements Serializable {

    private Long teleTotal;
    private Long telePending;
    private Long teleArranged;
    private Long teleDone;
    private Long teleCanceled;

    private Long onlineTotal;
    private Long onlineWaiting;
    private Long onlineAccepted;
    private Long onlineDone;
    private Long onlineRejected;
}
