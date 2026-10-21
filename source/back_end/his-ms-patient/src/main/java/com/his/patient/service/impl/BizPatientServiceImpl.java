package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.constant.DictTypeConst;
import com.his.common.enums.EnableStatusEnum;
import com.his.common.enums.MedicalInsuranceTypeEnum;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.common.enums.PatientMergeStatusEnum;
import com.his.spi.service.RedisSequenceService;
import com.his.common.util.SensitiveMaskUtil;
import com.his.common.util.TextUtil;
import com.his.patient.dto.PatientPageQueryDTO;
import com.his.patient.dto.PatientRegisterDTO;
import com.his.patient.dto.PatientSearchScopeDTO;
import com.his.patient.dto.PatientUpsertDTO;
import com.his.patient.entity.BizPatient;
import com.his.patient.entity.BizPatientTagRelation;
import com.his.patient.entity.SysPatientTag;
import com.his.patient.mapper.BizPatientMapper;
import com.his.patient.mapper.BizPatientTagRelationMapper;
import com.his.patient.mapper.SysPatientTagMapper;
import com.his.patient.service.*;
import com.his.patient.support.PatientProfileValidator;
import com.his.spi.dto.PatientAccountCreateDTO;
import com.his.spi.service.RegistFacadeService;
import com.his.spi.service.SystemFacadeService;
import com.his.spi.vo.TodayVisitVO;
import com.his.common.enums.PatientSearchScopeModeEnum;
import com.his.patient.support.PatientSearchScopeResolver;
import com.his.patient.vo.*;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.MaskEnum;
import com.his.common.support.FieldSpec;
import com.his.common.utils.UserUtils;
import com.his.spi.service.DictCacheService;
import com.his.spi.service.FieldChangeFacadeService;
import com.his.spi.service.PasswordCipherFacadeService;
import com.his.spi.service.SmsCodeService;
import com.his.patient.vo.SysPatientTagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 患者服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BizPatientServiceImpl extends ServiceImpl<BizPatientMapper, BizPatient> implements BizPatientService {
    /**
     * 对象类型：患者主档
     */
    private static final String PATIENT = "PATIENT";
    private final DictCacheService dictCacheService;
    /**
     * 患者主档参与字段级留痕的字段清单。
     *
     * <p><b>刻意不记余额 / 总费用 / 就诊次数与首末就诊冗余组</b>：
     * 那是收费与就诊链路回写的派生字段，不是人在表单里改的。每次挂号都会刷一遍
     * 最近就诊时间，把它们记进来，日志会被"系统自己改自己"刷爆，真正的人工修改反而找不着。
     *
     * <p><b>打码只打直接标识符</b>（身份证 / 手机号 / 住址 / 医保卡号），姓名、性别、
     * 过敏史这些业务字段原样留 —— 全打码的日志在检查时答不出"过敏史什么时候被改过"。
     */
    private final List<FieldSpec> PATIENT_FIELDS = FieldSpec.list(
            FieldSpec.of("patientName", "姓名"),
            FieldSpec.render("gender", "性别", v -> SysGenderEnum.getLabel((Integer) v)),
            FieldSpec.of("birthDate", "出生日期"),
            FieldSpec.masked("idCard", "身份证号", MaskEnum.ID_CARD),
            FieldSpec.masked("phone", "联系电话", MaskEnum.PHONE),
            FieldSpec.of("contactName", "紧急联系人"),
            FieldSpec.masked("contactPhone", "联系人电话", MaskEnum.PHONE),
            FieldSpec.of("contactRelation", "与患者关系"),
            FieldSpec.masked("address", "家庭住址", MaskEnum.ADDRESS),
            FieldSpec.of("nation", "民族"),
            FieldSpec.of("occupation", "职业"),
            FieldSpec.render("maritalStatus", "婚姻状况", v -> dictText(DictTypeConst.MARITAL_STATUS, v)),
            FieldSpec.of("bloodType", "血型"),
            FieldSpec.of("allergyHistory", "过敏史"),
            FieldSpec.of("medicalHistory", "既往病史"),
            FieldSpec.of("medicalInsuranceType", "费别"),
            FieldSpec.masked("medicalInsuranceNo", "医保卡号", MaskEnum.BANK_NO),
            FieldSpec.render("cardType", "证件类型", v -> dictText(DictTypeConst.CARD_TYPE, v)),
            FieldSpec.masked("cardNo", "证件号码", MaskEnum.BANK_NO),
            FieldSpec.render("status", "状态", v -> EnableStatusEnum.getLabel((Integer) v))
    );
    private final RedisSequenceService redisSequenceService;
    private final BizPatientTagRelationMapper bizPatientTagRelationMapper;
    /**
     * 健康档案六组的唯一写入口。建档时把主档的「过敏史 / 既往病史 / 联系人」文本
     * 落成结构化明细并回算投影，见 {@code syncAfterPatientSave}。
     */
    private final PatientHealthProfileService patientHealthProfileService;
    /**
     * 患者自助注册写登录账号用：建档后同步开通患者类型的登录账号。
     */
    private final SystemFacadeService systemFacadeService;
    /**
     * 患者标签字典读取：按名称/ID 批量取标签（标签定义已随患者标签迁入患者域，本域内部直查 mapper，不再绕道 Facade）。
     */
    private final SysPatientTagMapper sysPatientTagMapper;
    /**
     * 注册口令传输加密：前端提交的是 SM2 密文（与登录同一对公钥），这里还原成明文再交 BCrypt 落库。
     * 注册是初始口令第一次上网的场合，和登录一样不能收明文 —— 留明文口子等于登录加密白做。
     */
    private final PasswordCipherFacadeService passwordCipherService;
    /**
     * 注册验证码校验：手机号所有权由它证明，未通过则不建档、不开账号。
     */
    private final SmsCodeService smsCodeService;
    /**
     * 患者-标签关系的读取归它（标签口径只允许有一处，见该服务注释）。
     */
    private final BizPatientTagRelationService bizPatientTagRelationService;
    /**
     * 「今日就诊」事实端口（实现在就诊域 his-appoint 的 spi 适配器，只读挂号队列行）。
     *
     * <p>端口契约在公共模块 his-core-spi，实现由 his-web 聚合引入；
     * 实现缺失直接启动失败，不做静默降级。
     */
    private final RegistFacadeService registFacadeService;
    /**
     * 「要不要看今日就诊」的判定（按当前登录角色的岗位性质）。
     *
     * <p>身份由它自己从 SecurityContext 取，调用方不传任何参数 ——
     * 不允许调用方声明自己的搜索范围。
     */
    private final PatientSearchScopeResolver patientSearchScopeResolver;
    /**
     * 字段级修改日志落库器。
     *
     * <p>患者主档是 HIS 里被改得最频繁、也最该被追责的对象 —— 改姓名、改身份证、
     * 改过敏史都会直接影响后续诊疗。这里把「改之前是啥、改之后是啥」钉进表里。
     */
    private final FieldChangeFacadeService fieldChangeFacadeService;

    /**
     * 联系电话脱敏：11 位手机号保留前 3 后 4（138****5678）；
     * 其他长度（座机/历史脏数据）≥7 位保留前 2 后 2，更短全遮。
     */
    private static String maskPhone(String phone) {
        if (!TextUtil.hasText(phone)) {
            return null;
        }
        String s = phone.trim();
        int n = s.length();
        if (n == 11) {
            return s.substring(0, 3) + "****" + s.substring(n - 4);
        }
        if (n >= 7) {
            return s.substring(0, 2) + "****" + s.substring(n - 2);
        }
        return "****";
    }

    /**
     * 从身份证号解析出生日期（第 7~14 位 yyyyMMdd）。解析失败返回 null（不阻断建档）。
     */
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
     * 字典码值取文案，供 {@link #PATIENT_FIELDS} 的 render 回调。
     *
     * <p>为什么不直接在初始化器里写 {@code dictCacheService.getDicDataLabel(...)}：
     * {@code dictCacheService} 是构造器注入的 blank final 字段，而 {@code PATIENT_FIELDS}
     * 是**实例字段初始化器** —— Java 的确定赋值规则禁止初始化器读取尚未初始化的 final 字段
     * （编译期报"可能尚未初始化变量"，加 import 也解决不了）。
     * 绕一层实例方法，读操作就推迟到 render 真正被调用时，那时构造早已完成。
     */
    private String dictText(String dictType, Object code) {
        return dictCacheService.getDicDataLabel(dictType, (Integer) code);
    }

    @Override
    public PageResult<BizPatient> selectPatientPage(String patientName, String phone, String patientNo,
                                                    String medicalInsuranceType, String keyword, int pageNum, int pageSize) {
        return selectPatientPage(patientName, phone, patientNo, medicalInsuranceType, keyword, pageNum, pageSize, null, null);
    }

    @Override
    public PageResult<BizPatient> selectPatientPage(String patientName, String phone, String patientNo,
                                                    String medicalInsuranceType, String keyword, int pageNum, int pageSize,
                                                    List<Long> tagPatientIds) {
        return selectPatientPage(patientName, phone, patientNo, medicalInsuranceType, keyword, pageNum, pageSize,
                tagPatientIds, null);
    }

    @Override
    public PageResult<BizPatient> selectPatientPage(String patientName, String phone, String patientNo,
                                                    String medicalInsuranceType, String keyword, int pageNum, int pageSize,
                                                    List<Long> tagPatientIds, PatientSearchScopeDTO scope) {
        LambdaQueryWrapper<BizPatient> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(TextUtil.hasText(patientName), BizPatient::getPatientName, patientName)
                .like(TextUtil.hasText(phone), BizPatient::getPhone, phone)
                .like(TextUtil.hasText(patientNo), BizPatient::getPatientNo, patientNo)
                .eq(TextUtil.hasText(medicalInsuranceType), BizPatient::getMedicalInsuranceType, medicalInsuranceType)
                .in(tagPatientIds != null && !tagPatientIds.isEmpty(), BizPatient::getId, tagPatientIds);
        // 综合关键字：姓名 / 患者号 / 手机号 / 身份证号四者 OR，必须整体括号包裹，
        // 否则会把前面精确条件的 AND 关系吃掉（OR 优先级低于 AND）
        if (TextUtil.hasText(keyword)) {
            wrapper.and(w -> w.like(BizPatient::getPatientName, keyword)
                    .or().like(BizPatient::getPatientNo, keyword)
                    .or().like(BizPatient::getPhone, keyword)
                    .or().like(BizPatient::getIdCard, keyword));
        }

        // 今日就诊置顶。
        //
        // 【为什么必须写进 SQL 而不是前端排】列表是分页的（全局搜索每页 10 条），
        // 前端只能对「已经翻出来的这一页」重排 —— 今日患者若按建档时间落在第 3 页，
        // 前两页照样看不到他，前端再排也没用。所以排序必须在 LIMIT 之前生效。
        //
        // 【权重两级】我的今日（本人/本科室）→ 今日其他科室 → 其余（建档时间倒序）。
        // 医生搜一个姓时，最想要的是今天挂我号的这一个，而不是随便哪个今天在别科就诊的人。
        //
        // 【为什么用 last 且与 orderByDesc 互斥】MP 的 last() 是**追加**在 SQL 末尾，
        // 若再调 orderByDesc 会拼出两个 ORDER BY 子句直接报错。两条分支只能走一条。
        // ids 来自 Provider 返回的 Long 主键，非用户输入，无注入面。
        if (scope != null && scope.getTodayIds() != null && !scope.getTodayIds().isEmpty()) {
            StringBuilder order = new StringBuilder("ORDER BY ");
            if (scope.getMyTodayIds() != null && !scope.getMyTodayIds().isEmpty()) {
                order.append("CASE WHEN id IN (").append(joinIds(scope.getMyTodayIds()))
                        .append(") THEN 0 ELSE 1 END, ");
            }
            order.append("CASE WHEN id IN (").append(joinIds(scope.getTodayIds()))
                    .append(") THEN 0 ELSE 1 END, create_time DESC, id DESC");
            wrapper.last(order.toString());
        } else {
            // 二级键必须补主键：create_time 会大面积重复（造数/批量导入同一秒落库），
            // 只按它排序时 MySQL 不保证行间顺序稳定 —— 分页会出现「同一行在两页各出现一次、
            // 另一行谁也翻不到」，是静默少数据，比报错更难发现。
            wrapper.orderByDesc(BizPatient::getCreateTime).orderByDesc(BizPatient::getId);
        }

        Page<BizPatient> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), page.getRecords());
    }

    /**
     * 拼 id 列表：来源是 Long 主键，非用户输入，无注入面
     */
    private String joinIds(List<Long> ids) {
        return ids.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    @Override
    public BizPatient selectByPatientNo(String patientNo) {
        LambdaQueryWrapper<BizPatient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizPatient::getPatientNo, patientNo);
        return this.getOne(wrapper);
    }

    @Override
    public BizPatient selectByIdCard(String idCard) {
        LambdaQueryWrapper<BizPatient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizPatient::getIdCard, idCard).orderByAsc(BizPatient::getId);
        List<BizPatient> list = this.list(wrapper);
        if (list.size() > 1) {
            log.warn("身份证 {} 对应 {} 份档案（疑似重复建档），建档防重按第一条判定；"
                    + "请到「患者主索引」核实合并", idCard, list.size());
        }
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPatient(BizPatient patient) {
        PatientProfileValidator.validateForCreate(patient);
        if (TextUtil.hasText(patient.getIdCard())) {
            BizPatient existing = selectByIdCard(patient.getIdCard());
            if (existing != null) {
                throw new BusinessException("该身份证号已存在患者记录");
            }
        }
        patient.setPatientNo(redisSequenceService.generatePatientNo());
        if (patient.getBirthDate() != null) {
            patient.setAge(Period.between(patient.getBirthDate(), LocalDate.now()).getYears());
        }

        // status 是「启用状态」（0-停用 1-启用），不是删除标志 —— 曾错用 DelFlagEnum.NORMAL(0)，
        // 把当天所有新患者建成「停用」（患者下拉挂红签、列表页显示停用）。register/监护人建档都写 1，统一口径。
        patient.setStatus(EnableStatusEnum.ENABLED.getCode());
        patient.setMergeStatus(PatientMergeStatusEnum.NORMAL.getCode());
        boolean result = this.save(patient);
        if (!result) {
            return result;
        }
        patientHealthProfileService.syncAfterPatientSave(patient);
        autoAddStaticTags(patient);
        return result;
    }

    /**
     * 自动添加静态标签（根据患者信息）
     */
    private void autoAddStaticTags(BizPatient patient) {
        if (patient.getAge() == null) {
            return;
        }

        // 查询预置的静态标签（本域 mapper 直查）
        List<SysPatientTag> staticTags = sysPatientTagMapper.selectList(
                new LambdaQueryWrapper<SysPatientTag>().in(SysPatientTag::getTagName, List.of("老年", "儿童", "幼儿")));

        Map<String, Long> tagNameToIdMap = new HashMap<>();
        for (SysPatientTag tag : staticTags) {
            tagNameToIdMap.put(tag.getTagName(), tag.getTagId());
        }

        // 根据年龄自动打标签
        if (patient.getAge() >= 60 && tagNameToIdMap.containsKey("老年")) {
            addTagRelation(patient.getId(), tagNameToIdMap.get("老年"));
        } else if (patient.getAge() <= 14 && patient.getAge() > 3 && tagNameToIdMap.containsKey("儿童")) {
            addTagRelation(patient.getId(), tagNameToIdMap.get("儿童"));
        } else if (patient.getAge() <= 3 && tagNameToIdMap.containsKey("幼儿")) {
            addTagRelation(patient.getId(), tagNameToIdMap.get("幼儿"));
        }
    }

    /**
     * 添加标签关联关系
     */
    private void addTagRelation(Long patientId, Long tagId) {
        LambdaQueryWrapper<BizPatientTagRelation> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(BizPatientTagRelation::getPatientId, patientId)
                .eq(BizPatientTagRelation::getTagId, tagId);
        if (bizPatientTagRelationMapper.selectCount(checkWrapper) == 0) {
            BizPatientTagRelation relation = new BizPatientTagRelation();
            relation.setPatientId(patientId);
            relation.setTagId(tagId);
            relation.setSourceType(2); // 系统自动打标
            bizPatientTagRelationMapper.insert(relation);
        }
    }

    @Override
    public Map<Long, Integer> mapAppointCount(List<Long> patientIds) {
        Map<Long, Integer> appointCountMap = new HashMap<>();
        if (patientIds == null || patientIds.isEmpty()) {
            return appointCountMap;
        }
        for (PatientRegistCountVO row : baseMapper.countRegistByPatientIds(patientIds)) {
            if (row.getPatientId() != null && row.getCnt() != null) {
                appointCountMap.put(row.getPatientId(), row.getCnt().intValue());
            }
        }
        return appointCountMap;
    }

    @Override
    public PageResult<PatientVO> queryPatientPage(PatientPageQueryDTO queryDTO) {
        // 如果有标签过滤，先查询有该标签的患者ID列表
        List<Long> tagPatientIds = null;
        if (queryDTO.getTagId() != null) {
            tagPatientIds = bizPatientTagRelationService.listPatientIdsByTagId(queryDTO.getTagId());
            if (tagPatientIds.isEmpty()) {
                return PageResult.of(0, 0, 0, 0, List.of());
            }
        }

        // 今日就诊：一次取全，同时用于两处 ——
        // ① 传给 selectPatientPage 做「分页之前」的排序置顶；② 回填到 VO 供前端分组标注。
        //
        // 但**只有门诊岗位才取这份数据**（模式由当前角色决定，一处判定）。
        // 非门诊岗位（病案/质控/医保/审计/管理员…）搜人就是查档案：
        // 既不置顶、也不标注，顺带省掉每次敲键都全表扫一遍排队表。
        // 注意两者必须一起关：只关分组不关置顶 = 顶上几个患者没头没尾地排在前面，像随机排序。
        PatientSearchScopeModeEnum scopeMode = patientSearchScopeResolver.resolve();
        Map<Long, TodayVisitVO> todayVisits = scopeMode == PatientSearchScopeModeEnum.TODAY_FIRST
                ? loadTodayVisits()
                : Collections.emptyMap();
        PatientSearchScopeDTO scope = buildSearchScope(todayVisits);

        PageResult<BizPatient> pageResult = selectPatientPage(
                queryDTO.getPatientName(), queryDTO.getPhone(), queryDTO.getPatientNo(),
                queryDTO.getMedicalInsuranceType(), queryDTO.getKeyword(),
                queryDTO.getPageNum(), queryDTO.getPageSize(), tagPatientIds,
                scope);

        PageResult<PatientVO> voPageResult = new PageResult<>();
        voPageResult.setTotal(pageResult.getTotal());
        voPageResult.setPageNum(pageResult.getPageNum());
        voPageResult.setPageSize(pageResult.getPageSize());
        voPageResult.setPages(pageResult.getPages());
        List<PatientVO> voList = pageResult.getRecords().stream().map(patient -> {
            PatientVO vo = new PatientVO();
            BeanUtils.copyProperties(patient, vo);
            return vo;
        }).collect(Collectors.toList());
        fillTagsAndAppointCount(voList);
        fillTodayVisit(voList, todayVisits);
        maskListSensitiveFields(voList);
        voPageResult.setRecords(voList);
        return voPageResult;
    }

    @Override
    public PatientVO getPatientVOById(Long patientId) {
        BizPatient patient = this.getById(patientId);
        if (patient == null) {
            // 以前这里直接 BeanUtils.copyProperties(null, vo) → NPE → GlobalExceptionHandler 兜成 500，
            // 调用方只看到「500」根本不知道是自己把别的表的主键当患者 id 用了
            // （实测：前端误传排队表主键，两者都是 19 位雪花 ID，长度一样不会报类型错）。
            throw new BusinessException("患者不存在：" + patientId);
        }
        PatientVO vo = new PatientVO();
        BeanUtils.copyProperties(patient, vo);
        fillTagsAndAppointCount(List.of(vo));
        return vo;
    }

    @Override
    public PatientVO getPatientVOByNo(String patientNo) {
        BizPatient patient = selectByPatientNo(patientNo);
        PatientVO vo = new PatientVO();
        BeanUtils.copyProperties(patient, vo);
        return vo;
    }

    @Override
    public PatientDetailVO getPatientDetail(Long patientId) {
        PatientDetailVO detailVO = new PatientDetailVO();
        BizPatient patient = this.getById(patientId);
        BeanUtils.copyProperties(patient, detailVO);

        // 六组健康档案一次取回。这里**不再自己拼查询**：此前这个接口用四个 mapper 各查一次，
        // 只带回四组（缺用药史与联系人），排序与字段映射也各写一套 —— 于是「患者详情」和
        // 「健康档案页」对同一个患者能给出两套不一致的列表。现在两者共用同一个读实现。
        PatientHealthProfileVO profile = patientHealthProfileService.getProfile(patientId);
        detailVO.setAllergies(profile.getAllergies());
        detailVO.setPastDiseases(profile.getPastDiseases());
        detailVO.setSurgeryHistories(profile.getSurgeryHistories());
        detailVO.setFamilyHistories(profile.getFamilyHistories());
        detailVO.setMedications(profile.getMedications());
        detailVO.setContacts(profile.getContacts());

        // 患者标签：运营标记（VIP / 高血压 / 建档提醒…），与列表页同一实现、同一口径，
        // 且**不参与下面的临床裁剪** —— 收费窗口认 VIP、护士看注意事项都要用它。
        detailVO.setTags(bizPatientTagRelationService.mapTagsByPatientIds(List.of(patientId))
                .getOrDefault(patientId, List.of()));

        // 按岗位裁剪临床字段（服务端裁剪，不是"前端不渲染"）
        // 身份 / 联系方式 / 参保 / 账户人人可见（收费窗口要核对身份、核对余额）；
        // 「自述既往史/过敏史」与四张临床史（过敏/既往/手术/家族）属临床内容，
        // 没有对应临床读权限的岗位（收费 / 药房 / 前台导诊 / 医技）拿到的是空。
        // 前端会把这些行连同健康档案、就诊脉络两个 tab 一起收掉，
        // 所以不会出现"字段被清空后显示成 —，看着像该患者没有过敏史"这种误导。
        if (!canReadPatientClinical()) {
            detailVO.setAllergyHistory(null);
            detailVO.setMedicalHistory(null);
            detailVO.setAllergies(Collections.emptyList());
            detailVO.setPastDiseases(Collections.emptyList());
            detailVO.setSurgeryHistories(Collections.emptyList());
            detailVO.setFamilyHistories(Collections.emptyList());
            detailVO.setMedications(Collections.emptyList());
        }

        maskDetailSensitiveFields(detailVO);
        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientVO upsertPatient(PatientUpsertDTO upsertDTO) {
        BizPatient patient = new BizPatient();
        BeanUtils.copyProperties(upsertDTO, patient);
        if (upsertDTO.getId() == null) {
            PatientProfileValidator.validateForCreate(patient);
            fillBirthDateAndAge(patient);
            if (!addPatient(patient)) {
                throw new BusinessException("新增失败");
            }
            fieldChangeFacadeService.record(PATIENT, patient.getId(), patient.getPatientNo(),
                    patient.getPatientName(), null, patient, PATIENT_FIELDS);
        } else {
            PatientProfileValidator.validateForUpdate(patient);
            fillBirthDateAndAge(patient);
            BizPatient before = this.getById(upsertDTO.getId());
            if (!this.updateById(patient)) {
                throw new BusinessException("修改失败");
            }
            patientHealthProfileService.syncAfterPatientSave(patient);
            BizPatient after = this.getById(upsertDTO.getId());
            if (after != null) {
                fieldChangeFacadeService.record(PATIENT, after.getId(), after.getPatientNo(),
                        after.getPatientName(), before, after, PATIENT_FIELDS);
            }
        }
        PatientVO vo = new PatientVO();
        BeanUtils.copyProperties(patient, vo);
        return vo;
    }

    @Override
    public void removePatient(Long patientId) {
        if (!this.removeById(patientId)) {
            throw new BusinessException("删除失败");
        }
    }

    /**
     * 把今日就诊映射折算成排序权重：我的今日（本人/本科室）排最前，其余今日次之。
     */
    private PatientSearchScopeDTO buildSearchScope(Map<Long, TodayVisitVO> todayVisits) {
        PatientSearchScopeDTO scope = new PatientSearchScopeDTO();
        scope.setTodayIds(new ArrayList<>(todayVisits.keySet()));
        scope.setMyTodayIds(todayVisits.values().stream()
                .filter(v -> Boolean.TRUE.equals(v.getMine()))
                .map(TodayVisitVO::getPatientId)
                .collect(Collectors.toList()));
        return scope;
    }

    /**
     * 取「今日就诊」映射（一次调用覆盖当前页所有患者，避免逐条查询退化成 N+1）。
     *
     * <p>失败一律降级成空映射：拿不到今日就诊只是「不置顶、不标注」，
     * 患者搜索本身必须照常可用 —— 不能因为就诊域出问题就让全院搜不到人。
     */
    private Map<Long, TodayVisitVO> loadTodayVisits() {
        try {
            Map<Long, TodayVisitVO> visits = registFacadeService.todayVisits();
            return visits != null ? visits : Collections.emptyMap();
        } catch (Exception e) {
            log.warn("今日就诊信息获取失败，本次患者搜索按普通模式返回（不置顶/不标注）：{}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 回填今日就诊字段。今日无门诊就诊的患者**不写任何占位文案** ——
     * 「未判定 ≠ 正常」，留空由前端归入「全院档案」组，而不是显示「无就诊」。
     */
    private void fillTodayVisit(List<PatientVO> voList, Map<Long, TodayVisitVO> todayVisits) {
        if (voList.isEmpty() || todayVisits.isEmpty()) {
            return;
        }
        for (PatientVO vo : voList) {
            if (vo.getId() == null) {
                continue;
            }
            TodayVisitVO visit = todayVisits.get(vo.getId());
            if (visit == null) {
                continue;
            }
            vo.setTodayVisitStatus(visit.getStatus());
            vo.setTodayVisitText(visit.getStatusText());
            vo.setTodayVisitDept(visit.getDeptName());
            vo.setTodayVisitDoctor(visit.getDoctorName());
            vo.setTodayVisitQueueNo(visit.getQueueNo());
            vo.setTodayVisitMine(visit.getMine());
        }
    }

    /**
     * 列表出参加工：身份证 / 医保卡号 / 联系电话一律服务端脱敏，明文字段置 null。
     *
     * <p>建档时间与首次/最近就诊冗余组都是患者主档自身的列，由 BeanUtils 随实体带出，
     * 无需补查 —— 就诊字段在「挂号单结诊」时由就诊域回写
     * （见 his-appoint PatientVisitSummaryUpdater）。
     *
     * <p>置 null 是安全的 —— 编辑弹窗打开时会单独调 getById 拿全量，
     * 且 MP updateById 默认跳过 null 字段，前端把整行回传也不会把真实值洗掉。
     */
    private void maskListSensitiveFields(List<PatientVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        for (PatientVO vo : voList) {
            vo.setMedicalInsuranceNoMasked(SensitiveMaskUtil.maskCardNo(vo.getMedicalInsuranceNo()));
            vo.setMedicalInsuranceNo(null);
            vo.setPhoneMasked(maskPhone(vo.getPhone()));
            vo.setPhone(null);
            vo.setIdCardMasked(SensitiveMaskUtil.maskIdCard(vo.getIdCard()));
            vo.setIdCard(null);
        }
    }

    /**
     * 批量回填「患者标签」与「预约次数」。
     *
     * <p>必须批量查询：列表页每页最多几十条，若逐条查标签/计数会退化成 N+1
     * （全局患者搜索框每次输入都触发，N+1 会被放大成网络请求风暴）。
     */
    private void fillTagsAndAppointCount(List<PatientVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }
        List<Long> patientIds = voList.stream().map(PatientVO::getId).collect(Collectors.toList());

        // 1) 标签：患者-标签关系 → 标签字典
        Map<Long, List<SysPatientTagVO>> tagsMap = bizPatientTagRelationService.mapTagsByPatientIds(patientIds);

        // 2) 预约（挂号）次数
        Map<Long, Integer> appointCountMap = mapAppointCount(patientIds);

        for (PatientVO vo : voList) {
            vo.setTags(tagsMap.getOrDefault(vo.getId(), List.of()));
            vo.setAppointCount(appointCountMap.getOrDefault(vo.getId(), 0));
        }
    }

    /**
     * 患者档案（纯展示）出参脱敏：身份证 / 电话 / 紧急联系人电话 / 证件号 / 医保卡号。
     *
     * <p>只作用于详情出参。<b>编辑回显走 {@link #getPatientVOById(Long)}，那里必须留明文</b> ——
     * 表单是「回填 → 整对象 upsert」，存进去的星号会把真号洗掉（不可逆数据损坏）。
     */
    private void maskDetailSensitiveFields(PatientDetailVO vo) {
        vo.setIdCardMasked(SensitiveMaskUtil.maskIdCard(vo.getIdCard()));
        vo.setIdCard(null);
        vo.setPhoneMasked(maskPhone(vo.getPhone()));
        vo.setPhone(null);
        vo.setContactPhoneMasked(maskPhone(vo.getContactPhone()));
        vo.setContactPhone(null);
        vo.setCardNoMasked(SensitiveMaskUtil.maskMiddle(vo.getCardNo(), 2, 2));
        vo.setCardNo(null);
        vo.setMedicalInsuranceNoMasked(SensitiveMaskUtil.maskCardNo(vo.getMedicalInsuranceNo()));
        vo.setMedicalInsuranceNo(null);
    }

    /**
     * 当前登录角色能不能看这个患者的**临床内容**。
     *
     * <p>口径固定为 {@code patient:cdr:list} —— 与临床数据接口上的鉴权、
     * 前端 canViewClinical 用的是**同一个码**。一个概念只用一个码，避免"前端按 A 藏、后端按 B 拦"
     * 这种错配（那必然出现看得见点进去 403，或者藏起来了接口却敞着）。
     *
     * <p>权限集合已由 JwtAuthenticationFilter 按 token 里的 currentRole 收敛，
     * 所以多角色账号切到收费员后这里会返回 false（这正是要拦的场景）。
     */
    private boolean canReadPatientClinical() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            return false;
        }
        List<String> permissions = user.getPermissions();
        return permissions != null && permissions.contains("patient:cdr:list");
    }

    /**
     * 补齐出生日期与年龄。
     *
     * <p>出生日期选填、身份证必填 —— 身份证里就带着出生日期，不补的话：列表上的年龄是空的
     * （年龄由出生日期算），EMPI 的「同名+同性别+同出生日期 = L2」这一档也永远命不中。
     * 同时注意**新增分支原先根本没算 age**（只有修改分支算），所以新建的患者年龄要等
     * 下次编辑才出现 —— 现在两个分支走同一段逻辑。
     */
    private void fillBirthDateAndAge(BizPatient patient) {
        if (patient.getBirthDate() == null) {
            patient.setBirthDate(PatientProfileValidator.birthDateOfIdCard(patient.getIdCard()));
        }
        if (patient.getBirthDate() != null) {
            patient.setAge(Period.between(patient.getBirthDate(), LocalDate.now()).getYears());
        }
    }

    /**
     * 患者自助注册（小程序端）。
     *
     * <p>同时完成两件事：① 建一份患者主档；② 开通一个患者类型的登录账号
     * （username=手机号，password=BCrypt 加密，绑定患者 id）。
     * 登录时 {@code UserDetailsServiceImpl} 对患者账号统一授予 PATIENT 角色，无需额外角色关联。
     *
     * <p>身份证号作唯一性判定：已建档的身份证直接提示去登录，避免重复主档。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientRegisterVO register(PatientRegisterDTO dto) {
        if (dto.getIdCard() == null || !dto.getIdCard().matches("^\\d{17}[\\dXx]$")) {
            throw new BusinessException("身份证号格式不正确");
        }
        if (dto.getPhone() == null || !dto.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        // 口令在客户端已用 SM2 公钥加密，先还原再验长度；明文一律拒收（与 /auth/login 同一口径）
        String rawPassword = passwordCipherService.decrypt(dto.getPassword());
        if (rawPassword.length() < 6) {
            throw new BusinessException("密码至少 6 位");
        }

        // 身份证已建档 → 直接提示登录
        LambdaQueryWrapper<BizPatient> exist = new LambdaQueryWrapper<>();
        exist.eq(BizPatient::getIdCard, dto.getIdCard().toUpperCase()).last("LIMIT 1");
        if (this.count(exist) > 0) {
            throw new BusinessException("该身份证已建档，请直接登录");
        }
        // 手机号账号唯一
        if (systemFacadeService.accountNameExists(dto.getPhone())) {
            throw new BusinessException("该手机号已注册");
        }

        // 手机号所有权必须先证明，否则任何人都能拿别人的身份证号建档开账号。
        // 放在重档检查之后：验证码一次一用，别让它白烧在「已建档」这种提示上。
        String smsError = smsCodeService.verify(dto.getPhone(), SmsCodeService.SCENE_REGISTER, dto.getSmsCode());
        if (smsError != null) {
            throw new BusinessException(smsError);
        }

        // 1) 患者主档
        BizPatient patient = new BizPatient();
        patient.setPatientName(dto.getPatientName());
        patient.setGender(dto.getGender());
        patient.setIdCard(dto.getIdCard().toUpperCase());
        patient.setPhone(dto.getPhone());
        patient.setPatientNo(redisSequenceService.generatePatientNo());
        patient.setMedicalInsuranceType(MedicalInsuranceTypeEnum.SELF_PAY.getName()); // 小程序自助建档默认自费
        patient.setStatus(1);
        LocalDate birth = parseBirthDate(dto.getIdCard());
        if (birth != null) {
            patient.setBirthDate(birth);
            patient.setAge(Period.between(birth, LocalDate.now()).getYears());
        }
        patient.setCreateBy("mini-register");
        patient.setCreateTime(LocalDateTime.now());
        this.save(patient);

        // 2) 登录账号（患者类型）
        PatientAccountCreateDTO account = new PatientAccountCreateDTO();
        account.setUserName(dto.getPhone());
        account.setPassword(rawPassword);
        account.setRealName(dto.getPatientName());
        account.setPatientId(patient.getId());
        Long userId = systemFacadeService.createPatientAccount(account);

        PatientRegisterVO vo = new PatientRegisterVO();
        vo.setPatientId(patient.getId());
        vo.setUserId(userId);
        vo.setPatientNo(patient.getPatientNo());
        vo.setUsername(dto.getPhone());
        return vo;
    }

    @Override
    public void markLastVisit(Long patientId, LocalDateTime visitTime, Long deptId, String deptName,
                              Long doctorId, String doctorName) {
        baseMapper.markLastVisit(patientId, visitTime, deptId, deptName, doctorId, doctorName);
    }

    @Override
    public void markFirstVisit(Long patientId, LocalDateTime visitTime, Long deptId, String deptName,
                               Long doctorId, String doctorName) {
        baseMapper.markFirstVisit(patientId, visitTime, deptId, deptName, doctorId, doctorName);
    }

}