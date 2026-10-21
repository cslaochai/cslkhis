package com.his.pharmacy.service.impl;

import com.his.pharmacy.entity.BizTcmDecoct;
import com.his.pharmacy.service.ConsoleDecoctReceiptPrinter;
import com.his.pharmacy.service.DecoctReceiptPrinter;
import com.his.pharmacy.vo.TcmDecoctDetailVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 代煎回执控制台打印（学习阶段：不接煎药机/标签机）。
 */
@Slf4j
@Component
public class ConsoleDecoctReceiptPrinterImpl implements DecoctReceiptPrinter, ConsoleDecoctReceiptPrinter {

    @Override
    public String printReceipt(BizTcmDecoct decoct, List<TcmDecoctDetailVO.HerbLine> herbs) {
        String taskId = "PRINT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        StringBuilder sb = new StringBuilder("\n===== 中药代煎回执（控制台打印 " + taskId + "）=====\n");
        sb.append("单号 ").append(decoct.getDecoctNo())
                .append("  处方 ").append(decoct.getPrescriptionNo())
                .append("  ").append(decoct.getPatientName())
                .append("  ").append(decoct.getDoseCount()).append(" 剂\n");
        if (herbs != null) {
            for (TcmDecoctDetailVO.HerbLine h : herbs) {
                sb.append("  ").append(h.getDrugName())
                        .append("  每剂 ").append(h.getPerDoseText())
                        .append("  实发 ").append(h.getGrams()).append("g")
                        .append(h.getMethod() == null ? "" : "  [" + h.getMethod() + "]")
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
