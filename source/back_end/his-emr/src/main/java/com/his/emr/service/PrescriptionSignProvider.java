package com.his.emr.service;

import com.his.common.service.SignableContentProvider;
import com.his.common.enums.SignBizType;
import com.his.common.enums.SignScene;
import com.his.common.entity.SignSubject;
import com.his.emr.entity.BizPrescription;
import java.time.LocalDateTime;

public interface PrescriptionSignProvider extends SignableContentProvider {

    /**
     * 处方状态文案（1-草稿 2-已提交 3-已审核 4-已发药 5-已取消 6-已退药）；**未知码值不回落**。
     */
    public static String rxStatusText(Integer status) {
        if (status == null) {
            return "—";
        }
        return switch (status) {
            case 1 -> "草稿";
            case 2 -> "已提交";
            case 3 -> "已审核";
            case 4 -> "已发药";
            case 5 -> "已取消";
            case 6 -> "已退药";
            default -> "未知(" + status + ")";
        };
    }

    SignBizType bizType();

    SignSubject load(Long bizId);

    String blockReason(SignSubject subject, SignScene scene);

    void applySignAnchor(Long bizId, SignScene scene, Long signId, LocalDateTime signedTime);

    void revokeSignAnchor(Long bizId, Long signId);

    String canonical(BizPrescription p);
}
