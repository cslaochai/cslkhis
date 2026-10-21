package com.his.patient.controller;

import com.his.common.base.Result;
import com.his.patient.dto.AttendingBindDTO;
import com.his.patient.service.AttendingRelationService;
import com.his.patient.vo.AttendingRelationVO;
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
 * 主管医生管床关系（biz_attending_relation）：从住院管理页剥离后由患者域承接，URL 保持原契约。
 */
@Tag(name = "管床关系")
@RestController
@RequestMapping("/patient/inpatient")
@RequiredArgsConstructor
public class AttendingRelationController {

    /**
     * 住院参照数据的读权限持有者：入出院/会诊/手术/转科/输血/摆药 六类页面各自的 list 码任一即可。
     */
    private static final String READERS = "'ipd:inpatient:list', 'ipd:consultation:list', 'ipd:surgery:list', "
            + "'ipd:transfer:list', 'medtech:transfusion:list', 'pharmacy:wardDispense:list'";

    private static final String READ = "hasAnyAuthority(" + READERS + ")";

    private final AttendingRelationService attendingRelationService;

    @PreAuthorize(READ)
    @Operation(summary = "管床关系列表（主管/主诊组长/协作）")
    @GetMapping("/attending/list")
    public Result<List<AttendingRelationVO>> attendingList(@RequestParam Long admissionId,
                                                            @RequestParam(required = false) Integer status) {
        return Result.success(attendingRelationService.listByAdmission(admissionId, status));
    }

    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "建立/转交管床关系（主管/主诊组长/协作）")
    @PostMapping("/attending/bind")
    public Result<String> attendingBind(@RequestBody @Valid AttendingBindDTO dto) {
        return Result.success("管床关系已保存", String.valueOf(attendingRelationService.bind(dto)));
    }

    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "结束管床关系（置失效留痕）")
    @PostMapping("/attending/end")
    public Result<Void> attendingEnd(@RequestParam Long id) {
        attendingRelationService.endById(id);
        return Result.success("管床关系已结束", null);
    }

    @PreAuthorize("hasAuthority('ipd:inpatient:edit')")
    @Operation(summary = "删除误建管床关系（物理删）")
    @DeleteMapping("/attending/delete")
    public Result<Void> attendingDelete(@RequestParam Long id) {
        attendingRelationService.deleteById(id);
        return Result.success("已删除", null);
    }
}
