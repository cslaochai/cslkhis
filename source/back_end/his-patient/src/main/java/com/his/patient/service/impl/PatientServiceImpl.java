package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.base.RedisSequenceService;
import com.his.common.exception.BusinessException;
import com.his.common.support.SensitiveMaskUtils;
import com.his.security.PasswordCipher;
import com.his.patient.dto.PatientQueryPageDTO;
import com.his.patient.dto.PatientRegisterDTO;
import com.his.patient.dto.PatientSearchScopeDTO;
import com.his.patient.dto.PatientUpsertDTO;
import com.his.patient.entity.BizPatient;
import com.his.patient.entity.BizPatientTagRelation;
import com.his.patient.mapper.BizPatientMapper;
import com.his.patient.mapper.BizPatientTagRelationMapper;
import com.his.patient.service.PatientHealthProfileService;
import com.his.patient.service.PatientService;
import com.his.patient.service.PatientTodayVisit;
import com.his.patient.service.PatientTodayVisitProvider;
import com.his.patient.support.PatientProfileValidator;
import com.his.patient.support.PatientSearchScopeMode;
import com.his.patient.support.PatientSearchScopeResolver;
import com.his.patient.vo.PatientDetailVO;
import com.his.patient.vo.PatientHealthProfileVO;
import com.his.patient.vo.PatientRegisterVO;
import com.his.patient.vo.PatientVO;
import com.his.patient.service.BizPatientTagRelationService;
import com.his.security.entity.CurrentUser;
import com.his.security.UserUtils;
import com.his.system.entity.SysUser;
import com.his.system.service.SysUserService;
import com.his.system.entity.SysPatientTag;
import com.his.system.service.PatientTagService;
import com.his.system.vo.SysPatientTagVO;
import com.his.system.support.CodeText;
import com.his.system.support.FieldChangeRecorder;
import com.his.system.support.FieldSpec;
import com.his.system.support.Mask;
import com.his.system.service.SmsCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 患者服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PatientServiceImpl extends ServiceImpl<BizPatientMapper, BizPatient> implements PatientService {

    private final RedisSequenceService redisSequenceService;
    private final BizPatientTagRelationMapper tagRelationMapper;
    private final PatientTagService patientTagService;
    /**
     * 健康档案六组的唯一写入口。建档时把主档的「过敏史 / 既往病史 / 联系人」文本
     * 落成结构化明细并回算投影，见 {@code syncAfterPatientSave}。
     */
    private final PatientHealthProfileService healthProfileService;
    /**
     * 患者自助注册写登录账号用：建档后同步开通患者类型的登录账号。
     */
    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    /**
     * 注册口令传输加密：前端提交的是 SM2 密文（与登录同一对公钥），这里还原成明文再交 BCrypt 落库。
     * 注册是初始口令第一次上网的场合，和登录一样不能收明文 —— 留明文口子等于登录加密白做。
     */
    private final PasswordCipher passwordCipher;
    /**
     * 注册验证码校验：手机号所有权由它证明，未通过则不建档、不开账号。
     */
    private final SmsCodeService smsCodeService;
    /**
     * 患者-标签关系的读取归它（标签口径只允许有一处，见该服务注释）。
     */
    private final BizPatientTagRelationService tagRelationService;
    /**
     * 「今日就诊」提供者（实现在就诊域 his-appoint）。
     *
     * <p>用 ObjectProvider 而非直接注入：接口定义在本模块、实现放在依赖本模块的就诊域，
     * 直接注入会形成编译期反向依赖；同时就诊域缺席时这里拿到 null，搜索照常可用（降级不阻断）。
     */
    private final ObjectProvider<PatientTodayVisitProvider> todayVisitProvider;
    /**
     * 「要不要看今日就诊」的判定（按当前登录角色的岗位性质）。
     *
     * <p>身份由它自己从 SecurityContext 取，调用方不传任何参数 ——
     * 不允许调用方声明自己的搜索范围。
     */
    private final PatientSearchScopeResolver scopeResolver;
    /**
     * 字段级修改日志落库器。
     *
     * <p>患者主档是 HIS 里被改得最频繁、也最该被追责的对象 —— 改姓名、改身份证、
     * 改过敏史都会直接影响后续诊疗。这里把「改之前是啥、改之后是啥」钉进表里。
     */
    private final FieldChangeRecorder fieldChangeRecorder;

    /** 对象类型：患者主档 */
    private static final String PATIENT = "PATIENT";

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
    private static final List<FieldSpec> PATIENT_FIELDS = FieldSpec.list(
            FieldSpec.of("patientName", "姓名"),
            FieldSpec.render("gender", "性别", v -> CodeText.of(v, "男", "女", "未知")),
            FieldSpec.of("birthDate", "出生日期"),
            FieldSpec.masked("idCard", "身份证号", Mask.ID_CARD),
            FieldSpec.masked("phone", "联系电话", Mask.PHONE),
            FieldSpec.of("contactName", "紧急联系人"),
            FieldSpec.masked("contactPhone", "联系人电话", Mask.PHONE),
            FieldSpec.of("contactRelation", "与患者关系"),
            FieldSpec.masked("address", "家庭住址", Mask.ADDRESS),
            FieldSpec.of("nation", "民族"),
            FieldSpec.of("occupation", "职业"),
            FieldSpec.render("maritalStatus", "婚姻状况", v -> CodeText.of(v, "未婚", "已婚", "离异", "丧偶")),
            FieldSpec.of("bloodType", "血型"),
            FieldSpec.of("allergyHistory", "过敏史"),
            FieldSpec.of("medicalHistory", "既往病史"),
            FieldSpec.render("patientType", "患者类型",
                    v -> CodeText.of(v, "自费", "城镇职工医保", "城乡居民医保", "公费", "其他")),
            FieldSpec.masked("medicalInsuranceNo", "医保卡号", Mask.BANK_NO),
            FieldSpec.of("medicalInsuranceType", "医保类型"),
            FieldSpec.render("cardType", "证件类型", v -> CodeText.of(v, "身份证", "护照", "军官证")),
            FieldSpec.masked("cardNo", "证件号码", Mask.BANK_NO),
            FieldSpec.render("status", "状态", CodeText::enable)
    );

    @Override
    public PageResult<BizPatient> selectPatientPage(String patientName, String phone, String patientNo,
                                                    Integer patientType, String keyword, int pageNum, int pageSize) {
        return selectPatientPage(patientName, phone, patientNo, patientType, keyword, pageNum, pageSize, null, null);
    }

    @Override
    public PageResult<BizPatient> selectPatientPage(String patientName, String phone, String patientNo,
                                                    Integer patientType, String keyword, int pageNum, int pageSize,
                                                    List<Long> tagPatientIds) {
        return selectPatientPage(patientName, phone, patientNo, patientType, keyword, pageNum, pageSize,
                tagPatientIds, null);
    }

    @Override
    public PageResult<BizPatient> selectPatientPage(String patientName, String phone, String patientNo,
                                                    Integer patientType, String keyword, int pageNum, int pageSize,
                                                    List<Long> tagPatientIds, PatientSearchScopeDTO scope) {
        LambdaQueryWrapper<BizPatient> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(patientName), BizPatient::getPatientName, patientName)
                .like(StringUtils.hasText(phone), BizPatient::getPhone, phone)
                .like(StringUtils.hasText(patientNo), BizPatient::getPatientNo, patientNo)
                .eq(patientType != null, BizPatient::getPatientType, patientType)
                .in(tagPatientIds != null && !tagPatientIds.isEmpty(), BizPatient::getId, tagPatientIds);
        // 综合关键字：姓名 / 患者号 / 手机号 / 身份证号四者 OR，必须整体括号包裹，
        // 否则会把前面精确条件的 AND 关系吃掉（OR 优先级低于 AND）
        if (StringUtils.hasText(keyword)) {
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

    /** 拼 id 列表：来源是 Long 主键，非用户输入，无注入面 */
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
        // 【不能依赖 getOne 的默认行为】库里一旦已存在同身份证的多份档案（历史多来源导入的遗留，
        // 正是患者主索引要治理的对象），getOne 默认 throwEx=true 会抛 TooManyResultsException，
        // 于是**建档入口返回 500「系统内部错误」** —— 一个数据问题被伪装成系统故障，
        // 而且连"该身份证已存在"这句该给的提示都发不出来。
        // 建档防重的语义只是"存在就拒"，存在几条不影响结论，所以取第一条即可，
        // 多条的情况记 warn 指向 EMPI 去合并。
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
        // 写入口校验（姓名/性别/身份证必填，身份证校验位，手机号格式）——
        // 放在 service 而不是只在 controller：addPatient 是公开方法，谁能调谁就得受约束
        PatientProfileValidator.validateForCreate(patient);
        // 检查身份证号是否已存在
        if (StringUtils.hasText(patient.getIdCard())) {
            BizPatient existing = selectByIdCard(patient.getIdCard());
            if (existing != null) {
                throw new BusinessException("该身份证号已存在患者记录");
            }
        }
        patient.setPatientNo(redisSequenceService.generatePatientNo());
        // 计算年龄
        if (patient.getBirthDate() != null) {
            patient.setAge(Period.between(patient.getBirthDate(), LocalDate.now()).getYears());
        }

        patient.setStatus(1);
        boolean result = this.save(patient);
        if (!result) {
            return result;
        }
        // 新增患者成功后同步健康档案：主档表单里的「过敏史 / 既往病史 / 联系人」是快速录入入口，
        // 由 healthProfileService 落成结构化明细行，并回算主档文本投影（口径见该服务注释）。
        //
        // 这里**删掉了原来那段直接插患者联系方式的代码**：它把
        // `patient.getContactRelation()`（"配偶" 这样的文本）塞进 `relationship`
        //（tinyint 码值列），MySQL 隐式转换后静默存成 0 —— 页面上关系显示成 0，
        // 且不抛任何异常。现在 relationship 是 Integer，类型上就写不进去了。
        healthProfileService.syncAfterPatientSave(patient);

        // 自动打静态标签（根据年龄判断）
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

        // 查询预置的静态标签
        LambdaQueryWrapper<SysPatientTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.in(SysPatientTag::getTagName, "老年", "儿童", "幼儿");
        List<SysPatientTag> staticTags = patientTagService.list(tagWrapper);

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
        if (tagRelationMapper.selectCount(checkWrapper) == 0) {
            BizPatientTagRelation relation = new BizPatientTagRelation();
            relation.setPatientId(patientId);
            relation.setTagId(tagId);
            relation.setSourceType(2); // 系统自动打标
            tagRelationMapper.insert(relation);
        }
    }

    @Override
    public Map<Long, Integer> mapAppointCount(List<Long> patientIds) {
        Map<Long, Integer> appointCountMap = new HashMap<>();
        if (patientIds == null || patientIds.isEmpty()) {
            return appointCountMap;
        }
        for (Map<String, Object> row : baseMapper.countRegistByPatientIds(patientIds)) {
            Object pid = row.get("patientId");
            Object cnt = row.get("cnt");
            if (pid != null && cnt != null) {
                appointCountMap.put(Long.valueOf(pid.toString()), Integer.valueOf(cnt.toString()));
            }
        }
        return appointCountMap;
    }

    @Override
    public PageResult<PatientVO> queryPatientPage(PatientQueryPageDTO queryDTO) {
        // 如果有标签过滤，先查询有该标签的患者ID列表
        List<Long> tagPatientIds = null;
        if (queryDTO.getTagId() != null) {
            tagPatientIds = tagRelationService.listPatientIdsByTagId(queryDTO.getTagId());
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
        PatientSearchScopeMode scopeMode = scopeResolver.resolve();
        Map<Long, PatientTodayVisit> todayVisits = scopeMode == PatientSearchScopeMode.TODAY_FIRST
                ? loadTodayVisits()
                : Collections.emptyMap();
        PatientSearchScopeDTO scope = buildSearchScope(todayVisits);

        PageResult<BizPatient> pageResult = selectPatientPage(
                queryDTO.getPatientName(), queryDTO.getPhone(), queryDTO.getPatientNo(),
                queryDTO.getPatientType(), queryDTO.getKeyword(),
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
        PatientHealthProfileVO profile = healthProfileService.getProfile(patientId);
        detailVO.setAllergies(profile.getAllergies());
        detailVO.setPastDiseases(profile.getPastDiseases());
        detailVO.setSurgeryHistories(profile.getSurgeryHistories());
        detailVO.setFamilyHistories(profile.getFamilyHistories());
        detailVO.setMedications(profile.getMedications());
        detailVO.setContacts(profile.getContacts());

        // 患者标签：运营标记（VIP / 高血压 / 建档提醒…），与列表页同一实现、同一口径，
        // 且**不参与下面的临床裁剪** —— 收费窗口认 VIP、护士看注意事项都要用它。
        detailVO.setTags(tagRelationService.mapTagsByPatientIds(List.of(patientId))
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
            // 用药史属临床内容，同样裁剪。**联系人**不裁：收费窗口要照联系人打电话催费，
            // 而且联系人不含诊断信息 —— 裁剪它会连带把「谁来接这个病人」也藏掉。
            detailVO.setMedications(Collections.emptyList());
        }

        // 展示型出参：敏感号码在这里就遮掉，明文字段置 null（与列表同一批工具、同一口径）。
        // 前端只负责渲染后端给的值 —— 在前端遮等于明文仍在响应体里，抓包/日志/第二个调用方全漏。
        maskDetailSensitiveFields(detailVO);

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PatientVO upsertPatient(PatientUpsertDTO patientUpsertDTO) {
        BizPatient patient = new BizPatient();
        BeanUtils.copyProperties(patientUpsertDTO, patient);
        if (patientUpsertDTO.getId() == null) {
            // 新增：姓名 / 性别 / 身份证必填，身份证要过校验位（addPatient 内还会再跑一次）
            PatientProfileValidator.validateForCreate(patient);
            fillBirthDateAndAge(patient);
            if (!addPatient(patient)) {
                throw new BusinessException("新增失败");
            }
            // 建档留痕：只记建档时填了值的字段（old 传 null → 变更类型按新增渲染）。
            fieldChangeRecorder.record(PATIENT, patient.getId(), patient.getPatientNo(),
                    patient.getPatientName(), null, patient, PATIENT_FIELDS);
        } else {
            // 修改：身份证放宽为只验格式，理由见 PatientProfileValidator
            PatientProfileValidator.validateForUpdate(patient);
            fillBirthDateAndAge(patient);
            // 改之前先取快照 —— 少了这一步就永远答不出"改之前是什么"
            BizPatient before = this.getById(patientUpsertDTO.getId());
            if (!this.updateById(patient)) {
                throw new BusinessException("修改失败");
            }
            // 主档表单里的「过敏史 / 既往病史 / 联系人」是快速录入入口：
            // 该组还没有明细时落成明细，已有明细时按明细回算文本（口径见 PatientHealthProfileService）。
            // 不跑这一步的话，用户在主档里改完过敏史、健康档案页纹丝不动，两处又开始各说各话。
            healthProfileService.syncAfterPatientSave(patient);
            // 拿落库后的真实值比对，而不是拿 DTO 比：DTO 里 null 的字段 updateById 根本不会写，
            // 拿 DTO 比会把"这次没传"误记成"被清空了"；syncAfterPatientSave 的回算也只有落库后才看得见。
            BizPatient after = this.getById(patientUpsertDTO.getId());
            if (after != null) {
                fieldChangeRecorder.record(PATIENT, after.getId(), after.getPatientNo(),
                        after.getPatientName(), before, after, PATIENT_FIELDS);
            }
        }
        // 回传落库后的患者信息（前端新增患者后需立即拿到 id / patientNo 用于后续挂号）
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
    private PatientSearchScopeDTO buildSearchScope(Map<Long, PatientTodayVisit> todayVisits) {
        PatientSearchScopeDTO scope = new PatientSearchScopeDTO();
        scope.setTodayIds(new ArrayList<>(todayVisits.keySet()));
        scope.setMyTodayIds(todayVisits.values().stream()
                .filter(v -> Boolean.TRUE.equals(v.getMine()))
                .map(PatientTodayVisit::getPatientId)
                .collect(Collectors.toList()));
        return scope;
    }

    /**
     * 取「今日就诊」映射（一次调用覆盖当前页所有患者，避免逐条查询退化成 N+1）。
     *
     * <p>失败一律降级成空映射：拿不到今日就诊只是「不置顶、不标注」，
     * 患者搜索本身必须照常可用 —— 不能因为就诊域出问题就让全院搜不到人。
     */
    private Map<Long, PatientTodayVisit> loadTodayVisits() {
        PatientTodayVisitProvider provider = todayVisitProvider.getIfAvailable();
        if (provider == null) {
            return Collections.emptyMap();
        }
        try {
            Map<Long, PatientTodayVisit> visits = provider.todayVisits();
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
    private void fillTodayVisit(List<PatientVO> voList, Map<Long, PatientTodayVisit> todayVisits) {
        if (voList.isEmpty() || todayVisits.isEmpty()) {
            return;
        }
        for (PatientVO vo : voList) {
            if (vo.getId() == null) {
                continue;
            }
            PatientTodayVisit visit = todayVisits.get(vo.getId());
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
            vo.setMedicalInsuranceNoMasked(SensitiveMaskUtils.maskCardNo(vo.getMedicalInsuranceNo()));
            vo.setMedicalInsuranceNo(null);
            vo.setPhoneMasked(maskPhone(vo.getPhone()));
            vo.setPhone(null);
            vo.setIdCardMasked(SensitiveMaskUtils.maskIdCard(vo.getIdCard()));
            vo.setIdCard(null);
        }
    }

    /**
     * 联系电话脱敏：11 位手机号保留前 3 后 4（138****5678）；
     * 其他长度（座机/历史脏数据）≥7 位保留前 2 后 2，更短全遮。
     */
    private static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
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
        Map<Long, List<SysPatientTagVO>> tagsMap = tagRelationService.mapTagsByPatientIds(patientIds);

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
        vo.setIdCardMasked(SensitiveMaskUtils.maskIdCard(vo.getIdCard()));
        vo.setIdCard(null);
        vo.setPhoneMasked(maskPhone(vo.getPhone()));
        vo.setPhone(null);
        vo.setContactPhoneMasked(maskPhone(vo.getContactPhone()));
        vo.setContactPhone(null);
        vo.setCardNoMasked(SensitiveMaskUtils.maskMiddle(vo.getCardNo(), 2, 2));
        vo.setCardNo(null);
        vo.setMedicalInsuranceNoMasked(SensitiveMaskUtils.maskCardNo(vo.getMedicalInsuranceNo()));
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
        if (dto.getPatientName() == null || dto.getPatientName().isBlank()) {
            throw new BusinessException("请输入姓名");
        }
        if (dto.getIdCard() == null || !dto.getIdCard().matches("^\\d{17}[\\dXx]$")) {
            throw new BusinessException("身份证号格式不正确");
        }
        if (dto.getPhone() == null || !dto.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        // 口令在客户端已用 SM2 公钥加密，先还原再验长度；明文一律拒收（与 /auth/login 同一口径）
        String rawPassword = passwordCipher.decrypt(dto.getPassword());
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
        LambdaQueryWrapper<SysUser> uExist = new LambdaQueryWrapper<>();
        uExist.eq(SysUser::getUserName, dto.getPhone()).last("LIMIT 1");
        if (sysUserService.count(uExist) > 0) {
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
        patient.setPatientNo("P" + System.currentTimeMillis());
        patient.setPatientType(1); // 1-自费
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
        SysUser user = new SysUser();
        user.setUserName(dto.getPhone());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRealName(dto.getPatientName());
        user.setUserType(3);
        user.setPatientId(patient.getId());
        user.setStatus(1);
        user.setCreateBy("mini-register");
        user.setCreateTime(LocalDateTime.now());
        sysUserService.save(user);

        PatientRegisterVO vo = new PatientRegisterVO();
        vo.setPatientId(patient.getId());
        vo.setUserId(user.getId());
        vo.setPatientNo(patient.getPatientNo());
        vo.setUsername(user.getUserName());
        return vo;
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

}
