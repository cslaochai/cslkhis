package com.mss.controller;

import com.mss.common.base.PageResult;
import com.mss.common.base.Result;
import com.mss.dto.DictDataUpsertDTO;
import com.mss.dto.DictTypeUpsertDTO;
import com.mss.dto.IdDTO;
import com.mss.entity.MssDictData;
import com.mss.entity.MssDictType;
import com.mss.service.SystemDictService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典管理（类型 + 数据）
 */
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
public class SystemDictController {

    private final SystemDictService dictService;

    @GetMapping("/typePage")
    public Result<PageResult<MssDictType>> typePage(@RequestParam(required = false) String keyword,
                                                    @RequestParam(defaultValue = "1") long pageNum,
                                                    @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(dictService.typePage(keyword, pageNum, pageSize));
    }

    @PostMapping("/typeUpsert")
    public Result<MssDictType> typeUpsert(@Valid @RequestBody DictTypeUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "新增成功" : "修改成功", dictService.typeUpsert(dto));
    }

    @PostMapping("/typeDelete")
    public Result<Void> typeDelete(@RequestBody IdDTO dto) {
        dictService.typeDelete(Long.valueOf(dto.getId()));
        return Result.success("删除成功", null);
    }

    @GetMapping("/dataPage")
    public Result<PageResult<MssDictData>> dataPage(@RequestParam(required = false) String dictType,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(defaultValue = "1") long pageNum,
                                                    @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(dictService.dataPage(dictType, keyword, pageNum, pageSize));
    }

    @PostMapping("/dataUpsert")
    public Result<MssDictData> dataUpsert(@Valid @RequestBody DictDataUpsertDTO dto) {
        return Result.success(dto.getId() == null ? "新增成功" : "修改成功", dictService.dataUpsert(dto));
    }

    @PostMapping("/dataDelete")
    public Result<Void> dataDelete(@RequestBody IdDTO dto) {
        dictService.dataDelete(Long.valueOf(dto.getId()));
        return Result.success("删除成功", null);
    }

    @GetMapping("/dataList")
    public Result<List<MssDictData>> dataList(@RequestParam String dictType) {
        return Result.success(dictService.dataList(dictType));
    }
}
