package com.his.spi.service;

import com.his.spi.dto.MedicationGuideQueryDTO;
import com.his.spi.vo.MedicationGuideVO;

/**
 * 患者端用药说明端口（这盒药怎么吃）：按处方ID组装白话用药说明。
 *
 * <p>患者身份由服务端从登录态取，调用方只交代要哪张处方；返回已发药标志、药品明细与固定免责提示。
 */
public interface MedicationGuideFacadeService {

    /**
     * 生成一张处方的用药说明。
     *
     * @param query 处方ID（患者身份服务端取）
     * @return 用药说明；处方不存在/无权查看由提供方抛业务异常
     */
    MedicationGuideVO guide(MedicationGuideQueryDTO query);
}
