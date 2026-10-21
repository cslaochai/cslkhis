package com.his.outpatient.spi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.his.common.enums.PublishStatusEnum;
import com.his.common.enums.ScheduleStatusEnum;
import com.his.common.enums.StaffTypeEnum;
import com.his.outpatient.dto.ScheduleSelectQueryDTO;
import com.his.outpatient.entity.BizClinicSource;
import com.his.outpatient.entity.BizClinicSourceSlot;
import com.his.outpatient.entity.BizSchedule;
import com.his.outpatient.entity.BizShift;
import com.his.outpatient.mapper.BizClinicSourceMapper;
import com.his.outpatient.mapper.BizClinicSourceSlotMapper;
import com.his.outpatient.service.BizClinicSourceService;
import com.his.outpatient.service.ShiftService;
import com.his.outpatient.service.StaffScheduleService;
import com.his.spi.dto.AppointQueryDTO;
import com.his.spi.service.ClinicSourceFacadeService;
import com.his.spi.vo.ClinicSourceBriefVO;
import com.his.spi.vo.ClinicSourceSlotBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 门诊号源端口实现：号源快照读、按渠道原子扣还、今日出诊列表、患者端可挂号源，
 * 全部转调本模块内部 Mapper/服务，不向挂号域暴露排班编排。
 */
@Service
@RequiredArgsConstructor
public class ClinicSourceFacadeServiceImpl implements ClinicSourceFacadeService {

    private final BizClinicSourceMapper clinicSourceMapper;
    private final BizClinicSourceSlotMapper clinicSourceSlotMapper;
    private final ShiftService shiftService;
    private final StaffScheduleService staffScheduleService;
    private final BizClinicSourceService bizClinicSourceService;

    @Override
    public ClinicSourceBriefVO findClinicSource(Long sourceId) {
        if (sourceId == null) {
            return null;
        }
        BizClinicSource source = clinicSourceMapper.selectById(sourceId);
        return source == null ? null : toBrief(source);
    }

    @Override
    public ClinicSourceSlotBriefVO findSlot(Long slotId) {
        if (slotId == null) {
            return null;
        }
        BizClinicSourceSlot slot = clinicSourceSlotMapper.selectById(slotId);
        return slot == null ? null : toSlotBrief(slot);
    }

    @Override
    public boolean releasesClinicSource(Long staffScheduleId) {
        if (staffScheduleId == null) {
            return false;
        }
        BizSchedule schedule = staffScheduleService.getById(staffScheduleId);
        return staffScheduleService.releasesClinicSource(schedule);
    }

    @Override
    public Integer scheduleTypeOf(Long shiftId) {
        return shiftService.scheduleTypeOf(shiftId);
    }

    @Override
    public String findShiftName(Long shiftId) {
        if (shiftId == null) {
            return null;
        }
        BizShift shift = shiftService.getById(shiftId);
        return shift == null ? null : shift.getShiftName();
    }

    @Override
    public int deductScheduleSource(Long sourceId, boolean apptChannel, boolean sameDayWalkin) {
        if (sourceId == null) {
            return 0;
        }
        if (apptChannel) {
            return clinicSourceMapper.deductScheduleSourceForAppointment(sourceId);
        }
        if (sameDayWalkin) {
            return clinicSourceMapper.deductScheduleSourceForWalkinSameDay(sourceId);
        }
        return clinicSourceMapper.deductScheduleSourceForWalkin(sourceId);
    }

    @Override
    public int deductSlotSource(Long slotId, boolean apptChannel, boolean sameDayWalkin) {
        if (slotId == null) {
            return 0;
        }
        if (apptChannel) {
            return clinicSourceSlotMapper.deductSlotSourceForAppointment(slotId);
        }
        if (sameDayWalkin) {
            return clinicSourceSlotMapper.deductSlotSourceForWalkinSameDay(slotId);
        }
        return clinicSourceSlotMapper.deductSlotSourceForWalkin(slotId);
    }

