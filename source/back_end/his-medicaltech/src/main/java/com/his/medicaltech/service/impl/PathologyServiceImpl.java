package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.constant.DictType;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.common.util.TimeUtil;
import com.his.medicaltech.dto.PathologyDTO;
import com.his.medicaltech.entity.BizPathologyBlock;
import com.his.medicaltech.entity.BizPathologyOrder;
import com.his.medicaltech.enums.PathologyStatusEnum;
import com.his.medicaltech.mapper.BizPathologyBlockMapper;
import com.his.medicaltech.mapper.BizPathologyOrderMapper;
import com.his.medicaltech.service.PathologyService;
import com.his.medicaltech.vo.PathologyVO;
import com.his.system.entity.CurrentUser;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 病理亚专业服务
 *
 * <p>状态机（单向推进，只在服务端收口）：
 * 1 已登记 → 2 已接收标本 → 3 已取材 → 4 已制片 → 5 已初诊 → 6 已审核 → 7 已发布；8 已取消（终态）。
 *
 * <p>三条硬规则：
 * <ol>
 *   <li>审核人不得是初诊人本人 —— 自己写自己审等于没有二审；</li>
 *   <li>非冰冻 / 非细胞学单，进入「已制片」必须有至少一块已切片的蜡块 —— 没蜡块就说制片完成是假数据；</li>
 *   <li>已发布(7)一律不可再改、不可取消 —— 病理报告是法律文书级凭证。</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class PathologyServiceImpl extends ServiceImpl<BizPathologyOrderMapper, BizPathologyOrder> implements PathologyService {

    /**
     * 冰冻 / 细胞学：不经过蜡块制片流程，可提前出诊断
     */
    private static final int EXAM_FROZEN = 2;
    private static final int EXAM_CYTOLOGY = 3;


    private final BizPathologyOrderMapper bizPathologyOrderMapper;
    private final BizPathologyBlockMapper bizPathologyBlockMapper;
    private final DictCacheService dictCacheService;

    // 查询

    public PageResult<PathologyVO.ListVO> pageVO(PathologyDTO.Query q) {
        LambdaQueryWrapper<BizPathologyOrder> w = new LambdaQueryWrapper<>();
        w.eq(TextUtil.hasText(q.getOrderNo()), BizPathologyOrder::getOrderNo, q.getOrderNo())
                .eq(q.getPatientId() != null, BizPathologyOrder::getPatientId, q.getPatientId())
                .eq(q.getExamType() != null, BizPathologyOrder::getExamType, q.getExamType())
                .eq(q.getStatus() != null, BizPathologyOrder::getStatus, q.getStatus())
                .like(TextUtil.hasText(q.getPatientName()), BizPathologyOrder::getPatientName, TextUtil.trim(q.getPatientName()))
                .ge(q.getStartDate() != null, BizPathologyOrder::getVisitDate, q.getStartDate())
                .le(q.getEndDate() != null, BizPathologyOrder::getVisitDate, q.getEndDate())
                // 二级键：同 visit_date 的行顺序不稳 → 翻页会重复/丢行且不报错
                .orderByDesc(BizPathologyOrder::getId);
        Page<BizPathologyOrder> page = bizPathologyOrderMapper.selectPage(
                new Page<>(q.getPageNum(), q.getPageSize()), w);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(),
                toListVo(page.getRecords()));
    }

    public PathologyVO.StatsVO stats() {
        LambdaQueryWrapper<BizPathologyOrder> all = new LambdaQueryWrapper<>();
        all.ne(BizPathologyOrder::getStatus, PathologyStatusEnum.CANCELLED.getCode());
        long total = bizPathologyOrderMapper.selectCount(all);
        long pendingReceive = countByStatus(PathologyStatusEnum.REGISTERED.getCode());
        long processing = countByStatus(PathologyStatusEnum.RECEIVED.getCode())
                + countByStatus(PathologyStatusEnum.SAMPLED.getCode()) + countByStatus(PathologyStatusEnum.SLICED.getCode());
        long pendingAudit = countByStatus(PathologyStatusEnum.REPORTED.getCode());
        long published = countByStatus(PathologyStatusEnum.PUBLISHED.getCode());

        LocalDate today = LocalDate.now();
        long frozenToday = bizPathologyOrderMapper.selectCount(new LambdaQueryWrapper<BizPathologyOrder>()
                .eq(BizPathologyOrder::getIsFrozen, 1)
                .ge(BizPathologyOrder::getCreateTime, TimeUtil.dayStart(today)));

        PathologyVO.StatsVO vo = new PathologyVO.StatsVO();
        vo.setTotal(total);
        vo.setPendingReceive(pendingReceive);
        vo.setProcessing(processing);
        vo.setPendingAudit(pendingAudit);
        vo.setPublished(published);
        vo.setFrozenToday(frozenToday);
        return vo;
    }

    private long countByStatus(int status) {
        return bizPathologyOrderMapper.selectCount(new LambdaQueryWrapper<BizPathologyOrder>().eq(BizPathologyOrder::getStatus, status));
    }

    public PathologyVO.DetailVO getDetail(Long orderId) {
        BizPathologyOrder order = requireOrder(orderId);
        PathologyVO.DetailVO vo = new PathologyVO.DetailVO();
        BeanUtils.copyProperties(order, vo);
        vo.setStatusText(dictCacheService.getDicDataLabel(DictType.PATHOLOGY_STATUS, order.getStatus()));
        vo.setExamTypeText(dictCacheService.getDicDataLabel(DictType.PATHOLOGY_EXAM_TYPE, order.getExamType()));
        vo.setBlocks(listBlocks(orderId));
        return vo;
    }

    public List<PathologyVO.BlockVO> listBlocks(Long orderId) {
        List<BizPathologyBlock> list = bizPathologyBlockMapper.selectList(new LambdaQueryWrapper<BizPathologyBlock>()
                .eq(BizPathologyBlock::getOrderId, orderId)
                .orderByAsc(BizPathologyBlock::getId));
        List<PathologyVO.BlockVO> out = new ArrayList<>();
        for (BizPathologyBlock b : list) {
            PathologyVO.BlockVO v = new PathologyVO.BlockVO();
            BeanUtils.copyProperties(b, v);
            v.setStatusText(dictCacheService.getDicDataLabel(DictType.PATHOLOGY_BLOCK_STATUS, b.getStatus()));
            out.add(v);
        }
        return out;
    }

    private List<PathologyVO.ListVO> toListVo(List<BizPathologyOrder> records) {
        List<PathologyVO.ListVO> out = new ArrayList<>();
        for (BizPathologyOrder o : records) {
            PathologyVO.ListVO v = new PathologyVO.ListVO();
            BeanUtils.copyProperties(o, v);
            v.setStatusText(dictCacheService.getDicDataLabel(DictType.PATHOLOGY_STATUS, o.getStatus()));
            v.setBlockCount(bizPathologyBlockMapper.selectCount(new LambdaQueryWrapper<BizPathologyBlock>()
                    .eq(BizPathologyBlock::getOrderId, o.getId())).intValue());
            out.add(v);
        }
        return out;
    }

    // 写入

    @Transactional(rollbackFor = Exception.class)
    public PathologyVO.DetailVO upsertOrder(PathologyDTO.OrderUpsert dto) {
        BizPathologyOrder o = dto.getId() == null ? createOrder(dto) : updateOrder(dto);
        return getDetail(o.getId());
    }

    private BizPathologyOrder createOrder(PathologyDTO.OrderUpsert dto) {
        BizPathologyOrder o = new BizPathologyOrder();
        BeanUtils.copyProperties(dto, o);
        o.setId(null);
        LocalDate visitDate = dto.getVisitDate() != null ? dto.getVisitDate() : LocalDate.now();
        o.setVisitDate(visitDate);
        if (Integer.valueOf(EXAM_FROZEN).equals(dto.getExamType())) {
            o.setIsFrozen(1);
        } else if (o.getIsFrozen() == null) {
            o.setIsFrozen(0);
        }
        if (o.getExamType() == null) {
            o.setExamType(1);
        }
        o.setStatus(PathologyStatusEnum.REGISTERED.getCode());
        o.setOrderNo(nextOrderNo(visitDate));
        bizPathologyOrderMapper.insert(o);
        return o;
    }

    private BizPathologyOrder updateOrder(PathologyDTO.OrderUpsert dto) {
        BizPathologyOrder o = requireOrder(dto.getId());
        assertMutable(o);
        if (!PathologyStatusEnum.REGISTERED.is(o.getStatus())) {
            throw new BusinessException("仅「已登记」的病理单可修改基本信息（当前状态：" + dictCacheService.getDicDataLabel(DictType.PATHOLOGY_STATUS, o.getStatus()) + "）");
        }
        BeanUtils.copyProperties(dto, o);
        o.setId(dto.getId());
        if (Integer.valueOf(EXAM_FROZEN).equals(dto.getExamType())) {
            o.setIsFrozen(1);
        }
        bizPathologyOrderMapper.updateById(o);
        return o;
    }

    /**
     * 内镜活检送检：由内镜域调用，生成一条已登记的病理单
     */
    @Transactional(rollbackFor = Exception.class)
    public BizPathologyOrder createFromEndoscopy(PathologyDTO.FromEndoscopy dto) {
        BizPathologyOrder o = new BizPathologyOrder();
        BeanUtils.copyProperties(dto, o);
        LocalDate visitDate = dto.getVisitDate() != null ? dto.getVisitDate() : LocalDate.now();
        o.setVisitDate(visitDate);
        // 内镜活检是常规石蜡（冰冻是手术室内场景，内镜活检不走冰冻）
        o.setExamType(1);
        o.setIsFrozen(0);
        o.setSpecimenType("内镜活检");
        o.setSpecimenPart(TextUtil.hasText(dto.getBiopsyPart()) ? dto.getBiopsyPart() : dto.getSpecimenPart());
        o.setStatus(PathologyStatusEnum.REGISTERED.getCode());
        o.setOrderNo(nextOrderNo(visitDate));
        o.setRemark("内镜活检送检" + (TextUtil.hasText(dto.getSourceRecordNo()) ? "（来源：" + dto.getSourceRecordNo() + "）" : ""));
        bizPathologyOrderMapper.insert(o);
        return o;
    }

    @Transactional(rollbackFor = Exception.class)
    public void receive(PathologyDTO.Receive dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizPathologyOrder o = requireOrder(dto.getOrderId());
        assertMutable(o);
        if (!PathologyStatusEnum.REGISTERED.is(o.getStatus())) {
            throw new BusinessException("仅「已登记」的病理单可接收标本（当前：" + dictCacheService.getDicDataLabel(DictType.PATHOLOGY_STATUS, o.getStatus()) + "）");
        }
        if (TextUtil.hasText(dto.getSpecimenType())) {
            o.setSpecimenType(dto.getSpecimenType());
        }
        if (TextUtil.hasText(dto.getSpecimenPart())) {
            o.setSpecimenPart(dto.getSpecimenPart());
        }
        o.setStatus(PathologyStatusEnum.RECEIVED.getCode());
        o.setReceiveBy(operatorUser.getRealName());
        o.setReceiveTime(LocalDateTime.now().withNano(0));
        bizPathologyOrderMapper.updateById(o);
    }

    /**
     * 主单级流程推进：3 已取材 / 4 已制片
     */
    @Transactional(rollbackFor = Exception.class)
    public void process(PathologyDTO.Process dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizPathologyOrder o = requireOrder(dto.getOrderId());
        assertMutable(o);
        int target = dto.getTargetStatus();
        boolean skipBlock = isFrozenOrCytology(o);
        if (PathologyStatusEnum.SAMPLED.is(target)) {
            if (!PathologyStatusEnum.RECEIVED.is(o.getStatus())) {
                throw new BusinessException("取材前必须先接收标本（当前：" + dictCacheService.getDicDataLabel(DictType.PATHOLOGY_STATUS, o.getStatus()) + "）");
            }
            if (!skipBlock && countBlocks(o.getId(), 2, 4) == 0) {
                throw new BusinessException("尚无任何已取材的蜡块，不能把主单推进到「已取材」——请先登记蜡块并完成取材");
            }
            o.setSamplingBy(operatorUser.getRealName());
            o.setSamplingTime(LocalDateTime.now().withNano(0));
        } else if (PathologyStatusEnum.SLICED.is(target)) {
            if (!PathologyStatusEnum.SAMPLED.is(o.getStatus())) {
                throw new BusinessException("制片前必须先完成取材（当前：" + dictCacheService.getDicDataLabel(DictType.PATHOLOGY_STATUS, o.getStatus()) + "）");
            }
            if (!skipBlock && countBlocks(o.getId(), 4, 4) == 0) {
                throw new BusinessException("尚无任何已切片的蜡块，不能把主单推进到「已制片」");
            }
            o.setSliceBy(operatorUser.getRealName());
            o.setSliceTime(LocalDateTime.now().withNano(0));
        } else {
            throw new BusinessException("不支持的目标状态：" + target);
        }
        o.setStatus(target);
        bizPathologyOrderMapper.updateById(o);
    }

    @Transactional(rollbackFor = Exception.class)
    public BizPathologyBlock addBlock(PathologyDTO.BlockUpsert dto) {
        BizPathologyOrder o = requireOrder(dto.getOrderId());
        assertMutable(o);
        if (o.getStatus() < PathologyStatusEnum.RECEIVED.getCode()) {
            throw new BusinessException("标本尚未接收，不能登记蜡块");
        }
        long dup = bizPathologyBlockMapper.selectCount(new LambdaQueryWrapper<BizPathologyBlock>()
                .eq(BizPathologyBlock::getOrderId, dto.getOrderId())
                .eq(BizPathologyBlock::getBlockNo, dto.getBlockNo()));
        if (dup > 0) {
            throw new BusinessException("蜡块号 " + dto.getBlockNo() + " 在本病理单下已存在");
        }
        BizPathologyBlock b = new BizPathologyBlock();
        BeanUtils.copyProperties(dto, b);
        b.setId(null);
        b.setOrderNo(o.getOrderNo());
        b.setStatus(1);
        if (b.getBlockCount() == null) {
            b.setBlockCount(1);
        }
        if (b.getSliceCount() == null) {
            b.setSliceCount(0);
        }
        bizPathologyBlockMapper.insert(b);
        return b;
    }

    /**
     * 蜡块流转：1 取材 2 包埋 3 切片（单向推进），返回主单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long blockAction(PathologyDTO.BlockAction dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizPathologyBlock b = bizPathologyBlockMapper.selectById(dto.getBlockId());
        if (b == null) {
            throw new BusinessException("蜡块不存在：" + dto.getBlockId());
        }
        BizPathologyOrder o = requireOrder(b.getOrderId());
        assertMutable(o);
        String who = operatorUser.getRealName();
        LocalDateTime now = LocalDateTime.now().withNano(0);
        int action = dto.getAction();
        if (action == 1) {
            if (b.getStatus() >= 2) {
                throw new BusinessException("该蜡块已取材，不能重复取材");
            }
            b.setStatus(2);
            b.setSamplingBy(who);
            b.setSamplingTime(now);
        } else if (action == 2) {
            if (b.getStatus() < 2) {
                throw new BusinessException("蜡块尚未取材，不能包埋");
            }
            if (b.getStatus() >= 3) {
                throw new BusinessException("该蜡块已包埋");
            }
            b.setStatus(3);
            b.setEmbeddingBy(who);
            b.setEmbeddingTime(now);
        } else if (action == 3) {
            if (b.getStatus() < 3) {
                throw new BusinessException("蜡块尚未包埋，不能切片");
            }
            if (b.getStatus() >= 4) {
                throw new BusinessException("该蜡块已切片");
            }
            b.setStatus(4);
            b.setSliceBy(who);
            b.setSliceTime(now);
        } else {
            throw new BusinessException("不支持的蜡块动作：" + action);
        }
        bizPathologyBlockMapper.updateById(b);

        // 蜡块动作顺带把主单推进到对应阶段（首块取材→已取材；首块切片→已制片）
        if (b.getStatus() == 2 && PathologyStatusEnum.RECEIVED.is(o.getStatus())) {
            o.setStatus(PathologyStatusEnum.SAMPLED.getCode());
            o.setSamplingBy(who);
            o.setSamplingTime(now);
            bizPathologyOrderMapper.updateById(o);
        } else if (b.getStatus() == 4 && o.getStatus() < PathologyStatusEnum.SLICED.getCode()) {
            o.setStatus(PathologyStatusEnum.SLICED.getCode());
            o.setSliceBy(who);
            o.setSliceTime(now);
            bizPathologyOrderMapper.updateById(o);
        }
        return o.getId();
    }

    /**
     * 初诊
     */
    @Transactional(rollbackFor = Exception.class)
    public void report(PathologyDTO.Report dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizPathologyOrder o = requireOrder(dto.getOrderId());
        assertMutable(o);
        boolean frozen = isFrozenOrCytology(o);
        if (!frozen && !PathologyStatusEnum.SLICED.is(o.getStatus())) {
            throw new BusinessException("非冰冻/细胞学单必须先完成制片才能初诊（当前：" + dictCacheService.getDicDataLabel(DictType.PATHOLOGY_STATUS, o.getStatus()) + "）");
        }
        if (frozen && o.getStatus() < PathologyStatusEnum.RECEIVED.getCode()) {
            throw new BusinessException("标本尚未接收，不能出具冰冻诊断");
        }
        if (Integer.valueOf(1).equals(o.getIsFrozen()) && !TextUtil.hasText(dto.getFrozenResult())) {
            throw new BusinessException("冰冻单必须填写术中冰冻快速诊断结果");
        }
        o.setGrossFindings(dto.getGrossFindings());
        o.setMicroscopyFindings(dto.getMicroscopyFindings());
        o.setIhcResult(dto.getIhcResult());
        o.setDiagnosis(dto.getDiagnosis());
        o.setSuggestion(dto.getSuggestion());
        o.setFrozenResult(dto.getFrozenResult());
        o.setStatus(PathologyStatusEnum.REPORTED.getCode());
        o.setReportBy(operatorUser.getRealName());
        o.setReportTime(LocalDateTime.now().withNano(0));
        bizPathologyOrderMapper.updateById(o);
    }

    /**
     * 审核（审核人不得是初诊人本人）
     */
    @Transactional(rollbackFor = Exception.class)
    public void audit(PathologyDTO.Audit dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizPathologyOrder o = requireOrder(dto.getOrderId());
        assertMutable(o);
        if (!PathologyStatusEnum.REPORTED.is(o.getStatus())) {
            throw new BusinessException("仅「已初诊」的病理单可审核（当前：" + dictCacheService.getDicDataLabel(DictType.PATHOLOGY_STATUS, o.getStatus()) + "）");
        }
        String who = operatorUser.getRealName();
        if (who != null && who.equals(o.getReportBy())) {
            throw new BusinessException("审核人不得是初诊人本人（" + who + "）——病理报告必须两级签署");
        }
        o.setStatus(PathologyStatusEnum.AUDITED.getCode());
        o.setAuditBy(who);
        o.setAuditTime(LocalDateTime.now().withNano(0));
        if (TextUtil.hasText(dto.getAuditOpinion())) {
            o.setRemark(TextUtil.cut(o.getRemark() == null ? "" : o.getRemark() + " | 审核意见：" + dto.getAuditOpinion(), 480));
        }
        bizPathologyOrderMapper.updateById(o);
    }

    /**
     * 发布（终态前一步：已审核 → 已发布）
     */
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long orderId) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizPathologyOrder o = requireOrder(orderId);
        if (PathologyStatusEnum.PUBLISHED.is(o.getStatus())) {
            throw new BusinessException("该病理报告已发布");
        }
        if (!PathologyStatusEnum.AUDITED.is(o.getStatus())) {
            throw new BusinessException("报告发布前必须完成审核（当前：" + dictCacheService.getDicDataLabel(DictType.PATHOLOGY_STATUS, o.getStatus()) + "）");
        }
        o.setStatus(PathologyStatusEnum.PUBLISHED.getCode());
        o.setPublishBy(operatorUser.getRealName());
        o.setPublishTime(LocalDateTime.now().withNano(0));
        bizPathologyOrderMapper.updateById(o);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(PathologyDTO.Cancel dto) {
        BizPathologyOrder o = requireOrder(dto.getOrderId());
        if (PathologyStatusEnum.PUBLISHED.is(o.getStatus())) {
            throw new BusinessException("已发布的病理报告不能取消");
        }
        if (PathologyStatusEnum.CANCELLED.is(o.getStatus())) {
            throw new BusinessException("该病理单已取消");
        }
        o.setStatus(PathologyStatusEnum.CANCELLED.getCode());
        o.setCancelReason(TextUtil.cut(dto.getCancelReason(), 480));
        o.setCancelTime(LocalDateTime.now().withNano(0));
        bizPathologyOrderMapper.updateById(o);
    }

    // 内部

    private BizPathologyOrder requireOrder(Long id) {
        // C类：入参是主键参数而非请求 DTO，Bean Validation 只在 HTTP DTO 绑定时生效，无法下沉
        if (id == null) {
            throw new BusinessException("病理单ID不能为空");
        }
        BizPathologyOrder o = bizPathologyOrderMapper.selectById(id);
        if (o == null) {
            throw new BusinessException("病理单不存在：" + id);
        }
        return o;
    }

    private void assertMutable(BizPathologyOrder o) {
        if (PathologyStatusEnum.PUBLISHED.is(o.getStatus()) || PathologyStatusEnum.CANCELLED.is(o.getStatus())) {
            throw new BusinessException("已" + (PathologyStatusEnum.PUBLISHED.is(o.getStatus()) ? "发布" : "取消") + "的病理单不可再操作");
        }
    }

    private boolean isFrozenOrCytology(BizPathologyOrder o) {
        return Integer.valueOf(1).equals(o.getIsFrozen())
                || Integer.valueOf(EXAM_FROZEN).equals(o.getExamType())
                || Integer.valueOf(EXAM_CYTOLOGY).equals(o.getExamType());
    }

    /**
     * 统计某状态下区间内蜡块数（minStatus~maxStatus）
     */
    private long countBlocks(Long orderId, int minStatus, int maxStatus) {
        return bizPathologyBlockMapper.selectCount(new LambdaQueryWrapper<BizPathologyBlock>()
                .eq(BizPathologyBlock::getOrderId, orderId)
                .ge(BizPathologyBlock::getStatus, minStatus)
                .le(BizPathologyBlock::getStatus, maxStatus));
    }

    private String nextOrderNo(LocalDate date) {
        String day = date.format(DateFormats.COMPACT_DATE);
        long base = bizPathologyOrderMapper.selectCount(new LambdaQueryWrapper<BizPathologyOrder>()
                .ge(BizPathologyOrder::getCreateTime, TimeUtil.dayStart(date))
                .lt(BizPathologyOrder::getCreateTime, TimeUtil.dayStart(date.plusDays(1)))) + 1;
        for (int i = 0; i < 20; i++) {
            String no = "BL" + day + String.format("%03d", base + i);
            if (bizPathologyOrderMapper.selectCount(new LambdaQueryWrapper<BizPathologyOrder>()
                    .eq(BizPathologyOrder::getOrderNo, no)) == 0) {
                return no;
            }
        }
        return "BL" + day + System.currentTimeMillis() % 100000;
    }

    /**
     * 供内镜域按病理号反查（活检送检后回填）
     */
    public String orderNoById(Long id) {
        if (id == null) {
            return null;
        }
        BizPathologyOrder o = bizPathologyOrderMapper.selectById(id);
        return o == null ? null : o.getOrderNo();
    }

    /**
     * 判断某病理号是否存在（避免重复送检）
     */
    public boolean existsBySource(String sourceRecordNo) {
        if (!TextUtil.hasText(sourceRecordNo)) {
            return false;
        }
        return bizPathologyOrderMapper.selectCount(new LambdaQueryWrapper<BizPathologyOrder>()
                .like(BizPathologyOrder::getRemark, sourceRecordNo)) > 0;
    }

    @SuppressWarnings("unused")
    private boolean samePerson(String a, String b) {
        return Objects.equals(a, b);
    }
}
