package com.his.pharmacy.service;

import com.his.pharmacy.entity.BizTcmDecoct;
import com.his.pharmacy.vo.TcmDecoctDetailVO;

import java.util.List;

/**
 * 代煎回执打印出口
 */
public interface DecoctReceiptPrinter {

    /**
     * 打印代煎回执，返回打印任务号（形如 {@code PRINT-xxx}，写进审计内容里）。
     */
    String printReceipt(BizTcmDecoct decoct, List<TcmDecoctDetailVO.HerbLine> herbs);
}
