package com.his.clinical.controller;

import com.his.common.base.Result;
import com.his.clinical.service.PrevisitRecordService;
import com.his.spi.vo.PrevisitDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 预问诊（医生站侧）：接诊时按挂号读患者提前提交的病史问卷与摘要。
 */
@Tag(name = "预问诊")
@RestController
@RequestMapping("/previsit")
@RequiredArgsConstructor
public class PrevisitController {

    private final PrevisitRecordService previsitRecordService;

    /**
     * 按挂号取预问诊报告：患者小程序填的病史问卷与 AI 凝摘要，没填过则数据为空。
     * 用于医生工作站接诊时展示的预问诊报告卡，只读参考不改写病历。
     */
    @Operation(summary = "按挂号取预问诊报告（无则返回空 data）")
    @GetMapping("/getByRegist")
    @PreAuthorize("hasAuthority('opd:doctorWorkstation:list')")
    public Result<PrevisitDetailVO> getByRegist(@RequestParam Long registId) {
        return Result.success(previsitRecordService.getByRegist(registId));
    }
}
