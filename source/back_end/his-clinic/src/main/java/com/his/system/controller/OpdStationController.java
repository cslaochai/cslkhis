package com.his.system.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.system.dto.OpdAssignPageQueryDTO;
import com.his.system.dto.OpdAssignUpsertDTO;
import com.his.system.dto.OpdStationPageQueryDTO;
import com.his.system.dto.OpdStationUpsertDTO;
import com.his.system.service.OpdStationService;
import com.his.system.vo.OpdAssignCandidateVO;
import com.his.system.vo.OpdAssignVO;
import com.his.system.vo.OpdStationSelectVO;
import com.his.system.vo.OpdStationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 门诊公共工位字典 + 工位派岗（sql/277）。
 *
 * <p>回答「A 护士今天被排到总导诊/分诊台/输液室/注射室/采血室哪个位」；
 * 派岗不产出出勤事实，只把当天已排上班的人挪到公共位上，不进工时/缺勤闭环。</p>
 */
@Tag(name = "门诊工位派岗")
@RestController
@RequestMapping("/system/opdStation")
@RequiredArgsConstructor
public class OpdStationController {

    private final OpdStationService opdStationService;

    /** 工位下拉（只要求登录：派岗弹框选「排到哪个位」） */
    @Operation(summary = "工位下拉（只要求登录）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/selectList")
    public Result<List<OpdStationSelectVO>> selectList(@RequestParam(required = false) Integer stationKind) {
        return Result.success(opdStationService.selectListVO(stationKind));
    }

    /** 工位字典分页 */
    @Operation(summary = "工位字典分页")
    @PreAuthorize("hasAuthority('opd:station:list')")
    @PostMapping("/stationPage")
    public Result<PageResult<OpdStationVO>> stationPage(@Valid @RequestBody OpdStationPageQueryDTO dto) {
        return Result.success(opdStationService.pageStation(dto));
    }

    /** 新增/修改工位字典（编码全局唯一） */
    @Operation(summary = "新增/修改工位字典（编码全局唯一）")
    @PreAuthorize("hasAuthority('opd:station:add')")
    @PostMapping("/stationUpsert")
    public Result<Long> stationUpsert(@Valid @RequestBody OpdStationUpsertDTO dto) {
        Long id = opdStationService.upsertStation(dto);
        return Result.success(dto.getId() == null ? "新增成功" : "修改成功", id);
    }

    /** 删除工位字典（物理删；已被派岗占用的拦下） */
    @Operation(summary = "删除工位字典（物理删）")
    @PreAuthorize("hasAuthority('opd:station:delete')")
    @DeleteMapping("/stationDelete")
    public Result<Void> stationDelete(@RequestParam Long id) {
        opdStationService.deleteStation(id);
        return Result.success("删除成功", null);
    }

    /** 派岗记录分页（日期区间 + 工位 + 借调 + 姓名关键词） */
    @Operation(summary = "派岗记录分页")
    @PreAuthorize("hasAuthority('opd:station:list')")
    @PostMapping("/assignPage")
    public Result<PageResult<OpdAssignVO>> assignPage(@Valid @RequestBody OpdAssignPageQueryDTO dto) {
        return Result.success(opdStationService.pageAssign(dto));
    }

    /** 派岗候选名单：该日期确实上了班、且岗位类别符合工位要求的人（与保存时的校验同源） */
    @Operation(summary = "派岗候选名单（当天已发布上班 + 岗位类别相符）")
    @PreAuthorize("hasAuthority('opd:station:add')")
    @GetMapping("/assignCandidates")
    public Result<List<OpdAssignCandidateVO>> assignCandidates(@RequestParam Long stationId,
                                                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                              LocalDate assignDate) {
        return Result.success(opdStationService.listAssignCandidates(stationId, assignDate));
    }

    /** 排班员指定派岗（同日同工位同班同人唯一） */
    @Operation(summary = "排班员指定派岗（含跨单元借调登记）")
    @PreAuthorize("hasAuthority('opd:station:add')")
    @PostMapping("/assignUpsert")
    public Result<Long> assignUpsert(@Valid @RequestBody OpdAssignUpsertDTO dto) {
        Long id = opdStationService.upsertAssign(dto);
        return Result.success("派岗成功", id);
    }

    /** 撤销/删除一条派岗（物理删） */
    @Operation(summary = "撤销派岗（物理删）")
    @PreAuthorize("hasAuthority('opd:station:delete')")
    @DeleteMapping("/assignDelete")
    public Result<Void> assignDelete(@RequestParam Long id) {
        opdStationService.deleteAssign(id);
        return Result.success("已撤销", null);
    }
}
