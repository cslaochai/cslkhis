package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mis.common.base.PageVO;
import com.mis.config.UserContext;
import com.mis.dto.RecoverHandleDTO;
import com.mis.dto.RecoverQueryDTO;
import com.mis.entity.MssRecover;
import com.mis.mapper.MssRecoverMapper;
import com.mis.service.RecoverService;
import com.mis.vo.ActionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RecoverServiceImpl implements RecoverService {

    private final MssRecoverMapper recoverMapper;

    @Override
    public PageVO<MssRecover> recoverPage(RecoverQueryDTO q) {
        Page<MssRecover> page = new Page<>(q.getPage(), q.getSize());
        LambdaQueryWrapper<MssRecover> w = new LambdaQueryWrapper<>();
        if (hasText(q.getKeyword())) {
            String kw = q.getKeyword().trim();
            w.and(x -> x.like(MssRecover::getRecoverNo, kw)
                    .or().like(MssRecover::getSourceNo, kw)
                    .or().like(MssRecover::getTargetOrgName, kw));
        }
        if (q.getSourceType() != null) {
            w.eq(MssRecover::getSourceType, q.getSourceType());
        }
        if (hasText(q.getStatus())) {
            w.eq(MssRecover::getStatus, q.getStatus());
        }
        w.orderByDesc(MssRecover::getId);
        Page<MssRecover> res = recoverMapper.selectPage(page, w);
        return PageVO.of(res.getRecords(), res.getTotal());
    }

    @Override
    public MssRecover getDetail(Long id) {
        return recoverMapper.selectById(id);
    }

    @Override
    public ActionVO doRecover(RecoverHandleDTO dto) {
        MssRecover r = recoverMapper.selectById(dto.getId());
        if (r == null) {
            return ActionVO.fail("追回单不存在");
        }
        if (!"待追回".equals(r.getStatus())) {
            return ActionVO.fail("仅待追回状态可确认追回");
        }
        r.setStatus("已追回");
        r.setRecoverTime(LocalDateTime.now());
        r.setHandler(UserContext.username());
        r.setRemark(dto.getRemark());
        recoverMapper.updateById(r);
        return ActionVO.ok();
    }

    @Override
    public ActionVO writeOff(RecoverHandleDTO dto) {
        MssRecover r = recoverMapper.selectById(dto.getId());
        if (r == null) {
            return ActionVO.fail("追回单不存在");
        }
        if (!"待追回".equals(r.getStatus())) {
            return ActionVO.fail("仅待追回状态可核销");
        }
        r.setStatus("已核销");
        r.setRemark(dto.getRemark());
        recoverMapper.updateById(r);
        return ActionVO.ok();
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
