package com.his.emr.service;

import com.his.emr.entity.BizPrescriptionDetail;
import com.his.emr.entity.BizTcmDecoct;

import java.util.List;

/**
 * 代煎回执打印出口（sql/139 学习阶段口子）。
 *
 * <p>真实医院这里对接的是煎药室的标签机/配送单打印服务（每家一套私有协议），
 * 本仓库没有打印机，所以实现是控制台打印桩：把"打印"这个动作本身打出来，
 * 并回一个可核对的任务号，业务侧照旧写审计。
 * 将来接设备只需替换实现类，接口与调用点不动。
 */
public interface DecoctReceiptPrinter {

    /**
     * 打印代煎回执，返回打印任务号（形如 {@code PRINT-xxx}，写进审计内容里）。
     */
    String printReceipt(BizTcmDecoct decoct, List<BizPrescriptionDetail> details);
}
