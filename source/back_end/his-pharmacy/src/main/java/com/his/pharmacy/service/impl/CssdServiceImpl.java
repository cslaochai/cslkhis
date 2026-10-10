package com.his.pharmacy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.constant.DictType;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.pharmacy.dto.CssdDTO;
import com.his.pharmacy.entity.BizCssdPack;
import com.his.pharmacy.entity.BizCssdTrace;
import com.his.pharmacy.enums.CssdCheckResultEnum;
import com.his.pharmacy.enums.CssdNodeStatusEnum;
import com.his.pharmacy.mapper.BizCssdPackMapper;
import com.his.pharmacy.mapper.BizCssdTraceMapper;
import com.his.pharmacy.service.CssdService;
import com.his.pharmacy.vo.CssdPackVO;
import com.his.pharmacy.vo.CssdTraceVO;
import com.his.system.provider.DeptScopeService;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * CSSD 消毒供应追溯服务。
 */
@Service
@RequiredArgsConstructor
public class CssdServiceImpl extends ServiceImpl<BizCssdPackMapper, BizCssdPack> implements CssdService {
    private final DictCacheService dictCacheService;

    private final BizCssdPackMapper bizCssdPackMapper;

    private final BizCssdTraceMapper bizCssdTraceMapper;

    private final DeptScopeService deptScopeService;

