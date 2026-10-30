package com.his.clinical.service.impl;

import com.his.spi.dto.SignCommandDTO;
import com.his.spi.vo.SignatureVO;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.clinical.dto.*;
import com.his.common.entity.CurrentUser;
import com.his.clinical.service.EmrSignatureService;
import com.his.clinical.service.SignatureCenterService;
import com.his.common.utils.UserUtils;
import com.his.clinical.vo.OptionItemVO;
import com.his.clinical.vo.SignatureOptionsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 签名验签出参与命令装配。
 */
@Service
@RequiredArgsConstructor
public class SignatureCenterServiceImpl implements SignatureCenterService {

    private final EmrSignatureService emrSignatureService;

    private static OptionItemVO opt(Integer code, String text) {
        return new OptionItemVO(code == null ? null : code.longValue(), text);
    }

    private static void fillSigner(SignCommandDTO cmd, CurrentUser user) {
        cmd.setSignerId(employeeIdOf(user));
        cmd.setSignerName(nameOf(user));
        cmd.setSignerDeptId(user == null ? null : user.getDeptId());
        cmd.setSignerDeptName(user == null ? null : user.getDeptName());
    }

    /**
     * 员工ID 优先（签名必须落到员工，不能落成系统账号 admin 的 userId=1）
     */
    private static Long employeeIdOf(CurrentUser user) {
        if (user == null) {
            return null;
        }
        return user.getEmployeeId();
    }

    private static String nameOf(CurrentUser user) {
        if (user == null) {
            return null;
        }
        if (TextUtil.hasText(user.getEmployeeName())) {
            return user.getEmployeeName();
        }
        return user.getRealName();
    }

    @Override
    public SignatureOptionsVO queryOptions() {
        SignatureOptionsVO vo = new SignatureOptionsVO();
        List<OptionItemVO> bizTypes = new ArrayList<>();
        for (SignBizTypeEnum t : SignBizTypeEnum.values()) {
            bizTypes.add(opt(t.getCode(), t.getLabel()));
        }
        List<OptionItemVO> scenes = new ArrayList<>();
        for (SignSceneEnum s : SignSceneEnum.values()) {
            scenes.add(opt(s.getCode(), s.getLabel()));
        }
        List<OptionItemVO> signStatuses = new ArrayList<>();
        for (SignStatusEnum s : SignStatusEnum.values()) {
            signStatuses.add(opt(s.getCode(), s.getLabel()));
        }
        List<OptionItemVO> verifyStatuses = new ArrayList<>();
        for (SignVerifyStatusEnum s : SignVerifyStatusEnum.values()) {
            verifyStatuses.add(opt(s.getCode(), s.getLabel()));
        }
        vo.setBizTypes(bizTypes);
        vo.setScenes(scenes);
        vo.setSignStatuses(signStatuses);
        vo.setVerifyStatuses(verifyStatuses);

        List<OptionItemVO> certStatuses = new ArrayList<>();
        for (CertStatusEnum s : CertStatusEnum.values()) {
            certStatuses.add(opt(s.getCode(), s.getLabel()));
        }
        List<OptionItemVO> issuedModes = new ArrayList<>();
        for (CertIssuedModeEnum m : CertIssuedModeEnum.values()) {
            issuedModes.add(opt(m.getCode(), m.getLabel()));
        }
        vo.setCertStatuses(certStatuses);
        vo.setIssuedModes(issuedModes);
        return vo;
    }

    @Override
    public SignatureVO sign(SignatureSignDTO dto, String pin) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(dto.getBizType());
        cmd.setBizId(dto.getBizId());
        cmd.setSignScene(dto.getSignScene() == null ? SignSceneEnum.MAKEUP.getCode() : dto.getSignScene());
        fillSigner(cmd, user);
        cmd.setPin(pin);
        cmd.setRemark("补签：" + (dto.getRemark() == null ? "" : dto.getRemark()));
        return emrSignatureService.sign(cmd);
    }

    @Override
    public SignatureVO invalidate(SignatureInvalidateDTO dto) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        return emrSignatureService.invalidate(dto.getSignId(), dto.getReason(), employeeIdOf(user), nameOf(user));
    }
}
