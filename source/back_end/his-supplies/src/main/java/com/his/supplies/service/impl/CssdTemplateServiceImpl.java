package com.his.supplies.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.exception.BusinessException;
import com.his.security.UserUtils;
import com.his.supplies.dto.CssdDTO;
import com.his.supplies.entity.BizCssdPackTemplate;
import com.his.supplies.entity.BizCssdPackTemplateItem;
import com.his.supplies.enums.CssdSterilizeMethodEnum;
import com.his.supplies.mapper.BizCssdPackTemplateItemMapper;
import com.his.supplies.mapper.BizCssdPackTemplateMapper;
import com.his.supplies.service.CssdTemplateService;
import com.his.supplies.vo.CssdPackTemplateItemSelectListVO;
import com.his.supplies.vo.CssdPackTemplateItemVO;
import com.his.supplies.vo.CssdPackTemplateSelectListVO;
import com.his.supplies.vo.CssdPackTemplateVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * CSSD 器械包模板目录服务。
 *
 * <p>目录是"活数据"：编码/名称仅在启用行（del_flag=0）范围内做唯一校验；
 * 明细整删整插不做差量。追溯表存的是包名快照，模板改名/停用不影响历史包。
 */
@Service
@RequiredArgsConstructor
public class CssdTemplateServiceImpl implements CssdTemplateService {

    private final BizCssdPackTemplateMapper templateMapper;
    private final BizCssdPackTemplateItemMapper itemMapper;

    // 查询

    private static String tr(String s) {
        return s == null ? null : s.trim();
    }

    /**
     * 回收登记下拉数据源：仅启用模板
     */
    public List<CssdPackTemplateSelectListVO> selectList() {
        return templateMapper.selectList(new LambdaQueryWrapper<BizCssdPackTemplate>()
                        .eq(BizCssdPackTemplate::getDelFlag, 0)
                        .eq(BizCssdPackTemplate::getStatus, 1)
                        .orderByAsc(BizCssdPackTemplate::getTemplateCode))
                .stream().map(t -> {
                    CssdPackTemplateSelectListVO vo = new CssdPackTemplateSelectListVO();
                    BeanUtils.copyProperties(toVo(t, null), vo);
                    return vo;
                }).toList();
    }

    public IPage<CssdPackTemplateVO> listPage(CssdDTO.TemplateQueryPage q) {
        String kw = tr(q.getKeyword());
        LambdaQueryWrapper<BizCssdPackTemplate> w = new LambdaQueryWrapper<BizCssdPackTemplate>()
                .eq(BizCssdPackTemplate::getDelFlag, 0)
                .eq(q.getStatus() != null, BizCssdPackTemplate::getStatus, q.getStatus())
                .and(StringUtils.hasText(kw), x -> x
                        .like(BizCssdPackTemplate::getTemplateCode, kw)
                        .or().like(BizCssdPackTemplate::getPackName, kw))
                .orderByAsc(BizCssdPackTemplate::getTemplateCode);
        Page<BizCssdPackTemplate> page = templateMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);

