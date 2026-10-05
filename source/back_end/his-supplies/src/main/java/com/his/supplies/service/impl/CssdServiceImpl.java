package com.his.supplies.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.exception.BusinessException;
import com.his.security.UserUtils;
import com.his.supplies.dto.CssdDTO;
import com.his.supplies.entity.BizCssdPack;
import com.his.supplies.entity.BizCssdTrace;
import com.his.supplies.mapper.BizCssdPackMapper;
import com.his.supplies.mapper.BizCssdTraceMapper;
import com.his.supplies.service.CssdService;
import com.his.supplies.vo.CssdPackVO;
import com.his.supplies.vo.CssdTraceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * CSSD 消毒供应追溯服务。
 *
 * <p>状态机（线性推进，status = 最近完成的节点）：
 * 1已回收 → 2清洗中 → 3已打包 → 4灭菌中 → 5待发放 → 6已发放。
 * 推进到节点 4（灭菌开始）必填锅次+批次；节点 5（灭菌完成入储存）判不合格时包退回清洗（status 回 2），
 * 不合格留痕不删——三甲检查看的就是这条链。
 */
@Service
@RequiredArgsConstructor
public class CssdServiceImpl implements CssdService {

    private static final int NODE_RECEIVE = 1;
    private static final int NODE_WASH = 2;
    private static final int NODE_PACK = 3;
    private static final int NODE_STERILIZE = 4;
    private static final int NODE_STORE = 5;
    private static final int NODE_ISSUE = 6;

    private static final int RESULT_OK = 1;
    private static final int RESULT_NG = 2;

    private static final Map<Integer, String> NODE_NAME = Map.of(
            1, "回收", 2, "清洗", 3, "打包", 4, "灭菌", 5, "储存", 6, "发放");
    private static final Map<Integer, String> STATUS_NAME = Map.of(
            1, "已回收", 2, "清洗中", 3, "已打包", 4, "灭菌中", 5, "待发放", 6, "已发放");
    private static final Map<Integer, String> METHOD_NAME = Map.of(
            1, "高压蒸汽", 2, "环氧乙烷", 3, "低温等离子");
    private static final Map<Integer, String> RESULT_NAME = Map.of(1, "合格", 2, "不合格");

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BizCssdPackMapper packMapper;
    private final BizCssdTraceMapper traceMapper;

    // 回收登记

    private static String tr(String s) {
        return s == null ? null : s.trim();
    }

    // 流转

    private static LocalDateTime nowSeconds() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    // 查询

