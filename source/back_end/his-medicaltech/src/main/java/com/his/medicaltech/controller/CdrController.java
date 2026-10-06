package com.his.medicaltech.controller;

import com.his.common.base.Result;
import com.his.medicaltech.dto.CdrQueryDTO;
import com.his.medicaltech.service.CdrService;
import com.his.medicaltech.vo.CdrEventTypeSelectListVO;
import com.his.medicaltech.vo.CdrTimelineVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 患者全景时间轴（CDR / P5.2）
 *
 * <p>单独一个控制器而不是塞进 ReportController：报表是"全院聚合数字"，
 * CDR 是"单个患者的临床脉络"，两者的使用者、权限、性能特征都不同。
 *
 * <p>权限：{@code patient:cdr:list} —— 直接复用菜单权限码（角色配了"患者全景"菜单才有这个码），
 * 不新建一套权限体系。收费员/药剂师/检验技师等角色的菜单里没有这项，所以拿不到这个码
 * （见角色菜单关联）：这正是"按岗位裁剪患者视图"要的效果 ——
 * 窗口岗只看身份与费用，看不到诊断/病历/检验原文。
 */
@Tag(name = "患者全景时间轴（CDR）")
@RestController
@RequestMapping("/report/cdr")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('patient:cdr:list')")
public class CdrController {

    private final CdrService cdrService;

    @Operation(summary = "获取患者全景时间轴（按就诊次组织，含事件与数据缺口）")
    @GetMapping("/getDetailById")
    public Result<CdrTimelineVO> getDetailById(@Valid CdrQueryDTO dto) {
        return Result.success(cdrService.getTimeline(dto));
    }

    @Operation(summary = "事件类型字典（含归属的就诊形态）")
    @GetMapping("/eventDict")
    public Result<List<CdrEventTypeSelectListVO>> eventDict() {
        return Result.success(cdrService.eventDict());
    }
}
