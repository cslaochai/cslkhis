package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.common.entity.SignSubject;
import com.his.common.enums.SignBizTypeEnum;
import com.his.common.enums.SignSceneEnum;
import com.his.common.service.SignableContentProvider;
import com.his.patient.entity.BizInpatientOrder;
import com.his.patient.enums.InpatientOrderStatusEnum;
import com.his.patient.mapper.BizInpatientOrderMapper;
import com.his.patient.service.InpatientOrderSignProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * 住院医嘱的签名内容提供者（业务类型=3）——**双签**。
 *
 * <p>医嘱是"医生开立 + 护士校对"两道手，两道都要有人负责，所以是两次独立签名：
 * <ul>
 *   <li>{@link SignSceneEnum#ORDER_CREATE} → 写医师签名ID</li>
 *   <li>{@link SignSceneEnum#ORDER_VERIFY} → 写护士签名ID</li>
 * </ul>
 * 第二次签名的内容里会带上第一次的摘要（签名链，见 {@code SignSubject.contentWithPrev}），
 * 于是"护士校对之后医生又改了这条医嘱"会同时打断第二环的验签 ——
 * 否则两次签名各自绑同一份内容，改了谁都验得过去，双签就成了摆设。
 *
 * <p><b>规范化里绝不能出现 {@code order_status / verify_time / stop_time} 这类流程字段</b>：
 * 它们会随流程变，放进去等于"一校对，开立签名当场失效"。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InpatientOrderSignProviderImpl implements SignableContentProvider, InpatientOrderSignProvider {

    private final BizInpatientOrderMapper orderMapper;

    @Override
    public SignBizTypeEnum bizType() {
        return SignBizTypeEnum.INPATIENT_ORDER;
    }

    @Override
    public SignSubject load(Long bizId) {
        BizInpatientOrder o = orderMapper.selectById(bizId);
        if (o == null) {
            return null;
        }
        return new SignSubject(
                o.getId(),
                o.getOrderNo(),
                o.getPatientId(),
                o.getPatientName(),
                o.getDeptId(),
                o.getDeptName(),
                o.getOrderStatus(),
                InpatientOrderStatusEnum.getText(o.getOrderStatus()),
                InpatientOrderSignProvider.canonical(o));
    }

    @Override
    public String blockReason(SignSubject subject, SignSceneEnum scene) {
        BizInpatientOrder o = orderMapper.selectById(subject.bizId());
        if (o == null) {
            return "医嘱不存在或已被删除，无法签名";
        }
        if (scene == SignSceneEnum.ORDER_CREATE) {
            if (o.getDoctorSignId() != null) {
                return "医嘱 " + o.getOrderNo() + " 已有开立签名，不能重复签名";
            }
            return null;
        }
        if (scene == SignSceneEnum.ORDER_VERIFY) {
            if (o.getNurseSignId() != null) {
                return "医嘱 " + o.getOrderNo() + " 已有校对签名，不能重复签名";
            }
            if (Objects.equals(InpatientOrderStatusEnum.PENDING_VERIFY.getCode(), o.getOrderStatus())) {
                return "医嘱 " + o.getOrderNo() + " 还是「待校对」，不能签校对名；请先完成校对";
            }
            if (o.getVerifyNurseId() == null) {
                return "医嘱 " + o.getOrderNo() + " 没有校对护士记录，无法签校对名（请先完成校对）";
            }
            return null;
        }
        // 其它场景（提交/归档/补签）对医嘱没有意义
        return "医嘱只支持「开立签名」与「校对签名」两种场景，当前场景「" + scene.getText() + "」不适用";
    }

    @Override
    public void applySignAnchor(Long bizId, SignSceneEnum scene, Long signId, LocalDateTime signedTime) {
        BizInpatientOrder patch = new BizInpatientOrder();
        patch.setId(bizId);
        LocalDateTime t = signedTime == null ? null : signedTime.truncatedTo(ChronoUnit.SECONDS);
        if (scene == SignSceneEnum.ORDER_VERIFY) {
            patch.setNurseSignId(signId);
            patch.setNurseSignedTime(t);
        } else {
            patch.setDoctorSignId(signId);
            patch.setDoctorSignedTime(t);
        }
        orderMapper.updateById(patch);
    }

    @Override
    public void revokeSignAnchor(Long bizId, Long signId) {
        BizInpatientOrder o = orderMapper.selectById(bizId);
        if (o == null) {
            return;
        }
        // 医嘱行没有 sign_status 列，表达"没有有效签名"就只能把指针清空。
        // MyBatis-Plus 的 updateById 只更新非 null 字段（set null 等于"不改"），
        // 所以必须用 UpdateWrapper 显式 set null，否则签名作废了、医嘱上却还挂着旧签名ID。
        if (Objects.equals(signId, o.getDoctorSignId())) {
            orderMapper.update(null, new LambdaUpdateWrapper<BizInpatientOrder>()
                    .eq(BizInpatientOrder::getId, bizId)
                    .set(BizInpatientOrder::getDoctorSignId, null)
                    .set(BizInpatientOrder::getDoctorSignedTime, null));
            log.info("已清除医嘱开立签名指针 orderNo={} signId={}", o.getOrderNo(), signId);
        }
        if (Objects.equals(signId, o.getNurseSignId())) {
            orderMapper.update(null, new LambdaUpdateWrapper<BizInpatientOrder>()
                    .eq(BizInpatientOrder::getId, bizId)
                    .set(BizInpatientOrder::getNurseSignId, null)
                    .set(BizInpatientOrder::getNurseSignedTime, null));
            log.info("已清除医嘱校对签名指针 orderNo={} signId={}", o.getOrderNo(), signId);
        }
    }
}
