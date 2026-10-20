package com.his.emr.service;

import com.his.emr.entity.BizPrescriptionDetail;
import com.his.emr.entity.BizTcmDecoct;

import java.util.List;

public interface ConsoleDecoctReceiptPrinter extends DecoctReceiptPrinter {

    String printReceipt(BizTcmDecoct decoct, List<BizPrescriptionDetail> details);
}
