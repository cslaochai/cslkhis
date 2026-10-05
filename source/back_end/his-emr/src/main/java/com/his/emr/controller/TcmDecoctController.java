package com.his.emr.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.emr.dto.TcmDecoctAdvanceDTO;
import com.his.emr.dto.TcmDecoctCancelDTO;
import com.his.emr.dto.TcmDecoctIdDTO;
import com.his.emr.dto.TcmDecoctQueryPageDTO;
import com.his.emr.service.TcmDecoctService;
import com.his.emr.vo.TcmDecoctCountVO;
import com.his.emr.vo.TcmDecoctDetailVO;
import com.his.emr.vo.TcmDecoctVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 中药代煎台账（sql/139）。
 *
 * <p>代码长在 his-emr（要读处方与处方明细，模块依赖方向是 emr→pharmacy，反过来建不了单），
 * 但业务归属仍是药房，所以权限码用 {@code pharmacy:tcmDecoct:*}、菜单位于「药事管理」，
 * 与 {@code /charge/dispensing}（发药接口在 emr、权限码是 pharmacy:dispensing:*）同一口径。
 *
 * <p><b>没有新增、也没有删除</b>：单据由发药完成生成，停止流转只能作废。
 */
@Tag(name = "中药代煎台账")
@RestController
@RequestMapping("/pharmacy/tcmDecoct")
@RequiredArgsConstructor
public class TcmDecoctController {

    private final TcmDecoctService tcmDecoctService;

    @Operation(summary = "代煎台账分页")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('pharmacy:tcmDecoct:list')")
    public Result<PageResult<TcmDecoctVO>> listPage(@Valid @RequestBody TcmDecoctQueryPageDTO query) {
        return Result.success(tcmDecoctService.listPage(query));
    }

    @Operation(summary = "代煎单详情（含逐味明细）")
    @GetMapping("/getDetailById")
    @PreAuthorize("hasAuthority('pharmacy:tcmDecoct:list')")
    public Result<TcmDecoctDetailVO> getDetailById(@RequestParam Long id) {
        return Result.success(tcmDecoctService.getDetailById(id));
    }

    @Operation(summary = "四态计数（待煎/已煎/已取/已作废）")
    @GetMapping("/statusCount")
    @PreAuthorize("hasAuthority('pharmacy:tcmDecoct:list')")
    public Result<TcmDecoctCountVO> statusCount() {
        return Result.success(tcmDecoctService.getStatusCount());
    }

    @Operation(summary = "状态推进（待煎→已煎→已取，只进不退）")
    @PostMapping("/advance")
    @PreAuthorize("hasAuthority('pharmacy:tcmDecoct:edit')")
    public Result<TcmDecoctDetailVO> advance(@Valid @RequestBody TcmDecoctAdvanceDTO dto) {
        return Result.success("状态已更新", tcmDecoctService.advance(dto));
    }

    @Operation(summary = "作废（原因必填，本表无删除）")
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('pharmacy:tcmDecoct:cancel')")
    public Result<TcmDecoctDetailVO> cancel(@Valid @RequestBody TcmDecoctCancelDTO dto) {
        return Result.success("代煎单已作废", tcmDecoctService.cancel(dto));
    }

    @Operation(summary = "打印代煎回执（当前为控制台打印桩 + 审计留痕）")
    @PostMapping("/print")
    @PreAuthorize("hasAuthority('pharmacy:tcmDecoct:print')")
    public Result<TcmDecoctDetailVO> print(@Valid @RequestBody TcmDecoctIdDTO dto) {
        return Result.success("已发送到打印出口", tcmDecoctService.printReceipt(dto.getId()));
    }
}
