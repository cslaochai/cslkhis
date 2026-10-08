package com.his.emr.service;

import com.his.emr.entity.BizPrescriptionDetail;
import com.his.emr.entity.BizTcmDecoct;

import java.util.List;

/**
 * 代煎回执打印出口（sql/139 学习阶段口子）。
 */
public interface DecoctReceiptPrinter {

    /**
     * 打印代煎回执，返回打印任务号（形如 {@code PRINT-xxx}，写进审计内容里）。
     */
    String printReceipt(BizTcmDecoct decoct, List<BizPrescriptionDetail> details);
}
