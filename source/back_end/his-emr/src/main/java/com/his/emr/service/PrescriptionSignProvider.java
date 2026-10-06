package com.his.emr.service;

import com.his.common.entity.SignSubject;
import com.his.common.enums.PrescriptionStatusEnum;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.service.SignableContentProvider;
import com.his.emr.entity.BizPrescription;

import java.time.LocalDateTime;

public interface PrescriptionSignProvider extends SignableContentProvider {

    /**
     * 处方状态文案（1-草稿 2-已提交 3-已审核 4-已发药 5-已取消 6-已退药）；**未知码值不回落**。
     *
     * <p>签名正文措辞与 {@link PrescriptionStatusEnum} label 有差异（2「已提交」/3「已审核」/5「已取消」），
     * 属调用侧局部差异，按 §13 保留；枚举外的码值统一走枚举 {@code labelOrUnknown} 兜底。
     */
    static String rxStatusText(Integer status) {
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
            default -> PrescriptionStatusEnum.labelOrUnknown(status);
        };
    }

    SignBizTypeEnum bizType();

    SignSubject load(Long bizId);

    String blockReason(SignSubject subject, SignSceneEnum scene);

    void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime);

    void revokeSignAnchor(Long bizId, Long signId);

    String canonical(BizPrescription p);
}
