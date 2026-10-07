package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.medicaltech.dto.ExamApptDTO;
import com.his.medicaltech.entity.BizExamAppointment;
import com.his.medicaltech.entity.BizExamDevice;
import com.his.medicaltech.entity.BizExamDeviceItem;
import com.his.medicaltech.entity.BizExamSlot;
import com.his.medicaltech.mapper.BizExamAppointmentMapper;
import com.his.medicaltech.mapper.BizExamDeviceItemMapper;
import com.his.medicaltech.mapper.BizExamDeviceMapper;
import com.his.medicaltech.mapper.BizExamSlotMapper;
import com.his.medicaltech.service.ExamDeviceService;
import com.his.medicaltech.support.ExamGrid;
import com.his.medicaltech.vo.ExamApptVO;
import com.his.medicaltech.vo.ExamDeviceItemCountRowVO;
import com.his.medicaltech.vo.ExamEquipmentOptionRowVO;
import com.his.system.entity.SysInspectionItem;
import com.his.system.mapper.SysInspectionItemMapper;
import com.his.system.service.DictCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 检查预约设备档位服务（检查设备档位 + 设备可开展项目）。
 *
 * <p>两条值得说明的口径：
 * <ol>
 *   <li><b>改开放时间不自动重排号源</b>。已排出去的格子可能已经有患者占着，
 *       静默按新时间重排等于把已约的患者挪走。这里只回一句「有 N 天号源与当前开放时间不一致，
 *       请到号源台复核」，把决定权留给人。</li>
 *   <li><b>删除设备要先把预约清干净</b>。预约单里存的是设备快照，删掉档位不会改历史单，
 *       但删掉一台还在排程的设备会让"当班台本"凭空少一台机器，因此有在办预约一律拒绝。</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class ExamDeviceServiceImpl extends ServiceImpl<BizExamDeviceMapper, BizExamDevice> implements ExamDeviceService {

    private static final String DICT_DEVICE_TYPE = "his_exam_device_type";
    private static final String DICT_DEVICE_STATUS = "his_exam_device_status";

    private static final int OPEN = 1;
    private static final int PAUSED = 2;

    private final BizExamDeviceMapper deviceMapper;
    private final BizExamDeviceItemMapper deviceItemMapper;
    private final BizExamSlotMapper slotMapper;
    private final BizExamAppointmentMapper appointmentMapper;
    private final SysInspectionItemMapper inspectionItemMapper;
    private final DictCacheService dictText;

    // 查询

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static String upper(String s) {
        String t = trim(s);
        return t == null ? null : t.toUpperCase();
    }

    private static boolean eq(String a, String b) {
        return java.util.Objects.equals(a == null || a.isEmpty() ? null : a, b == null || b.isEmpty() ? null : b);
    }

    private static int nz(Integer v, int dft) {
        return v == null ? dft : v;
    }

    // 写入

    public PageResult<ExamApptVO.DeviceVO> listPage(ExamApptDTO.DeviceQuery q) {
        LambdaQueryWrapper<BizExamDevice> w = new LambdaQueryWrapper<>();
        String kw = trim(q.getKeyword());
        w.and(StringUtils.hasText(kw), x -> x.like(BizExamDevice::getDeviceName, kw)
                        .or().like(BizExamDevice::getDeviceCode, kw)
                        .or().like(BizExamDevice::getRoomName, kw))
                .eq(q.getDeviceType() != null, BizExamDevice::getDeviceType, q.getDeviceType())
                .eq(q.getDeptId() != null, BizExamDevice::getDeptId, q.getDeptId())
                .eq(q.getStatus() != null, BizExamDevice::getStatus, q.getStatus())
                .orderByAsc(BizExamDevice::getDeviceCode);
        Page<BizExamDevice> page = deviceMapper.selectPage(
                new Page<>(q.getPageNum(), q.getPageSize()), w);
        Map<Long, Integer> itemCounts = itemCountByDevice();
        Map<Long, String> equipmentNames = equipmentNameMap();
        List<ExamApptVO.DeviceVO> records = new ArrayList<>();
        for (BizExamDevice d : page.getRecords()) {
            records.add(toDeviceVo(d, itemCounts, equipmentNames));
        }
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    public List<ExamApptVO.DeviceSelectListVO> selectList(Integer deviceType, Long itemId) {
        LambdaQueryWrapper<BizExamDevice> w = new LambdaQueryWrapper<BizExamDevice>()
                .eq(deviceType != null, BizExamDevice::getDeviceType, deviceType)
                .eq(BizExamDevice::getStatus, OPEN)
                .orderByAsc(BizExamDevice::getDeviceCode);
        if (itemId != null) {
            // 只给"确实能做这个项目"的设备：路由事实源是映射表，不是项目表的 dept_id
            List<Long> deviceIds = deviceItemMapper.selectList(new LambdaQueryWrapper<BizExamDeviceItem>()
                            .eq(BizExamDeviceItem::getItemId, itemId))
                    .stream().map(BizExamDeviceItem::getDeviceId).distinct().toList();
            if (deviceIds.isEmpty()) {
                return new ArrayList<>();
            }
            w.in(BizExamDevice::getId, deviceIds);
        }
        List<ExamApptVO.DeviceSelectListVO> out = new ArrayList<>();
        for (BizExamDevice d : deviceMapper.selectList(w)) {
            ExamApptVO.DeviceSelectListVO v = new ExamApptVO.DeviceSelectListVO();
            BeanUtils.copyProperties(d, v);
            v.setDeviceTypeText(dictText.getDicDataLabel(DICT_DEVICE_TYPE, d.getDeviceType()));
            out.add(v);
        }
        return out;
    }

    // 内部

    public ExamApptVO.DeviceVO getDetail(Long deviceId) {
        BizExamDevice d = require(deviceId);
        ExamApptVO.DeviceVO vo = toDeviceVo(d, itemCountByDevice(), equipmentNameMap());
        vo.setItemList(itemList(deviceId));
        return vo;
    }

    public List<ExamApptVO.DeviceItemVO> itemList(Long deviceId) {
        List<BizExamDeviceItem> maps = deviceItemMapper.selectList(new LambdaQueryWrapper<BizExamDeviceItem>()
                .eq(BizExamDeviceItem::getDeviceId, deviceId)
                .orderByAsc(BizExamDeviceItem::getItemCode));
        List<ExamApptVO.DeviceItemVO> out = new ArrayList<>();
        for (BizExamDeviceItem m : maps) {
            ExamApptVO.DeviceItemVO v = new ExamApptVO.DeviceItemVO();
            BeanUtils.copyProperties(m, v);
            SysInspectionItem item = m.getItemId() == null ? null : inspectionItemMapper.selectById(m.getItemId());
            v.setItemDictMinutes(item == null ? null : item.getDuration());
            out.add(v);
        }
        return out;
    }

    /**
     * 检查项目候选（供设备配项目时挑选）
     */
    public List<ExamApptVO.ItemSelectListVO> itemCandidates(String keyword, Integer limit) {
        String kw = trim(keyword);
        List<SysInspectionItem> items = inspectionItemMapper.selectList(
                new LambdaQueryWrapper<SysInspectionItem>()
                        .and(StringUtils.hasText(kw), x -> x.like(SysInspectionItem::getItemName, kw)
                                .or().like(SysInspectionItem::getItemCode, kw))
                        .orderByAsc(SysInspectionItem::getItemCode)
                        .last("LIMIT " + Math.min(nz(limit, 50), 200)));
        List<ExamApptVO.ItemSelectListVO> out = new ArrayList<>();
        for (SysInspectionItem i : items) {
            ExamApptVO.ItemSelectListVO v = new ExamApptVO.ItemSelectListVO();
            v.setItemId(i.getId());
            v.setItemCode(i.getItemCode());
            v.setItemName(i.getItemName());
            v.setItemDictMinutes(i.getDuration());
            out.add(v);
        }
        return out;
    }

    public List<ExamApptVO.EquipmentSelectListVO> equipmentOptions() {
        List<ExamApptVO.EquipmentSelectListVO> out = new ArrayList<>();
        for (ExamEquipmentOptionRowVO row : deviceMapper.selectEquipmentOptions()) {
            ExamApptVO.EquipmentSelectListVO v = new ExamApptVO.EquipmentSelectListVO();
            v.setId(row.getId());
            v.setEquipmentCode(row.getEquipmentCode());
            v.setEquipmentName(row.getEquipmentName());
            v.setCategory(row.getCategory());
            v.setStatus(row.getStatus());
            out.add(v);
        }
        return out;
    }

    @Transactional(rollbackFor = Exception.class)
    public ExamApptVO.DeviceVO upsert(ExamApptDTO.DeviceUpsert dto) {
        BizExamDevice device = new BizExamDevice();
        BeanUtils.copyProperties(dto, device);
        device.setDeviceCode(upper(dto.getDeviceCode()));
        applyDefaults(device);
        validateGrid(device);

        if (deviceMapper.selectCount(new LambdaQueryWrapper<BizExamDevice>()
                .eq(BizExamDevice::getDeviceCode, device.getDeviceCode())
                .ne(dto.getId() != null, BizExamDevice::getId, dto.getId())) > 0) {
            throw new BusinessException("设备编码已存在：" + device.getDeviceCode());
        }
        if (deviceMapper.countDeletedByCode(device.getDeviceCode()) > 0) {
            throw new BusinessException("设备编码 " + device.getDeviceCode()
                    + " 已被一条已删除的档位占用（编码位随历史留痕一并保留，不复用），请改用其他编码");
        }

        String warning = null;
        Long savedId;
        if (dto.getId() == null) {
            deviceMapper.insert(device);
            savedId = device.getId();
        } else {
            BizExamDevice old = require(dto.getId());
            device.setId(old.getId());
            boolean gridChanged = !sameGrid(old, device);
            deviceMapper.updateById(device);
            savedId = old.getId();
            if (gridChanged) {
                warning = "开放时间/号源粒度已变更，历史号源格子不自动重排（已约患者不能被静默挪走）；"
                        + "请到号源台按新时间生成后续日期，并对账校验计数。";
            }
        }
        ExamApptVO.DeviceVO vo = getDetail(savedId);
        vo.setWarning(warning);
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long deviceId) {
        BizExamDevice d = require(deviceId);
        long active = appointmentMapper.selectCount(new LambdaQueryWrapper<BizExamAppointment>()
                .eq(BizExamAppointment::getDeviceId, deviceId)
                .eq(BizExamAppointment::getExamDate, LocalDate.now())
                .in(BizExamAppointment::getStatus, 1, 2));
        long future = appointmentMapper.selectCount(new LambdaQueryWrapper<BizExamAppointment>()
                .eq(BizExamAppointment::getDeviceId, deviceId)
                .ge(BizExamAppointment::getExamDate, LocalDate.now())
                .in(BizExamAppointment::getStatus, 1, 2));
        if (future > 0) {
            throw new BusinessException("设备「" + d.getDeviceName() + "」还有 " + future + " 张未完成的预约"
                    + "（其中今天 " + active + " 张），请先改约或取消后再删除档位");
        }
        deviceItemMapper.hardDeleteByDevice(deviceId);
        slotMapper.delete(new LambdaQueryWrapper<BizExamSlot>().eq(BizExamSlot::getDeviceId, deviceId));
        deviceMapper.deleteById(deviceId);
    }

    /**
     * 覆盖式保存：本次提交即最终清单，未提交的映射视为取消
     */
    @Transactional(rollbackFor = Exception.class)
    public int saveItems(ExamApptDTO.DeviceItemSave dto) {
        BizExamDevice device = require(dto.getDeviceId());
        List<ExamApptDTO.ItemRef> refs = dto.getItems() == null ? new ArrayList<>() : dto.getItems();
        List<Long> itemIds = new ArrayList<>();
        for (ExamApptDTO.ItemRef r : refs) {
            if (r.getItemId() != null && !itemIds.contains(r.getItemId())) {
                itemIds.add(r.getItemId());
            }
        }
        Map<Long, SysInspectionItem> items = new HashMap<>();
        for (Long id : itemIds) {
            SysInspectionItem it = inspectionItemMapper.selectById(id);
            if (it == null) {
                throw new BusinessException("检查项目不存在或已删除：" + id);
            }
            items.put(id, it);
        }
        deviceItemMapper.hardDeleteByDevice(device.getId());
        for (ExamApptDTO.ItemRef r : refs) {
            if (r.getItemId() == null) {
                continue;
            }
            SysInspectionItem it = items.get(r.getItemId());
            BizExamDeviceItem m = new BizExamDeviceItem();
            m.setDeviceId(device.getId());
            m.setItemId(it.getId());
            m.setItemCode(it.getItemCode());
            m.setItemName(it.getItemName());
            m.setExamMinutes(r.getExamMinutes());
            deviceItemMapper.insert(m);
        }
        return items.size();
    }

    private BizExamDevice require(Long deviceId) {
        // C类：入参是主键参数而非请求 DTO，Bean Validation 只在 HTTP DTO 绑定时生效，无法下沉
        if (deviceId == null) {
            throw new BusinessException("设备ID不能为空");
        }
        BizExamDevice d = deviceMapper.selectById(deviceId);
        if (d == null) {
            throw new BusinessException("预约设备不存在：" + deviceId);
        }
        return d;
    }

    private void applyDefaults(BizExamDevice d) {
        if (d.getSlotMinutes() == null) {
            d.setSlotMinutes(30);
        }
        if (d.getParallelCount() == null) {
            d.setParallelCount(1);
        }
        if (d.getAheadDays() == null) {
            d.setAheadDays(7);
        }
        if (d.getMaxSlotMinutes() == null) {
            d.setMaxSlotMinutes(240);
        }
        if (d.getStatus() == null) {
            d.setStatus(OPEN);
        }
    }

    /**
     * 配置合法性：一次把口径校死，别让脏配置在生成号源时才炸
     */
    private void validateGrid(BizExamDevice d) {
        int step = d.getSlotMinutes();
        if (step < 5 || step > 240 || step % 5 != 0) {
            throw new BusinessException("号源粒度应为 5~240 分钟且为 5 的整数倍，当前：" + step);
        }
        if (d.getParallelCount() < 1 || d.getParallelCount() > 20) {
            throw new BusinessException("单格并行号数应在 1~20 之间，当前：" + d.getParallelCount());
        }
        if (d.getAheadDays() < 1 || d.getAheadDays() > 90) {
            throw new BusinessException("可提前预约天数应在 1~90 之间，当前：" + d.getAheadDays());
        }
        if (d.getMaxSlotMinutes() < step) {
            throw new BusinessException("最长可占时长(" + d.getMaxSlotMinutes() + ")不得小于号源粒度(" + step + ")");
        }
        if (d.getStatus() != OPEN && d.getStatus() != PAUSED) {
            throw new BusinessException("设备状态只能是 1-开放预约 或 2-暂停预约");
        }
        int amS = ExamGrid.toMin(d.getAmStart());
        int amE = ExamGrid.toMin(d.getAmEnd());
        if (amE <= amS) {
            throw new BusinessException("上午开放时段不合法：" + d.getAmStart() + "-" + d.getAmEnd());
        }
        boolean hasPm = StringUtils.hasText(d.getPmStart()) || StringUtils.hasText(d.getPmEnd());
        if (hasPm) {
            if (!StringUtils.hasText(d.getPmStart()) || !StringUtils.hasText(d.getPmEnd())) {
                throw new BusinessException("下午开放时段的开始与结束必须同时填写");
            }
            int pmS = ExamGrid.toMin(d.getPmStart());
            int pmE = ExamGrid.toMin(d.getPmEnd());
            if (pmE <= pmS) {
                throw new BusinessException("下午开放时段不合法：" + d.getPmStart() + "-" + d.getPmEnd());
            }
            if (pmS < amE) {
                throw new BusinessException("下午开始(" + d.getPmStart() + ")不得早于上午结束(" + d.getAmEnd() + ")");
            }
        }
        if (ExamGrid.daySlots(d).isEmpty()) {
            throw new BusinessException("按当前开放时间切不出一个完整的 " + step + " 分钟格子，请放宽时段或调小粒度");
        }
    }

    private boolean sameGrid(BizExamDevice old, BizExamDevice neu) {
        return eq(old.getAmStart(), neu.getAmStart()) && eq(old.getAmEnd(), neu.getAmEnd())
                && eq(old.getPmStart(), neu.getPmStart()) && eq(old.getPmEnd(), neu.getPmEnd())
                && java.util.Objects.equals(old.getSlotMinutes(), neu.getSlotMinutes());
    }

    private ExamApptVO.DeviceVO toDeviceVo(BizExamDevice d, Map<Long, Integer> itemCounts,
                                           Map<Long, String> equipmentNames) {
        ExamApptVO.DeviceVO v = new ExamApptVO.DeviceVO();
        BeanUtils.copyProperties(d, v);
        v.setDeviceTypeText(dictText.getDicDataLabel(DICT_DEVICE_TYPE, d.getDeviceType()));
        v.setStatusText(dictText.getDicDataLabel(DICT_DEVICE_STATUS, d.getStatus()));
        v.setItemCount(itemCounts.getOrDefault(d.getId(), 0));
        v.setEquipmentName(d.getEquipmentId() == null ? null : equipmentNames.get(d.getEquipmentId()));
        v.setOpenRangeText(openRangeText(d));
        v.setDailySlots(ExamGrid.daySlots(d).size());
        return v;
    }

    private String openRangeText(BizExamDevice d) {
        String am = d.getAmStart() + "-" + d.getAmEnd();
        return StringUtils.hasText(d.getPmStart()) ? am + "、" + d.getPmStart() + "-" + d.getPmEnd() : am;
    }

    private Map<Long, Integer> itemCountByDevice() {
        Map<Long, Integer> map = new HashMap<>();
        for (ExamDeviceItemCountRowVO row : deviceMapper.countItemsByDevice()) {
            map.put(row.getDeviceId(), row.getItemCount() == null ? 0 : row.getItemCount().intValue());
        }
        return map;
    }

    /**
     * 台账表很小（实测几十行），一次读全量建映射，比每行再查一次便宜
     */
    private Map<Long, String> equipmentNameMap() {
        Map<Long, String> map = new HashMap<>();
        for (ExamEquipmentOptionRowVO row : deviceMapper.selectEquipmentOptions()) {
            map.put(row.getId(), row.getEquipmentName());
        }
        return map;
    }
}
