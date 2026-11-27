package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.BizTypeEnum;
import com.his.common.enums.SignQualReviewActionEnum;
import com.his.common.enums.SignQualReviewStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.utils.UserUtils;
import com.his.platform.entity.BizSignQualReview;
import com.his.platform.entity.SysEmployee;
import com.his.platform.entity.SysRole;
import com.his.platform.mapper.BizSignQualReviewMapper;
import com.his.platform.mapper.SysEmployeeMapper;
import com.his.platform.mapper.SysRoleMapper;
import com.his.platform.mapper.SysUserMapper;
import com.his.platform.service.SignCertService;
import com.his.platform.service.SignQualReviewService;
import com.his.platform.service.SysMessageService;
import com.his.platform.vo.EmployeePostVO;
import com.his.platform.vo.SignCertVO;
import com.his.platform.vo.SignQualReviewVO;
import com.his.spi.dto.SignCertRevokeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 签名资格复核实现：岗位异动不自动吊销证书，只把「该不该保留」变成医务科的待办。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignQualReviewServiceImpl extends ServiceImpl<BizSignQualReviewMapper, BizSignQualReview>
        implements SignQualReviewService {

    /**
     * 复核权（医务科）：决定保留还是吊销。
     */
    private static final String PERM_REVIEW = "sign:cert:review";
    /**
     * 证书办理权（信息科）：新岗位需要签名时通知其签发。
     */
    private static final String PERM_CERT_EDIT = "sign:cert:edit";

    private final SysRoleMapper sysRoleMapper;
    private final SignCertService signCertService;
    private final SysMessageService sysMessageService;
    private final SysUserMapper sysUserMapper;
    private final SysEmployeeMapper sysEmployeeMapper;

    @Override
    public void handlePostsChanged(Long employeeId, String empName,
                                   List<EmployeePostVO> before, List<EmployeePostVO> after) {
        if (employeeId == null) {
            return;
        }
        boolean beforeNeed = needSign(before);
        boolean afterNeed = needSign(after);
        if (beforeNeed == afterNeed) {
            // 医生换科室、医生转护士这类「还要不要签名」没变的情况，证书不动，也不生成复核单
            return;
        }
        SignCertVO cert = signCertService.getActiveCertVO(employeeId);
        if (afterNeed) {
            // 不需要签名 → 需要签名：没有有效证书时通知信息科办理
            if (cert == null) {
                String from = summary(before);
                String to = summary(after);
                afterCommit(() -> notifyIssueCert(employeeId, empName, from, to));
            }
            return;
        }
        // 需要签名 → 不需要签名：没有有效证书就没有可吊销对象，不生成空单
        if (cert == null) {
            return;
        }
        BizSignQualReview review = new BizSignQualReview();
        review.setEmployeeId(employeeId);
        review.setEmpName(empName);
        review.setFromPosts(summary(before));
        review.setToPosts(summary(after));
        review.setNewSignRequired(0);
        review.setCertId(cert.getId());
        review.setCertNo(cert.getCertNo());
        review.setStatus(SignQualReviewStatusEnum.PENDING.getCode());
        save(review);
        afterCommit(() -> notifyReview(review));
        log.info("生成签名资格复核单 empId={} from={} to={} certNo={}",
                employeeId, review.getFromPosts(), review.getToPosts(), cert.getCertNo());
    }

    @Override
    public List<SignQualReviewVO> listPending() {
        return list(new LambdaQueryWrapper<BizSignQualReview>()
                .eq(BizSignQualReview::getStatus, SignQualReviewStatusEnum.PENDING.getCode())
                .orderByDesc(BizSignQualReview::getCreateTime))
                .stream().map(this::toVO).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(Long reviewId, Integer action, String remark) {
        if (reviewId == null) {
            throw new BusinessException("复核单ID不能为空");
        }
        BizSignQualReview review = getById(reviewId);
        if (review == null) {
            throw new BusinessException("复核单不存在");
        }
        if (!Objects.equals(SignQualReviewStatusEnum.PENDING.getCode(), review.getStatus())) {
            throw new BusinessException("该复核单已处理（" + SignQualReviewStatusEnum.getLabelOrUnknown(review.getStatus()) + "），不能重复处理");
        }
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        SignQualReviewActionEnum actionEnum = SignQualReviewActionEnum.fromCode(action);
        if (actionEnum == null) {
            throw new BusinessException("处理动作取值不合法");
        }
        if (actionEnum == SignQualReviewActionEnum.REVOKE) {
            if (!TextUtil.hasText(remark)) {
                throw new BusinessException("吊销必须写明理由（复核结论会作为吊销依据留痕）");
            }
            SignCertVO cert = review.getCertId() == null
                    ? signCertService.getActiveCertVO(review.getEmployeeId())
                    : signCertService.getById(review.getCertId());
            if (cert == null) {
                throw new BusinessException("该员工当前没有可吊销的有效证书");
            }
            SignCertRevokeDTO dto = new SignCertRevokeDTO();
            dto.setCertId(cert.getId());
            dto.setReason(remark.trim());
            // 复核是医务科的业务判断，走业务吊销：吊销后自动通知信息科重办
            signCertService.revoke(dto, user.getEmployeeId(), user.getRealName());
            review.setStatus(SignQualReviewStatusEnum.REVOKED.getCode());
        } else {
            review.setStatus(SignQualReviewStatusEnum.KEPT.getCode());
        }
        review.setHandleRemark(remark);
        review.setHandleByName(user.getRealName());
        review.setHandleTime(LocalDateTime.now());
        updateById(review);
        log.info("签名资格复核处理 empId={} action={} certNo={} 操作人={}",
                review.getEmployeeId(), actionEnum.getLabel(), review.getCertNo(), user.getRealName());
    }

    /**
     * 岗位集合里是否还有「需要电子签名」的角色（只看今天生效的岗位）。
     */
    private boolean needSign(List<EmployeePostVO> posts) {
        if (posts == null || posts.isEmpty()) {
            return false;
        }
        LocalDate today = LocalDate.now();
        List<String> roleCodes = posts.stream()
                .filter(p -> (p.getEffectiveDate() == null || !today.isBefore(p.getEffectiveDate()))
                        && (p.getExpireDate() == null || !today.isAfter(p.getExpireDate())))
                .map(EmployeePostVO::getRoleCode)
                .filter(TextUtil::hasText)
                .distinct()
                .toList();
        if (roleCodes.isEmpty()) {
            return false;
        }
        List<SysRole> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getRoleCode, roleCodes));
        return roles.stream().anyMatch(r -> Integer.valueOf(1).equals(r.getSignRequired()));
    }

    /**
     * 「角色/科室」摘要在留痕里给人看，不参与任何判定。
     */
    private String summary(List<EmployeePostVO> posts) {
        if (posts == null || posts.isEmpty()) {
            return "（无岗位）";
        }
        return posts.stream()
                .map(p -> (TextUtil.hasText(p.getRoleName()) ? p.getRoleName() : p.getRoleCode())
                        + "/" + (TextUtil.hasText(p.getDeptName()) ? p.getDeptName() : String.valueOf(p.getDeptId())))
                .distinct()
                .collect(Collectors.joining("、"));
    }

    private void afterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

    /**
     * 通知医务科复核：岗位已不需要签名，证书是否吊销请其决定。
     */
    private void notifyReview(BizSignQualReview review) {
        try {
            List<Long> empIds = sysUserMapper.selectEmployeeIdsByPermission(PERM_REVIEW);
            if (empIds == null || empIds.isEmpty()) {
                log.warn("签名资格复核单无接收人 permission={} empId={}", PERM_REVIEW, review.getEmployeeId());
                return;
            }
            String title = "签名资格复核：" + review.getEmpName() + " 的岗位已不需要电子签名";
            String content = String.format("%s 的岗位由「%s」调整为「%s」，新岗位不需要电子签名资格。"
                            + "其有效证书 %s 是保留还是吊销，请复核；吊销后历史签名仍可验通，信息科会按需重新签发。",
                    review.getEmpName(), review.getFromPosts(), review.getToPosts(), review.getCertNo());
            List<SysEmployee> receivers = enabledEmployees(empIds);
            for (SysEmployee receiver : receivers) {
                sysMessageService.sendSystemMessage(receiver.getId(), receiver.getEmpName(), title, content,
                        BizTypeEnum.SIGN_REVIEW.getType(), review.getEmployeeId(), "warning", null, 0);
            }
        } catch (Exception e) {
            log.warn("签名资格复核通知发送失败 empId={}", review.getEmployeeId(), e);
        }
    }

    /**
     * 通知信息科：新岗位需要签名资格但该员工还没有有效证书。
     */
    private void notifyIssueCert(Long employeeId, String empName, String from, String to) {
        try {
            List<Long> empIds = sysUserMapper.selectEmployeeIdsByPermission(PERM_CERT_EDIT);
            if (empIds == null || empIds.isEmpty()) {
                log.warn("签名证书办理通知无接收人 permission={} empId={}", PERM_CERT_EDIT, employeeId);
                return;
            }
            String title = "新岗位需要电子签名资格，请办理证书";
            String content = String.format("%s 的岗位由「%s」调整为「%s」，新岗位需要电子签名，但当前没有有效证书。"
                    + "请核实后为其签发或重新签发。", empName, from, to);
            List<SysEmployee> receivers = enabledEmployees(empIds);
            for (SysEmployee receiver : receivers) {
                sysMessageService.sendSystemMessage(receiver.getId(), receiver.getEmpName(), title, content,
                        BizTypeEnum.SIGN_REVIEW.getType(), employeeId, "info", null, 0);
            }
        } catch (Exception e) {
            log.warn("签名证书办理通知发送失败 empId={}", employeeId, e);
        }
    }

    private List<SysEmployee> enabledEmployees(List<Long> empIds) {
        return sysEmployeeMapper.selectList(new LambdaQueryWrapper<SysEmployee>()
                .in(SysEmployee::getId, empIds)
                .eq(SysEmployee::getStatus, com.his.common.enums.EnableStatusEnum.ENABLED.getCode()));
    }

    private SignQualReviewVO toVO(BizSignQualReview review) {
        SignQualReviewVO vo = new SignQualReviewVO();
        vo.setId(review.getId());
        vo.setEmployeeId(review.getEmployeeId());
        vo.setEmpName(review.getEmpName());
        vo.setFromPosts(review.getFromPosts());
        vo.setToPosts(review.getToPosts());
        vo.setNewSignRequired(review.getNewSignRequired());
        vo.setCertId(review.getCertId());
        vo.setCertNo(review.getCertNo());
        vo.setStatus(review.getStatus());
        vo.setHandleRemark(review.getHandleRemark());
        vo.setHandleByName(review.getHandleByName());
        vo.setHandleTime(review.getHandleTime());
        vo.setCreateTime(review.getCreateTime());
        return vo;
    }
}
