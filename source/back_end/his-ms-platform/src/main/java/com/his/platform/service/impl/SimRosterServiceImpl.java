package com.his.platform.service.impl;

import com.his.platform.mapper.SimRosterMapper;
import com.his.platform.service.SimRosterService;
import com.his.platform.vo.SimClinicRosterVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimRosterServiceImpl implements SimRosterService {

    private final SimRosterMapper simRosterMapper;

    @Override
    public List<SimClinicRosterVO> clinicRoster() {
        List<SimClinicRosterVO> doctorRows = simRosterMapper.listDoctorClinics();
        Map<Long, String> triageByDept = simRosterMapper.listTriage().stream()
                .collect(Collectors.toMap(SimClinicRosterVO::getDeptId,
                        SimClinicRosterVO::getTriageUsername, (first, second) -> first));

        Map<Long, SimClinicRosterVO> roster = new LinkedHashMap<>();
        for (SimClinicRosterVO row : doctorRows) {
            roster.putIfAbsent(row.getDeptId(), row);
        }
        List<SimClinicRosterVO> result = new ArrayList<>(roster.values());
        result.forEach(row -> row.setTriageUsername(triageByDept.get(row.getDeptId())));
        return result;
    }
}