    @Transactional(rollbackFor = Exception.class)
    public CssdPackVO receive(CssdDTO.Receive dto) {
        int method = dto.getSterilizeMethod() == null ? 1 : dto.getSterilizeMethod();
        if (!METHOD_NAME.containsKey(method)) {
            throw new BusinessException("灭菌方式取值不合法（1-高压蒸汽 2-环氧乙烷 3-低温等离子）");
        }
        BizCssdPack p = new BizCssdPack();
        p.setPackNo(StringUtils.hasText(dto.getPackNo()) ? dto.getPackNo().trim() : nextPackNo());
        if (packMapper.selectIdByNoAny(p.getPackNo()) != null) {
            throw new BusinessException("器械包条码已存在：" + p.getPackNo());
        }
        p.setPackName(dto.getPackName().trim());
        p.setDeptId(dto.getDeptId());
        p.setDeptName(tr(dto.getDeptName()));
        p.setSterilizeMethod(method);
        p.setStatus(NODE_RECEIVE);
        p.setLastNodeTime(nowSeconds());
        p.setCreateBy(UserUtils.getCurrentEmployeeName());
        packMapper.insert(p);

        insertTrace(p, NODE_RECEIVE, dto.getRemark(), null, null, RESULT_OK,
                StringUtils.hasText(dto.getOperatorName()) ? dto.getOperatorName().trim()
                        : UserUtils.getCurrentEmployeeName());
        return toVo(p, loadTraces(p.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public CssdPackVO advance(CssdDTO.Advance dto) {
        BizCssdPack p = requirePack(dto.getPackId());
        int from = p.getStatus();
        if (from < NODE_RECEIVE || from >= NODE_ISSUE) {
            throw new BusinessException("当前状态【" + STATUS_NAME.get(from) + "】不允许流转（流程已完结或异常）");
        }
        int target = from + 1;
        int result = dto.getResult() == null ? RESULT_OK : dto.getResult();
        if (!RESULT_NAME.containsKey(result)) {
            throw new BusinessException("节点结果取值不合法（1-合格 2-不合格）");
        }
        String operator = StringUtils.hasText(dto.getOperatorName()) ? dto.getOperatorName().trim()
                : UserUtils.getCurrentEmployeeName();

        if (target == NODE_STERILIZE) {
            // ① 条件必填：锅次/批次只在推进到灭菌节点时必填（同一接口服务全部节点），DTO 注解一刀切会挡掉其他节点的合法请求
            if (!StringUtils.hasText(dto.getSterilizerNo()) || !StringUtils.hasText(dto.getBatchNo())) {
                throw new BusinessException("推进到灭菌节点必须填灭菌锅次与批次号");
            }
            p.setSterilizerNo(dto.getSterilizerNo().trim());
            p.setBatchNo(dto.getBatchNo().trim());
        }
        if (target == NODE_ISSUE) {
            if (dto.getDeptId() != null) {
                p.setDeptId(dto.getDeptId());
            }
            if (StringUtils.hasText(dto.getDeptName())) {
                p.setDeptName(dto.getDeptName().trim());
            }
            // ① 条件必填：仅发放节点要求，且认的是「本次补填 + 包上原有归属」合并后的结果，单看入参不填是合法的
            if (p.getDeptId() == null && !StringUtils.hasText(p.getDeptName())) {
                throw new BusinessException("发放节点必须确认申领科室");
            }
        }

        p.setStatus(target);
        p.setLastNodeTime(nowSeconds());
        p.setUpdateBy(UserUtils.getCurrentEmployeeName());
        packMapper.updateById(p);

        // 灭菌完成判不合格 → 包退回清洗（重新打包灭菌），追溯节点如实记录不合格
        if (target == NODE_STORE && result == RESULT_NG) {
            p.setStatus(NODE_WASH);
            p.setLastNodeTime(nowSeconds());
            packMapper.updateById(p);
        }
        insertTrace(p, target, dto.getRemark(), p.getSterilizerNo(), p.getBatchNo(), result, operator);
        return toVo(p, loadTraces(p.getId()));
    }

    // 私有

    public IPage<CssdPackVO> listPage(CssdDTO.QueryPage q) {
        String kw = tr(q.getKeyword());
        LambdaQueryWrapper<BizCssdPack> w = new LambdaQueryWrapper<BizCssdPack>()
                .eq(q.getStatus() != null, BizCssdPack::getStatus, q.getStatus())
                .and(StringUtils.hasText(kw), x -> x
                        .like(BizCssdPack::getPackNo, kw)
                        .or().like(BizCssdPack::getPackName, kw)
                        .or().like(BizCssdPack::getDeptName, kw))
                .orderByDesc(BizCssdPack::getLastNodeTime)
                .orderByDesc(BizCssdPack::getId);
        return packMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()), w)
                .convert(p -> toVo(p, null));
    }

    public CssdPackVO getDetailById(Long packId) {
        BizCssdPack p = requirePack(packId);
        return toVo(p, loadTraces(packId));
    }

    private BizCssdPack requirePack(Long packId) {
        BizCssdPack p = packMapper.selectById(packId);
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
        t.setNodeTime(nowSeconds());
        t.setOperatorName(operator);
        t.setSterilizerNo(sterilizerNo);
        t.setBatchNo(batchNo);
        t.setResult(result);
        t.setRemark(tr(remark));
        traceMapper.insert(t);
    }

    private List<CssdTraceVO> loadTraces(Long packId) {
        return traceMapper.selectList(new LambdaQueryWrapper<BizCssdTrace>()
                        .eq(BizCssdTrace::getPackId, packId)
                        .orderByAsc(BizCssdTrace::getNodeTime)
                        .orderByAsc(BizCssdTrace::getId))
                .stream().map(this::toTraceVo).toList();
    }

    /**
     * 条码自动生成：CSSD + yyyyMMdd + 顺延序号（查重含软删行，防唯一键冲突）
     */
    private String nextPackNo() {
        String date = LocalDate.now().format(NO_FMT);
        long seq = 1;
        for (int i = 0; i < 20; i++) {
            String no = "CSSD" + date + String.format("%03d", seq);
            if (packMapper.selectIdByNoAny(no) == null) {
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
        vo.setSterilizeMethodText(METHOD_NAME.get(p.getSterilizeMethod()));
        vo.setStatus(p.getStatus());
        vo.setStatusText(STATUS_NAME.get(p.getStatus()));
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
        vo.setNodeTypeText(NODE_NAME.get(t.getNodeType()));
        vo.setNodeTime(t.getNodeTime());
        vo.setOperatorName(t.getOperatorName());
        vo.setSterilizerNo(t.getSterilizerNo());
        vo.setBatchNo(t.getBatchNo());
        vo.setResult(t.getResult());
        vo.setResultText(RESULT_NAME.get(t.getResult()));
        vo.setRemark(t.getRemark());
        return vo;
    }
}
