package com.his.medicaltech.controller;

import com.his.common.base.Result;
import com.his.medicaltech.dto.CdrQueryDTO;
import com.his.medicaltech.service.CdrService;
import com.his.medicaltech.vo.CdrEventTypeSelectListVO;
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
 * 患者全景时间轴（CDR / P5.2）
 */
@Tag(name = "患者全景时间轴（CDR）")
@RestController
@RequestMapping("/report/cdr")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:cdr:list')")
public class CdrController {

    private final CdrService cdrService;

    @Operation(summary = "获取患者全景时间轴（按就诊次组织，含事件与数据缺口）")
    @GetMapping("/getDetailById")
    public Result<CdrTimelineVO> getDetailById(@Valid CdrQueryDTO dto) {
        return Result.success(cdrService.getTimeline(dto));
    }

    @Operation(summary = "事件类型字典（含归属的就诊形态）")
    @GetMapping("/eventDict")
    public Result<List<CdrEventTypeSelectListVO>> eventDict() {
        return Result.success(cdrService.eventDict());
    }
}
