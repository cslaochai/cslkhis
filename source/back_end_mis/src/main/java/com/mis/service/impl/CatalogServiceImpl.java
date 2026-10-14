package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mis.common.base.PageVO;
import com.mis.dto.CatalogQueryDTO;
import com.mis.dto.MappingMapDTO;
import com.mis.dto.MappingQueryDTO;
import com.mis.entity.MssCatalog;
import com.mis.entity.MssItemMapping;
import com.mis.mapper.MssCatalogMapper;
import com.mis.mapper.MssItemMappingMapper;
import com.mis.service.CatalogService;
import com.mis.vo.ActionVO;
import com.mis.vo.CatalogStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final MssCatalogMapper catalogMapper;
    private final MssItemMappingMapper mappingMapper;

    private static final List<String> CATEGORIES =
            List.of("西药", "中成药", "中草药", "诊疗项目", "医用耗材");

    @Override
    public PageVO<MssCatalog> catalogPage(CatalogQueryDTO q) {
        Page<MssCatalog> page = new Page<>(q.getPage(), q.getSize());
        LambdaQueryWrapper<MssCatalog> w = new LambdaQueryWrapper<>();
        if (hasText(q.getKeyword())) {
            String kw = q.getKeyword().trim();
            w.and(x -> x.like(MssCatalog::getName, kw).or().like(MssCatalog::getYbCode, kw));
        }
        if (hasText(q.getCategory())) {
            w.eq(MssCatalog::getCategory, q.getCategory());
        }
        if (hasText(q.getPayClass())) {
            w.eq(MssCatalog::getPayClass, q.getPayClass());
        }
        w.orderByAsc(MssCatalog::getId);
        Page<MssCatalog> res = catalogMapper.selectPage(page, w);
        return PageVO.of(res.getRecords(), res.getTotal());
    }

    @Override
    public CatalogStatsVO catalogStats() {
        List<MssCatalog> all = catalogMapper.selectList(null);
        Map<String, Long> byCat = new LinkedHashMap<>();
        CATEGORIES.forEach(c -> byCat.put(c, 0L));
        Map<String, Long> byClass = new LinkedHashMap<>();
        byClass.put("甲", 0L);
        byClass.put("乙", 0L);
        byClass.put("丙", 0L);
        long disabled = 0;
        for (MssCatalog c : all) {
            byCat.merge(c.getCategory(), 1L, Long::sum);
            if (c.getPayClass() != null) {
                byClass.merge(c.getPayClass(), 1L, Long::sum);
            }
            if (c.getStatus() != null && c.getStatus() == 0) {
                disabled++;
            }
        }
        CatalogStatsVO vo = new CatalogStatsVO();
        vo.setByCat(byCat);
        vo.setByClass(byClass);
        vo.setTotal(all.size());
        vo.setDisabled(disabled);
        return vo;
    }

    @Override
    public PageVO<MssItemMapping> mappingPage(MappingQueryDTO q) {
        Page<MssItemMapping> page = new Page<>(q.getPage(), q.getSize());
        LambdaQueryWrapper<MssItemMapping> w = new LambdaQueryWrapper<>();
        if (hasText(q.getKeyword())) {
            w.like(MssItemMapping::getHospItemName, q.getKeyword().trim());
        }
        if (q.getMapped() != null) {
            w.eq(MssItemMapping::getMapped, q.getMapped());
        }
        w.orderByAsc(MssItemMapping::getId);
        Page<MssItemMapping> res = mappingMapper.selectPage(page, w);
        return PageVO.of(res.getRecords(), res.getTotal());
    }

    @Override
    public ActionVO map(MappingMapDTO dto) {
        if (dto.getId() == null) {
            return ActionVO.fail("缺少对照记录id");
        }
        MssItemMapping m = mappingMapper.selectById(dto.getId());
        if (m == null) {
            return ActionVO.fail("对照记录不存在");
        }
        m.setYbCode(dto.getYbCode());
        m.setYbName(dto.getYbName());
        m.setPayClass(dto.getPayClass());
        m.setMapped(true);
        m.setUpdatedAt(LocalDateTime.now());
        mappingMapper.updateById(m);
        return ActionVO.ok();
    }

    @Override
    public ActionVO unmap(Long id) {
        MssItemMapping m = mappingMapper.selectById(id);
        if (m == null) {
            return ActionVO.fail("对照记录不存在");
        }
        m.setYbCode("");
        m.setYbName("");
        m.setPayClass("");
        m.setMapped(false);
        m.setUpdatedAt(null);
        mappingMapper.updateById(m);
        return ActionVO.ok();
    }

    @Override
    public ActionVO autoMatch() {
        List<MssCatalog> catalog = catalogMapper.selectList(null);
        List<MssItemMapping> mappings = mappingMapper.selectList(null);
        int n = 0;
        for (MssItemMapping m : mappings) {
            if (Boolean.TRUE.equals(m.getMapped())) {
                continue;
            }
            String hospHead = m.getHospItemName() == null ? "" : m.getHospItemName();
            String head = hospHead.length() >= 2 ? hospHead.substring(0, 2) : hospHead;
            MssCatalog hit = null;
            for (MssCatalog c : catalog) {
                String name = c.getName() == null ? "" : c.getName();
                String cHead = name.length() >= 2 ? name.substring(0, 2) : name;
                if (name.contains(head) || hospHead.contains(cHead)) {
                    hit = c;
                    break;
                }
            }
            if (hit != null) {
                m.setMapped(true);
                m.setYbCode(hit.getYbCode());
                m.setYbName(hit.getName());
                m.setPayClass(hit.getPayClass());
                m.setUpdatedAt(LocalDateTime.now());
                mappingMapper.updateById(m);
                n++;
            }
        }
        return ActionVO.matched(n);
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
