package com.mis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mis.common.base.PageVO;
import com.mis.dto.BizPageParam;
import com.mis.entity.MssRecon;
import com.mis.mapper.MssReconMapper;
import com.mis.service.ReconService;
import com.mis.vo.ActionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ReconServiceImpl implements ReconService {

    private final MssReconMapper reconMapper;

    @Override
    public PageVO<MssRecon> reconPage(BizPageParam q) {
        Page<MssRecon> page = new Page<>(q.getPage(), q.getSize());
        LambdaQueryWrapper<MssRecon> w = new LambdaQueryWrapper<>();
        w.orderByAsc(MssRecon::getPeriod);
        Page<MssRecon> res = reconMapper.selectPage(page, w);
        return PageVO.of(res.getRecords(), res.getTotal());
    }

    @Override
    public ActionVO doReconcile(String period) {
        if (period == null || period.isBlank()) {
            return ActionVO.fail("缺少账期");
        }
        MssRecon r = reconMapper.selectOne(new LambdaQueryWrapper<MssRecon>()
                .eq(MssRecon::getPeriod, period).last("limit 1"));
        if (r == null) {
            return ActionVO.fail("账期不存在");
        }
        if (!"待对账".equals(r.getStatus())) {
            return ActionVO.fail("该账期已对账");
        }
        r.setReceivedFromFund(r.getInsurancePay());
        r.setDiff(BigDecimal.ZERO.setScale(2));
        r.setStatus("已对账");
        reconMapper.updateById(r);
        return ActionVO.ok();
    }
}
