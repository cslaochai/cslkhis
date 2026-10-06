package com.his.patient.service;

import com.his.common.entity.SignSubject;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.service.SignableContentProvider;
import com.his.common.support.CanonicalText;
import com.his.patient.entity.BizInpatientOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface InpatientOrderSignProvider extends SignableContentProvider {

    /**
     * 规范化文本：只含"开立时刻的医嘱内容"
     */
    public static String canonical(BizInpatientOrder o) {
        return CanonicalText.create("INPATIENT_ORDER")
                .put("orderNo", o.getOrderNo())
                .put("admissionId", o.getAdmissionId())
                .put("patientId", o.getPatientId())
                .put("patientNo", o.getPatientNo())
                .put("orderType", o.getOrderType())
                .put("orderGroup", o.getOrderGroup())
                .put("orderClass", o.getOrderClass())
                .put("itemCode", o.getItemCode())
                .put("itemName", o.getItemName())
                .put("spec", o.getSpec())
                .put("unit", o.getUnit())
                .put("dosage", plain(o.getDosage()))
                .put("dosageUnit", o.getDosageUnit())
                .put("route", o.getRoute())
                .put("frequency", o.getFrequency())
                .put("quantity", plain(o.getQuantity()))
                .put("price", plain(o.getPrice()))
                .put("amount", plain(o.getAmount()))
                .put("startTime", o.getStartTime())
                .put("planEndTime", o.getPlanEndTime())
                .put("orderTime", o.getOrderTime())
                .put("doctorId", o.getDoctorId())
                .put("doctorName", o.getDoctorName())
                .put("isUrgent", o.getIsUrgent())
                .put("source", o.getSource())
                .build();
    }

    /**
     * BigDecimal 去尾零：金额 `10.00` 与 `10.0` 必须算出同一个摘要
     */
    static String plain(BigDecimal v) {
        return v == null ? null : v.stripTrailingZeros().toPlainString();
    }

    SignBizTypeEnum bizType();

    SignSubject load(Long bizId);

    String blockReason(SignSubject subject, SignSceneEnum scene);

    void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime);

    void revokeSignAnchor(Long bizId, Long signId);
}
