package com.his.iot.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.his.iot.dto.IotColdChainRecordQueryDTO;
import com.his.iot.entity.IotColdChainTempLog;
import com.his.iot.entity.IotDevice;
import com.his.iot.mapper.IotColdChainTempLogMapper;
import com.his.iot.mapper.IotDeviceMapper;
import com.his.iot.service.ColdChainArchiveService;
import com.his.iot.service.IotTelemetryService;
import com.his.iot.vo.IotColdChainRecordVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 冷链 GSP 归档实现。
 * <p>
 * 游标用归档表 MAX(ts)，不用「当前时间减固定窗口」：进程停过一天再启动，固定窗口会漏掉停机段；
 * 游标能自动补缺口，且首次最多回补 24 小时，避免一上来把 90 天原始数据全拉一遍。
 */
@Service
@RequiredArgsConstructor
public class ColdChainArchiveServiceImpl implements ColdChainArchiveService {

    private static final Logger log = LoggerFactory.getLogger(ColdChainArchiveServiceImpl.class);
    private static final int WINDOW_MINUTES = 5;
    private static final int BATCH_SIZE = 200;
    private static final int MAX_BACKFILL_DAYS = 7;
    private static final int FIRST_BACKFILL_HOURS = 24;

    private final IotDeviceMapper deviceMapper;
    private final IotColdChainTempLogMapper archiveMapper;
    private final IotTelemetryService telemetryService;

    @Override
    public int archiveRecent() {
        LocalDateTime now = LocalDateTime.now();
        // 只归档「已经走完」的 5 分钟窗口：当前窗口缺后半段，先落库会被唯一键挡住、永远补不全
        LocalDateTime end = now.withSecond(0).withNano(0)
                .withMinute(now.getMinute() / WINDOW_MINUTES * WINDOW_MINUTES)
                .minusMinutes(WINDOW_MINUTES);

        LocalDateTime maxTs = archiveMapper.selectMaxTs();
        LocalDateTime start = maxTs == null
                ? end.minusHours(FIRST_BACKFILL_HOURS)
                : maxTs.plusMinutes(WINDOW_MINUTES);
        if (start.isBefore(end.minusDays(MAX_BACKFILL_DAYS))) {
            start = end.minusDays(MAX_BACKFILL_DAYS);
        }
        if (start.isAfter(end)) {
            return 0;
        }

        List<IotDevice> devices = deviceMapper.selectList(Wrappers.<IotDevice>lambdaQuery()
                .eq(IotDevice::getDeviceType, "coldchain")
                .eq(IotDevice::getEnabled, 1)
                .orderByAsc(IotDevice::getDeviceNo));
        int total = 0;
        for (IotDevice device : devices) {
            total += archiveDevice(device, start, end, now);
        }
        if (total > 0) {
            log.info("冷链 GSP 归档完成：{}~{}，{} 台冷柜，写入 {} 行", start, end, devices.size(), total);
        }
        return total;
    }

    private int archiveDevice(IotDevice device, LocalDateTime start, LocalDateTime end, LocalDateTime now) {
        IotColdChainRecordQueryDTO query = new IotColdChainRecordQueryDTO();
        query.setDeviceNo(device.getDeviceNo());
        query.setStartTime(start);
        query.setEndTime(end);
        // 只查 TDengine 窗口，不带归档表：归档任务自己就是写入方，读回自己刚写的行只会撞唯一键
        List<IotColdChainRecordVO> records = telemetryService.coldChainRealtimeRecords(query);
        if (records.isEmpty()) {
            return 0;
        }

        List<IotColdChainTempLog> rows = new ArrayList<>(records.size());
        for (IotColdChainRecordVO record : records) {
            if (record.getTs() == null || record.getTemp() == null) {
                continue;
            }
            IotColdChainTempLog row = new IotColdChainTempLog();
            // @TableId(ASSIGN_ID) 只对 MP 自带的 save 生效，自定义 @Insert 不会填充 id；
            // 不显式给值，NOT NULL 的 id 会被 INSERT IGNORE 兜成 0，整表主键全 0
            row.setId(IdWorker.getId());
            row.setDeviceNo(device.getDeviceNo());
            row.setDeptName(device.getDeptName() == null ? "" : device.getDeptName());
            row.setCabinetName(device.getDeviceName() == null ? device.getDeviceNo() : device.getDeviceName());
            row.setTemp(record.getTemp());
            row.setTempMin(record.getTempMin());
            row.setTempMax(record.getTempMax());
            row.setHumidity(record.getHumidity());
            row.setPointCount(record.getPointCount() == null ? 0 : record.getPointCount());
            // 窗口内是否开过门 / 是否断过电，由 TDengine 的 MAX(door)/MIN(power) 还原。
            // 原来写死 0/1 等于替设备宣称「一直关着门、一直供电正常」，GSP 核这两列就是假数据
            row.setDoor(record.getDoor() == null ? 0 : record.getDoor());
            row.setPower(record.getPower() == null ? 1 : record.getPower());
            row.setRecordType(1);
            row.setTs(record.getTs());
            row.setCreateTime(now);
            rows.add(row);
        }
        int written = 0;
        for (int i = 0; i < rows.size(); i += BATCH_SIZE) {
            written += archiveMapper.insertIgnoreBatch(rows.subList(i, Math.min(i + BATCH_SIZE, rows.size())));
        }
        return written;
    }
}
