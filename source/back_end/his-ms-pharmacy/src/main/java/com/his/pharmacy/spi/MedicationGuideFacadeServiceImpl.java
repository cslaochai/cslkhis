package com.his.pharmacy.spi;

import com.his.common.exception.BusinessException;
import com.his.pharmacy.dto.PatientMedicationGuideDTO;
import com.his.pharmacy.service.PatientMedicationGuideCapability;
import com.his.pharmacy.vo.PatientMedicationGuideVO;
import com.his.pharmacy.vo.PatientMedicationItemVO;
import com.his.spi.dto.MedicationGuideQueryDTO;
import com.his.spi.service.MedicationGuideFacadeService;
import com.his.spi.vo.MedicationGuideItemVO;
import com.his.spi.vo.MedicationGuideVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 患者端用药说明端口实现：组装逻辑全部留在内部 {@link PatientMedicationGuideCapability}，
 * 这里只做内部 VO → spi VO 的字段映射，不复制任何白话化/注意事项判定。
 */
@Service
@RequiredArgsConstructor
public class MedicationGuideFacadeServiceImpl implements MedicationGuideFacadeService {

    private final PatientMedicationGuideCapability patientMedicationGuideCapability;

    @Override
    public MedicationGuideVO guide(MedicationGuideQueryDTO query) {
        if (query == null || query.getPrescriptionId() == null) {
            throw new BusinessException("prescriptionId不能为空");
        }
        PatientMedicationGuideDTO dto = new PatientMedicationGuideDTO();
        dto.setPrescriptionId(query.getPrescriptionId());
        return toVO(patientMedicationGuideCapability.execute(dto));
    }

    private MedicationGuideVO toVO(PatientMedicationGuideVO src) {
        if (src == null) {
            return null;
        }
        MedicationGuideVO vo = new MedicationGuideVO();
        vo.setPrescriptionId(src.getPrescriptionId());
        vo.setPrescriptionNo(src.getPrescriptionNo());
        vo.setVisitDate(src.getVisitDate());
        vo.setDeptName(src.getDeptName());
        vo.setDoctorName(src.getDoctorName());
        vo.setPrescriptionTypeText(src.getPrescriptionTypeText());
        vo.setDispensed(src.isDispensed());
        vo.setAdvice(src.getAdvice());
        List<PatientMedicationItemVO> srcItems = src.getItems();
        if (CollectionUtils.isEmpty(srcItems)) {
            vo.setItems(List.of());
            return vo;
        }
        List<MedicationGuideItemVO> items = new ArrayList<>(srcItems.size());
        for (PatientMedicationItemVO srcItem : srcItems) {
            items.add(toItem(srcItem));
        }
        vo.setItems(items);
        return vo;
    }

    private MedicationGuideItemVO toItem(PatientMedicationItemVO src) {
        MedicationGuideItemVO item = new MedicationGuideItemVO();
        item.setDrugName(src.getDrugName());
        item.setSpecification(src.getSpecification());
        item.setDosageForm(src.getDosageForm());
        item.setQuantityText(src.getQuantityText());
        item.setDosageText(src.getDosageText());
        item.setFrequencyText(src.getFrequencyText());
        item.setRouteText(src.getRouteText());
        item.setCourseText(src.getCourseText());
        item.setCautions(src.getCautions());
        item.setSpecText(src.getSpecText());
        return item;
    }
}
