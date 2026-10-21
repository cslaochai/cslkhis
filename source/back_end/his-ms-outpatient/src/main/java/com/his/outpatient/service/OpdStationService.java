package com.his.outpatient.service;

import com.his.common.base.PageResult;
import com.his.outpatient.dto.OpdAssignPageQueryDTO;
import com.his.outpatient.dto.OpdAssignUpsertDTO;
import com.his.outpatient.dto.OpdStationPageQueryDTO;
import com.his.outpatient.dto.OpdStationUpsertDTO;
import com.his.outpatient.vo.OpdAssignCandidateVO;
import com.his.outpatient.vo.OpdAssignVO;
import com.his.outpatient.vo.OpdStationSelectVO;
import com.his.outpatient.vo.OpdStationVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 门诊公共工位字典 + 工位派岗服务
 */
public interface OpdStationService {

    /**
     * 工位下拉：只列启用工位，供派岗弹框选「排到哪个位」。
     */
    List<OpdStationSelectVO> selectListVO(Integer stationKind);

    /**
     * 工位字典分页。
     */
    PageResult<OpdStationVO> pageStation(OpdStationPageQueryDTO dto);

    /**
     * 新增/修改工位字典（编码全局唯一）。
     */
    Long upsertStation(OpdStationUpsertDTO dto);

    /**
     * 删除工位字典（物理删；已被派岗占用的拦下）。
     */
    void deleteStation(Long id);

    /**
     * 派岗记录分页（日期区间 + 工位 + 借调 + 姓名关键词）。
     */
    PageResult<OpdAssignVO> pageAssign(OpdAssignPageQueryDTO dto);

    /**
     * 派岗候选名单：指定日期确实上了班、且岗位类别符合该工位要求的人，用于派岗弹框选人。
     */
    List<OpdAssignCandidateVO> listAssignCandidates(Long stationId, LocalDate assignDate);

    /**
     * 排班员指定派岗（同日同工位同班同人唯一，重复即撞键）。
     */
    Long upsertAssign(OpdAssignUpsertDTO dto);

    /**
     * 撤销/删除一条派岗（物理删）。
     */
    void deleteAssign(Long id);
}
