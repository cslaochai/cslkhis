package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.*;
import com.his.emr.service.SurveyService;
import com.his.emr.vo.SurveyAnswerVO;
import com.his.emr.vo.SurveyDispatchVO;
import com.his.emr.vo.SurveyStatVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 满意度评价发放/回收与看板
 */
@Tag(name = "满意度评价发放与回收")
@RestController
@RequestMapping("/survey")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    // 发放与回收

    @PreAuthorize("hasAuthority('qc:survey:list')")
    @Operation(summary = "发放/回收台账分页（手机号出参脱敏）")
    @PostMapping("/dispatch/listPage")
    public Result<PageResult<SurveyDispatchVO>> dispatchListPage(@Valid @RequestBody SurveyDispatchQueryPageDTO dto) {
        return Result.success(surveyService.dispatchListPage(dto));
    }

    @PreAuthorize("hasAuthority('qc:survey:edit')")
    @Operation(summary = "发放单详情（外呼拨号要明文手机号，仅此处返回）")
    @GetMapping("/dispatch/getById")
    public Result<SurveyDispatchVO> dispatchGetById(@RequestParam Long id) {
        return Result.success(surveyService.dispatchGetById(id));
    }

    @PreAuthorize("hasAuthority('qc:survey:add')")
    @Operation(summary = "按随访任务人工补发问卷（幂等，同任务同卷只一条）")
    @PostMapping("/dispatch/issue")
    public Result<SurveyDispatchVO> dispatchIssue(@Valid @RequestBody SurveyDispatchIssueDTO dto) {
        return Result.success("评价已发放", surveyService.issueFromFollowup(dto));
    }

    @PreAuthorize("hasAuthority('qc:survey:add')")
    @Operation(summary = "发放单状态推进（1-标记已推送 2-标记已拒答）")
    @PostMapping("/dispatch/mark")
    public Result<SurveyDispatchVO> dispatchMark(@Valid @RequestBody SurveyDispatchActionDTO dto) {
        return Result.success("已更新", surveyService.markDispatch(dto));
    }

    // 答卷

    @PreAuthorize("hasAuthority('qc:survey:list')")
    @Operation(summary = "答卷分页")
    @PostMapping("/answer/listPage")
    public Result<PageResult<SurveyAnswerVO>> answerListPage(@Valid @RequestBody SurveyAnswerQueryPageDTO dto) {
        return Result.success(surveyService.answerListPage(dto));
    }

    @PreAuthorize("hasAuthority('qc:survey:list')")
    @Operation(summary = "答卷详情（含逐题答案）")
    @GetMapping("/answer/getById")
    public Result<SurveyAnswerVO> answerGetById(@RequestParam Long id) {
        return Result.success(surveyService.answerGetById(id));
    }

    @PreAuthorize("hasAuthority('qc:survey:edit')")
    @Operation(summary = "回收录入（一次发放一张答卷；低分自动转投诉）")
    @PostMapping("/answer/submit")
    public Result<SurveyAnswerVO> answerSubmit(@Valid @RequestBody SurveyAnswerUpsertDTO dto) {
        return Result.success("评价已回收", surveyService.submitAnswer(dto));
    }

    @PreAuthorize("hasAuthority('qc:survey:edit')")
    @Operation(summary = "答卷作废（填错/重复；发放单退回待回收，答卷留档不删）")
    @PostMapping("/answer/voidAnswer")
    public Result<SurveyAnswerVO> answerVoid(@Valid @RequestBody SurveyAnswerVoidDTO dto) {
        return Result.success("答卷已作废", surveyService.voidAnswer(dto));
    }

    // 看板

    @PreAuthorize("hasAuthority('qc:survey:list')")
    @Operation(summary = "满意度看板（回收率/均分/NPS/维度短板/科室短板/渠道回收/近30日）")
    @GetMapping("/stat")
    public Result<SurveyStatVO> stat(@RequestParam(required = false) Long templateId,
                                     @RequestParam(required = false) Integer scene,
                                     @RequestParam(required = false) String dateFrom,
                                     @RequestParam(required = false) String dateTo) {
        return Result.success(surveyService.stat(templateId, scene, dateFrom, dateTo));
    }
}
