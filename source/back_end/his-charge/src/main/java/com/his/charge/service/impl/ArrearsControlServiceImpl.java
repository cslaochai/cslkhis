package com.his.charge.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.charge.dto.ArrearsBoardQueryDTO;
import com.his.charge.dto.ArrearsPolicyUpsertDTO;
import com.his.charge.entity.BizArrearsPolicy;
import com.his.charge.mapper.BizArrearsPolicyMapper;
import com.his.charge.service.ArrearsControlService;
import com.his.charge.vo.ArrearsPatientVO;
import com.his.charge.vo.ArrearsPolicyVO;
import com.his.common.exception.BusinessException;
import com.his.common.util.TimeUtil;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


/**
 * 欠费管控服务：策略（单行）读写 + 在院欠费患者榜。
 *
 * <p>口径铁律：榜单数字与 {@code InpatientAccountService.arrearsView} 必须逐项对应
 * （已发生 = L1 应收净额，已收 = 净预交 + 账单直收），SQL 写在
 * {@code BizArrearsPolicyMapper.selectArrearsBoard} 注释里钉死了，改口径两边一起改。
 * 这里用批量 SQL 而不是逐行调服务：欠费榜一页 10 个人，按人现算就是 30 次查询。
 */
@Service
@RequiredArgsConstructor
public class ArrearsControlServiceImpl extends ServiceImpl<BizArrearsPolicyMapper, BizArrearsPolicy> implements ArrearsControlService {

    private final BizArrearsPolicyMapper bizArrearsPolicyMapper;

    public ArrearsPolicyVO getPolicy() {
        return toVo(requirePolicy());
    }

    @Transactional(rollbackFor = Exception.class)
    public ArrearsPolicyVO upsertPolicy(ArrearsPolicyUpsertDTO dto) {
        BizArrearsPolicy p = requirePolicy();
        if (dto.getStopEnabled() != null && dto.getStopEnabled() == 1 && dto.getStopLine() == null) {
            throw new BusinessException("开启停费管控必须设置停费线");
        }
        if (dto.getWarnLine() != null && dto.getStopLine() != null
                && dto.getWarnLine().compareTo(dto.getStopLine()) > 0) {
            throw new BusinessException("预警线不能高于停费线（预警先于停费）");
        }
        p.setWarnLine(dto.getWarnLine());
        p.setStopLine(dto.getStopLine());
        p.setStopEnabled(dto.getStopEnabled());
        if (StringUtils.hasText(dto.getStopClasses())) {
            p.setStopClasses(dto.getStopClasses().trim());
        }
        p.setRemark(dto.getRemark());
        p.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        p.setUpdateTime(TimeUtil.nowSeconds());
        bizArrearsPolicyMapper.updateById(p);
        return toVo(p);
    }

    public IPage<ArrearsPatientVO> arrearsBoard(ArrearsBoardQueryDTO query) {
        String kw = query.getKeyword() == null ? null : query.getKeyword().trim();
        if (kw != null && kw.isEmpty()) {
            kw = null;
        }
        return bizArrearsPolicyMapper.selectArrearsBoard(new Page<>(query.getPageNum(), query.getPageSize()), kw);
    }


    private BizArrearsPolicy requirePolicy() {
        BizArrearsPolicy p = bizArrearsPolicyMapper.selectById(1L);
        if (p == null) {
            throw new BusinessException("欠费管控策略不存在（biz_arrears_policy id=1，请先执行 sql/85）");
        }
        return p;
    }

    private ArrearsPolicyVO toVo(BizArrearsPolicy p) {
        ArrearsPolicyVO vo = new ArrearsPolicyVO();
        BeanUtils.copyProperties(p, vo);
        return vo;
    }
}
