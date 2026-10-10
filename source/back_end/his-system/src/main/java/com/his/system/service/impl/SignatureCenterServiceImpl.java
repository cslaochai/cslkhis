package com.his.system.service.impl;

import com.his.system.config.SignProperties;
import com.his.common.enums.*;
import com.his.common.exception.BusinessException;
import com.his.system.utils.KeyPairFactory;
import com.his.common.util.TextUtil;
import com.his.system.dto.*;
import com.his.system.entity.CurrentUser;
import com.his.system.service.EmrSignatureService;
import com.his.system.service.ExternalCaChannelService;
import com.his.system.service.SignCertService;
import com.his.system.service.SignatureCenterService;
import com.his.system.utils.UserUtils;
import com.his.system.vo.SignCaProbeOutboundVO;
import com.his.system.vo.SignCaStatusVO;
import com.his.system.vo.SignCertSelectListVO;
import com.his.system.vo.SignCertVO;
import com.his.system.vo.SignOptionVO;
import com.his.system.vo.SignatureOptionsVO;
import com.his.system.vo.SignatureVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 签名中心出参与命令装配。
 */
@Service
@RequiredArgsConstructor
public class SignatureCenterServiceImpl implements SignatureCenterService {

    private final EmrSignatureService emrSignatureService;
    private final SignCertService signCertService;
    private final SignProperties signProperties;
    private final ExternalCaChannelService externalCaChannelService;

    private static SignOptionVO opt(Integer code, String text) {
        return new SignOptionVO(code == null ? null : code.longValue(), text);
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
        return user.getEmployeeId() != null ? user.getEmployeeId() : user.getUserId();
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
        List<SignOptionVO> bizTypes = new ArrayList<>();
        for (SignBizTypeEnum t : SignBizTypeEnum.values()) {
            bizTypes.add(opt(t.getCode(), t.getText()));
        }
        List<SignOptionVO> scenes = new ArrayList<>();
        for (SignSceneEnum s : SignSceneEnum.values()) {
            scenes.add(opt(s.getCode(), s.getText()));
        }
        List<SignOptionVO> signStatuses = new ArrayList<>();
        for (SignStatusEnum s : SignStatusEnum.values()) {
            signStatuses.add(opt(s.getCode(), s.getText()));
        }
        List<SignOptionVO> verifyStatuses = new ArrayList<>();
        for (SignVerifyStatusEnum s : SignVerifyStatusEnum.values()) {
            verifyStatuses.add(opt(s.getCode(), s.getText()));
        }
        List<SignOptionVO> timeSources = new ArrayList<>();
        for (TimeSourceEnum t : TimeSourceEnum.values()) {
            timeSources.add(opt(t.getCode(), t.getText()));
        }
        vo.setBizTypes(bizTypes);
        vo.setScenes(scenes);
        vo.setSignStatuses(signStatuses);
        vo.setVerifyStatuses(verifyStatuses);
        vo.setTimeSources(timeSources);

        List<SignOptionVO> certStatuses = new ArrayList<>();
        for (CertStatusEnum s : CertStatusEnum.values()) {
            certStatuses.add(opt(s.getCode(), s.getText()));
        }
        List<SignOptionVO> issuedModes = new ArrayList<>();
        for (CertIssuedModeEnum m : CertIssuedModeEnum.values()) {
            issuedModes.add(opt(m.getCode(), m.getText()));
        }
        vo.setCertStatuses(certStatuses);
        vo.setIssuedModes(issuedModes);
        return vo;
    }

    @Override
    public SignatureVO sign(SignatureSignDTO dto) {
        CurrentUser user = UserUtils.getCurrentUser();
        SignCommandDTO cmd = new SignCommandDTO();
        cmd.setBizType(dto.getBizType());
        cmd.setBizId(dto.getBizId());
        cmd.setSignScene(dto.getSignScene() == null ? SignSceneEnum.MAKEUP.getCode() : dto.getSignScene());
        fillSigner(cmd, user);
        cmd.setRemark("补签：" + (dto.getRemark() == null ? "" : dto.getRemark()));
        return emrSignatureService.sign(cmd);
    }

    @Override
    public SignatureVO invalidate(SignatureInvalidateDTO dto) {
        CurrentUser user = UserUtils.getCurrentUser();
        return emrSignatureService.invalidate(dto.getSignId(), dto.getReason(), employeeIdOf(user), nameOf(user));
    }

    @Override
    public List<SignCertSelectListVO> certOptions(String keyword) {
        return signCertService.selectList(keyword).stream().map(c -> {
            SignCertSelectListVO vo = new SignCertSelectListVO();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).toList();
    }

    @Override
    public SignCertVO issueCert(SignCertIssueDTO dto) {
        CurrentUser user = UserUtils.getCurrentUser();
        return signCertService.issue(dto, employeeIdOf(user), nameOf(user));
    }

    @Override
    public SignCertVO revokeCert(SignCertRevokeDTO dto) {
        CurrentUser user = UserUtils.getCurrentUser();
        return signCertService.revoke(dto, employeeIdOf(user), nameOf(user));
    }

    @Override
    public SignCaStatusVO signCaStatus() {
        SignCaStatusVO status = new SignCaStatusVO();
        status.setCaMode(signProperties.getCaMode());
        status.setProviderName(externalCaChannelService.available() ? externalCaChannelService.name() : null);
        status.setAvailable(externalCaChannelService.available());
        status.setHint(externalCaChannelService.available()
                ? "外部 CA 模式：证书签发会先本地生成密钥对、向适配器提交 CSR（当前为控制台打印，提交后中断签发，不回退自签）"
                : "内部自签模式（G6/G6b 形态）：证书由院内 KeyPairFactory 签发，信任根为院内，不对外声称法律效力");
        return status;
    }

    @Override
    public SignCaProbeOutboundVO probeCaOutbound() {
        if (!externalCaChannelService.available()) {
            throw new BusinessException("当前为内部自签模式（his.sign.ca-mode=internal），没有外部 CA 外发动作可探；"
                    + "配置 external 并重启后可探测");
        }
        // 一次性密钥对：只为让探针打印的公钥指纹真实，用完即弃，不落库、不注册
        KeyPairFactory.KeyPairPem pair = KeyPairFactory.generate();
        String subjectDn = "CN=CA外发探针（非签名人）, O=长沙市麓康医院";
        ExternalCaChannelService.IssuedCert issued = externalCaChannelService.issueCert(
                new ExternalCaChannelService.IssueRequest(subjectDn, pair.publicPem(), 365));
        SignCaProbeOutboundVO result = new SignCaProbeOutboundVO();
        result.setProviderName(externalCaChannelService.name());
        result.setCsrPrinted(true);
        result.setCertReturned(issued != null);
        result.setConclusion("CSR 已按适配器口径提交并打印到服务端控制台（[M8真CA口子] 标记段）；"
                + "未接入真 CA 前不会返回证书，也不会落任何证书记录");
        return result;
    }
}
