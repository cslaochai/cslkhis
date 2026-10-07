package com.his.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.ai.dto.LabPlainItemSearchDTO;
import com.his.ai.dto.LabPlainItemUpsertDTO;
import com.his.ai.entity.SysLabPlainItem;
import com.his.ai.mapper.SysLabPlainItemMapper;
import com.his.ai.service.LabPlainItemAdminService;
import com.his.ai.support.PatientTextGuard;
import com.his.ai.vo.LabPlainCoverageVO;
import com.his.ai.vo.LabPlainItemAdminVO;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.medicaltech.entity.BizLabResult;
import com.his.medicaltech.mapper.BizLabResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 白话词典维护实现。
 *
 * <p><b>维护的是患者会当作医嘱来读的文本</b>：所以保存前过一遍 {@link PatientTextGuard}。
 * 但这里与模型输出不同 —— 词典是院内检验科的人写的，专业表述（「携氧细胞」「免疫细胞」）要放行，
 * 闸门只拦「确诊 / 服用 / 剂量 / 建议吃」这类明确越界的措辞。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabPlainItemAdminServiceImpl implements LabPlainItemAdminService {

    private final SysLabPlainItemMapper plainMapper;
    private final BizLabResultMapper labResultMapper;
    private final PatientTextGuard patientTextGuard;

    private static LabPlainItemAdminVO toVO(SysLabPlainItem e) {
        LabPlainItemAdminVO vo = new LabPlainItemAdminVO();
        vo.setId(String.valueOf(e.getId()));
        vo.setGroupName(e.getGroupName());
        vo.setItemName(e.getItemName());
        vo.setPlainName(e.getPlainName());
        vo.setWhatIsIt(e.getWhatIsIt());
        vo.setHighText(e.getHighText());
        vo.setLowText(e.getLowText());
        vo.setStatus(e.getStatus());
        vo.setSortOrder(e.getSortOrder());
        vo.setRemark(e.getRemark());
        return vo;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * 写库前先截列宽：Data too long 会把业务失败升级成 500
     */
    private static String cut(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    @Override
    public PageResult<LabPlainItemAdminVO> adminPage(LabPlainItemSearchDTO dto) {
        // 越界夹取在 PageParam.getPageNum/getPageSize 里统一做，这里不再重复一遍
        int pageNum = dto.getPageNum();
        int pageSize = dto.getPageSize();

        LambdaQueryWrapper<SysLabPlainItem> w = new LambdaQueryWrapper<>();
        // like(cond, col, v)：实参先求值，这里先判空再取 trim，传空不会 NPE
        boolean hasKeyword = StringUtils.hasText(dto.getKeyword());
        String keyword = hasKeyword ? dto.getKeyword().trim() : null;
        w.and(hasKeyword, q -> q.like(SysLabPlainItem::getItemName, keyword)
                        .or()
                        .like(SysLabPlainItem::getPlainName, keyword))
                .eq(StringUtils.hasText(dto.getGroupName()), SysLabPlainItem::getGroupName, dto.getGroupName())
                .eq(dto.getStatus() != null, SysLabPlainItem::getStatus, dto.getStatus())
                // 分页补唯一二级键 id，避免同 sort_order 的行在第二页重复出现
                .orderByAsc(SysLabPlainItem::getGroupName)
                .orderByAsc(SysLabPlainItem::getSortOrder)
                .orderByAsc(SysLabPlainItem::getId);

        IPage<SysLabPlainItem> page = plainMapper.selectPage(new Page<>(pageNum, pageSize), w);
        List<LabPlainItemAdminVO> records = new ArrayList<>();
        for (SysLabPlainItem e : page.getRecords()) {
            records.add(toVO(e));
        }
        long total = page.getTotal();
        long pages = (total + pageSize - 1) / pageSize;
        return PageResult.of(total, pageNum, pageSize, pages, records);
    }

    // 私有

    @Override
    public List<String> selectGroupNames() {
        return plainMapper.selectGroupNames();
    }

    @Override
    public LabPlainItemAdminVO adminGetById(Long id) {
        SysLabPlainItem e = plainMapper.selectById(id);
        if (e == null || !Objects.equals(e.getDelFlag(), 0)) {
            throw new BusinessException("词条不存在或已删除");
        }
        return toVO(e);
    }

    @Override
    public String adminUpsert(LabPlainItemUpsertDTO dto) {
        String itemName = trim(dto.getItemName());
        if (!StringUtils.hasText(itemName)) {
            throw new BusinessException("检验项目名称不能为空");
        }
        checkPatientText(itemName, dto.getWhatIsIt());
        checkPatientText(itemName, dto.getHighText());
        checkPatientText(itemName, dto.getLowText());

        Long id = dto.getId();
        SysLabPlainItem exist = plainMapper.selectByItemName(itemName);
        if (exist != null && (id == null || !exist.getId().equals(id))) {
            // 唯一键不含 del_flag，撞键只会得到一个看不懂的 SQL 异常
            throw new BusinessException("检验项目名称已存在：" + itemName);
        }

        SysLabPlainItem entity = id != null ? plainMapper.selectById(id) : null;
        boolean isNew = entity == null;
        if (isNew) {
            entity = new SysLabPlainItem();
            entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
            entity.setSortOrder(dto.getSortOrder() == null ? 999 : dto.getSortOrder());
        }
        entity.setGroupName(cut(trim(dto.getGroupName()), 32));
        entity.setItemName(itemName);
        entity.setPlainName(cut(trim(dto.getPlainName()), 64));
        entity.setWhatIsIt(cut(trim(dto.getWhatIsIt()), 200));
        entity.setHighText(cut(trim(dto.getHighText()), 200));
        entity.setLowText(cut(trim(dto.getLowText()), 200));
        entity.setRemark(cut(trim(dto.getRemark()), 500));
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getSortOrder() != null) {
            entity.setSortOrder(dto.getSortOrder());
        }

        if (isNew) {
            plainMapper.insert(entity);
        } else {
            plainMapper.updateById(entity);
        }
        return String.valueOf(entity.getId());
    }

    @Override
    public void adminDelete(Long id) {
        if (plainMapper.selectById(id) == null) {
            throw new BusinessException("词条不存在或已删除");
        }
        plainMapper.purgeById(id);
    }

    @Override
    public LabPlainCoverageVO coverage() {
        // 分母：库内实际出现过的检验项目名（去重，不含空值）
        QueryWrapper<BizLabResult> w = new QueryWrapper<>();
        // 直接写列名：LambdaQueryWrapper 没有公开的 select(String...)，
        // 而分组统计要同时投影列名和 COUNT(*)，只能用 QueryWrapper
        w.select("laboratory_item_name", "COUNT(*) AS ref_cnt")
                .isNotNull("laboratory_item_name")
                .ne("laboratory_item_name", "")
                .groupBy("laboratory_item_name");
        List<Map<String, Object>> rows = labResultMapper.selectMaps(w);

        // 分子：词典里有的项目名。停用条目单独记一笔，因为它对患者端等于没配
        Map<String, Integer> enabledMap = new HashMap<>();
        Map<String, Integer> allMap = new HashMap<>();
        List<SysLabPlainItem> dict = plainMapper.selectList(
                new LambdaQueryWrapper<SysLabPlainItem>());
        for (SysLabPlainItem e : dict) {
            if (e.getItemName() == null || e.getItemName().isEmpty()) {
                continue;
            }
            allMap.put(e.getItemName(), e.getStatus());
            if (Objects.equals(e.getStatus(), 1) && Objects.equals(e.getDelFlag(), 0)) {
                enabledMap.put(e.getItemName(), 1);
            }
        }

        List<LabPlainCoverageVO.MissingItemVO> missing = new ArrayList<>();
        int total = 0;
        int covered = 0;
        for (Map<String, Object> row : rows) {
            Object nameObj = row.get("laboratory_item_name");
            if (nameObj == null) {
                continue;
            }
            String name = nameObj.toString();
            if (name.isEmpty()) {
                continue;
            }
            total++;
            if (enabledMap.containsKey(name)) {
                covered++;
                continue;
            }
            LabPlainCoverageVO.MissingItemVO m = new LabPlainCoverageVO.MissingItemVO();
            m.setItemName(name);
            m.setRefCount(toInt(row.get("ref_cnt")));
            // 词典里有同名但停用/已删除 → 改启用即可，不用重新写文案
            m.setDisabledOnly(allMap.containsKey(name));
            missing.add(m);
        }
        missing.sort((a, b) -> Integer.compare(
                b.getRefCount() == null ? 0 : b.getRefCount(),
                a.getRefCount() == null ? 0 : a.getRefCount()));

        LabPlainCoverageVO vo = new LabPlainCoverageVO();
        vo.setTotalItemName(total);
        vo.setCoveredCount(covered);
        vo.setMissingCount(missing.size());
        vo.setCoverageRate(total == 0 ? 100 : (int) Math.round(covered * 100.0 / total));
        vo.setGroupNames(plainMapper.selectGroupNames());
        vo.setMissingList(missing);
        return vo;
    }

    private void checkPatientText(String itemName, String text) {
        if (!StringUtils.hasText(text)) {
            return;
        }
        if (!patientTextGuard.isSafe(text)) {
            throw new BusinessException("「" + itemName + "」的说明出现诊断或用药类措辞，"
                    + "白话词典只解释指标含义，不给诊断和用药建议：" + text);
        }
    }
}
