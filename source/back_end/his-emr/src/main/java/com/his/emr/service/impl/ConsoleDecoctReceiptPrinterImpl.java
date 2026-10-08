package com.his.emr.service.impl;

import com.his.emr.entity.BizPrescriptionDetail;
import com.his.emr.entity.BizTcmDecoct;
import com.his.emr.service.ConsoleDecoctReceiptPrinter;
import com.his.emr.service.DecoctReceiptPrinter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 代煎回执控制台打印（学习阶段：不接煎药机/标签机）。
 *
 * <p>把回执内容原样打到控制台，现象与"点了打印、纸上该有什么"一一对得上，
 * 便于验证出参是否正确；真接设备时换掉本类即可。
 */
@Slf4j
@Component
public class ConsoleDecoctReceiptPrinterImpl implements DecoctReceiptPrinter, ConsoleDecoctReceiptPrinter {

    @Override
    public String printReceipt(BizTcmDecoct decoct, List<BizPrescriptionDetail> details) {
        String taskId = "PRINT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        StringBuilder sb = new StringBuilder("\n===== 中药代煎回执（控制台打印 " + taskId + "）=====\n");
        sb.append("单号 ").append(decoct.getDecoctNo())
                .append("  处方 ").append(decoct.getPrescriptionNo())
                .append("  ").append(decoct.getPatientName())
                .append("  ").append(decoct.getDoseCount()).append(" 剂\n");
        if (details != null) {
            for (BizPrescriptionDetail d : details) {
                sb.append("  ").append(d.getDrugName())
                        .append("  每剂 ").append(d.getSingleDosage())
                        .append("  实发 ").append(d.getQuantity()).append("g")
                        .append(d.getRoute() == null ? "" : "  [" + d.getRoute() + "]")
                        .append("\n");
            }
        }
        sb.append("煎法提示 ").append(decoct.getMethodSummary())
                .append("  状态 ").append(decoct.getDecoctStatus()).append("\n");
        sb.append("==================================================");
        log.info(sb.toString());
        return taskId;
    }
}
