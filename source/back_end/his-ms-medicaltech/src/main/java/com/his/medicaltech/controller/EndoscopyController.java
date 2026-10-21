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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 内镜检查控制器：内镜登记、签到、执行、活检送检与报告审核发布的接口入口。
 */
@Tag(name = "内镜管理")
@RestController
@RequestMapping("/medicaltech/endoscopy")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('medtech:endoscopy:list', 'portal:workbench:view', 'report:stats:list')")
public class EndoscopyController {

    private final EndoscopyService endoscopyService;

    /**
     * 内镜检查记录分页：按检查号、患者、内镜类型、状态与检查日期区间收口。
     * 用于内镜工作站主表格的查询列表。
     */
    @Operation(summary = "分页查询内镜检查记录")
    @PostMapping("/listPage")
    public Result<PageResult<EndoscopyVO.ListVO>> listPage(@Valid @RequestBody EndoscopyDTO.Query query) {
        return Result.success(endoscopyService.pageVO(query));
    }

    /**
     * 内镜工作量按状态汇总：待检、检查中、待审核、已发布与活检送检条数。
     * 用于内镜工作站查询卡下方的状态统计。
     */
    @Operation(summary = "内镜统计")
    @GetMapping("/stats")
    public Result<EndoscopyVO.StatsVO> stats() {
        return Result.success(endoscopyService.stats());
    }

    /**
     * 单条内镜检查详情：含镜下所见、活检送检信息与报告全文。
     * 用于工作站列表行点击打开的详情弹框。
     */
    @Operation(summary = "内镜检查详情")
    @GetMapping("/getDetailById")
    public Result<EndoscopyVO.DetailVO> getDetailById(@RequestParam Long recordId) {
        return Result.success(endoscopyService.getDetail(recordId));
    }

    /**
     * 登记或修改内镜检查：患者与检查安排信息一次落主档。
     * 用于工作站「登记检查」弹框，新增与修改共用同一个入口。
     */
    @PreAuthorize("hasAuthority('medtech:endoscopy:add')")
    @Operation(summary = "登记/修改内镜检查")
    @PostMapping("/recordUpsert")
    public Result<EndoscopyVO.DetailVO> recordUpsert(@Valid @RequestBody EndoscopyDTO.RecordUpsert dto) {
        EndoscopyVO.DetailVO vo = endoscopyService.upsertRecord(dto);
        return Result.success("内镜检查 " + vo.getRecordNo() + " 已保存", vo);
    }

    /**
     * 患者到检签到：状态从已登记推进到已签到。
     * 用于工作站列表行的「签到」动作，是内镜检查开始前的排队确认环节。
     */
    @PreAuthorize("hasAuthority('medtech:endoscopy:edit')")
    @Operation(summary = "签到")
    @PostMapping("/checkIn")
    public Result<EndoscopyVO.DetailVO> checkIn(@Valid @RequestBody EndoscopyDTO.IdOnly dto) {
        endoscopyService.checkIn(dto.getRecordId());
        return Result.success("已签到", endoscopyService.getDetail(dto.getRecordId()));
    }

    /**
     * 回写检查执行结果：内镜医师、镜下所见，肠镜带 Boston 肠道准备评分、胃镜带幽门螺杆菌结果，
     * 两者按内镜类型互斥。用于签到后的「执行」弹框保存，检查中可再次保存补全所见。
     */
    @PreAuthorize("hasAuthority('medtech:endoscopy:edit')")
    @Operation(summary = "执行检查（医师/所见/活检信息）")
    @PostMapping("/execute")
    public Result<EndoscopyVO.DetailVO> execute(@Valid @RequestBody EndoscopyDTO.Execute dto) {
        endoscopyService.execute(dto);
        return Result.success("检查已执行", endoscopyService.getDetail(dto.getRecordId()));
    }

    /**
     * 活检标本送病理：生成病理单并回填病理号，同一检查重复送检由服务端拒绝。
     * 用于工作站检查中行的「送病理」，是内镜与病理两个亚专业之间的联动点。
     */
    @PreAuthorize("hasAuthority('medtech:endoscopy:edit')")
    @Operation(summary = "活检送病理（生成病理单并回填病理号，重复送检拒绝）")
    @PostMapping("/sendBiopsy")
    public Result<EndoscopyVO.DetailVO> sendBiopsy(@Valid @RequestBody EndoscopyDTO.BiopsySend dto) {
        String orderNo = endoscopyService.sendBiopsy(dto);
        return Result.success("已送检病理，病理号 " + orderNo, endoscopyService.getDetail(dto.getRecordId()));
    }

    /**
     * 出具内镜报告：镜下诊断与报告正文落库，状态从检查中推进到已出报告。
     * 用于工作站「报告」弹框提交。
     */
    @PreAuthorize("hasAuthority('medtech:endoscopy:edit')")
    @Operation(summary = "出具报告")
    @PostMapping("/report")
    public Result<EndoscopyVO.DetailVO> report(@Valid @RequestBody EndoscopyDTO.Report dto) {
        endoscopyService.report(dto);
        return Result.success("报告已出具", endoscopyService.getDetail(dto.getRecordId()));
    }

    /**
     * 审核内镜报告：审核人与报告人不得为同一人，由服务端硬校验。
     * 用于工作站待审核行的「审核」，是发布前的质控环节。
     */
    @PreAuthorize("hasAuthority('medtech:endoscopy:edit')")
    @Operation(summary = "审核（审核人不得是报告人本人）")
    @PostMapping("/audit")
    public Result<EndoscopyVO.DetailVO> audit(@Valid @RequestBody EndoscopyDTO.Audit dto) {
        endoscopyService.audit(dto);
        return Result.success("审核通过", endoscopyService.getDetail(dto.getRecordId()));
    }

    /**
     * 发布内镜报告：仅已审核的报告可发布，发布后申请方才能查看结论。
     * 用于工作站已审核行的「发布」。
     */
    @PreAuthorize("hasAuthority('medtech:endoscopy:edit')")
    @Operation(summary = "发布报告")
    @PostMapping("/publish")
    public Result<EndoscopyVO.DetailVO> publish(@Valid @RequestBody EndoscopyDTO.Publish dto) {
        endoscopyService.publish(dto.getRecordId());
        return Result.success("报告已发布", endoscopyService.getDetail(dto.getRecordId()));
    }

    /**
     * 取消内镜检查：作废原因必填，已发布与已取消的记录由服务端拒绝再取消。
     * 用于工作站未走完发布流程的行点「取消」并填写原因。
     */
    @PreAuthorize("hasAuthority('medtech:endoscopy:delete')")
    @Operation(summary = "取消检查")
    @PostMapping("/cancel")
    public Result<EndoscopyVO.DetailVO> cancel(@Valid @RequestBody EndoscopyDTO.Cancel dto) {
        endoscopyService.cancel(dto);
        return Result.success("检查已取消", endoscopyService.getDetail(dto.getRecordId()));
    }
}
