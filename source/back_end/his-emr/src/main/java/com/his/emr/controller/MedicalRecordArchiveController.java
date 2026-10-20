package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.ArchivePageQueryDTO;
import com.his.emr.service.MedicalRecordArchiveService;
import com.his.emr.vo.BizMedicalRecordArchiveVO;
import com.his.emr.vo.MedicalRecordArchiveCountVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 病历归档控制器：出院/就诊病历的归档、封存与超期催办接口入口。
 */
@Tag(name = "病历归档")
@RestController
@RequestMapping("/charge/archive")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('emr:archiveBorrow:list', 'emr:archive:list')")
public class MedicalRecordArchiveController {

    private final MedicalRecordArchiveService medicalRecordArchiveService;

    /**
     * 归档记录分页：按患者、归档状态与病历号/姓名关键字筛选，收口到登录岗位可见科室。
     * 用于「病历归档」页查询卡；病案借阅申请选病案时复用同一份清单（靠关键字模糊搜）。
     */
    @Operation(summary = "分页查询归档记录")
    @PostMapping("/listPage")
    public Result<PageResult<BizMedicalRecordArchiveVO>> listPage(@Valid @RequestBody ArchivePageQueryDTO queryDTO) {
        return Result.success(medicalRecordArchiveService.selectArchivePage(queryDTO.getPatientId(), queryDTO.getArchiveStatus(),
                queryDTO.getKeyword(), queryDTO.getPageNum(), queryDTO.getPageSize()));
    }

    /**
     * 单份病案的归档详情（归档时间、封存时间等台账信息）。
     * 用于病历归档页的详情弹框。
     */
    @Operation(summary = "获取归档详情")
    @GetMapping("/getById")
    public Result<BizMedicalRecordArchiveVO> getById(@RequestParam Long id) {
        return Result.success(medicalRecordArchiveService.getArchiveDetail(id));
    }

    /**
     * 病案归档：待归档推进为已归档并记归档时间。
     * 用于病历归档页行上的「归档」动作，是借阅复印的前提；超期未归档由催办提醒兜底。
     */
    @PreAuthorize("hasAuthority('emr:archive:edit')")
    @Operation(summary = "归档病历")
    @PostMapping("/archive")
    public Result<Void> archive(@RequestParam Long id) {
        boolean success = medicalRecordArchiveService.archive(id);
        return success ? Result.success("归档成功", null) : Result.error("归档失败");
    }

    /**
     * 病案封存：只有已归档的病案能封，封存后进入只读留档状态。
     * 用于病历归档页行上的「封存」动作（纠纷/复印争议时锁定病案）。
     */
    @PreAuthorize("hasAuthority('emr:archive:edit')")
    @Operation(summary = "封存病历")
    @PostMapping("/seal")
    public Result<Void> seal(@RequestParam Long id) {
        boolean success = medicalRecordArchiveService.seal(id);
        return success ? Result.success("封存成功", null) : Result.error("封存失败");
    }

    /**
     * 归档三态计数（待归档 / 已归档 / 已封存 + 合计）。
     * 用于病历归档页顶部计数卡，由后端聚合，不拿当前页列表去数。
     */
    @Operation(summary = "归档三态计数（待归档 / 已归档 / 已封存）")
    @GetMapping("/statusCount")
    public Result<MedicalRecordArchiveCountVO> statusCount() {
        return Result.success(medicalRecordArchiveService.statusCount());
    }

    /**
     * 手动补跑归档超期提醒：给超时限仍未归档的病历医生发站内信，返回本轮发送条数。
     * 与每日定时的催归档提醒同一口径，每份病历每天最多一条，可反复补跑。
     */
    @PreAuthorize("hasAuthority('emr:archive:edit')")
    @Operation(summary = "手动补跑归档超期提醒")
    @PostMapping("/notifyOverdue")
    public Result<Integer> notifyOverdue() {
        int sent = medicalRecordArchiveService.notifyOverdueArchives();
        return Result.success("已发送 " + sent + " 条归档超期提醒", sent);
    }
}
