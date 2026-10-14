package com.mss.controller;

import com.mss.common.base.PageResult;
import com.mss.common.base.Result;
import com.mss.dto.PersonQueryDTO;
import com.mss.dto.PersonSaveDTO;
import com.mss.entity.JinbInsuredPerson;
import com.mss.service.InsuredPersonService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 参保人查询 / 管理
 */
@RestController
@RequestMapping("/insure")
public class InsuredPersonController {

    private final InsuredPersonService personService;

    public InsuredPersonController(InsuredPersonService personService) {
        this.personService = personService;
    }

    /** 参保查询（身份证 / 医保卡号 / 姓名） */
    @PostMapping("/person/query")
    public Result<JinbInsuredPerson> query(@RequestBody(required = false) PersonQueryDTO dto) {
        return Result.success(personService.query(dto));
    }

    /** 管理端分页 */
    @GetMapping("/person/listPage")
    public Result<PageResult<JinbInsuredPerson>> listPage(@RequestParam(required = false) String keyword,
                                                          @RequestParam(defaultValue = "1") int pageNum,
                                                          @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(personService.page(keyword, pageNum, pageSize));
    }

    /** 新增/同步参保人 */
    @PostMapping("/person/save")
    public Result<JinbInsuredPerson> save(@RequestBody @Valid PersonSaveDTO dto) {
        return Result.success(personService.save(dto));
    }
}
