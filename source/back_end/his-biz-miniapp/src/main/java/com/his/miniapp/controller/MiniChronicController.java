package com.his.miniapp.controller;

import com.his.api.service.EmrPatientApi;
import com.his.common.base.Result;
import com.his.api.vo.ChronicMyRecordsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 患者端慢病档案
 */
@Tag(name = "患者端-慢病档案")
@RestController
@RequestMapping("/miniapp/chronic")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PATIENT')")
public class MiniChronicController {

    private final EmrPatientApi emrPatientApi;

    @Operation(summary = "我的慢病档案（含长处方资格）")
    @GetMapping("/myRecords")
    public Result<ChronicMyRecordsVO> myRecords() {
        return Result.success(emrPatientApi.myRecords());
    }
}
