package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.common.enums.UserTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.patient.dto.GuardianBindDTO;
import com.his.patient.dto.GuardianSendAddCodeDTO;
import com.his.patient.dto.GuardianSendBindCodeDTO;
import com.his.patient.dto.GuardianUpsertDTO;
import com.his.patient.entity.BizPatient;
import com.his.patient.entity.BizPatientGuardian;
import com.his.patient.enums.GuardianRelationEnum;
import com.his.patient.mapper.BizPatientGuardianMapper;
import com.his.patient.mapper.BizPatientMapper;
import com.his.patient.service.PatientGuardianService;
import com.his.patient.service.PatientService;
import com.his.patient.vo.GuardianPatientVO;
import com.his.patient.vo.SmsSendVO;
import com.his.security.UserUtils;
import com.his.security.entity.CurrentUser;
import com.his.system.entity.SysUser;
import com.his.system.mapper.SysUserMapper;
import com.his.system.service.SmsCodeService;
import com.his.system.service.SysAuditLogService;
import com.his.system.service.SysMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientGuardianServiceImpl implements PatientGuardianService {

    /**
     * 1 个账号最多绑定的就诊人数（风控上限）
     */
    private static final int MAX_BINDINGS_PER_USER = 5;
    /**
     * 1 个就诊人最多被多少个账号绑定（防多人窥探同一档案）
     */
    private static final int MAX_BINDERS_PER_PATIENT = 3;

    private final BizPatientGuardianMapper guardianMapper;
    private final BizPatientMapper patientMapper;
    private final PatientService patientService;
    private final SysUserMapper sysUserMapper;
    private final SysMessageService sysMessageService;
    private final SmsCodeService smsCodeService;
    private final SysAuditLogService auditLogService;

    private static String normalizeIdCard(String idCard) {
        String s = idCard == null ? "" : idCard.trim().toUpperCase();
        if (!s.matches("^\\d{17}[\\dX]$")) {
            throw new BusinessException("身份证号格式不正确");
        }
        return s;
    }

    private static LocalDate parseBirthDate(String idCard) {
        if (idCard == null || idCard.length() < 14) {
            return null;
        }
        try {
            return LocalDate.parse(idCard.substring(6, 14), DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 后端出参一律打码（前端脱敏挡不住抓包），格式：前6 + ******** + 后4
     */
    private static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 15) {
            return idCard == null || idCard.isEmpty() ? null : "***";
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 手机号打码：138****8888
     */
    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone == null || phone.isEmpty() ? null : "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    @Override
    public List<Long> accessiblePatientIds(CurrentUser user) {
        if (user == null || user.getUserId() == null
                || !Integer.valueOf(UserTypeEnum.PATIENT.getCode()).equals(user.getUserType())) {
            return List.of();
        }
        Set<Long> ids = new LinkedHashSet<>();
        LambdaQueryWrapper<BizPatientGuardian> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizPatientGuardian::getUserId, user.getUserId())
                .eq(BizPatientGuardian::getStatus, 1);
        guardianMapper.selectList(wrapper).forEach(g -> ids.add(g.getPatientId()));
        if (user.getPatientId() != null) {
            ids.add(user.getPatientId());
        }
        return new ArrayList<>(ids);
    }

    @Override
    public boolean canAccessPatient(Long patientId) {
        if (patientId == null) {
            return false;
        }
        return accessiblePatientIds(UserUtils.getCurrentUser()).contains(patientId);
    }

    @Override
    public boolean patientScopeViolated(Long patientId) {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || !Integer.valueOf(UserTypeEnum.PATIENT.getCode()).equals(user.getUserType())) {
            return false;
        }
        return !canAccessPatient(patientId);
    }

    @Override
    public List<GuardianPatientVO> myPatients() {
        CurrentUser user = requirePatientUser();
        LambdaQueryWrapper<BizPatientGuardian> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizPatientGuardian::getUserId, user.getUserId())
                .eq(BizPatientGuardian::getStatus, 1)
                .orderByDesc(BizPatientGuardian::getIsDefault)
                .orderByAsc(BizPatientGuardian::getCreateTime);
        List<BizPatientGuardian> bindings = guardianMapper.selectList(wrapper);

        List<GuardianPatientVO> list = new ArrayList<>();
        Set<Long> seen = new LinkedHashSet<>();
        // 库里真正的默认项。「本人」不能无脑报 isDefault=1：它排在列表首位，
        // 会把用户实际选定的默认就诊人盖掉，小程序端回填时就永远回到本人。
        Long defaultPatientId = bindings.stream()
                .filter(g -> Integer.valueOf(1).equals(g.getIsDefault()))
                .map(BizPatientGuardian::getPatientId)
                .findFirst()
                .orElse(null);
        // 「本人」置顶：优先用用户的患者ID 口径，即使关系表还没回填也能看到
        if (user.getPatientId() != null) {
            BizPatient self = patientMapper.selectById(user.getPatientId());
            if (self != null) {
                seen.add(self.getId());
                boolean selfIsDefault = defaultPatientId == null || defaultPatientId.equals(self.getId());
                list.add(toVO(self, GuardianRelationEnum.SELF.getCode(), selfIsDefault ? 1 : 0, true));
            }
        }
        for (BizPatientGuardian g : bindings) {
            if (seen.contains(g.getPatientId())) {
                continue;
            }
            BizPatient p = patientMapper.selectById(g.getPatientId());
            if (p == null) {
                continue;
            }
            seen.add(p.getId());
            list.add(toVO(p, g.getRelation(), g.getIsDefault(), false));
        }
        return list;
    }

    @Override
    public SmsSendVO sendBindCode(GuardianSendBindCodeDTO dto) {
        CurrentUser user = requirePatientUser();
        String idCard = normalizeIdCard(dto.getIdCard());
        BizPatient patient = patientService.selectByIdCard(idCard);
        if (patient == null || !patient.getPatientName().equals(dto.getPatientName().trim())) {
            // 与 bindPatient 同口径的合并文案，不区分哪种不匹配，避免探测建档
            throw new BusinessException("未找到匹配的就诊档案，请核对姓名与身份证号");
        }
        if (!StringUtils.hasText(patient.getPhone())) {
            throw new BusinessException("该就诊人建档未预留手机号，请持有效证件到窗口办理绑定");
        }
        SmsCodeService.SendResult send = smsCodeService.send(patient.getPhone(), SmsCodeService.SCENE_BIND);
        if (!send.success()) {
            throw new BusinessException(send.message());
        }
        SmsSendVO vo = new SmsSendVO();
        vo.setMockCode(send.code());
        vo.setPhoneMask(maskPhone(patient.getPhone()));
        auditGuardian(user, "sendBindCode", patient.getId(), true,
                "patient=" + patient.getPatientName() + ", idCard=" + maskIdCard(idCard));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GuardianPatientVO bindPatient(GuardianBindDTO dto) {
        CurrentUser user = requirePatientUser();
        String idCard = normalizeIdCard(dto.getIdCard());
        if (!GuardianRelationEnum.isValid(dto.getRelation())) {
            throw new BusinessException("关系码值不正确");
        }
        BizPatient patient = patientService.selectByIdCard(idCard);
        if (patient == null || !patient.getPatientName().equals(dto.getPatientName().trim())) {
            // 双因子任一不匹配即拒绝，且不区分哪种不匹配 —— 避免拿此接口探测谁建过档
            auditGuardian(user, "bindPatient", null, false, "姓名与身份证号未匹配到就诊档案, idCard=" + maskIdCard(idCard));
            throw new BusinessException("未找到匹配的就诊档案，请核对姓名与身份证号");
        }
        if (existsBinding(user.getUserId(), patient.getId())) {
            throw new BusinessException("该就诊人已绑定");
        }
        checkBindQuota(user, patient);

        // 收码号码只认档案预留手机号（前端传不了）：能收到码即视为本人授权
        String recordPhone = patient.getPhone();
        if (!StringUtils.hasText(recordPhone)) {
            throw new BusinessException("该就诊人建档未预留手机号，请持有效证件到窗口办理绑定");
        }
        // 验证码一次一用：放在其他校验之后，别让码白烧在配额这种提示上
        String smsError = smsCodeService.verify(recordPhone, SmsCodeService.SCENE_BIND, dto.getSmsCode());
        if (smsError != null) {
            auditGuardian(user, "bindPatient", patient.getId(), false, smsError + ", idCard=" + maskIdCard(idCard));
            throw new BusinessException(smsError);
        }
        BizPatientGuardian g = insertBinding(user, patient.getId(), dto.getRelation());
        auditGuardian(user, "bindPatient", patient.getId(), true,
                "patient=" + patient.getPatientName() + ", idCard=" + maskIdCard(idCard) + ", relation=" + g.getRelation());
        return toVO(patient, g.getRelation(), g.getIsDefault(), patient.getId().equals(user.getPatientId()));
    }

    @Override
    public SmsSendVO sendAddCode(GuardianSendAddCodeDTO dto) {
        requirePatientUser();
        SmsCodeService.SendResult send = smsCodeService.send(dto.getPhone().trim(), SmsCodeService.SCENE_ADD);
        if (!send.success()) {
            throw new BusinessException(send.message());
        }
        SmsSendVO vo = new SmsSendVO();
        vo.setMockCode(send.code());
        vo.setPhoneMask(maskPhone(dto.getPhone().trim()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GuardianPatientVO addPatient(GuardianUpsertDTO dto) {
        CurrentUser user = requirePatientUser();
        String idCard = normalizeIdCard(dto.getIdCard());
        if (!GuardianRelationEnum.isValid(dto.getRelation())) {
            throw new BusinessException("关系码值不正确");
        }
        if (patientService.selectByIdCard(idCard) != null) {
            throw new BusinessException("该身份证已建档，请改用「绑定就诊人」");
        }
        if (countActiveBindings(user.getUserId()) >= MAX_BINDINGS_PER_USER) {
            throw new BusinessException("每个账号最多绑定 " + MAX_BINDINGS_PER_USER + " 位就诊人");
        }
        String phone = dto.getPhone().trim();
        String smsError = smsCodeService.verify(phone, SmsCodeService.SCENE_ADD, dto.getSmsCode());
        if (smsError != null) {
            auditGuardian(user, "addPatient", null, false, smsError + ", idCard=" + maskIdCard(idCard));
            throw new BusinessException(smsError);
        }

        BizPatient patient = new BizPatient();
        patient.setPatientName(dto.getPatientName().trim());
        patient.setGender(dto.getGender());
        patient.setIdCard(idCard);
        patient.setPhone(phone);
        patient.setPatientNo("P" + System.currentTimeMillis());
        patient.setPatientType(1); // 1-自费
        patient.setStatus(1);
        LocalDate birth = parseBirthDate(idCard);
        if (birth != null) {
            patient.setBirthDate(birth);
            patient.setAge(Period.between(birth, LocalDate.now()).getYears());
        }
        patient.setCreateBy("mini-guardian");
        patient.setCreateTime(LocalDateTime.now());
        patientMapper.insert(patient);

        BizPatientGuardian g = insertBinding(user, patient.getId(), dto.getRelation());
        auditGuardian(user, "addPatient", patient.getId(), true,
                "patient=" + patient.getPatientName() + ", idCard=" + maskIdCard(idCard) + ", phone=" + maskPhone(phone));
        return toVO(patient, g.getRelation(), g.getIsDefault(), false);
    }

    // 私有

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindPatient(Long patientId) {
        CurrentUser user = requirePatientUser();
        if (patientId.equals(user.getPatientId())) {
            throw new BusinessException("账号本人档案不可解绑");
        }
        BizPatientGuardian binding = findBinding(user.getUserId(), patientId);
        if (binding == null) {
            throw new BusinessException("未找到该就诊人绑定");
        }
        guardianMapper.deleteById(binding.getId());
        BizPatient patient = patientMapper.selectById(patientId);
        auditGuardian(user, "unbindPatient", patientId, true,
                "patient=" + (patient == null ? "?" : patient.getPatientName()));
        // 解绑的正是默认项时，把「本人」（若已绑）或剩余最早一条顶上为默认
        if (Integer.valueOf(1).equals(binding.getIsDefault())) {
            promoteFallbackDefault(user);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long patientId) {
        CurrentUser user = requirePatientUser();
        if (!patientId.equals(user.getPatientId()) && findBinding(user.getUserId(), patientId) == null) {
            throw new BusinessException("未找到该就诊人绑定");
        }
        guardianMapper.update(null, new LambdaUpdateWrapper<BizPatientGuardian>()
                .eq(BizPatientGuardian::getUserId, user.getUserId())
                .set(BizPatientGuardian::getIsDefault, 0));
        if (!patientId.equals(user.getPatientId())) {
            BizPatientGuardian binding = findBinding(user.getUserId(), patientId);
            binding.setIsDefault(1);
            guardianMapper.updateById(binding);
        }
    }

    @Override
    public void bindOpenid(String openid) {
        CurrentUser user = requirePatientUser();
        if (openid == null || openid.isBlank() || openid.length() > 64) {
            throw new BusinessException("openid 不合法");
        }
        // 只更新自己账号的 openid；唯一性冲突（该微信已绑别的账号）直接拒绝，不做抢占
        if (sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getOpenid, openid)
                .ne(SysUser::getId, user.getUserId())) > 0) {
            throw new BusinessException("该微信已绑定其他账号");
        }
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getUserId())
                .set(SysUser::getOpenid, openid));
    }

    @Override
    public String testNotify() {
        CurrentUser user = requirePatientUser();
        Map<String, String> data = new HashMap<>();
        data.put("thing1", "就诊人绑定测试");
        data.put("character_string2", String.valueOf(System.currentTimeMillis() % 1000000));
        boolean ok = sysMessageService.sendWechatMessage(
                user.getUserId(), user.getRealName(), "test",
                "pages/home/home", data,
                "患者端通知测试", "这是一条微信订阅消息通道自测消息", "mini_test", user.getUserId());
        return ok ? "发送成功（详见 sys_message channel=wechat 留痕）"
                : "发送失败（原因见 sys_message channel=wechat 最新一条 remark：未启用/未配模板/未绑openid 都会落在这里）";
    }

    private CurrentUser requirePatientUser() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null || !Integer.valueOf(UserTypeEnum.PATIENT.getCode()).equals(user.getUserType())) {
            throw new BusinessException("仅患者账号可管理就诊人");
        }
        return user;
    }

    private boolean existsBinding(Long userId, Long patientId) {
        return findBinding(userId, patientId) != null;
    }

    private BizPatientGuardian findBinding(Long userId, Long patientId) {
        LambdaQueryWrapper<BizPatientGuardian> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizPatientGuardian::getUserId, userId)
                .eq(BizPatientGuardian::getPatientId, patientId)
                .eq(BizPatientGuardian::getStatus, 1)
                .last("LIMIT 1");
        return guardianMapper.selectOne(wrapper);
    }

    private BizPatientGuardian insertBinding(CurrentUser user, Long patientId, Integer relation) {
        // 解绑是软删，uk_user_patient(user_id, patient_id) 仍占位 —— 重绑必须复活旧行，
        // 直接 insert 会撞唯一键（且报错完全无法向用户解释）。复活只能走裸 SQL（见 mapper 注释）。
        BizPatientGuardian softDeleted = guardianMapper.selectSoftDeleted(user.getUserId(), patientId);
        // 账号名下第一条绑定（且「本人」不占位时）自动顶为默认，小程序首启免手动设置
        boolean firstBinding = user.getPatientId() == null && countActiveBindings(user.getUserId()) == 0;
        if (softDeleted != null) {
            guardianMapper.reviveSoftDeleted(softDeleted.getId(), relation, firstBinding ? 1 : 0);
            BizPatientGuardian revived = findBinding(user.getUserId(), patientId);
            if (revived != null) {
                return revived;
            }
        }
        BizPatientGuardian g = new BizPatientGuardian();
        g.setUserId(user.getUserId());
        g.setPatientId(patientId);
        g.setRelation(relation);
        g.setStatus(1);
        g.setIsDefault(firstBinding ? 1 : 0);
        g.setCreateBy("mini-guardian");
        g.setCreateTime(LocalDateTime.now());
        guardianMapper.insert(g);
        return g;
    }

    private long countActiveBindings(Long userId) {
        LambdaQueryWrapper<BizPatientGuardian> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizPatientGuardian::getUserId, userId)
                .eq(BizPatientGuardian::getStatus, 1);
        return guardianMapper.selectCount(wrapper);
    }

    private long countBoundUsers(Long patientId) {
        LambdaQueryWrapper<BizPatientGuardian> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizPatientGuardian::getPatientId, patientId)
                .eq(BizPatientGuardian::getStatus, 1);
        return guardianMapper.selectCount(wrapper);
    }

    private void checkBindQuota(CurrentUser user, BizPatient patient) {
        if (countActiveBindings(user.getUserId()) >= MAX_BINDINGS_PER_USER) {
            throw new BusinessException("每个账号最多绑定 " + MAX_BINDINGS_PER_USER + " 位就诊人");
        }
        if (countBoundUsers(patient.getId()) >= MAX_BINDERS_PER_PATIENT) {
            throw new BusinessException("该就诊人绑定账号数已达上限，请持有效证件到窗口办理");
        }
    }

    private void auditGuardian(CurrentUser user, String operation, Long patientId, boolean success, String detail) {
        auditLogService.record(user.getUserId(), user.getUsername(), "患者端-就诊人", operation,
                "biz_patient", patientId, detail, success, success ? null : detail);
    }

    private void promoteFallbackDefault(CurrentUser user) {
        LambdaQueryWrapper<BizPatientGuardian> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizPatientGuardian::getUserId, user.getUserId())
                .eq(BizPatientGuardian::getStatus, 1)
                .ne(user.getPatientId() != null, BizPatientGuardian::getPatientId, user.getPatientId())
                .orderByAsc(BizPatientGuardian::getCreateTime)
                .last("LIMIT 1");
        BizPatientGuardian fallback = guardianMapper.selectOne(wrapper);
        if (fallback != null) {
            fallback.setIsDefault(1);
            guardianMapper.updateById(fallback);
        }
    }

    private GuardianPatientVO toVO(BizPatient p, Integer relation, Integer isDefault, boolean self) {
        GuardianPatientVO vo = new GuardianPatientVO();
        vo.setPatientId(p.getId());
        vo.setPatientNo(p.getPatientNo());
        vo.setPatientName(p.getPatientName());
        vo.setGender(p.getGender());
        vo.setAge(p.getAge());
        vo.setBirthDate(p.getBirthDate());
        vo.setPhone(maskPhone(p.getPhone()));
        vo.setIdCard(maskIdCard(p.getIdCard()));
        vo.setRelation(relation);
        vo.setRelationText(GuardianRelationEnum.labelOf(relation));
        vo.setIsDefault(isDefault == null ? 0 : isDefault);
        vo.setSelf(self);
        return vo;
    }
}
