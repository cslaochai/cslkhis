package com.his.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.entity.CurrentUser;
import com.his.common.exception.BusinessException;
import com.his.common.utils.UserUtils;
import com.his.platform.entity.SysPrintRecord;
import com.his.platform.mapper.SysPrintRecordMapper;
import com.his.platform.service.SysPrintRecordService;
import com.his.spi.dto.PrintRecordSaveDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 打印台账实现。
 * <p>序号规则：同一 (biz_type, biz_id) 内从 1 递增；补打（>=2）必须带原因。
 * 序号不是「读一遍再加一」算准的，靠 uk_print_no 唯一键兜底——两个人同时点打印，
 * 第二个 insert 撞键，转成「正在被其他人打印」，不会出现两条 print_no=1。</p>
 */
@Service
@RequiredArgsConstructor
public class SysPrintRecordServiceImpl extends ServiceImpl<SysPrintRecordMapper, SysPrintRecord>
        implements SysPrintRecordService {

    @Override
    public int countByBiz(Integer bizType, Long bizId) {
        if (bizType == null || bizId == null) {
            return 0;
        }
        long count = this.count(new LambdaQueryWrapper<SysPrintRecord>()
                .eq(SysPrintRecord::getBizType, bizType)
                .eq(SysPrintRecord::getBizId, bizId));
        return (int) count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int record(PrintRecordSaveDTO saveDTO) {
        if (saveDTO == null || saveDTO.getBizType() == null || saveDTO.getBizId() == null) {
            throw new BusinessException("打印对象不完整，无法记账");
        }
        SysPrintRecord latest = this.getOne(new LambdaQueryWrapper<SysPrintRecord>()
                .eq(SysPrintRecord::getBizType, saveDTO.getBizType())
                .eq(SysPrintRecord::getBizId, saveDTO.getBizId())
                .orderByDesc(SysPrintRecord::getPrintNo)
                .last("LIMIT 1"), false);
        int printNo = latest == null ? 1 : latest.getPrintNo() + 1;
        if (printNo > 1 && StringUtils.isBlank(saveDTO.getPrintReason())) {
            throw new BusinessException("该标签已打印过（第 " + (printNo - 1) + " 次），补打请填写原因");
        }

        CurrentUser operator = UserUtils.getCurrentUser();
        SysPrintRecord row = new SysPrintRecord();
        row.setBizType(saveDTO.getBizType());
        row.setBizId(saveDTO.getBizId());
        row.setCodeValue(StringUtils.defaultString(saveDTO.getCodeValue()));
        row.setPatientId(saveDTO.getPatientId() == null ? 0L : saveDTO.getPatientId());
        row.setPrintNo(printNo);
        row.setPrintReason(StringUtils.defaultString(saveDTO.getPrintReason()));
        row.setRenderCode(StringUtils.defaultString(saveDTO.getRenderCode()));
        row.setPaperSpec(StringUtils.defaultString(saveDTO.getPaperSpec()));
        row.setPrinterName(StringUtils.defaultString(saveDTO.getPrinterName()));
        row.setTerminalIp(StringUtils.EMPTY);
        row.setSnapshotJson(saveDTO.getSnapshotJson());
        row.setOperatorId(operator.getEmployeeId());
        row.setOperatorName(operator.getRealName());
        row.setPrintTime(LocalDateTime.now());
        try {
            this.save(row);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("该标签正在被其他人打印，请稍后重试");
        }
        return printNo;
    }
}
