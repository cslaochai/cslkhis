package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.util.TimeUtil;
import com.his.common.exception.BusinessException;
import com.his.system.dto.WasteDTO;
import com.his.system.entity.BizMedicalWaste;
import com.his.system.enums.WasteStatusEnum;
import com.his.system.mapper.BizMedicalWasteMapper;
import com.his.system.service.WasteService;
import com.his.system.vo.WasteVO;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import com.his.system.service.DictCacheService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 医疗废物登记服务。
 *
 * <p>三态：1已登记 → 2已交接 → 3已处置，不可逆。
 * 已交接/已处置的记录禁删——交接单是与处置公司的对外凭证，删了台账对不上。
 */
@Service
@RequiredArgsConstructor
public class WasteServiceImpl implements WasteService {
    @Autowired
    private DictCacheService dictText;

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BizMedicalWasteMapper wasteMapper;

    private static String tr(String s) {
        return s == null ? null : s.trim();
    }

    @Transactional(rollbackFor = Exception.class)
    public WasteVO create(WasteDTO.Create dto) {
        // ① 条件必填：科室ID与科室名称二选一即可（前端可只传名称），单字段加 @NotNull 会把合法请求挡成 400
        if (dto.getDeptId() == null && !StringUtils.hasText(dto.getDeptName())) {
            throw new BusinessException("产生科室不能为空");
        }
        BizMedicalWaste w = new BizMedicalWaste();
        w.setWasteNo(nextWasteNo());
        w.setWasteType(dto.getWasteType());
        w.setWeightKg(dto.getWeightKg());
        w.setDeptId(dto.getDeptId());
        w.setDeptName(tr(dto.getDeptName()));
        w.setCollectTime(dto.getCollectTime().truncatedTo(ChronoUnit.SECONDS));
        w.setCollectorName(StringUtils.hasText(dto.getCollectorName()) ? dto.getCollectorName().trim()
                : UserUtils.getCurrentUser().getRealName());
        w.setStatus(WasteStatusEnum.REGISTERED.getCode());
        w.setCreateBy(UserUtils.getCurrentUser().getRealName());
        wasteMapper.insert(w);
        return toVo(w);
    }

    @Transactional(rollbackFor = Exception.class)
    public WasteVO handover(WasteDTO.Handover dto) {
        BizMedicalWaste w = requireWaste(dto.getId());
        if (!WasteStatusEnum.REGISTERED.is(w.getStatus())) {
            throw new BusinessException("只有已登记的医废可以交接（当前：" + WasteStatusEnum.getText(w.getStatus()) + "）");
        }
        w.setStatus(WasteStatusEnum.HANDED_OVER.getCode());
        w.setHandoverName(dto.getHandoverName().trim());
        w.setHandoverTime(TimeUtil.nowSeconds());
        w.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        w.setUpdateTime(w.getHandoverTime());
        wasteMapper.updateById(w);
        return toVo(w);
    }

    @Transactional(rollbackFor = Exception.class)
    public WasteVO dispose(WasteDTO.Dispose dto) {
        BizMedicalWaste w = requireWaste(dto.getId());
        if (!WasteStatusEnum.HANDED_OVER.is(w.getStatus())) {
            throw new BusinessException("只有已交接的医废可以确认处置（当前：" + WasteStatusEnum.getText(w.getStatus()) + "）");
        }
        w.setStatus(WasteStatusEnum.DISPOSED.getCode());
        w.setDisposalCompany(dto.getDisposalCompany().trim());
        w.setDisposalTime(TimeUtil.nowSeconds());
        w.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        w.setUpdateTime(w.getDisposalTime());
        wasteMapper.updateById(w);
        return toVo(w);
    }

    // 私有

    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        BizMedicalWaste w = requireWaste(id);
        if (!WasteStatusEnum.REGISTERED.is(w.getStatus())) {
            throw new BusinessException("已交接/已处置的医废记录不可删除（交接单是对外凭证）");
        }
        wasteMapper.deleteById(id);
    }

    public IPage<WasteVO> listPage(WasteDTO.QueryPage q) {
        String kw = tr(q.getKeyword());
        LocalDate begin = q.getCollectDateBegin();
        LocalDate end = q.getCollectDateEnd();
        LambdaQueryWrapper<BizMedicalWaste> w = new LambdaQueryWrapper<BizMedicalWaste>()
                .eq(q.getWasteType() != null, BizMedicalWaste::getWasteType, q.getWasteType())
                .eq(q.getStatus() != null, BizMedicalWaste::getStatus, q.getStatus())
                .and(StringUtils.hasText(kw), x -> x
                        .like(BizMedicalWaste::getWasteNo, kw)
                        .or().like(BizMedicalWaste::getDeptName, kw))
                .ge(begin != null, BizMedicalWaste::getCollectTime, begin == null ? null : begin.atStartOfDay())
                // 日期边界铁律：按日期过滤必须补全天边界，否则当天时点全被滤掉
                .le(end != null, BizMedicalWaste::getCollectTime, end == null ? null : end.atTime(23, 59, 59))
                .orderByDesc(BizMedicalWaste::getCollectTime)
                .orderByDesc(BizMedicalWaste::getId);
        return wasteMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w)
                .convert(this::toVo);
    }

    private BizMedicalWaste requireWaste(Long id) {
        BizMedicalWaste w = wasteMapper.selectById(id);
        if (w == null || Objects.equals(w.getDelFlag(), 1)) {
            throw new BusinessException("医废登记不存在（id=" + id + "）");
        }
        return w;
    }

    /**
     * 交接单号自动生成：MW + yyyyMMdd + 顺延序号（查重含软删行，防唯一键冲突）
     */
    private String nextWasteNo() {
        String date = LocalDate.now().format(NO_FMT);
        long seq = 1;
        for (int i = 0; i < 20; i++) {
            String no = "MW" + date + String.format("%03d", seq);
            if (wasteMapper.selectIdByNoAny(no) == null) {
                return no;
            }
            seq++;
        }
        throw new BusinessException("医废交接单号生成失败，请稍后重试");
    }

    private WasteVO toVo(BizMedicalWaste w) {
        WasteVO vo = new WasteVO();
        vo.setId(w.getId());
        vo.setWasteNo(w.getWasteNo());
        vo.setWasteType(w.getWasteType());
        vo.setWasteTypeText(dictText.getDicDataLabel("biz_system_wasteTypeEnum", w.getWasteType()));
        vo.setWeightKg(w.getWeightKg());
        vo.setDeptId(w.getDeptId());
        vo.setDeptName(w.getDeptName());
        vo.setCollectTime(w.getCollectTime());
        vo.setCollectorName(w.getCollectorName());
        vo.setStatus(w.getStatus());
        vo.setStatusText(WasteStatusEnum.getText(w.getStatus()));
        vo.setHandoverName(w.getHandoverName());
        vo.setHandoverTime(w.getHandoverTime());
        vo.setDisposalCompany(w.getDisposalCompany());
        vo.setDisposalTime(w.getDisposalTime());
        vo.setCreateBy(w.getCreateBy());
        vo.setCreateTime(w.getCreateTime());
        return vo;
    }
}