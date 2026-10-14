package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mis.common.base.PageVO;
import com.mis.dto.FilingQueryDTO;
import com.mis.dto.FilingUpsertDTO;
import com.mis.entity.MssFiling;
import com.mis.mapper.MssFilingMapper;
import com.mis.service.FilingService;
import com.mis.vo.ActionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FilingServiceImpl implements FilingService {

    private final MssFilingMapper filingMapper;

    @Override
    public PageVO<MssFiling> filingPage(FilingQueryDTO q) {
        Page<MssFiling> page = new Page<>(q.getPage(), q.getSize());
        LambdaQueryWrapper<MssFiling> w = new LambdaQueryWrapper<>();
        if (hasText(q.getKeyword())) {
            String kw = q.getKeyword().trim();
            w.and(x -> x.like(MssFiling::getName, kw)
                    .or().like(MssFiling::getInsuranceArea, kw)
                    .or().like(MssFiling::getFromArea, kw)
                    .or().like(MssFiling::getHospital, kw));
        }
        if (hasText(q.getStatus())) {
            w.eq(MssFiling::getStatus, q.getStatus());
        }
        w.orderByDesc(MssFiling::getId);
        Page<MssFiling> res = filingMapper.selectPage(page, w);
        return PageVO.of(res.getRecords(), res.getTotal());
    }

    @Override
    public ActionVO upsert(FilingUpsertDTO dto) {
        MssFiling f;
        if (dto.getId() == null) {
            f = new MssFiling();
            f.setStatus("审核中");
            f.setApplyTime(LocalDate.now());
        } else {
            f = filingMapper.selectById(dto.getId());
            if (f == null) {
                return ActionVO.fail("备案记录不存在");
            }
        }
        f.setName(dto.getName());
        f.setIdCardMask(dto.getIdCardMask());
        f.setInsuranceArea(dto.getInsuranceArea());
        f.setHospital(dto.getHospital());
        f.setReason(dto.getReason());
        f.setFromArea(dto.getFromArea());
        f.setValidStart(dto.getValidStart());
        f.setValidEnd(dto.getValidEnd());
        if (dto.getId() == null) {
            filingMapper.insert(f);
        } else {
            filingMapper.updateById(f);
        }
        return ActionVO.ok();
    }

    @Override
    public ActionVO revoke(Long id) {
        MssFiling f = filingMapper.selectById(id);
        if (f == null) {
            return ActionVO.fail("备案记录不存在");
        }
        if ("已失效".equals(f.getStatus())) {
            return ActionVO.fail("备案已失效");
        }
        f.setStatus("已失效");
        filingMapper.updateById(f);
        return ActionVO.ok();
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
