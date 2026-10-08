package com.his.patient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.patient.dto.InpatientTransferAcceptDTO;
import com.his.patient.dto.InpatientTransferCancelDTO;
import com.his.patient.dto.InpatientTransferQueryPageDTO;
import com.his.patient.dto.InpatientTransferUpsertDTO;
import com.his.patient.service.InpatientTransferService;
import com.his.patient.vo.InpatientTransferVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 住院转科（P4.2：发起 → 转入科室接收 → 停原医嘱 + 换科室换床 + 回写病历）
 */
@Tag(name = "住院转科")
@RestController
@RequestMapping("/patient/inpatient/transferRecord")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ipd:transfer:list')")
public class InpatientTransferController {

    private final InpatientTransferService inpatientTransferService;

    @Operation(summary = "转科记录分页（toDeptId 用于转入科室工作台过滤）")
    @GetMapping("/listPage")
    public Result<IPage<InpatientTransferVO>> listPage(@Valid InpatientTransferQueryPageDTO query) {
        return Result.success(inpatientTransferService.listPage(query));
    }

    @Operation(summary = "转科详情")
    @GetMapping("/getDetailById")
    public Result<InpatientTransferVO> getDetailById(@RequestParam Long transferId) {
        return Result.success(inpatientTransferService.getDetailById(transferId));
    }

    @Operation(summary = "某次住院的转科轨迹（按发生顺序升序）")
    @GetMapping("/listByAdmission")
    public Result<List<InpatientTransferVO>> listByAdmission(@RequestParam Long admissionId) {
        return Result.success(inpatientTransferService.listByAdmission(admissionId));
    }

    @PreAuthorize("hasAuthority('ipd:transfer:add')")
    @Operation(summary = "发起转科（返回转科单号；此时未生效，需转入科室接收）")
    @PostMapping("/save")
    public Result<String> save(@RequestBody @Valid InpatientTransferUpsertDTO dto) {
        return Result.success("转科申请已提交，等待转入科室接收", inpatientTransferService.save(dto));
    }

    @PreAuthorize("hasAuthority('ipd:transfer:edit')")
    @Operation(summary = "转入科室接收（转科真正生效：停原长期医嘱 + 换科室换床 + 回写病历）")
    @PostMapping("/accept")
    public Result<Void> accept(@RequestBody @Valid InpatientTransferAcceptDTO dto) {
        inpatientTransferService.accept(dto);
        return Result.success("已接收，转科生效", null);
    }

    @PreAuthorize("hasAuthority('ipd:transfer:delete')")
    @Operation(summary = "取消转科申请（仅「待接收」；已接收的必须再发起一次转科）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid InpatientTransferCancelDTO dto) {
        inpatientTransferService.cancel(dto);
        return Result.success("转科申请已取消", null);
    }

    @Operation(summary = "待接收转科数（转入科室工作台角标用）")
    @GetMapping("/countPending")
    public Result<Long> countPending(@RequestParam(required = false) Long toDeptId,
                                     @RequestParam(required = false) Long admissionId) {
        return Result.success(inpatientTransferService.countPending(toDeptId, admissionId));
    }
}
