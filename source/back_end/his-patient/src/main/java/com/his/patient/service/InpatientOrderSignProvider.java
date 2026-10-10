package com.his.patient.service;

import com.his.system.entity.SignSubject;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.system.service.SignableContentProvider;
import com.his.common.support.CanonicalText;
import com.his.common.util.NumUtil;
import com.his.patient.entity.BizInpatientOrder;

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
                .put("dosage", NumUtil.plain(o.getDosage()))
                .put("dosageUnit", o.getDosageUnit())
                .put("route", o.getRoute())
                .put("frequency", o.getFrequency())
                .put("quantity", NumUtil.plain(o.getQuantity()))
                .put("price", NumUtil.plain(o.getPrice()))
                .put("amount", NumUtil.plain(o.getAmount()))
                .put("startTime", o.getStartTime())
                .put("planEndTime", o.getPlanEndTime())
                .put("orderTime", o.getOrderTime())
                .put("doctorId", o.getDoctorId())
                .put("doctorName", o.getDoctorName())
                .put("isUrgent", o.getIsUrgent())
                .put("source", o.getSource())
                .build();
    }

    SignBizTypeEnum bizType();

    SignSubject load(Long bizId);

    String blockReason(SignSubject subject, SignSceneEnum scene);

    void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime);

    void revokeSignAnchor(Long bizId, Long signId);
}