        List<Long> ids = page.getRecords().stream().map(BizCssdPackTemplate::getId).toList();
        Map<Long, Long> countMap = ids.isEmpty() ? Map.of()
                : itemMapper.selectList(new LambdaQueryWrapper<BizCssdPackTemplateItem>()
                        .in(BizCssdPackTemplateItem::getTemplateId, ids))
                .stream().collect(Collectors.groupingBy(BizCssdPackTemplateItem::getTemplateId, Collectors.counting()));
        return page.convert(t -> {
            CssdPackTemplateVO vo = toVo(t, null);
            vo.setItemCount(countMap.getOrDefault(t.getId(), 0L).intValue());
            return vo;
        });
    }

    public CssdPackTemplateVO getDetailById(Long templateId) {
        BizCssdPackTemplate t = requireTemplate(templateId);
        return toVo(t, loadItems(templateId));
    }

    // 写

    /**
     * 模板编辑器组成明细下拉：启用模板下的器械名称去重汇总（带出规格/单位）
     */
    public List<CssdPackTemplateItemSelectListVO> itemSelectList() {
        return itemMapper.selectDistinctItemSummary().stream().map(i -> {
            CssdPackTemplateItemSelectListVO vo = new CssdPackTemplateItemSelectListVO();
            BeanUtils.copyProperties(i, vo);
            return vo;
        }).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public CssdPackTemplateVO upsert(CssdDTO.TemplateUpsert dto) {
        if (CssdSterilizeMethodEnum.fromCode(dto.getSterilizeMethod()) == null) {
            throw new BusinessException("灭菌方式取值不合法（1-高压蒸汽 2-环氧乙烷 3-低温等离子）");
        }
        String code = tr(dto.getTemplateCode());
        String name = tr(dto.getPackName());

        BizCssdPackTemplate self = dto.getId() == null ? null : requireTemplate(dto.getId());

        Long dupCode = templateMapper.selectCount(new LambdaQueryWrapper<BizCssdPackTemplate>()
                .eq(BizCssdPackTemplate::getDelFlag, 0)
                .eq(BizCssdPackTemplate::getTemplateCode, code)
                .ne(self != null, BizCssdPackTemplate::getId, self == null ? null : self.getId()));
        if (dupCode > 0) {
            throw new BusinessException("包编码已存在：" + code);
        }
        Long dupName = templateMapper.selectCount(new LambdaQueryWrapper<BizCssdPackTemplate>()
                .eq(BizCssdPackTemplate::getDelFlag, 0)
                .eq(BizCssdPackTemplate::getPackName, name)
                .ne(self != null, BizCssdPackTemplate::getId, self == null ? null : self.getId()));
        if (dupName > 0) {
            throw new BusinessException("器械包名称已存在：" + name);
        }

        BizCssdPackTemplate t = self == null ? new BizCssdPackTemplate() : self;
        t.setTemplateCode(code);
        t.setPackName(name);
        t.setSterilizeMethod(dto.getSterilizeMethod());
        t.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        t.setRemark(tr(dto.getRemark()));
        if (self == null) {
            t.setCreateBy(UserUtils.getCurrentEmployeeName());
            templateMapper.insert(t);
        } else {
            t.setUpdateBy(UserUtils.getCurrentEmployeeName());
            templateMapper.updateById(t);
            itemMapper.delete(new LambdaQueryWrapper<BizCssdPackTemplateItem>()
                    .eq(BizCssdPackTemplateItem::getTemplateId, t.getId()));
        }

        List<BizCssdPackTemplateItem> items = dto.getItems().stream().map(i -> {
            BizCssdPackTemplateItem it = new BizCssdPackTemplateItem();
            it.setTemplateId(t.getId());
            it.setItemName(tr(i.getItemName()));
            it.setSpec(tr(i.getSpec()));
            it.setUnit(StringUtils.hasText(i.getUnit()) ? i.getUnit().trim() : "件");
            it.setQuantity(i.getQuantity());
            return it;
        }).toList();
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setSortNo(i + 1);
            itemMapper.insert(items.get(i));
        }
        return toVo(t, loadItems(t.getId()));
    }

    // 私有

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long templateId) {
        requireTemplate(templateId);
        BizCssdPackTemplate t = new BizCssdPackTemplate();
        t.setId(templateId);
        t.setDelFlag(1);
        t.setStatus(0);
        t.setUpdateBy(UserUtils.getCurrentEmployeeName());
        templateMapper.updateById(t);
    }

    private BizCssdPackTemplate requireTemplate(Long templateId) {
        BizCssdPackTemplate t = templateMapper.selectById(templateId);
        if (t == null || Objects.equals(t.getDelFlag(), 1)) {
            throw new BusinessException("器械包模板不存在（id=" + templateId + "）");
        }
        return t;
    }

    private List<CssdPackTemplateItemVO> loadItems(Long templateId) {
        return itemMapper.selectList(new LambdaQueryWrapper<BizCssdPackTemplateItem>()
                        .eq(BizCssdPackTemplateItem::getTemplateId, templateId)
                        .orderByAsc(BizCssdPackTemplateItem::getSortNo)
                        .orderByAsc(BizCssdPackTemplateItem::getId))
                .stream().map(this::toItemVo).toList();
    }

    private CssdPackTemplateVO toVo(BizCssdPackTemplate t, List<CssdPackTemplateItemVO> items) {
        CssdPackTemplateVO vo = new CssdPackTemplateVO();
        vo.setId(t.getId());
        vo.setTemplateCode(t.getTemplateCode());
        vo.setPackName(t.getPackName());
        vo.setSterilizeMethod(t.getSterilizeMethod());
        vo.setSterilizeMethodText(CssdSterilizeMethodEnum.labelOf(t.getSterilizeMethod()));
        vo.setStatus(t.getStatus());
        vo.setRemark(t.getRemark());
        vo.setItems(items);
        vo.setCreateBy(t.getCreateBy());
        vo.setCreateTime(t.getCreateTime());
        return vo;
    }

    private CssdPackTemplateItemVO toItemVo(BizCssdPackTemplateItem i) {
        CssdPackTemplateItemVO vo = new CssdPackTemplateItemVO();
        vo.setId(i.getId());
        vo.setTemplateId(i.getTemplateId());
        vo.setItemName(i.getItemName());
        vo.setSpec(i.getSpec());
        vo.setUnit(i.getUnit());
        vo.setQuantity(i.getQuantity());
        vo.setSortNo(i.getSortNo());
        return vo;
    }
}
