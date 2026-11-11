package com.his.app.controller;

import com.his.app.service.MiniCheckupService;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.spi.vo.PatientCheckupRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 患者端-体检报告（查看本人体检单与已出报告）。
 */
@Tag(name = "患者端-体检报告")
@RestController
@RequestMapping("/miniapp/checkup")
@RequiredArgsConstructor
public class MiniCheckupController {

    private final MiniCheckupService miniCheckupService;

    @Operation(summary = "我的体检记录")
    @PreAuthorize("hasAuthority('PATIENT')")
    @GetMapping("/list")
    public Result<PageResult<PatientCheckupRecordVO>> list(@RequestParam Long patientId,
                                                           @RequestParam int pageNum,
                                                           @RequestParam int pageSize) {
        return Result.success(miniCheckupService.myRecords(patientId, pageNum, pageSize));
    }

    @Operation(summary = "体检报告详情")
    @PreAuthorize("hasAuthority('PATIENT')")
    @GetMapping("/detail")
    public Result<PatientCheckupRecordVO> detail(@RequestParam Long patientId, @RequestParam Long recordId) {
        return Result.success(miniCheckupService.recordDetail(patientId, recordId));
    }
}
