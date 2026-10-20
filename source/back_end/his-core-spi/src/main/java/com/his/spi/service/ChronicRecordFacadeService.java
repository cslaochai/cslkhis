package com.his.spi.service;

import com.his.spi.vo.ChronicRecordListVO;

import java.util.List;

/**
 * 慢病建档端口的在管档案查询：把患者当前有效的慢病档案交给跨域做资格判定与病种上下文补充。
 */
public interface ChronicRecordFacadeService {

    /**
     * 取某患者已认定的慢病档案。
     *
     * <p>用于长处方资格判定，以及随访话术生成时补齐「这个患者得的是什么病」的病种上下文。
     *
     * @param patientId 患者ID
     * @return 有效档案，按建档先后倒序；无档案返回空集合
     */
    List<ChronicRecordListVO> activeList(Long patientId);
}
