package com.his.operation.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.enums.SysGenderEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TimeUtil;
import com.his.operation.dto.AnesthesiaFollowupQueryPageDTO;
import com.his.operation.dto.AnesthesiaFollowupUpsertDTO;
import com.his.operation.entity.BizAnesthesiaFollowup;
import com.his.operation.entity.BizAnesthesiaRecord;
import com.his.operation.enums.AnesthesiaFollowupStatusEnum;
import com.his.operation.mapper.BizAnesthesiaFollowupMapper;
import com.his.operation.mapper.BizAnesthesiaRecordMapper;
import com.his.operation.service.AnesthesiaFollowupService;
import com.his.operation.support.FollowupAdverseItems;
import com.his.operation.vo.AnesthesiaFollowupVO;
import com.his.operation.vo.OperationApplyVO;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 麻醉术后随访实现（P134.3）。
 *
 * <p>刻意固化的点：
 * <ol>
 *   <li><b>患者/住院/单号快照一律从麻醉记录带出</b>，前端只传 recordId ——
 *       允许前端传患者姓名就会出现"随访单上的人 ≠ 被麻醉的人"。</li>
 *   <li><b>轮次服务端算（max+1）</b>：同一条麻醉记录下两个"第 1 轮"是最难发现的台账错乱。</li>
 *   <li><b>完成即锁死</b>：草稿可改可删，已完成不可改不可删 —— 随访的价值是"当时看到的是什么样"。</li>
 *   <li><b>单号序号不滤 del_flag</b>：uk_followup_no 不含软删标记，删草稿后按可见行数计号会撞回被删单号。</li>
 *   <li><b>长文本先截后存</b>（1000 列宽），入参层不拦长度（L13 口径）。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnesthesiaFollowupServiceImpl implements AnesthesiaFollowupService {

    /**
     * 麻醉记录状态：已提交
     */
    private static final int RECORD_SUBMITTED = 1;

    /**
     * 麻醉记录状态：已审核
     */
    private static final int RECORD_AUDITED = 2;

    /**
     * 随访状态：唯一口径 AnesthesiaFollowupStatusEnum（0草稿 1已完成）
     */

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final BizAnesthesiaFollowupMapper followupMapper;
    private final BizAnesthesiaRecordMapper recordMapper;

    // 查询

    /**
     * 写库长文本一律先截到列宽（超长 insert 失败会让用户连草稿都存不下）
     */
    private static String cut(String value, int max) {
        if (value == null) {
            return null;
        }
        String s = value.trim();
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private static LocalDateTime now() {
        return TimeUtil.nowSeconds();
    }

    /**
     * 时间统一截到秒，保证「写进去的 = 读回来的」（库表是 DATETIME(0)）
     */
    @Override
    public IPage<AnesthesiaFollowupVO> listPage(AnesthesiaFollowupQueryPageDTO query) {
        if (query == null) {
            query = new AnesthesiaFollowupQueryPageDTO();
        }
        IPage<AnesthesiaFollowupVO> page = followupMapper.selectFollowupPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query);
        page.getRecords().forEach(this::decorate);
        return page;
    }

    @Override
    public AnesthesiaFollowupVO getDetailById(Long id) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (id == null) {
            throw new BusinessException("随访单ID不能为空");
        }
        AnesthesiaFollowupVO vo = followupMapper.selectFollowupById(id);
        if (vo == null) {
            throw new BusinessException("随访单不存在");
        }
        decorate(vo);
        vo.setAdverseItemOptions(adverseItems());
        return vo;
    }

    // 写

    @Override
    public List<AnesthesiaFollowupVO> listByRecord(Long recordId) {
        // C-非 DTO 入参：校验对象是 @RequestParam 标量参数，Bean Validation 不覆盖，保留
        if (recordId == null) {
            throw new BusinessException("麻醉记录ID不能为空");
        }
        List<AnesthesiaFollowupVO> list = followupMapper.selectByRecord(recordId);
        list.forEach(this::decorate);
        return list;
    }

    @Override
    public long countOverduePending() {
        return followupMapper.countOverduePending();
    }

    @Override
    public List<OperationApplyVO.CheckItem> adverseItems() {
        List<OperationApplyVO.CheckItem> list = new ArrayList<>();
        FollowupAdverseItems.all().forEach((code, label) -> {
            OperationApplyVO.CheckItem item = new OperationApplyVO.CheckItem();
            item.setCode(code);
            item.setLabel(label);
            item.setRequired(false);
            list.add(item);
        });
        return list;
    }

    // 校验与展示态

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String save(AnesthesiaFollowupUpsertDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        Set<Integer> adverse;
        try {
            adverse = FollowupAdverseItems.parse(dto.getAdverseItems());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }

        BizAnesthesiaFollowup entity;
        if (dto.getId() == null) {
            BizAnesthesiaRecord record = followableRecord(dto.getRecordId());
            entity = new BizAnesthesiaFollowup();
            entity.setRecordId(record.getId());
            entity.setRecordNo(record.getRecordNo());
            entity.setApplyId(record.getApplyId());
            entity.setAdmissionId(record.getAdmissionId());
            entity.setPatientId(record.getPatientId());
            entity.setPatientName(record.getPatientName());
            entity.setGender(record.getGender());
            entity.setAge(record.getAge());
            entity.setRoundNo(followupMapper.maxRoundOf(record.getId()) + 1);
            entity.setFollowupNo(nextFollowupNo());
            entity.setFollowupStatus(AnesthesiaFollowupStatusEnum.DRAFT.getCode());
            entity.setFollowupDoctorId(operatorUser.getEmployeeId());
            entity.setFollowupDoctorName(operatorUser.getRealName());
        } else {
            entity = mustGetDraft(dto.getId());
            if (!Objects.equals(entity.getRecordId(), dto.getRecordId())) {
                throw new BusinessException("不允许把随访单改挂到另一条麻醉记录上");
            }
        }
        validateTimeAgainstAnesthesia(dto.getFollowupTime(), entity.getRecordId());

        entity.setFollowupTime(TimeUtil.toSeconds(dto.getFollowupTime()));
        entity.setPainScore(dto.getPainScore());
        entity.setRecovery(dto.getRecovery());
        entity.setAdverseItems(FollowupAdverseItems.serialize(adverse));
        entity.setAdverseNote(cut(dto.getAdverseNote(), 1000));
        entity.setHandling(cut(dto.getHandling(), 1000));
        if (dto.getRemark() != null) {
            entity.setRemark(cut(dto.getRemark(), 500));
        }

        if (dto.getId() == null) {
            followupMapper.insert(entity);
        } else {
            followupMapper.updateById(entity);
        }
        log.info("{}麻醉随访 followupNo={} recordNo={} 轮次={} 恢复={} 并发症={} 随访人={}",
                dto.getId() == null ? "新建" : "修改", entity.getFollowupNo(), entity.getRecordNo(),
                entity.getRoundNo(), FollowupAdverseItems.recoveryText(entity.getRecovery()),
                StringUtils.hasText(entity.getAdverseItems()) ? entity.getAdverseItems() : "无",
                entity.getFollowupDoctorName());
        return entity.getFollowupNo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(Long id) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizAnesthesiaFollowup entity = mustGetDraft(id);

        // 完成闸门的分量都在文案里：缺一项都不允许"随访"对外生效
        if (entity.getPainScore() == null) {
            throw new BusinessException("疼痛评分（NRS）未填，不能完成随访 —— 疼痛是术后最常被追问的一项");
        }
        if (entity.getRecovery() == null) {
            throw new BusinessException("麻醉恢复情况未评，不能完成随访");
        }
        Set<Integer> adverse;
        try {
            adverse = FollowupAdverseItems.parse(entity.getAdverseItems());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }
        if (!adverse.isEmpty()) {
            if (!StringUtils.hasText(entity.getAdverseNote())) {
                throw new BusinessException("已勾选并发症，必须写明并发症经过（勾了却说不出发生了什么，比不勾更糟）");
            }
            if (!StringUtils.hasText(entity.getHandling())) {
                throw new BusinessException("已勾选并发症，必须写明处理措施与转归（发现了却没处理记录，是随访里最糟的一条链）");
            }
        }
        validateTimeAgainstAnesthesia(entity.getFollowupTime(), entity.getRecordId());

        entity.setFollowupStatus(AnesthesiaFollowupStatusEnum.DONE.getCode());
        entity.setFinishTime(now());
        if (entity.getFollowupDoctorId() == null) {
            entity.setFollowupDoctorId(operatorUser.getEmployeeId());
            entity.setFollowupDoctorName(operatorUser.getRealName());
        }
        followupMapper.updateById(entity);
        log.info("麻醉随访完成 followupNo={} 轮次={} 疼痛={} 恢复={} 完成人={}",
                entity.getFollowupNo(), entity.getRoundNo(), entity.getPainScore(),
                FollowupAdverseItems.recoveryText(entity.getRecovery()), operatorUser.getRealName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizAnesthesiaFollowup entity = mustGetDraft(id);
        followupMapper.deleteById(entity.getId());
        log.info("删除麻醉随访草稿 followupNo={} 操作人={}", entity.getFollowupNo(), operatorUser.getRealName());
    }

    /**
     * 麻醉记录必须存在且已提交/已审核（未定稿的麻醉过程没有"术后"可言）
     */
    private BizAnesthesiaRecord followableRecord(Long recordId) {
        BizAnesthesiaRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("麻醉记录不存在");
        }
        boolean ok = Objects.equals(RECORD_SUBMITTED, record.getRecordStatus())
                || Objects.equals(RECORD_AUDITED, record.getRecordStatus());
        if (!ok) {
            throw new BusinessException("麻醉记录 " + record.getRecordNo()
                    + " 尚未提交（当前状态=" + record.getRecordStatus() + "），不能随访 —— "
                    + "麻醉过程还没定稿，评「恢复得怎么样」评的是一个不存在的基准");
        }
        return record;
    }

    /**
     * 随访时间不得早于麻醉结束时间（时间轴上的自相矛盾必须拦）
     */
    private void validateTimeAgainstAnesthesia(LocalDateTime followupTime, Long recordId) {
        BizAnesthesiaRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException("麻醉记录不存在（recordId=" + recordId + "）");
        }
        if (record.getAnesthesiaEndTime() == null) {
            throw new BusinessException("麻醉记录 " + record.getRecordNo()
                    + " 没有登记麻醉结束时间，无法确定随访基准，请先补全麻醉记录时间轴");
        }
        if (followupTime.isBefore(record.getAnesthesiaEndTime())) {
            throw new BusinessException("随访时间不能早于麻醉结束时间（"
                    + record.getAnesthesiaEndTime() + "）—— 麻醉还没结束就「术后随访」，是伪造");
        }
    }

    private BizAnesthesiaFollowup mustGetDraft(Long id) {
        // C-非 DTO 入参：私有 helper 校验方法参数，被多入口复用，Bean Validation 不覆盖，保留
        if (id == null) {
            throw new BusinessException("随访单ID不能为空");
        }
        BizAnesthesiaFollowup entity = followupMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException("随访单不存在");
        }
        if (!AnesthesiaFollowupStatusEnum.DRAFT.is(entity.getFollowupStatus())) {
            throw new BusinessException("随访单 " + entity.getFollowupNo()
                    + " 已完成，不能修改或删除（完成即锁死 —— 随访的价值是「当时看到的是什么样」）");
        }
        return entity;
    }

    private void decorate(AnesthesiaFollowupVO vo) {
        vo.setGenderText(SysGenderEnum.getText(vo.getGender()));
        vo.setRecoveryText(FollowupAdverseItems.recoveryText(vo.getRecovery()));
        vo.setFollowupStatusText(FollowupAdverseItems.statusText(vo.getFollowupStatus()));
        vo.setRoundText(FollowupAdverseItems.roundText(vo.getRoundNo()));
        try {
            vo.setAdverseItemsText(FollowupAdverseItems.summaryText(vo.getAdverseItems()));
        } catch (IllegalArgumentException e) {
            // 存量脏码不能把整个列表打挂：原样带出，让「未知(n)」显形
            vo.setAdverseItemsText(vo.getAdverseItems());
        }
        boolean draft = AnesthesiaFollowupStatusEnum.DRAFT.is(vo.getFollowupStatus());
        vo.setCanEdit(draft);
        vo.setCanFinish(draft);
        vo.setCanDelete(draft);
    }

    private String nextFollowupNo() {
        String prefix = "MS" + LocalDate.now().format(NO_DATE);
        long seq = followupMapper.countByNoPrefix(prefix) + 1;
        return prefix + String.format("%04d", seq);
    }

    /**
     * 留痕一律用**员工ID**（不是用户的ID），与医嘱/站内信同一口径
     */
}
