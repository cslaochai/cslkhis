package com.his.patient.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.VisitStatusEnum;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.patient.entity.BizVisit;
import com.his.patient.mapper.BizVisitMapper;
import com.his.spi.service.PatientVisitFacadeService;
import com.his.spi.service.RedisSequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 就诊次窄端口适配器：只依赖本域 mapper 与序列端口，不牵住院编排服务，避免 Bean 环。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PatientVisitFacadeServiceImpl implements PatientVisitFacadeService {

    private final BizVisitMapper bizVisitMapper;

    private final RedisSequenceService redisSequenceService;

    @Override
    public Long ensureOpenVisit(Long patientId, Long registId) {
        LocalDateTime now = TimeUtil.nowSeconds();
        LocalDateTime dayStart = TimeUtil.dayStart(now.toLocalDate());
        LocalDateTime dayEnd = TimeUtil.dayStart(now.toLocalDate().plusDays(1));

        BizVisit existing = bizVisitMapper.selectOne(new LambdaQueryWrapper<BizVisit>()
                .eq(BizVisit::getPatientId, patientId)
                .eq(BizVisit::getVisitStatus, VisitStatusEnum.OPEN.getCode())
                .ge(BizVisit::getStartTime, dayStart)
                .lt(BizVisit::getStartTime, dayEnd)
                .orderByDesc(BizVisit::getVisitId)
                .last("LIMIT 1"));
        if (existing != null) {
            if (registId != null && !containsRegistId(existing.getRegistIds(), registId)) {
                existing.setRegistIds(TextUtil.hasText(existing.getRegistIds())
                        ? existing.getRegistIds() + "," + registId
                        : String.valueOf(registId));
                bizVisitMapper.updateById(existing);
            }
            return existing.getVisitId();
        }

        BizVisit visit = new BizVisit();
        visit.setVisitNo(redisSequenceService.generateVisitNo());
        visit.setPatientId(patientId);
        visit.setStartTime(now);
        visit.setTotalAmount(BigDecimal.ZERO);
        visit.setVisitStatus(VisitStatusEnum.OPEN.getCode());
        visit.setRegistIds(registId == null ? null : String.valueOf(registId));
        visit.setRemark("住院收治时建立的就诊次");
        bizVisitMapper.insert(visit);
        log.info("新建就诊次 visitNo={} visitId={} patientId={} registId={}",
                visit.getVisitNo(), visit.getVisitId(), patientId, registId);
        return visit.getVisitId();
    }

    /**
     * regist_ids 是逗号分隔的字符串列；按整段比对，避免 "1" 命中 "11" 这种子串误判
     */
    private boolean containsRegistId(String registIds, Long registId) {
        if (!TextUtil.hasText(registIds) || registId == null) {
            return false;
        }
        for (String part : registIds.split(",")) {
            if (part.trim().equals(String.valueOf(registId))) {
                return true;
            }
        }
        return false;
    }
}
