package com.his.charge.spi;

import com.his.charge.support.DrgCodes;
import com.his.charge.support.DrgFacts;
import com.his.charge.support.DrgGrouper;
import com.his.charge.support.DrgSchemeCache;
import com.his.spi.dto.DrgGroupFactsDTO;
import com.his.spi.service.DrgGroupFacadeService;
import com.his.spi.vo.DrgGroupResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * DRG 入组端口实现。
 */
@Service
@RequiredArgsConstructor
public class DrgGroupFacadeServiceImpl implements DrgGroupFacadeService {

    private final DrgGrouper drgGrouper;
    private final DrgSchemeCache drgSchemeCache;

    @Override
    public List<DrgGroupResultVO> group(List<DrgGroupFactsDTO> factsList) {
        if (factsList == null || factsList.isEmpty()) {
            return List.of();
        }
        // 版本在批首取一次：整批必须同一份方案快照，逐条取会让「同一次统计」跨到两个版本上
        String version = drgSchemeCache.getDefaultVersion();
        List<DrgGroupResultVO> out = new ArrayList<>(factsList.size());
        for (DrgGroupFactsDTO facts : factsList) {
            DrgGrouper.GroupResult result = drgGrouper.group(toFacts(facts), version);
            DrgGroupResultVO vo = new DrgGroupResultVO();
            vo.setDrgCode(result.drgCode());
            vo.setGrouped(result.grouped());
            out.add(vo);
        }
        return out;
    }

    /**
     * 编码串的归一与拆分只在这一处发生：跨域传原文，避免调用方各自抄一份比码口径。
     */
    private DrgFacts toFacts(DrgGroupFactsDTO dto) {
        return new DrgFacts(dto.getMainDiag(),
                DrgCodes.split(dto.getMainOperCodes()),
                DrgCodes.split(dto.getOtherDiagCodes()),
                DrgCodes.split(dto.getOtherOperCodes()),
                dto.getGender(), dto.getAge(), dto.getAgeUnit(), dto.getAdmissionWeightG());
    }
}
