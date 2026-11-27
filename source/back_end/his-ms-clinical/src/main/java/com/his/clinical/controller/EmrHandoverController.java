package com.his.clinical.controller;

import com.his.clinical.dto.EmrHandoverAssignDTO;
import com.his.clinical.dto.EmrHandoverPageQueryDTO;
import com.his.clinical.service.EmrHandoverService;
import com.his.clinical.vo.EmrHandoverVO;
import com.his.common.base.PageResult;
import com.his.common.base.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 病历草稿移交代补控制器：离岗/调离医生名下未完成草稿的承接闭环。
 */
@Tag(name = "病历草稿移交代补")
@RestController
@RequestMapping("/emr/handover")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('opd:doctorWorkstation:list', 'emr:medicalRecord:list', 'emr:medicalReview:list', 'opd:todayVisits:list', 'opd:emergencyDoctor:list')")
public class EmrHandoverController {

    private final EmrHandoverService emrHandoverService;

    /**
     * 承接人视角：本人待代补的草稿移交记录。
     */
    @Operation(summary = "我的待代补草稿")
    @GetMapping("/myReceiving")
    public Result<List<EmrHandoverVO>> myReceiving() {
        return Result.success(emrHandoverService.listMyReceiving());
    }

    /**
     * 管理侧移交台账分页（科室上级/质控/医务查看全部移交记录）。
     */
    @PreAuthorize("hasAuthority('emr:medicalRecord:handover')")
    @Operation(summary = "移交台账分页")
    @PostMapping("/listPage")
    public Result<PageResult<EmrHandoverVO>> listPage(@Valid @RequestBody EmrHandoverPageQueryDTO queryDTO) {
        return Result.success(emrHandoverService.pageManaged(queryDTO));
    }

    /**
     * 批量移交草稿给承接人（上级医师/质控岗代补）。
     */
    @PreAuthorize("hasAuthority('emr:medicalRecord:handover')")
    @Operation(summary = "移交草稿代补")
    @PostMapping("/assign")
    public Result<Void> assign(@Valid @RequestBody EmrHandoverAssignDTO dto) {
        emrHandoverService.assign(dto);
        return Result.success("移交成功", null);
    }

    /**
     * 撤销待代补移交单。
     */
    @PreAuthorize("hasAuthority('emr:medicalRecord:handover')")
    @Operation(summary = "撤销移交单")
    @PostMapping("/revoke")
    public Result<Void> revoke(@RequestParam Long id) {
        emrHandoverService.revoke(id);
        return Result.success("已撤销", null);
    }
}
