package com.his.pharmacy.controller;

import com.his.common.base.PageResult;
import com.his.common.base.Result;
import com.his.pharmacy.dto.TcmDecoctAdvanceDTO;
import com.his.pharmacy.dto.TcmDecoctCancelDTO;
import com.his.pharmacy.dto.TcmDecoctIdDTO;
import com.his.pharmacy.dto.TcmDecoctPageQueryDTO;
import com.his.pharmacy.service.TcmDecoctService;
import com.his.pharmacy.vo.TcmDecoctCountVO;
import com.his.pharmacy.vo.TcmDecoctDetailVO;
import com.his.pharmacy.vo.TcmDecoctVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 中药代煎台账控制器：代煎单查询、状态推进、作废与回执打印的接口入口。
 */
@Tag(name = "中药代煎台账")
@RestController
@RequestMapping("/pharmacy/tcmDecoct")
@RequiredArgsConstructor
public class TcmDecoctController {

    private final TcmDecoctService tcmDecoctService;

    /**
     * 代煎台账分页：按单号/患者/处方号关键字与代煎状态筛出煎药任务，附剂数、味数、总克数与煎法脚注。
     * 用于「中药代煎」页查询卡下方的台账表格，单据由发药完成写入，页面没有新增入口。
     */
    @Operation(summary = "代煎台账分页")
    @PostMapping("/listPage")
    @PreAuthorize("hasAuthority('pharmacy:tcmDecoct:list')")
    public Result<PageResult<TcmDecoctVO>> listPage(@Valid @RequestBody TcmDecoctPageQueryDTO query) {
        return Result.success(tcmDecoctService.listPage(query));
    }

    /**
     * 代煎单详情：连逐味药材明细与煎法脚注一起返回。
     * 用于台账行点开的详情弹框，也是推进、作废、打印后回读单据状态的取数口。
     */
    @Operation(summary = "代煎单详情（含逐味明细）")
    @GetMapping("/getDetailById")
    @PreAuthorize("hasAuthority('pharmacy:tcmDecoct:list')")
    public Result<TcmDecoctDetailVO> getDetailById(@RequestParam Long id) {
        return Result.success(tcmDecoctService.getDetailById(id));
    }

    /**
     * 待煎、已煎、已取、已作废四态单量一次给齐。
     * 用于「中药代煎」页状态筛选按钮上的计数，随台账刷新同步更新。
     */
    @Operation(summary = "四态计数（待煎/已煎/已取/已作废）")
    @GetMapping("/statusCount")
    @PreAuthorize("hasAuthority('pharmacy:tcmDecoct:list')")
    public Result<TcmDecoctCountVO> statusCount() {
        return Result.success(tcmDecoctService.getStatusCount());
    }

    /**
     * 把代煎单推进到指定状态（目标态为已煎或已取），只能逐级前进不回退。
     * 用于台账操作列的煎制完成与患者取药两个动作，取走回执后单据即为终态。
     */
    @Operation(summary = "状态推进（待煎→已煎→已取，只进不退）")
    @PostMapping("/advance")
    @PreAuthorize("hasAuthority('pharmacy:tcmDecoct:edit')")
    public Result<TcmDecoctDetailVO> advance(@Valid @RequestBody TcmDecoctAdvanceDTO dto) {
        return Result.success("状态已更新", tcmDecoctService.advance(dto));
    }

    /**
     * 作废代煎单并记录作废原因，作废是本表唯一的退出方式（不提供删除）且为终态。
     * 用于台账操作列的作废确认；患者退药只会在单据仍为待煎时自动作废，已煎出的须由药房在此手工判定。
     */
    @Operation(summary = "作废（原因必填，本表无删除）")
    @PostMapping("/cancel")
    @PreAuthorize("hasAuthority('pharmacy:tcmDecoct:cancel')")
    public Result<TcmDecoctDetailVO> cancel(@Valid @RequestBody TcmDecoctCancelDTO dto) {
        return Result.success("代煎单已作废", tcmDecoctService.cancel(dto));
    }

    /**
     * 出具代煎取药回执并把单号、剂数、味数、总克数连同打印任务号留进审计。
     * 用于台账操作列的打印，煎制完成后随患者取药一并出具。
     */
    @Operation(summary = "打印代煎回执（当前为控制台打印 + 审计留痕）")
    @PostMapping("/print")
    @PreAuthorize("hasAuthority('pharmacy:tcmDecoct:print')")
    public Result<TcmDecoctDetailVO> print(@Valid @RequestBody TcmDecoctIdDTO dto) {
        return Result.success("已发送到打印出口", tcmDecoctService.printReceipt(dto.getId()));
    }
}
