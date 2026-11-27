package com.his.platform.controller;

import com.his.common.base.Result;
import com.his.platform.dto.SignQualReviewHandleDTO;
import com.his.platform.service.SignQualReviewService;
import com.his.platform.vo.SignQualReviewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 签名资格复核（医务科）：人事异动把签名岗换成非签名岗时，决定证书保留还是吊销。
 */
@Tag(name = "签名资格复核")
@RestController
@RequestMapping("/sign/qualReview")
@RequiredArgsConstructor
public class SignQualReviewController {

    private final SignQualReviewService signQualReviewService;

    @PreAuthorize("hasAuthority('sign:cert:review')")
    @Operation(summary = "待复核的签名资格复核单")
    @GetMapping("/listPending")
    public Result<List<SignQualReviewVO>> listPending() {
        return Result.success(signQualReviewService.listPending());
    }

    @PreAuthorize("hasAuthority('sign:cert:review')")
    @Operation(summary = "处理签名资格复核（1-保留证书 2-吊销证书）")
    @PostMapping("/handle")
    public Result<Void> handle(@Valid @RequestBody SignQualReviewHandleDTO dto) {
        signQualReviewService.handle(dto.getId(), dto.getAction(), dto.getRemark());
        return Result.success();
    }
}
