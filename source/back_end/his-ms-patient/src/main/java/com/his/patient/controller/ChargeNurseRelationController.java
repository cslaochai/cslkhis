package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.ChargeNurseBindDTO;
import com.his.patient.service.ChargeNurseRelationService;
import com.his.patient.vo.ChargeNurseRelationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 责任护士关系（biz_charge_nurse_relation）：从住院管理页剥离后由患者域承接，URL 保持原契约。
 */
@Tag(name = "责任护士关系")
@RestController
@RequestMapping("/patient/inpatient")
@RequiredArgsConstructor
public class ChargeNurseRelationController {

    /**
     * 住院参照数据的读权限持有者：入出院/会诊/手术/转科/输血/摆药 六类页面各自的 list 码任一即可。
     */
    private static final String READERS = "'ipd:inpatient:list', 'ipd:consultation:list', 'ipd:surgery:list', "
            + "'ipd:transfer:list', 'medtech:transfusion:list', 'pharmacy:wardDispense:list'";

    private static final String READ = "hasAnyAuthority(" + READERS + ")";

    private final ChargeNurseRelationService chargeNurseRelationService;

    @PreAuthorize(READ)
    @Operation(summary = "责任护士关系列表")
    @GetMapping("/nurse/list")
    public Result<List<ChargeNurseRelationVO>> nurseList(@RequestParam Long admissionId,
                                                         @RequestParam(required = false) Integer status) {
        return Result.success(chargeNurseRelationService.listByAdmission(admissionId, status));
    }

    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "建立/转交责任护士")
    @PostMapping("/nurse/bind")
    public Result<String> nurseBind(@RequestBody @Valid ChargeNurseBindDTO dto) {
        return Result.success("责任护士已保存", String.valueOf(chargeNurseRelationService.bind(dto)));
    }

    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "结束责任护士关系（置失效留痕）")
    @PostMapping("/nurse/end")
    public Result<Void> nurseEnd(@RequestParam Long id) {
        chargeNurseRelationService.endById(id);
        return Result.success("责任护士关系已结束", null);
    }

    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "删除误建责任护士关系（物理删）")
    @DeleteMapping("/nurse/delete")
    public Result<Void> nurseDelete(@RequestParam Long id) {
        chargeNurseRelationService.deleteById(id);
        return Result.success("已删除", null);
    }
}
