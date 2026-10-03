package com.his.emr.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.Constants;
import com.his.common.base.PageResult;
import com.his.common.base.RedisSequenceService;
import com.his.common.exception.BusinessException;
import com.his.emr.dto.SurveyTemplateQueryPageDTO;
import com.his.emr.dto.SurveyTemplateUpsertDTO;
import com.his.emr.entity.BizSurveyDispatch;
import com.his.emr.entity.BizSurveyItem;
import com.his.emr.entity.BizSurveyTemplate;
import com.his.emr.mapper.BizSurveyDispatchMapper;
import com.his.emr.mapper.BizSurveyItemMapper;
import com.his.emr.mapper.BizSurveyTemplateMapper;
import com.his.emr.service.SurveyTemplateService;
import com.his.emr.vo.SurveyTemplateSelectListVO;
import com.his.emr.vo.SurveyTemplateVO;
import com.his.security.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.his.emr.enums.SurveyQuestionTypeEnum;
import com.his.emr.enums.SurveyTemplateStatusEnum;

/**
 * 满意度问卷模板服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyTemplateServiceImpl implements SurveyTemplateService {

    private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 维度码值范围（字典 his_survey_dimension 1-7） */
    private static final int DIMENSION_MIN = 1;
    private static final int DIMENSION_MAX = 7;
    /** 题型码值范围（字典 his_survey_question_type 1-5） */
    private static final int QT_MIN = 1;
    private static final int QT_MAX = 5;

    private final BizSurveyTemplateMapper templateMapper;
    private final BizSurveyItemMapper itemMapper;
    private final BizSurveyDispatchMapper dispatchMapper;
    private final RedisSequenceService sequenceService;

    @Override
    public PageResult<SurveyTemplateVO> listPage(SurveyTemplateQueryPageDTO dto) {
        LambdaQueryWrapper<BizSurveyTemplate> wrapper = new LambdaQueryWrapper<>();
        String keyword = trimToNull(dto.getKeyword());
        wrapper.and(keyword != null, w -> w.like(BizSurveyTemplate::getTemplateName, keyword)
                        .or().like(BizSurveyTemplate::getTemplateNo, keyword))
                .eq(dto.getScene() != null, BizSurveyTemplate::getScene, dto.getScene())
                .eq(dto.getStatus() != null, BizSurveyTemplate::getStatus, dto.getStatus())
                .orderByDesc(BizSurveyTemplate::getId);
        Page<BizSurveyTemplate> page = templateMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
        List<Long> ids = page.getRecords().stream().map(BizSurveyTemplate::getId).collect(Collectors.toList());
        // 题目数一次批量捞：逐行 count 会在「一页 20 张卷」时打出 20 条 SQL
        Map<Long, Integer> itemCounts = ids.isEmpty() ? Map.of()
                : itemMapper.countByTemplates(ids).stream().collect(Collectors.toMap(
                        r -> asLong(r.get("t")), r -> (int) asLong(r.get("c")), (a, b) -> a));
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
        List<BizSurveyTemplate> list = templateMapper.selectList(new LambdaQueryWrapper<BizSurveyTemplate>()
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
            vo.setItemCount(Math.toIntExact(itemMapper.selectCount(new LambdaQueryWrapper<BizSurveyItem>()
                    .eq(BizSurveyItem::getTemplateId, t.getId()))));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public SurveyTemplateVO getDetailById(Long id) {
        BizSurveyTemplate template = requireTemplate(id);
        SurveyTemplateVO vo = new SurveyTemplateVO();
        BeanUtils.copyProperties(template, vo);
        vo.setItems(itemMapper.selectByTemplate(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SurveyTemplateVO upsert(SurveyTemplateUpsertDTO dto) {
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
        template.setTemplateName(trimToNull(dto.getTemplateName()));
        template.setScene(dto.getScene());
        template.setDescription(cut(dto.getDescription(), 500));
        template.setRemark(cut(dto.getRemark(), 512));
        validateItems(dto.getItems());

        if (isNew) {
            template.setCreateBy(currentOperator());
            templateMapper.insert(template);
        } else {
            templateMapper.updateById(template);
        }

        // 整卷覆盖：物理删旧题再插 —— uk_survey_item(template_id,seq_no) 不含 del_flag，软删必撞键
        itemMapper.purgeByTemplate(template.getId());
        for (SurveyTemplateUpsertDTO.Item src : dto.getItems()) {
            itemMapper.insert(toItem(template.getId(), src));
        }
        return getDetailById(template.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        requireTemplate(id);
        // 已被发放引用就不许删：历史答卷要能回答「当时问的是哪张卷」，
        // 删了模板，报表上的模板名与题目快照就成了无源之水。改用停用。
        Long used = dispatchMapper.selectCount(new LambdaQueryWrapper<BizSurveyDispatch>()
                .eq(BizSurveyDispatch::getTemplateId, id));
        if (used != null && used > 0) {
            throw new BusinessException("该问卷已发放 " + used + " 次，不能删除，请改为「停用」");
        }
        // 题目物理删（模板没了，题目留着只会被 uk 继续占位），模板本身走 MP 软删
        itemMapper.purgeByTemplate(id);
        return templateMapper.deleteById(id) > 0;
    }

    @Override
    public SurveyTemplateVO findEnabledForScene(Integer scene) {
        BizSurveyTemplate template = templateMapper.selectOne(new LambdaQueryWrapper<BizSurveyTemplate>()
                .eq(BizSurveyTemplate::getStatus, SurveyTemplateStatusEnum.ENABLED.getCode())
                .eq(BizSurveyTemplate::getScene, scene)
                .orderByDesc(BizSurveyTemplate::getId)
                .last("LIMIT 1"));
        if (template == null) {
            return null;
        }
        SurveyTemplateVO vo = new SurveyTemplateVO();
        BeanUtils.copyProperties(template, vo);
        vo.setItems(itemMapper.selectByTemplate(template.getId()));
        return vo;
    }

    // 内部

    /**
     * 题目校验：题号卷内唯一、维度/题型在字典码值范围内。
     *
     * <p>题号重复不在这里靠数据库报错 —— 撞 uk_survey_item 的 500 对配置页用户毫无意义，
     * 前置校验才能把「第 3 题和第 5 题题号都是 4」说清楚。
     */
    private void validateItems(List<SurveyTemplateUpsertDTO.Item> items) {
        Set<Integer> seqs = new HashSet<>();
        for (SurveyTemplateUpsertDTO.Item item : items) {
            if (!seqs.add(item.getSeqNo())) {
                throw new BusinessException("题号重复：" + item.getSeqNo());
            }
            if (item.getDimension() < DIMENSION_MIN || item.getDimension() > DIMENSION_MAX) {
                throw new BusinessException("评价维度取值不合法（" + DIMENSION_MIN + "-" + DIMENSION_MAX + "）");
            }
            if (item.getQuestionType() < QT_MIN || item.getQuestionType() > QT_MAX) {
                throw new BusinessException("题型取值不合法（" + QT_MIN + "-" + QT_MAX + "）");
            }
            if (Objects.equals(item.getQuestionType(), SurveyQuestionTypeEnum.SCALE.getCode())
                    && item.getMaxScore() != null && item.getMaxScore() != 5) {
                throw new BusinessException("量表题满分固定为 5（李克特 5 级），否则百分制口径就断了");
            }
        }
    }

    private BizSurveyItem toItem(Long templateId, SurveyTemplateUpsertDTO.Item src) {
        BizSurveyItem item = new BizSurveyItem();
        item.setTemplateId(templateId);
        item.setSeqNo(src.getSeqNo());
        item.setDimension(src.getDimension());
        item.setQuestionType(src.getQuestionType());
        item.setTitle(cut(src.getTitle(), 255));
        item.setRequired(src.getRequired() == null ? 1 : src.getRequired());
        item.setWeight(src.getWeight() == null ? BigDecimal.ONE : src.getWeight());
        item.setMaxScore(src.getMaxScore() == null ? defaultMaxScore(src.getQuestionType()) : src.getMaxScore());
        item.setCreateBy(currentOperator());
        return item;
    }

    /** 满分按题型兜底：量表 5、NPS 10、其余 0（不参与计分） */
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
        BizSurveyTemplate template = id == null ? null : templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("问卷模板不存在");
        }
        return template;
    }

    private String nextNo() {
        return Constants.SURVEY_TEMPLATE_NO_PREFIX + LocalDate.now().format(NO_DATE)
                + String.format("%04d", sequenceService.next(Constants.SURVEY_TEMPLATE_NO_KEY_PREFIX));
    }

    private String currentOperator() {
        String name = UserUtils.getCurrentEmployeeName();
        return StringUtils.hasText(name) ? name : "system";
    }

    /** 入库前截到列宽：超长文本让服务端截断，而不是让 insert 报 Data too long 变成 500 */
    private static String cut(String v, int max) {
        if (v == null) {
            return null;
        }
        String s = v.trim();
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static String trimToNull(String v) {
        return StringUtils.hasText(v) ? v.trim() : null;
    }

    /** 聚合列（COUNT/BIGINT）在 JDBC 侧可能是 Long/BigInteger/BigDecimal，统一按字符串转 */
    private static long asLong(Object v) {
        return v == null ? 0L : new BigDecimal(String.valueOf(v)).longValue();
    }
}
