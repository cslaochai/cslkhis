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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 住院转科（P4.2：发起 → 转入科室接收 → 停原医嘱 + 换科室换床 + 回写病历）
 *
 * <p>为什么不复用 {@code /patient/inpatient/transfer}：那个端点是**换床**（限同科室）。
 * 两个动作的业务后果完全不同（换床不动科室、不停医嘱、不改病案首页），
 * 共用一个 URL 迟早会出现"以为在换床，其实把患者转到别的科了"。
 *
 * <p>约定：查询一律 GET，写操作一律 POST，路径驼峰。
 *
 * <p>按钮可用性（canAccept / canCancel）由后端在列表与详情里给出，前端不自己判状态。
 */
@Tag(name = "住院转科")
@RestController
@RequestMapping("/patient/inpatient/transferRecord")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ipd:transfer:list')")
public class InpatientTransferController {

    private final InpatientTransferService transferService;

    @Operation(summary = "转科记录分页（toDeptId 用于转入科室工作台过滤）")
    @GetMapping("/listPage")
    public Result<IPage<InpatientTransferVO>> listPage(InpatientTransferQueryPageDTO query) {
        return Result.success(transferService.listPage(query));
    }

    @Operation(summary = "转科详情")
    @GetMapping("/getDetailById")
    public Result<InpatientTransferVO> getDetailById(@RequestParam Long transferId) {
        return Result.success(transferService.getDetailById(transferId));
    }

    @Operation(summary = "某次住院的转科轨迹（按发生顺序升序）")
    @GetMapping("/listByAdmission")
    public Result<List<InpatientTransferVO>> listByAdmission(@RequestParam Long admissionId) {
        return Result.success(transferService.listByAdmission(admissionId));
    }

    @PreAuthorize("hasAuthority('ipd:transfer:add')")
    @Operation(summary = "发起转科（返回转科单号；此时未生效，需转入科室接收）")
    @PostMapping("/save")
    public Result<String> save(@RequestBody @Valid InpatientTransferUpsertDTO dto) {
        return Result.success("转科申请已提交，等待转入科室接收", transferService.save(dto));
    }

    @PreAuthorize("hasAuthority('ipd:transfer:edit')")
    @Operation(summary = "转入科室接收（转科真正生效：停原长期医嘱 + 换科室换床 + 回写病历）")
    @PostMapping("/accept")
    public Result<Void> accept(@RequestBody @Valid InpatientTransferAcceptDTO dto) {
        transferService.accept(dto);
        return Result.success("已接收，转科生效", null);
    }

    @PreAuthorize("hasAuthority('ipd:transfer:delete')")
    @Operation(summary = "取消转科申请（仅「待接收」；已接收的必须再发起一次转科）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid InpatientTransferCancelDTO dto) {
        transferService.cancel(dto);
        return Result.success("转科申请已取消", null);
    }

    @Operation(summary = "待接收转科数（转入科室工作台角标用）")
    @GetMapping("/countPending")
    public Result<Long> countPending(@RequestParam(required = false) Long toDeptId,
                                     @RequestParam(required = false) Long admissionId) {
        return Result.success(transferService.countPending(toDeptId, admissionId));
    }
}
