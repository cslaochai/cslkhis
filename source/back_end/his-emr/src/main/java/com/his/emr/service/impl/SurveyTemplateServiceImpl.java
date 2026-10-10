package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.constant.BizCodeConst;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.system.service.RedisSequenceService;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.emr.dto.SurveyTemplateQueryPageDTO;
import com.his.emr.dto.SurveyTemplateUpsertDTO;
import com.his.emr.entity.BizSurveyDispatch;
import com.his.emr.entity.BizSurveyItem;
import com.his.emr.entity.BizSurveyTemplate;
import com.his.emr.enums.SurveyQuestionTypeEnum;
import com.his.emr.enums.SurveyTemplateStatusEnum;
import com.his.emr.mapper.BizSurveyDispatchMapper;
import com.his.emr.mapper.BizSurveyItemMapper;
import com.his.emr.mapper.BizSurveyTemplateMapper;
import com.his.emr.service.SurveyTemplateService;
import com.his.emr.vo.SurveyTemplateItemCountVO;
import com.his.emr.vo.SurveyTemplateSelectListVO;
import com.his.emr.vo.SurveyTemplateVO;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 满意度问卷模板服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyTemplateServiceImpl extends ServiceImpl<BizSurveyTemplateMapper, BizSurveyTemplate> implements SurveyTemplateService {

    private final BizSurveyTemplateMapper bizSurveyTemplateMapper;
    private final BizSurveyItemMapper bizSurveyItemMapper;
    private final BizSurveyDispatchMapper bizSurveyDispatchMapper;
    private final RedisSequenceService redisSequenceService;

    @Override
    public PageResult<SurveyTemplateVO> listPage(SurveyTemplateQueryPageDTO dto) {
        LambdaQueryWrapper<BizSurveyTemplate> wrapper = new LambdaQueryWrapper<>();
        String keyword = TextUtil.trimToNull(dto.getKeyword());
        wrapper.and(keyword != null, w -> w.like(BizSurveyTemplate::getTemplateName, keyword)
                        .or().like(BizSurveyTemplate::getTemplateNo, keyword))
                .eq(dto.getScene() != null, BizSurveyTemplate::getScene, dto.getScene())
                .eq(dto.getStatus() != null, BizSurveyTemplate::getStatus, dto.getStatus())
                .orderByDesc(BizSurveyTemplate::getId);
        Page<BizSurveyTemplate> page = bizSurveyTemplateMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<Long> ids = page.getRecords().stream().map(BizSurveyTemplate::getId).collect(Collectors.toList());
        // 题目数一次批量捞：逐行 count 会在「一页 20 张卷」时打出 20 条 SQL
        Map<Long, Integer> itemCounts = ids.isEmpty() ? Map.of()
                : bizSurveyItemMapper.countByTemplates(ids).stream().collect(Collectors.toMap(
                SurveyTemplateItemCountVO::getTemplateId,
                r -> r.getCnt() == null ? 0 : r.getCnt().intValue(), (a, b) -> a));
        List<SurveyTemplateVO> records = page.getRecords().stream().map(t -> {
            SurveyTemplateVO vo = new SurveyTemplateVO();
            BeanUtils.copyProperties(t, vo);
            vo.setItemCount(itemCounts.getOrDefault(t.getId(), 0));
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), records);
    }

    @Override
    public List<SurveyTemplateSelectListVO> selectEnabled(Integer scene) {
        List<BizSurveyTemplate> list = bizSurveyTemplateMapper.selectList(new LambdaQueryWrapper<BizSurveyTemplate>()
                .eq(BizSurveyTemplate::getStatus, SurveyTemplateStatusEnum.ENABLED.getCode())
                .eq(scene != null, BizSurveyTemplate::getScene, scene)
                .orderByDesc(BizSurveyTemplate::getId));
        return list.stream().map(t -> {
            SurveyTemplateSelectListVO vo = new SurveyTemplateSelectListVO();
            vo.setId(t.getId());
            vo.setTemplateNo(t.getTemplateNo());
            vo.setTemplateName(t.getTemplateName());
            vo.setScene(t.getScene());
            vo.setStatus(t.getStatus());
            vo.setItemCount(Math.toIntExact(bizSurveyItemMapper.selectCount(new LambdaQueryWrapper<BizSurveyItem>()
                    .eq(BizSurveyItem::getTemplateId, t.getId()))));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public SurveyTemplateVO getDetailById(Long id) {
        BizSurveyTemplate template = requireTemplate(id);
        SurveyTemplateVO vo = new SurveyTemplateVO();
        BeanUtils.copyProperties(template, vo);
        vo.setItems(bizSurveyItemMapper.selectByTemplate(id));
        return vo;
    }

    // 内部

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SurveyTemplateVO upsert(SurveyTemplateUpsertDTO dto) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizSurveyTemplate template;
        boolean isNew = dto.getId() == null;
        if (isNew) {
            template = new BizSurveyTemplate();
            template.setTemplateNo(nextNo());
            template.setStatus(dto.getStatus() == null ? SurveyTemplateStatusEnum.ENABLED.getCode() : dto.getStatus());
        } else {
            template = requireTemplate(dto.getId());
            if (dto.getStatus() != null) {
                template.setStatus(dto.getStatus());
            }
        }
        template.setTemplateName(TextUtil.trimToNull(dto.getTemplateName()));
        template.setScene(dto.getScene());
        template.setDescription(TextUtil.cut(dto.getDescription(), 500));
        template.setRemark(TextUtil.cut(dto.getRemark(), 512));
        validateItems(dto.getItems());

        if (isNew) {
            template.setCreateBy(operatorUser.getRealName());
            bizSurveyTemplateMapper.insert(template);
        } else {
            bizSurveyTemplateMapper.updateById(template);
        }

        // 整卷覆盖：物理删旧题再插 —— uk_survey_item(template_id,seq_no) 不含 del_flag，软删必撞键
        bizSurveyItemMapper.purgeByTemplate(template.getId());
        for (SurveyTemplateUpsertDTO.Item src : dto.getItems()) {
            bizSurveyItemMapper.insert(toItem(template.getId(), src));
        }
        return getDetailById(template.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        requireTemplate(id);
        // 已被发放引用就不许删：历史答卷要能回答「当时问的是哪张卷」，
        // 删了模板，报表上的模板名与题目快照就成了无源之水。改用停用。
        Long used = bizSurveyDispatchMapper.selectCount(new LambdaQueryWrapper<BizSurveyDispatch>()
                .eq(BizSurveyDispatch::getTemplateId, id));
        if (used != null && used > 0) {
            throw new BusinessException("该问卷已发放 " + used + " 次，不能删除，请改为「停用」");
        }
        // 题目物理删（模板没了，题目留着只会被 uk 继续占位），模板本身走 MP 软删
        bizSurveyItemMapper.purgeByTemplate(id);
        return bizSurveyTemplateMapper.deleteById(id) > 0;
    }

    @Override
    public SurveyTemplateVO findEnabledForScene(Integer scene) {
        BizSurveyTemplate template = bizSurveyTemplateMapper.selectOne(new LambdaQueryWrapper<BizSurveyTemplate>()
                .eq(BizSurveyTemplate::getStatus, SurveyTemplateStatusEnum.ENABLED.getCode())
                .eq(BizSurveyTemplate::getScene, scene)
                .orderByDesc(BizSurveyTemplate::getId)
                .last("LIMIT 1"));
        if (template == null) {
            return null;
        }
        SurveyTemplateVO vo = new SurveyTemplateVO();
        BeanUtils.copyProperties(template, vo);
        vo.setItems(bizSurveyItemMapper.selectByTemplate(template.getId()));
        return vo;
    }

    /**
     * 题目校验：题号卷内唯一、量表题满分固定 5。
     *
     * <p>题号重复不在这里靠数据库报错 —— 撞 uk_survey_item 的 500 对配置页用户毫无意义，
     * 前置校验才能把「第 3 题和第 5 题题号都是 4」说清楚。
     *
     * <p>维度/题型的码值合法性由 DTO 上的 {@code @InEnum} 把关，这里不再手写范围判断
     * （AGENTS.md §16）。
     */
    private void validateItems(List<SurveyTemplateUpsertDTO.Item> items) {
        Set<Integer> seqs = new HashSet<>();
        for (SurveyTemplateUpsertDTO.Item item : items) {
            if (!seqs.add(item.getSeqNo())) {
                throw new BusinessException("题号重复：" + item.getSeqNo());
            }
            if (Objects.equals(item.getQuestionType(), SurveyQuestionTypeEnum.SCALE.getCode())
                    && item.getMaxScore() != null && item.getMaxScore() != 5) {
                throw new BusinessException("量表题满分固定为 5（李克特 5 级），否则百分制口径就断了");
            }
        }
    }

    private BizSurveyItem toItem(Long templateId, SurveyTemplateUpsertDTO.Item src) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizSurveyItem item = new BizSurveyItem();
        item.setTemplateId(templateId);
        item.setSeqNo(src.getSeqNo());
        item.setDimension(src.getDimension());
        item.setQuestionType(src.getQuestionType());
        item.setTitle(TextUtil.cut(src.getTitle(), 255));
        item.setRequired(src.getRequired() == null ? 1 : src.getRequired());
        item.setWeight(src.getWeight() == null ? BigDecimal.ONE : src.getWeight());
        item.setMaxScore(src.getMaxScore() == null ? defaultMaxScore(src.getQuestionType()) : src.getMaxScore());
        item.setCreateBy(operatorUser.getRealName());
        return item;
    }

    /**
     * 满分按题型兜底：量表 5、NPS 10、其余 0（不参与计分）
     */
    private int defaultMaxScore(Integer questionType) {
        if (Objects.equals(questionType, SurveyQuestionTypeEnum.SCALE.getCode())) {
            return 5;
        }
        if (Objects.equals(questionType, SurveyQuestionTypeEnum.NPS.getCode())) {
            return 10;
        }
        return 0;
    }

    private BizSurveyTemplate requireTemplate(Long id) {
        BizSurveyTemplate template = id == null ? null : bizSurveyTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("问卷模板不存在");
        }
        return template;
    }

    private String nextNo() {
        return BizCodeConst.SURVEY_TEMPLATE_NO_PREFIX + LocalDate.now().format(DateFormats.COMPACT_DATE)
                + String.format("%04d", redisSequenceService.next(BizCodeConst.SURVEY_TEMPLATE_NO_KEY_PREFIX));
    }
}
