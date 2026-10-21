package com.his.inpatient.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.his.common.base.Result;
import com.his.inpatient.dto.InpatientTransferAcceptDTO;
import com.his.inpatient.dto.InpatientTransferCancelDTO;
import com.his.inpatient.dto.InpatientTransferPageQueryDTO;
import com.his.inpatient.dto.InpatientTransferUpsertDTO;
import com.his.inpatient.service.InpatientTransferService;
import com.his.inpatient.vo.InpatientTransferVO;
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

    /**
     * 转科记录分页：可按住院次、患者、转出与转入科室、状态与类型过滤。
     * 用于「转科管理」页查询卡，按转入科室过滤时即该科室的待办工作台。
     */
    @Operation(summary = "转科记录分页（toDeptId 用于转入科室工作台过滤）")
    @GetMapping("/listPage")
    public Result<IPage<InpatientTransferVO>> listPage(@Valid InpatientTransferPageQueryDTO query) {
        return Result.success(inpatientTransferService.listPage(query));
    }

    /**
     * 单条转科记录详情，含医嘱处置结果说明。
     * 用于「转科管理」页的转科详情弹框。
     */
    @Operation(summary = "转科详情")
    @GetMapping("/getDetailById")
    public Result<InpatientTransferVO> getDetailById(@RequestParam Long transferId) {
        return Result.success(inpatientTransferService.getDetailById(transferId));
    }

    /**
     * 某次住院名下的全部转科记录，按发生顺序升序。
     * 供按住院次回看转科轨迹与追溯当前科室归属时取数（首条的转出科室即入院科室）。
     */
    @Operation(summary = "某次住院的转科轨迹（按发生顺序升序）")
    @GetMapping("/listByAdmission")
    public Result<List<InpatientTransferVO>> listByAdmission(@RequestParam Long admissionId) {
        return Result.success(inpatientTransferService.listByAdmission(admissionId));
    }

    /**
     * 发起转科申请：只留下待接收的单，科室、床位与医嘱都不动。
     * 用于「转科管理」页的发起转科弹框保存。
     *
     * @return 转科单号，接收方凭它定位这张待接收单
     */
    @PreAuthorize("hasAuthority('ipd:transfer:add')")
    @Operation(summary = "发起转科（返回转科单号；此时未生效，需转入科室接收）")
    @PostMapping("/save")
    public Result<String> save(@RequestBody @Valid InpatientTransferUpsertDTO dto) {
        return Result.success("转科申请已提交，等待转入科室接收", inpatientTransferService.save(dto));
    }

    /**
     * 转入科室接收转科申请，转科在此刻才生效：停原长期医嘱、换科室换床并回写转科记录病历。
     * 用于「转科管理」页的接收弹框提交；目标床位已被占时在此拦下。
     */
    @PreAuthorize("hasAuthority('ipd:transfer:edit')")
    @Operation(summary = "转入科室接收（转科真正生效：停原长期医嘱 + 换科室换床 + 回写病历）")
    @PostMapping("/accept")
    public Result<Void> accept(@RequestBody @Valid InpatientTransferAcceptDTO dto) {
        inpatientTransferService.accept(dto);
        return Result.success("已接收，转科生效", null);
    }

    /**
     * 取消转科申请：仅待接收的单可取消，已接收的只能再发起一次反向转科。
     * 用于「转科管理」页待接收行的取消操作，取消原因必填。
     */
    @PreAuthorize("hasAuthority('ipd:transfer:delete')")
    @Operation(summary = "取消转科申请（仅「待接收」；已接收的必须再发起一次转科）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid InpatientTransferCancelDTO dto) {
        inpatientTransferService.cancel(dto);
        return Result.success("转科申请已取消", null);
    }

    /**
     * 待接收转科数量统计。
     * 用于转入科室工作台的待办角标；按住院次过滤时只看该患者。
     */
    @Operation(summary = "待接收转科数（转入科室工作台角标用）")
    @GetMapping("/countPending")
    public Result<Long> countPending(@RequestParam(required = false) Long toDeptId,
                                     @RequestParam(required = false) Long admissionId) {
        return Result.success(inpatientTransferService.countPending(toDeptId, admissionId));
    }
}
