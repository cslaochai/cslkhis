package com.his.pharmacy.service.impl;

import com.his.pharmacy.service.DrugTraceUploadChannelService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 药品追溯码上传通道（对接国家医保局追溯码采集接口）。
 *
 * <p>当前未真对接医保局平台，所以是打印：把上传内容完整留到日志里，
 * 真对接时照着这份报文在本类内改适配层即可 —— 与微信/TSA 同一套路：
 * 外部系统不在线时业务照常闭环，只是"上传"这一环留痕为未上传，不把采集/核销动作卡死。
 *
 * <p>失败分支给了一个确定入口：<b>追溯码以 FAIL 开头的行固定上传失败</b>，
 * 这样"上传失败→落原因→重传"这条链在没有真网关时也能被验证脚本真实命中。
 */
@Slf4j
@Service
public class DrugTraceUploadChannelServiceImpl implements DrugTraceUploadChannelService {

    /**
     * 批量上传追溯码事件
     *
     * @param lines 本次上传的事件行（一码一行）
     * @return 与入参等长、同序的回执
     */
    public List<UploadAck> upload(List<UploadLine> lines) {
        List<UploadAck> acks = new ArrayList<>(lines.size());
        for (UploadLine line : lines) {
            UploadAck ack = new UploadAck();
            ack.setTraceId(line.getTraceId());
            boolean fail = line.getTraceCode() != null && line.getTraceCode().toUpperCase().startsWith("FAIL");
            if (fail) {
                ack.setSuccess(false);
                ack.setMessage("医保平台校验不通过：追溯码不存在于国家药品追溯数据库");
            } else {
                ack.setSuccess(true);
                ack.setMessage("");
            }
            log.info("[药品追溯码上传] eventType={} traceNo={} code={} drug={} batch={} patient={} result={}",
                    line.getEventType(), line.getTraceNo(), line.getTraceCode(),
                    line.getDrugName(), line.getBatchNo(), line.getPatientName(), ack.isSuccess() ? "SUCCESS" : "FAIL");
            acks.add(ack);
        }
        return acks;
    }
}
