package com.his.medicaltech.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.medicaltech.dto.UltrasoundDTO;
import com.his.medicaltech.service.UltrasoundService;
import com.his.medicaltech.vo.UltrasoundVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 超声亚专业控制器：超声检查登记、签到、执行、测量、报告审核与发布全链的接口入口。
 */
@Tag(name = "超声管理")
@RestController
@RequestMapping("/medicaltech/ultrasound")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('medtech:ultrasound:list')")
public class UltrasoundController {

    private final UltrasoundService ultrasoundService;

    /**
     * 超声检查单分页：按检查号、患者、检查类型、状态与日期区间筛选。
     * 用于超声检查管理页查询卡。
     */
    @Operation(summary = "分页查询超声检查记录")
    @PostMapping("/listPage")
    public Result<PageResult<UltrasoundVO.ListVO>> listPage(@Valid @RequestBody UltrasoundDTO.Query query) {
        return Result.success(ultrasoundService.pageVO(query));
    }

    /**
     * 统计待检、检查中、待审核、已发布与今日检查量。
     * 超声管理页与列表并行拉取，渲染顶部环节计数卡。
     */
    @Operation(summary = "超声统计")
    @GetMapping("/stats")
    public Result<UltrasoundVO.StatsVO> stats() {
        return Result.success(ultrasoundService.stats());
    }

    /**
     * 超声检查全量详情，含结构化测量值清单。
     * 超声管理页打开详情弹框及各环节弹框回显时加载。
     */
    @Operation(summary = "超声检查详情（含结构化测量值）")
    @GetMapping("/getDetailById")
    public Result<UltrasoundVO.DetailVO> getDetailById(@RequestParam Long recordId) {
        return Result.success(ultrasoundService.getDetail(recordId));
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:add')")
    /**
     * 新建或修订超声检查单据，回执带检查号。
     * 用于超声管理页登记/编辑弹框保存。
     */
    @Operation(summary = "登记/修改超声检查")
    @PostMapping("/recordUpsert")
    public Result<UltrasoundVO.DetailVO> recordUpsert(@Valid @RequestBody UltrasoundDTO.RecordUpsert dto) {
        UltrasoundVO.DetailVO vo = ultrasoundService.upsertRecord(dto);
        return Result.success("超声检查 " + vo.getRecordNo() + " 已保存", vo);
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:edit')")
    /**
     * 登记患者到场，检查单进入待检环节。
     * 用于超声管理页对已登记患者执行签到。
     */
    @Operation(summary = "签到")
    @PostMapping("/checkIn")
    public Result<UltrasoundVO.DetailVO> checkIn(@Valid @RequestBody UltrasoundDTO.IdOnly dto) {
        ultrasoundService.checkIn(dto.getRecordId());
        return Result.success("已签到", ultrasoundService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:edit')")
    /**
     * 登记检查执行情况并推进单据状态。
     * 用于超声检查执行弹框由检查医师完成操作后提交。
     */
    @Operation(summary = "执行检查")
    @PostMapping("/execute")
    public Result<UltrasoundVO.DetailVO> execute(@Valid @RequestBody UltrasoundDTO.Execute dto) {
        ultrasoundService.execute(dto);
        return Result.success("检查已执行", ultrasoundService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:add')")
    /**
     * 整单覆盖保存结构化测量值，异常标志由服务端判定。
     * 用于超声管理页检查中录入测量数据后的保存。
     */
    @Operation(summary = "保存结构化测量值（整单覆盖，异常标志服务端判定）")
    @PostMapping("/saveMeasures")
    public Result<UltrasoundVO.DetailVO> saveMeasures(@Valid @RequestBody UltrasoundDTO.MeasureSave dto) {
        int n = ultrasoundService.saveMeasures(dto);
        return Result.success("已保存 " + n + " 条测量值", ultrasoundService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:edit')")
    /**
     * 填写超声所见与提示意见出具报告。
     * 用于超声报告编辑弹框提交报告内容。
     */
    @Operation(summary = "出具报告（超声所见 + 提示）")
    @PostMapping("/report")
    public Result<UltrasoundVO.DetailVO> report(@Valid @RequestBody UltrasoundDTO.Report dto) {
        ultrasoundService.report(dto);
        return Result.success("报告已出具", ultrasoundService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:edit')")
    /**
     * 对已出具的报告审核签字，审核人不得为报告人本人。
     * 用于超声管理页对已报告单据执行审核。
     */
    @Operation(summary = "审核（审核人不得是报告人本人）")
    @PostMapping("/audit")
    public Result<UltrasoundVO.DetailVO> audit(@Valid @RequestBody UltrasoundDTO.Audit dto) {
        ultrasoundService.audit(dto);
        return Result.success("审核通过", ultrasoundService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:edit')")
    /**
     * 发布已审核的超声报告，报告链路收口。
     * 用于超声管理页对审核通过的报告执行发布。
     */
    @Operation(summary = "发布报告")
    @PostMapping("/publish")
    public Result<UltrasoundVO.DetailVO> publish(@Valid @RequestBody UltrasoundDTO.Publish dto) {
        ultrasoundService.publish(dto.getRecordId());
        return Result.success("报告已发布", ultrasoundService.getDetail(dto.getRecordId()));
    }

    @PreAuthorize("hasAuthority('medtech:ultrasound:delete')")
    /**
     * 取消超声检查并记录取消原因。
     * 用于超声管理页终止尚未完成的检查。
     */
    @Operation(summary = "取消检查")
    @PostMapping("/cancel")
    public Result<UltrasoundVO.DetailVO> cancel(@Valid @RequestBody UltrasoundDTO.Cancel dto) {
        ultrasoundService.cancel(dto);
        return Result.success("检查已取消", ultrasoundService.getDetail(dto.getRecordId()));
    }
}
