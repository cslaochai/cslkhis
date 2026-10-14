package com.mss.controller;

import com.mss.common.base.PageVO;
import com.mss.common.base.Result;
import com.mss.dto.BillQueryDTO;
import com.mss.dto.IdDTO;
import com.mss.service.SettleService;
import com.mss.vo.BillVO;
import com.mss.vo.SettleResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 结算单管理
 */
@RestController
@RequestMapping("/bill")
@RequiredArgsConstructor
public class BillController {

    private final SettleService settleService;

    @GetMapping("/listPage")
    public Result<PageVO<BillVO>> listPage(BillQueryDTO query) {
        return Result.success(settleService.billPage(query));
    }

    @GetMapping("/getDetailById")
    public Result<BillVO> detail(@RequestParam String id) {
        return Result.success(settleService.billDetail(Long.valueOf(id)));
    }

    @PostMapping("/reverse")
    public Result<SettleResultVO> reverse(@RequestBody IdDTO dto) {
        return Result.success(settleService.reverse(Long.valueOf(dto.getId())));
    }
}
