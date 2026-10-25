package com.his.medicaltech.controller;

import com.his.common.base.Result;
import com.his.medicaltech.dto.CdrQueryDTO;
import com.his.medicaltech.service.CdrService;
import com.his.medicaltech.vo.CdrEventTypeSelectListVO;
import com.his.medicaltech.vo.CdrSummaryVO;
import com.his.medicaltech.vo.CdrTimelineVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 患者全景时间轴控制器：把散在门诊、住院、医技、收费、病案的记录按就诊次串成时间轴的接口入口。
 */
@Tag(name = "患者全景时间轴（CDR）")
@RestController
@RequestMapping("/report/cdr")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:cdr:list')")
public class CdrController {

    private final CdrService cdrService;

    /**
     * 取一名患者的全景时间轴：按就诊次组织事件，并给出病历完整性缺口与归属不到就诊次的记录。
     * 用于患者全景时间轴页面，也用于患者详情弹框的「就诊脉络」页签。
     *
     * @param dto 患者必填，日期区间与事件类型可选；查询口径含档案归并，被并档案的历史一并出现
     */
    @Operation(summary = "获取患者全景时间轴（按就诊次组织，含事件与数据缺口）")
    @GetMapping("/getDetailById")
    public Result<CdrTimelineVO> getDetailById(@Valid CdrQueryDTO dto) {
        return Result.success(cdrService.getTimeline(dto));
    }

    /**
     * 事件类型字典：每种事件连同它归属的就诊形态。
     * 用于患者全景时间轴页面的事件类型筛选下拉。
     */
    @Operation(summary = "事件类型字典（含归属的就诊形态）")
    @GetMapping("/eventDict")
    public Result<List<CdrEventTypeSelectListVO>> eventDict() {
        return Result.success(cdrService.eventDict());
    }

    @Operation(summary = "临床画像（过敏/慢病/危急值/异常检验/30天就诊/重复检查）")
    @GetMapping("/clinicalSummary")
    public Result<java.util.Map<String, Object>> clinicalSummary(@Valid CdrQueryDTO dto) {
        return Result.success(cdrService.getClinicalSummary(dto));
    }
}
