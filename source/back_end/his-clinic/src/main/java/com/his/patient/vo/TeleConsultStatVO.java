package com.his.patient.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 互联网医院 / 远程会诊统计（服务端 group by 出，不让前端数当前页）。
 */
@Data
public class TeleConsultStatVO implements Serializable {

    /** 远程会诊总量 */
    private Long teleTotal;
    /** 远程会诊待安排数 */
    private Long telePending;
    /** 远程会诊已安排数 */
    private Long teleArranged;
    /** 远程会诊已完成数 */
    private Long teleDone;
    /** 远程会诊已取消数 */
    private Long teleCanceled;

    /** 在线问诊总量 */
    private Long onlineTotal;
    /** 在线问诊待接诊数 */
    private Long onlineWaiting;
    /** 在线问诊已接诊数 */
    private Long onlineAccepted;
    /** 在线问诊已完成数 */
    private Long onlineDone;
    /** 在线问诊已拒绝数 */
    private Long onlineRejected;
}