    @Transactional(rollbackFor = Exception.class)
    public CssdPackVO receive(CssdDTO.Receive dto) {
        int method = dto.getSterilizeMethod() == null ? 1 : dto.getSterilizeMethod();
        BizCssdPack p = new BizCssdPack();
        p.setPackNo(TextUtil.hasText(dto.getPackNo()) ? dto.getPackNo().trim() : nextPackNo());
        if (bizCssdPackMapper.selectIdByNoAny(p.getPackNo()) != null) {
            throw new BusinessException("器械包条码已存在：" + p.getPackNo());
        }
        if (dto.getDeptId() != null) {
            deptScopeService.assertDeptAccessible(dto.getDeptId());
        }
        p.setPackName(dto.getPackName().trim());
        p.setDeptId(dto.getDeptId());
        p.setDeptName(TextUtil.trim(dto.getDeptName()));
        p.setSterilizeMethod(method);
        p.setStatus(CssdNodeStatusEnum.RECEIVED.getCode());
        p.setLastNodeTime(TimeUtil.nowSeconds());
        p.setCreateBy(UserUtils.getCurrentUser().getRealName());
        bizCssdPackMapper.insert(p);

        insertTrace(p, CssdNodeStatusEnum.RECEIVED.getCode(), dto.getRemark(), null, null, CssdCheckResultEnum.OK.getCode(),
                TextUtil.hasText(dto.getOperatorName()) ? dto.getOperatorName().trim()
                        : UserUtils.getCurrentUser().getRealName());
        return toVo(p, loadTraces(p.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public CssdPackVO advance(CssdDTO.Advance dto) {
        BizCssdPack p = requirePack(dto.getPackId());
        int from = p.getStatus();
        if (from < CssdNodeStatusEnum.RECEIVED.getCode() || from >= CssdNodeStatusEnum.ISSUED.getCode()) {
            throw new BusinessException("当前状态【" + CssdNodeStatusEnum.getText(from) + "】不允许流转（流程已完结或异常）");
        }
        int target = from + 1;
        int result = dto.getResult() == null ? CssdCheckResultEnum.OK.getCode() : dto.getResult();
        String operator = TextUtil.hasText(dto.getOperatorName()) ? dto.getOperatorName().trim()
                : UserUtils.getCurrentUser().getRealName();

        if (CssdNodeStatusEnum.STERILIZING.is(target)) {
            // ① 条件必填：锅次/批次只在推进到灭菌节点时必填（同一接口服务全部节点），DTO 注解一刀切会挡掉其他节点的合法请求
            if (!TextUtil.hasText(dto.getSterilizerNo()) || !TextUtil.hasText(dto.getBatchNo())) {
                throw new BusinessException("推进到灭菌节点必须填灭菌锅次与批次号");
            }
            p.setSterilizerNo(dto.getSterilizerNo().trim());
            p.setBatchNo(dto.getBatchNo().trim());
        }
        if (CssdNodeStatusEnum.ISSUED.is(target)) {
            if (dto.getDeptId() != null) {
                deptScopeService.assertDeptAccessible(dto.getDeptId());
                p.setDeptId(dto.getDeptId());
            }
            if (TextUtil.hasText(dto.getDeptName())) {
                p.setDeptName(dto.getDeptName().trim());
            }
            // ① 条件必填：仅发放节点要求，且认的是「本次补填 + 包上原有归属」合并后的结果，单看入参不填是合法的
            if (p.getDeptId() == null && !TextUtil.hasText(p.getDeptName())) {
                throw new BusinessException("发放节点必须确认申领科室");
            }
        }

        p.setStatus(target);
        p.setLastNodeTime(TimeUtil.nowSeconds());
        p.setUpdateBy(UserUtils.getCurrentUser().getRealName());
        bizCssdPackMapper.updateById(p);

        // 灭菌完成判不合格 → 包退回清洗（重新打包灭菌），追溯节点如实记录不合格
        if (CssdNodeStatusEnum.STORED.is(target) && CssdCheckResultEnum.NG.is(result)) {
            p.setStatus(CssdNodeStatusEnum.WASHING.getCode());
            p.setLastNodeTime(TimeUtil.nowSeconds());
            bizCssdPackMapper.updateById(p);
        }
        insertTrace(p, target, dto.getRemark(), p.getSterilizerNo(), p.getBatchNo(), result, operator);
        return toVo(p, loadTraces(p.getId()));
    }

    // 私有

    public IPage<CssdPackVO> listPage(CssdDTO.QueryPage q) {
        String kw = TextUtil.trim(q.getKeyword());
        LambdaQueryWrapper<BizCssdPack> w = new LambdaQueryWrapper<BizCssdPack>()
                .eq(q.getStatus() != null, BizCssdPack::getStatus, q.getStatus())
                .and(TextUtil.hasText(kw), x -> x
                        .like(BizCssdPack::getPackNo, kw)
                        .or().like(BizCssdPack::getPackName, kw)
                        .or().like(BizCssdPack::getDeptName, kw))
                .orderByDesc(BizCssdPack::getLastNodeTime)
                .orderByDesc(BizCssdPack::getId);
        return bizCssdPackMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w)
                .convert(p -> toVo(p, null));
    }

    public CssdPackVO getDetailById(Long packId) {
        BizCssdPack p = requirePack(packId);
        return toVo(p, loadTraces(packId));
    }

    private BizCssdPack requirePack(Long packId) {
        BizCssdPack p = bizCssdPackMapper.selectById(packId);
        if (p == null || Objects.equals(p.getDelFlag(), 1)) {
            throw new BusinessException("器械包不存在（id=" + packId + "）");
        }
        return p;
    }

    private void insertTrace(BizCssdPack p, int nodeType, String remark,
                             String sterilizerNo, String batchNo, int result, String operator) {
        BizCssdTrace t = new BizCssdTrace();
        t.setPackId(p.getId());
        t.setPackNo(p.getPackNo());
        t.setNodeType(nodeType);
        t.setNodeTime(TimeUtil.nowSeconds());
        t.setOperatorName(operator);
        t.setSterilizerNo(sterilizerNo);
        t.setBatchNo(batchNo);
        t.setResult(result);
        t.setRemark(TextUtil.trim(remark));
        bizCssdTraceMapper.insert(t);
    }

    private List<CssdTraceVO> loadTraces(Long packId) {
        return bizCssdTraceMapper.selectList(new LambdaQueryWrapper<BizCssdTrace>()
                        .eq(BizCssdTrace::getPackId, packId)
                        .orderByAsc(BizCssdTrace::getNodeTime)
                        .orderByAsc(BizCssdTrace::getId))
                .stream().map(this::toTraceVo).toList();
    }

    /**
     * 条码自动生成：CSSD + yyyyMMdd + 顺延序号（查重含软删行，防唯一键冲突）
     */
    private String nextPackNo() {
        String date = LocalDate.now().format(DateFormats.COMPACT_DATE);
        long seq = 1;
        for (int i = 0; i < 20; i++) {
            String no = "CSSD" + date + String.format("%03d", seq);
            if (bizCssdPackMapper.selectIdByNoAny(no) == null) {
                return no;
            }
            seq++;
        }
        throw new BusinessException("器械包条码生成失败，请手工指定条码");
    }

    private CssdPackVO toVo(BizCssdPack p, List<CssdTraceVO> traces) {
        CssdPackVO vo = new CssdPackVO();
        vo.setId(p.getId());
        vo.setPackNo(p.getPackNo());
        vo.setPackName(p.getPackName());
        vo.setDeptId(p.getDeptId());
        vo.setDeptName(p.getDeptName());
        vo.setSterilizeMethod(p.getSterilizeMethod());
        vo.setSterilizeMethodText(dictCacheService.getDicDataLabel(DictType.CSSD_STERIL_METHOD, p.getSterilizeMethod()));
        vo.setStatus(p.getStatus());
        vo.setStatusText(CssdNodeStatusEnum.getText(p.getStatus()));
        vo.setSterilizerNo(p.getSterilizerNo());
        vo.setBatchNo(p.getBatchNo());
        vo.setLastNodeTime(p.getLastNodeTime());
        vo.setCreateBy(p.getCreateBy());
        vo.setCreateTime(p.getCreateTime());
        vo.setTraces(traces);
        return vo;
    }

    private CssdTraceVO toTraceVo(BizCssdTrace t) {
        CssdTraceVO vo = new CssdTraceVO();
        vo.setId(t.getId());
        vo.setPackId(t.getPackId());
        vo.setPackNo(t.getPackNo());
        vo.setNodeType(t.getNodeType());
        vo.setNodeTypeText(CssdNodeStatusEnum.actionLabelOf(t.getNodeType()));
        vo.setNodeTime(t.getNodeTime());
        vo.setOperatorName(t.getOperatorName());
        vo.setSterilizerNo(t.getSterilizerNo());
        vo.setBatchNo(t.getBatchNo());
        vo.setResult(t.getResult());
        vo.setResultText(CssdCheckResultEnum.getText(t.getResult()));
        vo.setRemark(t.getRemark());
        return vo;
    }
}