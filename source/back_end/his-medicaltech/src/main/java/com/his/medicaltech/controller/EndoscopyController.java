package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.EndoscopyDTO;
import com.his.medicaltech.service.EndoscopyService;
import com.his.medicaltech.vo.EndoscopyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 内镜亚专业接口（URL 前缀 /medicaltech/endoscopy）
 *
 * <p>操作人一律服务端取当前登录人，不接受前端传姓名。
 */
@Tag(name = "内镜管理")
@RestController
@RequestMapping("/medicaltech/endoscopy")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('medtech:endoscopy:list', 'portal:workbench:view', 'report:stats:list')")
public class EndoscopyController {

    private final EndoscopyService endoscopyService;

    @Operation(summary = "分页查询内镜检查记录")
    @PostMapping("/listPage")
    public Result<PageResult<EndoscopyVO.ListVO>> listPage(@RequestBody EndoscopyDTO.Query query) {
        return Result.success(endoscopyService.pageVO(query));
    }

    @Operation(summary = "内镜统计")
    @GetMapping("/stats")
    public Result<EndoscopyVO.StatsVO> stats() {
        return Result.success(endoscopyService.stats());
    }

    @Operation(summary = "内镜检查详情")
    @GetMapping("/getDetailById")
    public Result<EndoscopyVO.DetailVO> getDetailById(@RequestParam Long recordId) {
        return Result.success(endoscopyService.getDetail(recordId));
    }

    @PreAuthorize("hasAuthority('medtech:endoscopy:add')")
    @Operation(summary = "登记/修改内镜检查")
    @PostMapping("/recordUpsert")
    public Result<EndoscopyVO.DetailVO> recordUpsert(@Valid @RequestBody EndoscopyDTO.RecordUpsert dto) {
        EndoscopyVO.DetailVO vo = endoscopyService.upsertRecord(dto);
        return Result.success("内镜检查 " + vo.getRecordNo() + " 已保存", vo);
    }

    @PreAuthorize("hasAuthority('medtech:endoscopy:edit')")
    @Operation(summary = "签到")
    @PostMapping("/checkIn")
    public Result<EndoscopyVO.DetailVO> checkIn(@Valid @RequestBody EndoscopyDTO.IdOnly dto) {
        endoscopyService.checkIn(dto.getRecordId());
        return Result.success("已签到", endoscopyService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:endoscopy:edit')")
    @Operation(summary = "执行检查（医师/所见/活检信息）")
    @PostMapping("/execute")
    public Result<EndoscopyVO.DetailVO> execute(@Valid @RequestBody EndoscopyDTO.Execute dto) {
        endoscopyService.execute(dto);
        return Result.success("检查已执行", endoscopyService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:endoscopy:edit')")
    @Operation(summary = "活检送病理（生成病理单并回填病理号，重复送检拒绝）")
    @PostMapping("/sendBiopsy")
    public Result<EndoscopyVO.DetailVO> sendBiopsy(@Valid @RequestBody EndoscopyDTO.BiopsySend dto) {
        String orderNo = endoscopyService.sendBiopsy(dto);
        return Result.success("已送检病理，病理号 " + orderNo, endoscopyService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:endoscopy:edit')")
    @Operation(summary = "出具报告")
    @PostMapping("/report")
    public Result<EndoscopyVO.DetailVO> report(@Valid @RequestBody EndoscopyDTO.Report dto) {
        endoscopyService.report(dto);
        return Result.success("报告已出具", endoscopyService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:endoscopy:edit')")
    @Operation(summary = "审核（审核人不得是报告人本人）")
    @PostMapping("/audit")
    public Result<EndoscopyVO.DetailVO> audit(@Valid @RequestBody EndoscopyDTO.Audit dto) {
        endoscopyService.audit(dto);
        return Result.success("审核通过", endoscopyService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:endoscopy:edit')")
    @Operation(summary = "发布报告")
    @PostMapping("/publish")
    public Result<EndoscopyVO.DetailVO> publish(@Valid @RequestBody EndoscopyDTO.Publish dto) {
        endoscopyService.publish(dto.getRecordId());
        return Result.success("报告已发布", endoscopyService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:endoscopy:delete')")
    @Operation(summary = "取消检查")
    @PostMapping("/cancel")
    public Result<EndoscopyVO.DetailVO> cancel(@Valid @RequestBody EndoscopyDTO.Cancel dto) {
        endoscopyService.cancel(dto);
        return Result.success("检查已取消", endoscopyService.getDetail(dto.getRecordId()));
    }
}
