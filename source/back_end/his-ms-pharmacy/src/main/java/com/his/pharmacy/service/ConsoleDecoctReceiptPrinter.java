package com.his.pharmacy.service;

import com.his.pharmacy.entity.BizTcmDecoct;
import com.his.pharmacy.vo.TcmDecoctDetailVO;

import java.util.List;

public interface ConsoleDecoctReceiptPrinter extends DecoctReceiptPrinter {

    String printReceipt(BizTcmDecoct decoct, List<TcmDecoctDetailVO.HerbLine> herbs);
}