    @Override
    public int releaseScheduleSource(Long sourceId, boolean apptChannel) {
        if (sourceId == null) {
            return 0;
        }
        if (apptChannel) {
            return clinicSourceMapper.releaseScheduleSourceForAppointment(sourceId);
        }
        return clinicSourceMapper.releaseScheduleSourceForWalkin(sourceId);
    }

    @Override
    public int releaseSlotSource(Long slotId, boolean apptChannel) {
        if (slotId == null) {
            return 0;
        }
        if (apptChannel) {
            return clinicSourceSlotMapper.releaseSlotSourceForAppointment(slotId);
        }
        return clinicSourceSlotMapper.releaseSlotSourceForWalkin(slotId);
    }

    @Override
    public List<ClinicSourceBriefVO> listTodayClinicSources(Collection<Long> deptIds, LocalDate date) {
        if (deptIds == null || deptIds.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDate day = date != null ? date : LocalDate.now();
        LambdaQueryWrapper<BizClinicSource> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BizClinicSource::getDeptId, deptIds)
                .eq(BizClinicSource::getScheduleDate, day)
                .eq(BizClinicSource::getStaffType, StaffTypeEnum.DOCTOR.getCode())
                .eq(BizClinicSource::getStatus, ScheduleStatusEnum.NORMAL.getCode())
                .in(BizClinicSource::getPublishStatus,
                        PublishStatusEnum.PUBLISHED.getCode(), PublishStatusEnum.CHANGED.getCode())
                .orderByAsc(BizClinicSource::getStartTime);
        List<BizClinicSource> rows = clinicSourceMapper.selectList(wrapper);
        List<ClinicSourceBriefVO> result = new ArrayList<>(rows.size());
        for (BizClinicSource row : rows) {
            result.add(toBrief(row));
        }
        return result;
    }

    @Override
    public List<ClinicSourceBriefVO> listPatientScheduleSources(AppointQueryDTO query) {
        if (query == null) {
            return Collections.emptyList();
        }
        ScheduleSelectQueryDTO selectQuery = new ScheduleSelectQueryDTO();
        selectQuery.setDeptId(query.getDeptId());
        selectQuery.setDoctorId(query.getDoctorId());
        selectQuery.setVisitDate(query.getVisitDate());
        List<BizClinicSource> rows = bizClinicSourceService.scheduleSelectListUnbounded(selectQuery);
        List<ClinicSourceBriefVO> result = new ArrayList<>(rows.size());
        for (BizClinicSource row : rows) {
            result.add(toBrief(row));
        }
        return result;
    }

    private ClinicSourceBriefVO toBrief(BizClinicSource source) {
        ClinicSourceBriefVO vo = new ClinicSourceBriefVO();
        vo.setId(source.getId());
        vo.setStaffScheduleId(source.getStaffScheduleId());
        vo.setScheduleDate(source.getScheduleDate());
        vo.setDeptId(source.getDeptId());
        vo.setDeptName(source.getDeptName());
        vo.setRoomId(source.getRoomId());
        vo.setRoomName(source.getRoomName());
        vo.setDoctorId(source.getDoctorId());
        vo.setDoctorName(source.getDoctorName());
        vo.setStaffType(source.getStaffType());
        vo.setStartTime(source.getStartTime());
        vo.setEndTime(source.getEndTime());
        vo.setShiftId(source.getShiftId());
        vo.setAvailableSource(source.getAvailableSource());
        vo.setRegistFee(source.getRegistFee());
        vo.setDiagnosisFee(source.getDiagnosisFee());
        vo.setDiagnosisTier(source.getDiagnosisTier());
        vo.setIsExpert(source.getIsExpert());
        return vo;
    }

    private ClinicSourceSlotBriefVO toSlotBrief(BizClinicSourceSlot slot) {
        ClinicSourceSlotBriefVO vo = new ClinicSourceSlotBriefVO();
        vo.setId(slot.getId());
        vo.setScheduleId(slot.getScheduleId());
        vo.setStartTime(slot.getStartTime());
        vo.setEndTime(slot.getEndTime());
        vo.setStatus(slot.getStatus());
        return vo;
    }
}
