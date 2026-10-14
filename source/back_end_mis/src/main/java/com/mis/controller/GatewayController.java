package com.mis.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mis.common.base.Result;
import com.mis.common.base.PageVO;
import com.mis.dto.CancelSettlementDTO;
import com.mis.dto.UploadSettlementDTO;
import com.mis.entity.JinbSettlement;
import com.mis.service.SettlementGatewayService;
import com.mis.vo.DayBillItemVO;
import com.mis.vo.ReceiptVO;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 医保前置网关（HIS 前置机对端）：2304 上传 / 2305 撤销 / 日对账
 */
@RestController
public class GatewayController {

    private final ObjectMapper objectMapper;
    private final SettlementGatewayService gatewayService;

    public GatewayController(ObjectMapper objectMapper, SettlementGatewayService gatewayService) {
        this.objectMapper = objectMapper;
        this.gatewayService = gatewayService;
    }

    /** 2304 结算信息上传（接收 HIS 报盘原文）；报文非法时按前置机口径回 infcode=-1，而非 HTTP 5xx */
    @PostMapping("/gateway/2304")
    public Result<ReceiptVO> upload2304(@RequestBody String body) {
        UploadSettlementDTO dto;
        try {
            dto = objectMapper.readValue(body, UploadSettlementDTO.class);
        } catch (JsonProcessingException e) {
            return Result.success(parseFail("2304", e));
        }
        return Result.success(gatewayService.receive2304(dto, body));
    }

    /** 2305 结算信息撤销 */
    @PostMapping("/gateway/2305")
    public Result<ReceiptVO> upload2305(@RequestBody String body) {
        CancelSettlementDTO dto;
        try {
            dto = objectMapper.readValue(body, CancelSettlementDTO.class);
        } catch (JsonProcessingException e) {
            return Result.success(parseFail("2305", e));
        }
        return Result.success(gatewayService.receive2305(dto, body));
    }

    /** 拉取账期内平台侧“对方账” */
    @GetMapping("/gateway/dayBill")
    public Result<List<DayBillItemVO>> dayBill(@RequestParam String billDate) {
        return Result.success(gatewayService.dayBill(LocalDate.parse(billDate)));
    }

    /** 管理端：平台侧结算单分页（机构 2304 报盘接收台账，可按患者/单号过滤） */
    @GetMapping("/admin/settlement/listPage")
    public Result<PageVO<JinbSettlement>> settlementListPage(@RequestParam(required = false) String month,
                                                             @RequestParam(required = false) Integer status,
                                                             @RequestParam(required = false) String patientName,
                                                             @RequestParam(required = false) String settlementNo,
                                                             @RequestParam(required = false) String billNo,
                                                             @RequestParam(required = false) String tradeNo,
                                                             @RequestParam(defaultValue = "1") int pageNum,
                                                             @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(gatewayService.settlementPage(month, status, patientName, settlementNo, billNo, tradeNo, pageNum, pageSize));
    }

    /** 管理端：平台侧结算单明细（含 2304 原始报文） */
    @GetMapping("/admin/settlement/getById")
    public Result<JinbSettlement> settlementById(@RequestParam Long id) {
        return Result.success(gatewayService.getById(id));
    }

    private ReceiptVO parseFail(String msgType, JsonProcessingException e) {
        ReceiptVO vo = new ReceiptVO();
        vo.setInfcode(-1);
        vo.setMsgType(msgType);
        vo.setErrMsg("报文格式错误，解析失败：" + e.getOriginalMessage());
        return vo;
    }
}
