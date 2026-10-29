package com.his.platform.spi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.platform.mapper.BizTsaTokenMapper;
import com.his.platform.service.TsaChannelService;
import com.his.platform.service.TsaService;
import com.his.spi.dto.TsaTimeSourceDTO;
import com.his.spi.dto.TsaTokenPageQueryDTO;
import com.his.spi.dto.TsaTokenVerifyDTO;
import com.his.spi.service.TsaStampFacadeService;
import com.his.spi.vo.TsaStampVO;
import com.his.spi.vo.TsaStatusVO;
import com.his.spi.vo.TsaTokenVerifyVO;
import com.his.spi.vo.TsaTokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TsaStampFacadeService 在 his-sign 侧的实现：盖章/验真代理通道，
 * 状态聚合、令牌台账分页、切换时间来源与按台账ID复验转调内部运维服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TsaStampFacadeServiceImpl implements TsaStampFacadeService {

    private final TsaChannelService tsaChannelService;

    private final TsaService tsaService;

    private final BizTsaTokenMapper bizTsaTokenMapper;

    // ---------- 运行时：盖章 / 验真 / 通道就绪 ----------

    @Override
    public TsaStampVO stamp(String digestHex) {
        TsaChannelService.Stamp stamp = tsaChannelService.stamp(digestHex);
        if (stamp == null) {
            return null;
        }
        return new TsaStampVO(stamp.serial(), stamp.tsaTime(), stamp.tokenValue());
    }

    @Override
    public boolean verifyToken(String serial, String digestHex, LocalDateTime tsaTime, String tokenValue) {
        return tsaChannelService.verifyToken(serial, digestHex, tsaTime, tokenValue);
    }

    @Override
    public boolean available() {
        return tsaChannelService.available();
    }

    @Override
    public String readyName() {
        return tsaChannelService.readyName();
    }

    @Override
    public int effectiveTimeSource() {
        return tsaService.effectiveTimeSource();
    }

    @Override
    public long countTokens() {
        return bizTsaTokenMapper.selectCount(null);
    }

    // ---------- TSA 运维 ----------

    @Override
    public TsaStatusVO status() {
        com.his.platform.vo.TsaStatusVO inner = tsaService.status();
        TsaStatusVO vo = new TsaStatusVO();
        BeanUtils.copyProperties(inner, vo);
        return vo;
    }

    @Override
    public IPage<TsaTokenVO> listTokenPage(TsaTokenPageQueryDTO query) {
        com.his.platform.dto.TsaTokenPageQueryDTO inner = new com.his.platform.dto.TsaTokenPageQueryDTO();
        BeanUtils.copyProperties(query, inner);
        IPage<com.his.platform.vo.TsaTokenVO> page = tsaService.listPage(inner);
        Page<TsaTokenVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<TsaTokenVO> rows = new ArrayList<>(page.getRecords().size());
        for (com.his.platform.vo.TsaTokenVO row : page.getRecords()) {
            TsaTokenVO vo = new TsaTokenVO();
            BeanUtils.copyProperties(row, vo);
            rows.add(vo);
        }
        result.setRecords(rows);
        return result;
    }

    @Override
    public void updateTimeSource(TsaTimeSourceDTO dto) {
        tsaService.updateTimeSource(dto.getTimeSource());
    }

    @Override
    public TsaTokenVerifyVO verifyTokenById(TsaTokenVerifyDTO dto) {
        com.his.platform.vo.TsaTokenVerifyVO inner = tsaService.verifyToken(dto.getId());
        TsaTokenVerifyVO vo = new TsaTokenVerifyVO();
        BeanUtils.copyProperties(inner, vo);
        return vo;
    }
}
