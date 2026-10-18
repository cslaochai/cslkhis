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
 * 满意度评价控制器：问卷发放与回收推进、答卷录入作废、满意度看板统计的接口入口。
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
    public Result<PageResult<SurveyDispatchVO>> dispatchListPage(@Valid @RequestBody SurveyDispatchPageQueryDTO dto) {
        return Result.success(surveyService.dispatchListPage(dto));
    }

    /**
     * 发放单详情：这里返回明文手机号，其余接口只给脱敏值。
     * 用于「满意度评价」页「发放与回收」页签打开答卷代填弹框时，供随访员照着拨号并回填患者信息。
     */
    @PreAuthorize("hasAuthority('qc:survey:edit')")
    @Operation(summary = "发放单详情（外呼拨号要明文手机号，仅此处返回）")
    @GetMapping("/dispatch/getById")
    public Result<SurveyDispatchVO> dispatchGetById(@RequestParam Long id) {
        return Result.success(surveyService.dispatchGetById(id));
    }

    /**
     * 按随访任务手工发放一张问卷，记录回收渠道与有效期；同一任务同一问卷幂等，重复发放只留一条。
     * 用于「随访管理」页随访任务的补发问卷弹框，服务当时没配问卷或需要电话重收一份的缺口。
     */
    @PreAuthorize("hasAuthority('qc:survey:add')")
    @Operation(summary = "按随访任务人工补发问卷（幂等，同任务同卷只一条）")
    @PostMapping("/dispatch/issue")
    public Result<SurveyDispatchVO> dispatchIssue(@Valid @RequestBody SurveyDispatchIssueDTO dto) {
        return Result.success("评价已发放", surveyService.issueFromFollowup(dto));
    }

    /**
     * 推进发放单状态：标记已推送或已拒答并留随访员备注；「已回收」只能由答卷写入，不能在这里点出来。
     * 用于「满意度评价」页「发放与回收」页签操作列的电话外呼结果登记。
     */
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
    public Result<PageResult<SurveyAnswerVO>> answerListPage(@Valid @RequestBody SurveyAnswerPageQueryDTO dto) {
        return Result.success(surveyService.answerListPage(dto));
    }

    /**
     * 答卷详情：除总分与渠道外还带逐题答案快照，列表行不含这部分。
     * 用于「满意度评价」页「答卷台账」页签点开只读详情弹框。
     */
    @PreAuthorize("hasAuthority('qc:survey:list')")
    @Operation(summary = "答卷详情（含逐题答案）")
    @GetMapping("/answer/getById")
    public Result<SurveyAnswerVO> answerGetById(@RequestParam Long id) {
        return Result.success(surveyService.answerGetById(id));
    }

    /**
     * 回收录入一张答卷：一张发放单只落一张答卷，逐题得分由服务端算总分与均分，低分答卷自动登记投诉单。
     * 用于「发放与回收」页签的答卷代填弹框保存（电话代填是当前唯一可回收的渠道）。
     */
    @PreAuthorize("hasAuthority('qc:survey:edit')")
    @Operation(summary = "回收录入（一次发放一张答卷；低分自动转投诉）")
    @PostMapping("/answer/submit")
    public Result<SurveyAnswerVO> answerSubmit(@Valid @RequestBody SurveyAnswerUpsertDTO dto) {
        return Result.success("评价已回收", surveyService.submitAnswer(dto));
    }

    /**
     * 作废填错或重复的答卷：发放单退回待回收可重填，答卷留档不删且不再进统计。
     * 用于「答卷台账」页签操作列的作废，需填写作废原因。
     */
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
