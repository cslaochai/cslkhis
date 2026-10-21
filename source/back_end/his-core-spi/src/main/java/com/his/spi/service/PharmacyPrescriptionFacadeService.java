package com.his.spi.service;

import com.his.spi.vo.PrescriptionPharmacyDetailVO;
import com.his.spi.vo.PrescriptionPharmacyVO;

import java.util.List;

/**
 * 药房域处方端口（事实宿主：临床域 his-emr，实现待临床模块落地）。
 *
 * <p>发药闸门、麻精限量校验、中药代煎建单所需的处方头/明细只读快照，
 * 以及整单发完/退药时的处方状态联动；药房域不直连处方实体与 Mapper。
 */
public interface PharmacyPrescriptionFacadeService {

    /**
     * 按处方ID取处方头只读快照；不存在返回 {@code null}。
     */
    PrescriptionPharmacyVO findPrescription(Long prescriptionId);

    /**
     * 按处方ID取处方明细只读快照（按明细ID升序）；无明细返回空列表。
     */
    List<PrescriptionPharmacyDetailVO> listDetails(Long prescriptionId);

    /**
     * 整单发完：处方推进到已发药，记录发药时间与发药人。
     *
     * @param prescriptionId 处方ID
     * @param pharmacistName 发药药师姓名
     */
    void markDispensed(Long prescriptionId, String pharmacistName);

    /**
     * 退药联动：处方置已退药，记录退费时间/人/原因。
     *
     * @param prescriptionId 处方ID
     * @param operatorName   操作人姓名
     * @param reason         退药原因
     */
    void markReturned(Long prescriptionId, String operatorName, String reason);
}
