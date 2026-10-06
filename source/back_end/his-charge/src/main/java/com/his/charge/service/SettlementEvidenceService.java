package com.his.charge.service;


import com.his.charge.entity.BizInsuranceSettlement;
import com.his.charge.support.SettlementEvidence;
import java.util.List;

public interface SettlementEvidenceService {

    SettlementEvidence aggregate(BizInsuranceSettlement settlement);

    List<BizInsuranceSettlement> recentSamePatientSettlements(Long patientId, java.time.LocalDateTime since, Long excludeId);
}
