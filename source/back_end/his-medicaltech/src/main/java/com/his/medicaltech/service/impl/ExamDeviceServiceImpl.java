package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.constant.DictTypeConst;
import com.his.common.exception.BusinessException;
import com.his.common.util.NumUtil;
import com.his.common.util.TextUtil;
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
import com.his.system.provider.DeptScopeService;
import com.his.system.service.DictCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 检查预约设备档位服务（检查设备档位 + 设备可开展项目）。
 */
@Service
@RequiredArgsConstructor
public class ExamDeviceServiceImpl extends ServiceImpl<BizExamDeviceMapper, BizExamDevice> implements ExamDeviceService {


    private static final int OPEN = 1;
    private static final int PAUSED = 2;

    private final BizExamDeviceMapper bizExamDeviceMapper;
    private final BizExamDeviceItemMapper bizExamDeviceItemMapper;
    private final BizExamSlotMapper bizExamSlotMapper;
    private final BizExamAppointmentMapper bizExamAppointmentMapper;
    private final SysInspectionItemMapper sysInspectionItemMapper;
    private final DictCacheService dictCacheService;
    private final DeptScopeService deptScopeService;

    // 查询

    private static String upper(String s) {
        String t = TextUtil.trim(s);
        return t == null ? null : t.toUpperCase();
    }

    private static boolean eq(String a, String b) {
        return java.util.Objects.equals(a == null || a.isEmpty() ? null : a, b == null || b.isEmpty() ? null : b);
    }

    // 写入

    public PageResult<ExamApptVO.DeviceVO> listPage(ExamApptDTO.DeviceQuery q) {
        // 科室数据权限收口：按前端所选科室做越权校验，未选则按岗位可见科室集合过滤
        List<Long> deptIds = deptScopeService.scopedDeptIds(q.getDeptId());
        LambdaQueryWrapper<BizExamDevice> w = new LambdaQueryWrapper<>();
        String kw = TextUtil.trim(q.getKeyword());
        w.and(TextUtil.hasText(kw), x -> x.like(BizExamDevice::getDeviceName, kw)
                        .or().like(BizExamDevice::getDeviceCode, kw)
                        .or().like(BizExamDevice::getRoomName, kw))
                .eq(q.getDeviceType() != null, BizExamDevice::getDeviceType, q.getDeviceType())
                .eq(q.getDeptId() != null, BizExamDevice::getDeptId, q.getDeptId())
                .in(deptIds != null, BizExamDevice::getDeptId, deptIds)
                .eq(q.getStatus() != null, BizExamDevice::getStatus, q.getStatus())
                .orderByAsc(BizExamDevice::getDeviceCode);
        Page<BizExamDevice> page = bizExamDeviceMapper.selectPage(
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
            List<Long> deviceIds = bizExamDeviceItemMapper.selectList(new LambdaQueryWrapper<BizExamDeviceItem>()
                            .eq(BizExamDeviceItem::getItemId, itemId))
                    .stream().map(BizExamDeviceItem::getDeviceId).distinct().toList();
            if (deviceIds.isEmpty()) {
                return new ArrayList<>();
            }
            w.in(BizExamDevice::getId, deviceIds);
        }
        List<ExamApptVO.DeviceSelectListVO> out = new ArrayList<>();
        for (BizExamDevice d : bizExamDeviceMapper.selectList(w)) {
            ExamApptVO.DeviceSelectListVO v = new ExamApptVO.DeviceSelectListVO();
            BeanUtils.copyProperties(d, v);
            v.setDeviceTypeText(dictCacheService.getDicDataLabel(DictTypeConst.EXAM_DEVICE_TYPE, d.getDeviceType()));
            out.add(v);
        }
        return out;
    }

    // 内部

    public ExamApptVO.DeviceVO getDetail(Long deviceId) {
        BizExamDevice d = require(deviceId);
        assertDeptAccessible(d.getDeptId());
        ExamApptVO.DeviceVO vo = toDeviceVo(d, itemCountByDevice(), equipmentNameMap());
        vo.setItemList(itemList(deviceId));
        return vo;
    }

