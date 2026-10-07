package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.constant.DictType;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.pharmacy.dto.CssdDTO;
import com.his.pharmacy.entity.BizCssdPackTemplate;
import com.his.pharmacy.entity.BizCssdPackTemplateItem;
import com.his.pharmacy.mapper.BizCssdPackTemplateItemMapper;
import com.his.pharmacy.mapper.BizCssdPackTemplateMapper;
import com.his.pharmacy.service.CssdTemplateService;
import com.his.pharmacy.vo.CssdPackTemplateItemSelectListVO;
import com.his.pharmacy.vo.CssdPackTemplateItemVO;
import com.his.pharmacy.vo.CssdPackTemplateSelectListVO;
import com.his.pharmacy.vo.CssdPackTemplateVO;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class CssdTemplateServiceImpl extends ServiceImpl<BizCssdPackTemplateMapper, BizCssdPackTemplate> implements CssdTemplateService {
    private final DictCacheService dictCacheService;

    private final BizCssdPackTemplateMapper bizCssdPackTemplateMapper;

    private final BizCssdPackTemplateItemMapper bizCssdPackTemplateItemMapper;

    public List<CssdPackTemplateSelectListVO> selectList() {
        return bizCssdPackTemplateMapper.selectList(new LambdaQueryWrapper<BizCssdPackTemplate>()
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
        String kw = TextUtil.trim(q.getKeyword());
        LambdaQueryWrapper<BizCssdPackTemplate> w = new LambdaQueryWrapper<BizCssdPackTemplate>()
                .eq(BizCssdPackTemplate::getDelFlag, 0)
                .eq(q.getStatus() != null, BizCssdPackTemplate::getStatus, q.getStatus())
                .and(TextUtil.hasText(kw), x -> x
                        .like(BizCssdPackTemplate::getTemplateCode, kw)
                        .or().like(BizCssdPackTemplate::getPackName, kw))
                .orderByAsc(BizCssdPackTemplate::getTemplateCode);
        Page<BizCssdPackTemplate> page = bizCssdPackTemplateMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w);

        List<Long> ids = page.getRecords().stream().map(BizCssdPackTemplate::getId).toList();
        Map<Long, Long> countMap = ids.isEmpty() ? Map.of()
                : bizCssdPackTemplateItemMapper.selectList(new LambdaQueryWrapper<BizCssdPackTemplateItem>()
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
        return bizCssdPackTemplateItemMapper.selectDistinctItemSummary().stream().map(i -> {
            CssdPackTemplateItemSelectListVO vo = new CssdPackTemplateItemSelectListVO();
            BeanUtils.copyProperties(i, vo);
            return vo;
        }).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public CssdPackTemplateVO upsert(CssdDTO.TemplateUpsert dto) {
        String code = TextUtil.trim(dto.getTemplateCode());
        String name = TextUtil.trim(dto.getPackName());

        BizCssdPackTemplate self = dto.getId() == null ? null : requireTemplate(dto.getId());

        Long dupCode = bizCssdPackTemplateMapper.selectCount(new LambdaQueryWrapper<BizCssdPackTemplate>()
                .eq(BizCssdPackTemplate::getDelFlag, 0)
                .eq(BizCssdPackTemplate::getTemplateCode, code)
                .ne(self != null, BizCssdPackTemplate::getId, self == null ? null : self.getId()));
        if (dupCode > 0) {
            throw new BusinessException("包编码已存在：" + code);
        }
        Long dupName = bizCssdPackTemplateMapper.selectCount(new LambdaQueryWrapper<BizCssdPackTemplate>()
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
        t.setRemark(TextUtil.trim(dto.getRemark()));
        if (self == null) {
            t.setCreateBy(UserUtils.getCurrentUser().getRealName());
            bizCssdPackTemplateMapper.insert(t);
        } else {
            t.setUpdateBy(UserUtils.getCurrentUser().getRealName());
            bizCssdPackTemplateMapper.updateById(t);
            bizCssdPackTemplateItemMapper.delete(new LambdaQueryWrapper<BizCssdPackTemplateItem>()
                    .eq(BizCssdPackTemplateItem::getTemplateId, t.getId()));
        }

        List<BizCssdPackTemplateItem> items = dto.getItems().stream().map(i -> {
            BizCssdPackTemplateItem it = new BizCssdPackTemplateItem();
            it.setTemplateId(t.getId());
            it.setItemName(TextUtil.trim(i.getItemName()));
            it.setSpec(TextUtil.trim(i.getSpec()));
            it.setUnit(TextUtil.hasText(i.getUnit()) ? i.getUnit().trim() : "件");
            it.setQuantity(i.getQuantity());
            return it;
        }).toList();
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setSortNo(i + 1);
            bizCssdPackTemplateItemMapper.insert(items.get(i));
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
        t.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        bizCssdPackTemplateMapper.updateById(t);
    }

    private BizCssdPackTemplate requireTemplate(Long templateId) {
        BizCssdPackTemplate t = bizCssdPackTemplateMapper.selectById(templateId);
        if (t == null || Objects.equals(t.getDelFlag(), 1)) {
            throw new BusinessException("器械包模板不存在（id=" + templateId + "）");
        }
        return t;
    }

    private List<CssdPackTemplateItemVO> loadItems(Long templateId) {
        return bizCssdPackTemplateItemMapper.selectList(new LambdaQueryWrapper<BizCssdPackTemplateItem>()
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
        vo.setSterilizeMethodText(dictCacheService.getDicDataLabel(DictType.CSSD_STERIL_METHOD, t.getSterilizeMethod()));
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