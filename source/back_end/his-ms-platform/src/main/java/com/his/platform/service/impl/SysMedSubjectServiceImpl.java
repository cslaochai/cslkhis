package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.platform.dto.DeptSubjectItemDTO;
import com.his.platform.dto.DeptSubjectMapUpsertDTO;
import com.his.platform.dto.MedSubjectUpsertDTO;
import com.his.platform.entity.SysDepartment;
import com.his.platform.entity.SysDeptSubjectMap;
import com.his.platform.entity.SysMedSubject;
import com.his.platform.mapper.SysDepartmentMapper;
import com.his.platform.mapper.SysDeptSubjectMapMapper;
import com.his.platform.mapper.SysMedSubjectMapper;
import com.his.platform.service.SysMedSubjectService;
import com.his.platform.vo.DeptSubjectItemVO;
import com.his.platform.vo.DeptSubjectVO;
import com.his.platform.vo.MedSubjectVO;
import com.his.spi.service.MedSubjectFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 诊疗科目与科室映射服务实现。
 *
 * <p>同时实现 {@link com.his.spi.service.MedSubjectFacadeService}：住院/收费等模块要取
 * 「科室的标准诊疗科目码」时走 SPI 端口，不能直接查本模块的表。
 */
@Service
@RequiredArgsConstructor
public class SysMedSubjectServiceImpl extends ServiceImpl<SysMedSubjectMapper, SysMedSubject>
        implements SysMedSubjectService, MedSubjectFacadeService {

    /**
     * 一级科目代码：两位数字（01 ~ 99）
     */
    private static final Pattern CODE_LEVEL_1 = Pattern.compile("^\\d{2}$");

    /**
     * 二级科目代码：一级代码 + 点 + 两位序号（如 03.01）
     */
    private static final Pattern CODE_LEVEL_2 = Pattern.compile("^\\d{2}\\.\\d{2}$");

    /**
     * 临床科室口径：门诊(1) / 住院(4) / 急诊(6) 任一命中即算
     */
    private static final String[] CLINICAL_DEPT_TYPES = {"1", "4", "6"};

    private final SysMedSubjectMapper sysMedSubjectMapper;
    private final SysDeptSubjectMapMapper sysDeptSubjectMapMapper;
    private final SysDepartmentMapper sysDepartmentMapper;

    @Override
    public List<MedSubjectVO> listTree() {
        List<SysMedSubject> all = sysMedSubjectMapper.selectList(new LambdaQueryWrapper<SysMedSubject>()
                .orderByAsc(SysMedSubject::getSortOrder)
                .orderByAsc(SysMedSubject::getSubjectCode));
        Map<String, Integer> countMap = deptCountMap();

        // 二级科目按 parentCode 归组，再挂到一级下面；一级科目里没有对应二级的就是空 children
        Map<String, List<MedSubjectVO>> childrenMap = new LinkedHashMap<>();
        List<MedSubjectVO> roots = new ArrayList<>();
        for (SysMedSubject subject : all) {
            MedSubjectVO vo = toSubjectVO(subject, countMap);
            if (Integer.valueOf(2).equals(subject.getSubjectLevel())) {
                childrenMap.computeIfAbsent(subject.getParentCode(), k -> new ArrayList<>()).add(vo);
            } else {
                roots.add(vo);
            }
        }
        for (MedSubjectVO root : roots) {
            root.setChildren(childrenMap.getOrDefault(root.getSubjectCode(), Collections.emptyList()));
        }
        return roots;
    }

    @Override
    public String upsertSubject(MedSubjectUpsertDTO upsertDTO) {
        String code = upsertDTO.getSubjectCode() == null ? null : upsertDTO.getSubjectCode().trim();
        Integer level = upsertDTO.getSubjectLevel();

        // ---- 前置校验（全部在写库前）----
        String codeError = validateCode(code, level, upsertDTO.getParentCode());
        if (codeError != null) {
            throw new BusinessException(codeError);
        }
        if (codeExists(code, upsertDTO.getId())) {
            throw new BusinessException("科目代码已存在：" + code);
        }
        if (Integer.valueOf(2).equals(level)) {
            SysMedSubject parent = getByCode(upsertDTO.getParentCode());
            if (parent == null) {
                throw new BusinessException("上级科目不存在：" + upsertDTO.getParentCode());
            }
            if (!Integer.valueOf(1).equals(parent.getSubjectLevel())) {
                throw new BusinessException("二级科目只能挂在一级科目下：" + parent.getSubjectCode());
            }
            if (!code.startsWith(parent.getSubjectCode() + ".")) {
                throw new BusinessException("二级科目代码必须以「" + parent.getSubjectCode() + ".」开头：" + code);
            }
        }

        SysMedSubject subject = new SysMedSubject();
        BeanUtils.copyProperties(upsertDTO, subject);
        subject.setSubjectCode(code);
        subject.setParentCode(Integer.valueOf(2).equals(level) ? upsertDTO.getParentCode() : null);
        if (subject.getIsClinical() == null) {
            subject.setIsClinical(1);
        }
        if (subject.getStatus() == null) {
            subject.setStatus(1);
        }
        if (subject.getSortOrder() == null) {
            subject.setSortOrder(0);
        }

        if (upsertDTO.getId() == null) {
            sysMedSubjectMapper.insert(subject);
            return "新增成功";
        }
        if (sysMedSubjectMapper.selectById(upsertDTO.getId()) == null) {
            throw new BusinessException("诊疗科目不存在");
        }
        // 不允许把已经有二级科目的一级科目改成二级，否则那批二级会变成孤儿
        if (!Integer.valueOf(2).equals(level) && hasChildren(subject.getSubjectCode())) {
            throw new BusinessException("该科目下还有二级科目，不能改为二级科目");
        }
        sysMedSubjectMapper.updateById(subject);
        return "修改成功";
    }

    @Override
    public void deleteSubject(Long id) {
        SysMedSubject subject = sysMedSubjectMapper.selectById(id);
        if (subject == null) {
            throw new BusinessException("诊疗科目不存在");
        }
        if (hasChildren(subject.getSubjectCode())) {
            throw new BusinessException("该科目下还有二级科目，请先删除二级科目");
        }
        Long mapped = sysDeptSubjectMapMapper.selectCount(new LambdaQueryWrapper<SysDeptSubjectMap>()
                .eq(SysDeptSubjectMap::getSubjectCode, subject.getSubjectCode()));
        if (mapped != null && mapped > 0) {
            throw new BusinessException("该科目已被 " + mapped + " 个科室引用，请先解除科室映射");
        }
        // 物理删除：subject_code 上有唯一键，逻辑删除会让"删了再建同码"撞 Duplicate entry
        sysMedSubjectMapper.deleteById(id);
    }

    @Override
    public DeptSubjectVO getDeptMap(Long deptId) {
        SysDepartment dept = sysDepartmentMapper.selectById(deptId);
        if (dept == null) {
            throw new BusinessException("科室不存在");
        }
        List<SysDeptSubjectMap> rels = sysDeptSubjectMapMapper.selectList(
                new LambdaQueryWrapper<SysDeptSubjectMap>()
                        .eq(SysDeptSubjectMap::getDeptId, deptId)
                        .orderByDesc(SysDeptSubjectMap::getIsPrimary)
                        .orderByAsc(SysDeptSubjectMap::getSubjectCode));
        Map<String, SysMedSubject> subjectMap = subjectMapWithParents(
                rels.stream().map(SysDeptSubjectMap::getSubjectCode).collect(Collectors.toSet()));
        return toDeptSubjectVO(dept, rels, subjectMap, parentNameMap(Collections.singleton(dept)));
    }

    @Override
    public List<DeptSubjectVO> listDeptMapBySubject(String subjectCode) {
        List<SysDeptSubjectMap> rels = sysDeptSubjectMapMapper.selectList(
                new LambdaQueryWrapper<SysDeptSubjectMap>()
                        .eq(SysDeptSubjectMap::getSubjectCode, subjectCode)
                        .eq(SysDeptSubjectMap::getIsPrimary, 1));
        if (rels.isEmpty()) {
            return Collections.emptyList();
        }
        List<SysDepartment> depts = sysDepartmentMapper.selectBatchIds(
                rels.stream().map(SysDeptSubjectMap::getDeptId).collect(Collectors.toSet()));
        Map<Long, SysDepartment> deptMap = depts.stream()
                .collect(Collectors.toMap(SysDepartment::getId, d -> d, (a, b) -> a));
        Map<String, SysMedSubject> subjectMap = subjectMapWithParents(Collections.singleton(subjectCode));
        Map<Long, String> parentNames = parentNameMap(depts);
        return rels.stream()
                .map(rel -> toDeptSubjectVO(deptMap.get(rel.getDeptId()), Collections.singletonList(rel),
                        subjectMap, parentNames))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveDeptMap(DeptSubjectMapUpsertDTO upsertDTO) {
        Long deptId = upsertDTO.getDeptId();
        if (sysDepartmentMapper.selectById(deptId) == null) {
            throw new BusinessException("科室不存在");
        }

        // 同一科目只留一条；主科目最多一条
        LinkedHashMap<String, DeptSubjectItemDTO> items = new LinkedHashMap<>();
        List<DeptSubjectItemDTO> raw = upsertDTO.getItems() == null
                ? Collections.emptyList() : upsertDTO.getItems();
        for (DeptSubjectItemDTO item : raw) {
            if (item == null || !TextUtil.hasText(item.getSubjectCode())) {
                continue;
            }
            items.putIfAbsent(item.getSubjectCode().trim(), item);
        }
        if (items.isEmpty()) {
            sysDeptSubjectMapMapper.delete(new LambdaQueryWrapper<SysDeptSubjectMap>()
                    .eq(SysDeptSubjectMap::getDeptId, deptId));
            return "已清空该科室的诊疗科目";
        }

        // 科目必须真实存在且启用，否则映射会指向空气或指向停用科目
        Map<String, SysMedSubject> subjectMap = subjectMapByCodes(items.keySet());
        List<String> missing = items.keySet().stream()
                .filter(code -> subjectMap.get(code) == null)
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new BusinessException("诊疗科目不存在：" + String.join("、", missing));
        }
        List<String> disabled = items.keySet().stream()
                .filter(code -> Integer.valueOf(0).equals(subjectMap.get(code).getStatus()))
                .collect(Collectors.toList());
        if (!disabled.isEmpty()) {
            throw new BusinessException("诊疗科目已停用：" + String.join("、", disabled));
        }

        List<String> primaries = items.values().stream()
                .filter(i -> Integer.valueOf(1).equals(i.getIsPrimary()))
                .map(DeptSubjectItemDTO::getSubjectCode)
                .collect(Collectors.toList());
        if (primaries.size() > 1) {
            throw new BusinessException("一个科室只能有一个上报主科目，当前选了 " + primaries.size() + " 个");
        }
        // 一个都没指定主科目时，把第一条设为主：病案首页上报要求必填主科目，
        // 留空会导致上报端取不到值，这里替用户兜一手（页面上会明确显示"主"标记）
        String primaryCode = primaries.isEmpty() ? items.keySet().iterator().next() : primaries.get(0);

        sysDeptSubjectMapMapper.delete(new LambdaQueryWrapper<SysDeptSubjectMap>()
                .eq(SysDeptSubjectMap::getDeptId, deptId));
        for (String code : items.keySet()) {
            SysDeptSubjectMap rel = new SysDeptSubjectMap();
            rel.setDeptId(deptId);
            rel.setSubjectCode(code);
            rel.setIsPrimary(code.equals(primaryCode) ? 1 : 0);
            sysDeptSubjectMapMapper.insert(rel);
        }
        return "保存成功，共 " + items.size() + " 个科目";
    }

    @Override
    public List<DeptSubjectVO> listUnmappedDepts() {
        List<SysDepartment> all = sysDepartmentMapper.selectList(new LambdaQueryWrapper<SysDepartment>()
                .orderByAsc(SysDepartment::getDeptCode));
        if (all.isEmpty()) {
            return Collections.emptyList();
        }
        // 有下级的节点是分组目录（内科系统 / 外科系统 …），本身不参与上报，不该要求配科目
        Set<Long> parentIds = all.stream()
                .map(SysDepartment::getParentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> mappedDeptIds = sysDeptSubjectMapMapper.selectList(
                        new LambdaQueryWrapper<SysDeptSubjectMap>().select(SysDeptSubjectMap::getDeptId))
                .stream().map(SysDeptSubjectMap::getDeptId).collect(Collectors.toSet());

        Map<Long, String> parentNames = parentNameMap(all);
        return all.stream()
                .filter(d -> isClinical(d.getDeptType()))
                .filter(d -> !parentIds.contains(d.getId()))
                .filter(d -> !mappedDeptIds.contains(d.getId()))
                .map(d -> toDeptSubjectVO(d, Collections.emptyList(), Collections.emptyMap(), parentNames))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // MedSubjectFacadeService（跨模块出口）
    // ------------------------------------------------------------------

    @Override
    public String getPrimarySubjectCode(Long deptId) {
        if (deptId == null) {
            return null;
        }
        List<SysDeptSubjectMap> rels = sysDeptSubjectMapMapper.selectList(
                new LambdaQueryWrapper<SysDeptSubjectMap>()
                        .eq(SysDeptSubjectMap::getDeptId, deptId)
                        .eq(SysDeptSubjectMap::getIsPrimary, 1));
        // 唯一性由 saveDeptMap 保证，这里仍按集合取第一条：真出现脏数据也不要抛异常把上报打断
        return rels.isEmpty() ? null : rels.get(0).getSubjectCode();
    }

    @Override
    public Map<Long, String> getPrimarySubjectCodes(Collection<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysDeptSubjectMapMapper.selectList(new LambdaQueryWrapper<SysDeptSubjectMap>()
                        .in(SysDeptSubjectMap::getDeptId, deptIds)
                        .eq(SysDeptSubjectMap::getIsPrimary, 1)).stream()
                .collect(Collectors.toMap(SysDeptSubjectMap::getDeptId,
                        SysDeptSubjectMap::getSubjectCode, (a, b) -> a));
    }

    @Override
    public String getSubjectName(String subjectCode) {
        if (!TextUtil.hasText(subjectCode)) {
            return null;
        }
        SysMedSubject subject = getByCode(subjectCode);
        return subject == null ? null : subject.getSubjectName();
    }

    // ------------------------------------------------------------------
    // 内部方法
    // ------------------------------------------------------------------

    /**
     * 校验科目代码与层级是否自洽。返回 null 表示通过。
     */
    private String validateCode(String code, Integer level, String parentCode) {
        if (!TextUtil.hasText(code)) {
            return "科目代码不能为空";
        }
        if (Integer.valueOf(2).equals(level)) {
            if (!CODE_LEVEL_2.matcher(code).matches()) {
                return "二级科目代码格式应为「一级代码.两位序号」，如 03.01";
            }
            if (!TextUtil.hasText(parentCode)) {
                return "二级科目必须指定上级科目";
            }
            return null;
        }
        if (!CODE_LEVEL_1.matcher(code).matches()) {
            return "一级科目代码格式应为两位数字，如 03";
        }
        return null;
    }

    private boolean codeExists(String subjectCode, Long excludeId) {
        LambdaQueryWrapper<SysMedSubject> wrapper = new LambdaQueryWrapper<SysMedSubject>()
                .eq(SysMedSubject::getSubjectCode, subjectCode);
        if (excludeId != null) {
            wrapper.ne(SysMedSubject::getId, excludeId);
        }
        Long count = sysMedSubjectMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    private boolean hasChildren(String subjectCode) {
        Long count = sysMedSubjectMapper.selectCount(new LambdaQueryWrapper<SysMedSubject>()
                .eq(SysMedSubject::getParentCode, subjectCode));
        return count != null && count > 0;
    }

    private SysMedSubject getByCode(String subjectCode) {
        return sysMedSubjectMapper.selectOne(new LambdaQueryWrapper<SysMedSubject>()
                .eq(SysMedSubject::getSubjectCode, subjectCode));
    }

    private Map<String, SysMedSubject> subjectMapByCodes(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysMedSubjectMapper.selectList(new LambdaQueryWrapper<SysMedSubject>()
                        .in(SysMedSubject::getSubjectCode, codes)).stream()
                .collect(Collectors.toMap(SysMedSubject::getSubjectCode, s -> s, (a, b) -> a));
    }

    /**
     * 同 {@link #subjectMapByCodes}，但把上级科目一并带进来。
     *
     * <p>科室通常只映射二级科目（03.01），而列表要显示「内科 / 呼吸内科专业」，
     * 不带上父科目就得逐条回查（N+1）。两次查询换掉 N 次，值。
     */
    private Map<String, SysMedSubject> subjectMapWithParents(Collection<String> codes) {
        Map<String, SysMedSubject> map = new HashMap<>(subjectMapByCodes(codes));
        Set<String> missingParents = map.values().stream()
                .map(SysMedSubject::getParentCode)
                .filter(TextUtil::hasText)
                .filter(code -> !map.containsKey(code))
                .collect(Collectors.toSet());
        if (!missingParents.isEmpty()) {
            map.putAll(subjectMapByCodes(missingParents));
        }
        return map;
    }

    /**
     * 科目 code → 已映射科室数（只算 is_primary=1）。
     */
    private Map<String, Integer> deptCountMap() {
        List<Map<String, Object>> rows = sysDeptSubjectMapMapper.selectDeptCountGroupBySubject();
        Map<String, Integer> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object code = row.get("subjectCode");
            Object cnt = row.get("deptCount");
            if (code != null && cnt instanceof Number) {
                map.put(String.valueOf(code), ((Number) cnt).intValue());
            }
        }
        return map;
    }

    /**
     * 父科室 id → 名称。优先用传入集合里已有的科室，不够再按 id 补查（避免整表再捞一次）。
     */
    private Map<Long, String> parentNameMap(Collection<SysDepartment> depts) {
        Map<Long, String> known = depts.stream()
                .filter(d -> d.getId() != null)
                .collect(Collectors.toMap(SysDepartment::getId, SysDepartment::getDeptName, (a, b) -> a));
        Set<Long> missing = depts.stream()
                .map(SysDepartment::getParentId)
                .filter(Objects::nonNull)
                .filter(id -> !known.containsKey(id))
                .collect(Collectors.toSet());
        if (missing.isEmpty()) {
            return known;
        }
        Map<Long, String> extra = sysDepartmentMapper.selectBatchIds(missing).stream()
                .collect(Collectors.toMap(SysDepartment::getId, SysDepartment::getDeptName, (a, b) -> a));
        known.putAll(extra);
        return known;
    }

    private boolean isClinical(String deptType) {
        if (!TextUtil.hasText(deptType)) {
            return false;
        }
        List<String> types = Arrays.asList(deptType.split(","));
        for (String clinical : CLINICAL_DEPT_TYPES) {
            if (types.contains(clinical)) {
                return true;
            }
        }
        return false;
    }

    private MedSubjectVO toSubjectVO(SysMedSubject subject, Map<String, Integer> countMap) {
        MedSubjectVO vo = new MedSubjectVO();
        BeanUtils.copyProperties(subject, vo);
        vo.setMappedDeptCount(countMap.getOrDefault(subject.getSubjectCode(), 0));
        return vo;
    }

    /**
     * 组装「科室 × 科目」视图。dept 为空时返回 null，由调用方过滤（科室被删但映射残留的兜底）。
     */
    private DeptSubjectVO toDeptSubjectVO(SysDepartment dept, List<SysDeptSubjectMap> rels,
                                          Map<String, SysMedSubject> subjectMap,
                                          Map<Long, String> parentNames) {
        if (dept == null) {
            return null;
        }
        DeptSubjectVO vo = new DeptSubjectVO();
        vo.setDeptId(dept.getId());
        vo.setDeptCode(dept.getDeptCode());
        vo.setDeptName(dept.getDeptName());
        vo.setDeptType(dept.getDeptType());
        vo.setParentName(parentNames.get(dept.getParentId()));
        List<DeptSubjectItemVO> items = new ArrayList<>();
        for (SysDeptSubjectMap rel : rels) {
            SysMedSubject subject = subjectMap.get(rel.getSubjectCode());
            SysMedSubject parent = subject == null || !TextUtil.hasText(subject.getParentCode())
                    ? null : subjectMap.get(subject.getParentCode());
            DeptSubjectItemVO item = new DeptSubjectItemVO();
            item.setSubjectCode(rel.getSubjectCode());
            item.setSubjectName(subject == null ? "" : subject.getSubjectName());
            item.setParentName(parent == null ? null : parent.getSubjectName());
            item.setIsPrimary(rel.getIsPrimary());
            items.add(item);
            if (Integer.valueOf(1).equals(rel.getIsPrimary())) {
                vo.setPrimarySubjectCode(rel.getSubjectCode());
                vo.setPrimarySubjectName(item.getSubjectName());
            }
        }
        vo.setSubjects(items);
        return vo;
    }
}
