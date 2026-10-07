package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.enums.EnableStatusEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.patient.dto.OrderDictQueryPageDTO;
import com.his.patient.dto.OrderDictUpsertDTO;
import com.his.patient.entity.SysOrderDictData;
import com.his.patient.mapper.SysOrderDictDataMapper;
import com.his.patient.service.OrderDictService;
import com.his.patient.support.OrderDictTypes;
import com.his.patient.vo.OrderDictListVO;
import com.his.patient.vo.OrderDictUsageCountVO;
import com.his.system.entity.CurrentUser;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 医嘱基础字典服务实现（sql/142）。
 *
 * <p>四条必须守住的口径：
 * <ol>
 *   <li><b>只认三种 dictType</b>：越界直接拒绝，避免这个口子变成通用字典后门。</li>
 *   <li><b>值不可改</b>：值被存量医嘱行引用，改了历史医嘱就渲染成「未知(xxx)」；
 *       要换值就停用旧的、新增一条。修改只放开显示名 / 排序 / 启用状态 / 备注。</li>
 *   <li><b>同类型内值唯一</b>：字典数据没有唯一键（只有 idx_dict_type），
 *       重复值会让下拉出现两个一样的选项，选中哪个全看顺序 —— 只能服务端自己判重。</li>
 *   <li><b>写完刷缓存</b>：医生站下拉走的是 {@code sys:dict:*} 的 24 小时缓存，
 *       不刷的话改完字典要等一天才生效（或等运维去点「刷新缓存」）。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDictServiceImpl extends ServiceImpl<SysOrderDictDataMapper, SysOrderDictData> implements OrderDictService {

    private final SysOrderDictDataMapper sysOrderDictDataMapper;

    private final DictCacheService dictCacheService;

    @Override
    public IPage<OrderDictListVO> listPage(OrderDictQueryPageDTO query) {
        String dictType = requireType(query.getDictType());
        String keyword = TextUtil.hasText(query.getKeyword()) ? query.getKeyword().trim() : null;
        LambdaQueryWrapper<SysOrderDictData> wrapper = new LambdaQueryWrapper<SysOrderDictData>()
                .eq(SysOrderDictData::getDictType, dictType)
                .eq(query.getStatus() != null, SysOrderDictData::getStatus, query.getStatus())
                .and(keyword != null, w -> w.like(SysOrderDictData::getDictLabel, keyword)
                        .or().like(SysOrderDictData::getDictValue, keyword))
                // 分页必须补唯一二级键：同排序的项在翻页时会在两页重复出现
                .orderByAsc(SysOrderDictData::getDictSort)
                .orderByAsc(SysOrderDictData::getId);

        Map<String, Long> usage = loadUsage(dictType);
        return sysOrderDictDataMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper)
                .convert(entity -> {
                    OrderDictListVO vo = new OrderDictListVO();
                    BeanUtils.copyProperties(entity, vo);
                    vo.setDictTypeText(OrderDictTypes.text(entity.getDictType()));
                    vo.setStatusText(EnableStatusEnum.getText(entity.getStatus()));
                    vo.setBuiltIn(Objects.equals(1, entity.getDictSource()));
                    Long cnt = usage.get(entity.getDictValue());
                    vo.setUsageCount(cnt == null ? 0L : cnt);
                    return vo;
                });
    }

    @Override
    public List<OrderDictListVO> selectList(String dictType) {
        String type = requireType(dictType);
        List<SysOrderDictData> list = sysOrderDictDataMapper.selectList(new LambdaQueryWrapper<SysOrderDictData>()
                .eq(SysOrderDictData::getDictType, type)
                .eq(SysOrderDictData::getStatus, 1)
                .orderByAsc(SysOrderDictData::getDictSort)
                .orderByAsc(SysOrderDictData::getId));
        Map<String, Long> usage = loadUsage(type);
        List<OrderDictListVO> vos = new ArrayList<>(list.size());
        for (SysOrderDictData entity : list) {
            OrderDictListVO vo = new OrderDictListVO();
            BeanUtils.copyProperties(entity, vo);
            vo.setDictTypeText(OrderDictTypes.text(entity.getDictType()));
            vo.setStatusText(EnableStatusEnum.getText(entity.getStatus()));
            vo.setBuiltIn(Objects.equals(1, entity.getDictSource()));
            Long cnt = usage.get(entity.getDictValue());
            vo.setUsageCount(cnt == null ? 0L : cnt);
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public Long upsert(OrderDictUpsertDTO dto) {
        // ②非web入口：整个入参对象的判空，DTO 字段注解表达不了
        if (dto == null) {
            throw new BusinessException("字典内容不能为空");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        String dictType = requireType(dto.getDictType());

        String label = dto.getDictLabel() == null ? null : dto.getDictLabel().trim();
        if (label.length() > 100) {
            throw new BusinessException("显示名不能超过 100 字");
        }

        Integer status = dto.getStatus() == null ? 1 : dto.getStatus();

        String operator = operatorUser.getRealName();
        if (dto.getId() == null) {
            String value = dto.getDictValue() == null ? null : dto.getDictValue().trim();
            // ①条件必填：只有新增（id==null）才要求字典值，修改分支允许不传，@NotBlank 会把合法修改挡成 400
            if (!TextUtil.hasText(value)) {
                throw new BusinessException("字典值不能为空");
            }
            if (value.length() > 100) {
                throw new BusinessException("字典值不能超过 100 字");
            }
            assertValueNotDuplicated(dictType, value, null);

            SysOrderDictData entity = new SysOrderDictData();
            entity.setDictType(dictType);
            entity.setDictValue(value);
            entity.setDictLabel(label);
            entity.setDictSort(nextSort(dictType, dto.getDictSort()));
            entity.setListClass("primary");
            entity.setIsDefault(0);
            entity.setStatus(status);
            entity.setRemark(trimRemark(dto.getRemark()));
            entity.setDictSource(2);
            entity.setCreateBy(operator);
            entity.setUpdateBy(operator);
            sysOrderDictDataMapper.insert(entity);
            refreshCache(dictType);
            log.info("新增医嘱字典 {} = {}（{}）操作人={}", dictType, value, label, operator);
            return entity.getId();
        }

        SysOrderDictData entity = sysOrderDictDataMapper.selectById(dto.getId());
        if (entity == null || !dictType.equals(entity.getDictType())) {
            throw new BusinessException("字典项不存在");
        }
        if (dto.getDictValue() != null && !Objects.equals(entity.getDictValue(), dto.getDictValue().trim())) {
            throw new BusinessException("字典值不允许修改（存量医嘱已在用它），请停用后新增一条");
        }
        entity.setDictLabel(label);
        entity.setStatus(status);
        if (dto.getDictSort() != null) {
            entity.setDictSort(dto.getDictSort());
        }
        entity.setRemark(trimRemark(dto.getRemark()));
        entity.setUpdateBy(operator);
        sysOrderDictDataMapper.updateById(entity);
        refreshCache(dictType);
        log.info("修改医嘱字典 id={} {} = {} 状态={} 操作人={}", entity.getId(), dictType, entity.getDictValue(), status, operator);
        return entity.getId();
    }

    @Override
    public void deleteById(Long id, String dictType) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        String type = requireType(dictType);
        // ②非web入口：service 方法参数判空，没有 DTO 字段可挂注解（HTTP 侧 @RequestParam 已必填）
        if (id == null) {
            throw new BusinessException("字典项ID不能为空");
        }
        SysOrderDictData entity = sysOrderDictDataMapper.selectById(id);
        if (entity == null || !type.equals(entity.getDictType())) {
            throw new BusinessException("字典项不存在");
        }
        // 逻辑删：字典行留着，历史医嘱按原值仍能渲染出文案
        sysOrderDictDataMapper.deleteById(id);
        refreshCache(type);
        log.info("删除医嘱字典 id={} {} = {} 操作人={}", id, type, entity.getDictValue(), operatorUser.getRealName());
    }

    // 内部

    private String requireType(String dictType) {
        if (!OrderDictTypes.isManaged(dictType)) {
            throw new BusinessException("字典类型不合法（仅允许 给药途径 / 用药频次 / 剂量单位）");
        }
        return dictType;
    }

    /**
     * 字典数据没有 (dict_type, dict_value) 唯一键，重复值只能服务端拦
     */
    private void assertValueNotDuplicated(String dictType, String value, Long excludeId) {
        Long hits = sysOrderDictDataMapper.selectCount(new LambdaQueryWrapper<SysOrderDictData>()
                .eq(SysOrderDictData::getDictType, dictType)
                .eq(SysOrderDictData::getDictValue, value)
                .ne(excludeId != null, SysOrderDictData::getId, excludeId));
        if (hits != null && hits > 0) {
            throw new BusinessException("该字典值已存在，请换一个值");
        }
    }

    /**
     * 不传排序就排到最后（max+1），避免新加的项插在最前面把常用项挤下去
     */
    private int nextSort(String dictType, Integer sort) {
        if (sort != null && sort > 0) {
            return sort;
        }
        List<SysOrderDictData> list = sysOrderDictDataMapper.selectList(new LambdaQueryWrapper<SysOrderDictData>()
                .eq(SysOrderDictData::getDictType, dictType)
                .orderByDesc(SysOrderDictData::getDictSort)
                .last("LIMIT 1"));
        int max = list.isEmpty() || list.get(0).getDictSort() == null ? 0 : list.get(0).getDictSort();
        return max + 1;
    }

    /**
     * 使用量：按类型对应的医嘱列 GROUP BY 一次拿全，不在列表里逐行数
     */
    private Map<String, Long> loadUsage(String dictType) {
        String column = OrderDictTypes.orderColumn(dictType);
        Map<String, Long> usage = new HashMap<>();
        if (column == null) {
            return usage;
        }
        try {
            for (OrderDictUsageCountVO row : sysOrderDictDataMapper.countOrderUsage(column)) {
                if (row.getDictValue() == null) {
                    continue;
                }
                usage.put(row.getDictValue(), row.getCnt() == null ? 0L : row.getCnt());
            }
        } catch (Exception e) {
            // 统计失败不该让字典页打不开：使用量是辅助信息，置 0 继续
            log.warn("统计医嘱字典使用量失败 dictType={} column={}", dictType, column, e);
        }
        return usage;
    }

    /**
     * 写完必须刷缓存：医生站下拉走 sys:dict:* 缓存（24h），不刷当天看不到新值
     */
    private void refreshCache(String dictType) {
        try {
            dictCacheService.refreshDictCache(dictType);
        } catch (Exception e) {
            log.warn("刷新医嘱字典缓存失败 dictType={}（数据已落库，缓存将在过期后自动一致）", dictType, e);
        }
    }


    private String trimRemark(String remark) {
        if (remark == null) {
            return null;
        }
        String trimmed = remark.trim();
        return trimmed.length() > 500 ? trimmed.substring(0, 500) : trimmed;
    }

}