    public List<ExamApptVO.DeviceItemVO> itemList(Long deviceId) {
        List<BizExamDeviceItem> maps = bizExamDeviceItemMapper.selectList(new LambdaQueryWrapper<BizExamDeviceItem>()
                .eq(BizExamDeviceItem::getDeviceId, deviceId)
                .orderByAsc(BizExamDeviceItem::getItemCode));
        List<ExamApptVO.DeviceItemVO> out = new ArrayList<>();
        for (BizExamDeviceItem m : maps) {
            ExamApptVO.DeviceItemVO v = new ExamApptVO.DeviceItemVO();
            BeanUtils.copyProperties(m, v);
            SysInspectionItem item = m.getItemId() == null ? null : sysInspectionItemMapper.selectById(m.getItemId());
            v.setItemDictMinutes(item == null ? null : item.getDuration());
            out.add(v);
        }
        return out;
    }

    /**
     * 检查项目候选（供设备配项目时挑选）
     */
    public List<ExamApptVO.ItemSelectListVO> itemCandidates(String keyword, Integer limit) {
        String kw = TextUtil.trim(keyword);
        List<SysInspectionItem> items = sysInspectionItemMapper.selectList(
                new LambdaQueryWrapper<SysInspectionItem>()
                        .and(TextUtil.hasText(kw), x -> x.like(SysInspectionItem::getItemName, kw)
                                .or().like(SysInspectionItem::getItemCode, kw))
                        .orderByAsc(SysInspectionItem::getItemCode)
                        .last("LIMIT " + Math.min(NumUtil.orDefault(limit, 50), 200)));
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
        for (ExamEquipmentOptionRowVO row : bizExamDeviceMapper.selectEquipmentOptions()) {
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
        // 科室数据权限：设备绑定了科室时校验越权（B类：前端选科室，全院角色不受限）
        if (device.getDeptId() != null) {
            deptScopeService.resolveDeptId(device.getDeptId());
        }
        device.setDeviceCode(upper(dto.getDeviceCode()));
        applyDefaults(device);
        validateGrid(device);

        if (bizExamDeviceMapper.selectCount(new LambdaQueryWrapper<BizExamDevice>()
                .eq(BizExamDevice::getDeviceCode, device.getDeviceCode())
                .ne(dto.getId() != null, BizExamDevice::getId, dto.getId())) > 0) {
            throw new BusinessException("设备编码已存在：" + device.getDeviceCode());
        }
        if (bizExamDeviceMapper.countDeletedByCode(device.getDeviceCode()) > 0) {
            throw new BusinessException("设备编码 " + device.getDeviceCode()
                    + " 已被一条已删除的档位占用（编码位随历史留痕一并保留，不复用），请改用其他编码");
        }

        String warning = null;
        Long savedId;
        if (dto.getId() == null) {
            bizExamDeviceMapper.insert(device);
            savedId = device.getId();
        } else {
            BizExamDevice old = require(dto.getId());
            device.setId(old.getId());
            boolean gridChanged = !sameGrid(old, device);
            bizExamDeviceMapper.updateById(device);
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
        assertDeptAccessible(d.getDeptId());
        long active = bizExamAppointmentMapper.selectCount(new LambdaQueryWrapper<BizExamAppointment>()
                .eq(BizExamAppointment::getDeviceId, deviceId)
                .eq(BizExamAppointment::getExamDate, LocalDate.now())
                .in(BizExamAppointment::getStatus, 1, 2));
        long future = bizExamAppointmentMapper.selectCount(new LambdaQueryWrapper<BizExamAppointment>()
                .eq(BizExamAppointment::getDeviceId, deviceId)
                .ge(BizExamAppointment::getExamDate, LocalDate.now())
                .in(BizExamAppointment::getStatus, 1, 2));
        if (future > 0) {
            throw new BusinessException("设备「" + d.getDeviceName() + "」还有 " + future + " 张未完成的预约"
                    + "（其中今天 " + active + " 张），请先改约或取消后再删除档位");
        }
        bizExamDeviceItemMapper.hardDeleteByDevice(deviceId);
        bizExamSlotMapper.delete(new LambdaQueryWrapper<BizExamSlot>().eq(BizExamSlot::getDeviceId, deviceId));
        bizExamDeviceMapper.deleteById(deviceId);
    }

    /**
     * 覆盖式保存：本次提交即最终清单，未提交的映射视为取消
     */
    @Transactional(rollbackFor = Exception.class)
    public int saveItems(ExamApptDTO.DeviceItemSave dto) {
        BizExamDevice device = require(dto.getDeviceId());
        assertDeptAccessible(device.getDeptId());
        List<ExamApptDTO.ItemRef> refs = dto.getItems() == null ? new ArrayList<>() : dto.getItems();
        List<Long> itemIds = new ArrayList<>();
        for (ExamApptDTO.ItemRef r : refs) {
            if (r.getItemId() != null && !itemIds.contains(r.getItemId())) {
                itemIds.add(r.getItemId());
            }
        }
        Map<Long, SysInspectionItem> items = new HashMap<>();
        for (Long id : itemIds) {
            SysInspectionItem it = sysInspectionItemMapper.selectById(id);
            if (it == null) {
                throw new BusinessException("检查项目不存在或已删除：" + id);
            }
            items.put(id, it);
        }
        bizExamDeviceItemMapper.hardDeleteByDevice(device.getId());
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
            bizExamDeviceItemMapper.insert(m);
        }
        return items.size();
    }

    private BizExamDevice require(Long deviceId) {
        // C-非 web 入参：私有 helper 按主键捞单，被多个入口（DTO 字段与标量参数）复用，Bean Validation 不覆盖，保留
        if (deviceId == null) {
            throw new BusinessException("设备ID不能为空");
        }
        BizExamDevice d = bizExamDeviceMapper.selectById(deviceId);
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
        boolean hasPm = TextUtil.hasText(d.getPmStart()) || TextUtil.hasText(d.getPmEnd());
        if (hasPm) {
            if (!TextUtil.hasText(d.getPmStart()) || !TextUtil.hasText(d.getPmEnd())) {
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
        v.setDeviceTypeText(dictCacheService.getDicDataLabel(DictTypeConst.EXAM_DEVICE_TYPE, d.getDeviceType()));
        v.setStatusText(dictCacheService.getDicDataLabel(DictTypeConst.EXAM_DEVICE_STATUS, d.getStatus()));
        v.setItemCount(itemCounts.getOrDefault(d.getId(), 0));
        v.setEquipmentName(d.getEquipmentId() == null ? null : equipmentNames.get(d.getEquipmentId()));
        v.setOpenRangeText(openRangeText(d));
        v.setDailySlots(ExamGrid.daySlots(d).size());
        return v;
    }

    private String openRangeText(BizExamDevice d) {
        String am = d.getAmStart() + "-" + d.getAmEnd();
        return TextUtil.hasText(d.getPmStart()) ? am + "、" + d.getPmStart() + "-" + d.getPmEnd() : am;
    }

    private Map<Long, Integer> itemCountByDevice() {
        Map<Long, Integer> map = new HashMap<>();
        for (ExamDeviceItemCountRowVO row : bizExamDeviceMapper.countItemsByDevice()) {
            map.put(row.getDeviceId(), row.getItemCount() == null ? 0 : row.getItemCount().intValue());
        }
        return map;
    }

    /**
     * 台账表很小（实测几十行），一次读全量建映射，比每行再查一次便宜
     */
    private Map<Long, String> equipmentNameMap() {
        Map<Long, String> map = new HashMap<>();
        for (ExamEquipmentOptionRowVO row : bizExamDeviceMapper.selectEquipmentOptions()) {
            map.put(row.getId(), row.getEquipmentName());
        }
        return map;
    }

    /** 科室数据权限：设备科室可空（未绑科室=全院共享，放行） */
    private void assertDeptAccessible(Long deptId) {
        if (deptId != null && !deptScopeService.canAccessDept(deptId)) {
            throw new BusinessException("该设备所属科室不在当前岗位的数据范围内");
        }
    }

}
