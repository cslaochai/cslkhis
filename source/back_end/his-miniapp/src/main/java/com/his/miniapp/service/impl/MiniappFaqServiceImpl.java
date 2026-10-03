package com.his.miniapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.miniapp.dto.FaqSearchDTO;
import com.his.miniapp.dto.FaqUpsertDTO;
import com.his.miniapp.entity.SysFaq;
import com.his.miniapp.mapper.MiniappFaqMapper;
import com.his.miniapp.service.MiniappFaqService;
import com.his.miniapp.support.FaqSearchSupport;
import com.his.miniapp.vo.FaqAdminVO;
import com.his.miniapp.vo.FaqCategoryVO;
import com.his.miniapp.vo.FaqListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 患者端常见问题实现。
 *
 * <p><b>刻意不做语义检索</b>：几十条人工语料用关键词打分就够，
 * 上向量检索只会多一个"为什么这条排在前面"说不清的黑盒。
 * 真出现搜不到的情况，正确动作是补关键词，不是换检索算法。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniappFaqServiceImpl implements MiniappFaqService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int DEFAULT_HOT_LIMIT = 8;
    private static final int MAX_HOT_LIMIT = 20;

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MiniappFaqMapper faqMapper;

    @Override
    public List<FaqCategoryVO> categories() {
        Map<String, FaqCategoryVO> map = new LinkedHashMap<>();
        for (SysFaq faq : enabledFaqs()) {
            FaqCategoryVO vo = map.computeIfAbsent(faq.getCategoryCode(), code -> {
                FaqCategoryVO item = new FaqCategoryVO();
                item.setCategoryCode(code);
                item.setCategoryName(faq.getCategoryName());
                item.setCount(0);
                item.setSortOrder(faq.getSortOrder());
                return item;
            });
            vo.setCount(vo.getCount() + 1);
            if (vo.getSortOrder() == null || (faq.getSortOrder() != null && faq.getSortOrder() < vo.getSortOrder())) {
                vo.setSortOrder(faq.getSortOrder());
            }
        }
        return new ArrayList<>(map.values()).stream()
                .sorted(Comparator.comparing(vo -> vo.getSortOrder() == null ? Integer.MAX_VALUE : vo.getSortOrder()))
                .toList();
    }

    @Override
    public PageResult<FaqListVO> search(FaqSearchDTO dto) {
        List<SysFaq> all = enabledFaqs();
        if (dto != null && StringUtils.hasText(dto.getCategoryCode())) {
            all = all.stream().filter(f -> dto.getCategoryCode().equals(f.getCategoryCode())).toList();
        }
        String keyword = dto == null ? null : dto.getKeyword();
        List<String> terms = FaqSearchSupport.splitTerms(keyword);
        List<SysFaq> matched;
        if (terms.isEmpty()) {
            matched = all.stream()
                    .sorted(Comparator.comparing(f -> f.getSortOrder() == null ? Integer.MAX_VALUE : f.getSortOrder()))
                    .toList();
        } else {
            Map<Long, Integer> scores = new LinkedHashMap<>();
            for (SysFaq faq : all) {
                int score = FaqSearchSupport.scoreOf(faq, terms);
                if (score > 0) {
                    scores.put(faq.getId(), score);
                }
            }
            matched = all.stream()
                    .filter(f -> scores.containsKey(f.getId()))
                    .sorted(Comparator.<SysFaq>comparingInt(f -> scores.get(f.getId())).reversed()
                            .thenComparing(f -> f.getSortOrder() == null ? Integer.MAX_VALUE : f.getSortOrder()))
                    .toList();
        }

        int size = pageSize(dto == null ? null : dto.getPageSize());
        int current = pageNum(dto == null ? null : dto.getPageNum());
        long total = matched.size();
        int from = Math.min((current - 1) * size, matched.size());
        int to = Math.min(from + size, matched.size());
        List<FaqListVO> records = matched.subList(from, to).stream().map(MiniappFaqServiceImpl::toVO).toList();
        return PageResult.of(total, current, size, (total + size - 1) / size, records);
    }

    @Override
    public FaqListVO getById(Long faqId) {
        if (faqId == null) {
            throw new BusinessException("常见问题ID不能为空");
        }
        SysFaq faq = faqMapper.selectOne(new LambdaQueryWrapper<SysFaq>()
                .eq(SysFaq::getId, faqId)
                .eq(SysFaq::getStatus, 1));
        if (faq == null) {
            throw new BusinessException("常见问题不存在或已停用");
        }
        // 查看次数自增：用 SQL 自增而不是 updateById 回写对象值，避免并发下互相覆盖
        faqMapper.update(null, new LambdaUpdateWrapper<SysFaq>()
                .eq(SysFaq::getId, faqId)
                .setSql("view_count = view_count + 1"));
        return toVO(faq);
    }

    @Override
    public List<FaqListVO> hotList(Integer limit) {
        int size = limit == null || limit < 1 ? DEFAULT_HOT_LIMIT : Math.min(limit, MAX_HOT_LIMIT);
        return enabledFaqs().stream()
                .sorted(Comparator.comparing((SysFaq f) -> f.getHotFlag() == null ? 0 : f.getHotFlag()).reversed()
                        .thenComparing(f -> f.getViewCount() == null ? 0 : f.getViewCount(), Comparator.reverseOrder())
                        .thenComparing(f -> f.getSortOrder() == null ? Integer.MAX_VALUE : f.getSortOrder()))
                .limit(size)
                .map(MiniappFaqServiceImpl::toVO)
                .toList();
    }

    @Override
    public int feedback(Long faqId, Integer helpful) {
        if (faqId == null) {
            throw new BusinessException("常见问题ID不能为空");
        }
        boolean useful = helpful == null || helpful != 0;
        boolean updated = faqMapper.update(null, new LambdaUpdateWrapper<SysFaq>()
                .eq(SysFaq::getId, faqId)
                .eq(SysFaq::getStatus, 1)
                .setSql(useful ? "helpful_count = helpful_count + 1" : "useless_count = useless_count + 1")) > 0;
        if (!updated) {
            throw new BusinessException("常见问题不存在或已停用");
        }
        return 1;
    }

    @Override
    public PageResult<FaqAdminVO> adminPage(FaqSearchDTO dto) {
        List<SysFaq> all = faqMapper.selectList(new LambdaQueryWrapper<SysFaq>()
                .orderByAsc(SysFaq::getSortOrder)
                .orderByAsc(SysFaq::getId));
        if (dto != null && StringUtils.hasText(dto.getCategoryCode())) {
            all = all.stream().filter(f -> dto.getCategoryCode().equals(f.getCategoryCode())).toList();
        }
        String keyword = dto == null ? null : dto.getKeyword();
        if (StringUtils.hasText(keyword)) {
            all = all.stream()
                    .filter(f -> (f.getQuestion() != null && f.getQuestion().contains(keyword.trim()))
                            || (f.getKeywords() != null && f.getKeywords().contains(keyword.trim())))
                    .toList();
        }
        int size = pageSize(dto == null ? null : dto.getPageSize());
        int current = pageNum(dto == null ? null : dto.getPageNum());
        long total = all.size();
        int from = Math.min((current - 1) * size, all.size());
        int to = Math.min(from + size, all.size());
        List<FaqAdminVO> records = all.subList(from, to).stream().map(MiniappFaqServiceImpl::toAdminVO).toList();
        return PageResult.of(total, current, size, (total + size - 1) / size, records);
    }

    @Override
    public FaqAdminVO adminGetById(Long faqId) {
        SysFaq faq = faqMapper.selectById(faqId);
        if (faq == null) {
            throw new BusinessException("常见问题不存在");
        }
        return toAdminVO(faq);
    }

    @Override
    public String adminUpsert(FaqUpsertDTO dto) {
        boolean isNew = !StringUtils.hasText(dto.getId()) || !dto.getId().matches("\\d{1,20}");
        SysFaq entity = isNew ? new SysFaq() : faqMapper.selectById(Long.parseLong(dto.getId()));
        if (!isNew && entity == null) {
            throw new BusinessException("常见问题不存在：" + dto.getId());
        }
        if (isNew) {
            entity.setFaqNo(nextFaqNo());
            entity.setViewCount(0);
            entity.setHelpfulCount(0);
            entity.setUselessCount(0);
        }
        entity.setCategoryCode(dto.getCategoryCode().trim());
        entity.setCategoryName(dto.getCategoryName().trim());
        entity.setQuestion(cut(dto.getQuestion(), 200));
        entity.setAnswer(cut(dto.getAnswer(), 1000));
        entity.setKeywords(cut(dto.getKeywords(), 500));
        entity.setHotFlag(dto.getHotFlag() == null ? 0 : dto.getHotFlag());
        entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        entity.setSortOrder(dto.getSortOrder() == null ? 999 : dto.getSortOrder());
        if (isNew) {
            faqMapper.insert(entity);
        } else {
            faqMapper.updateById(entity);
        }
        return String.valueOf(entity.getId());
    }

    @Override
    public void adminDelete(Long faqId) {
        if (faqId == null) {
            throw new BusinessException("常见问题ID不能为空");
        }
        faqMapper.purgeById(faqId);
    }

    // 私有

    /** 编号 FAQ + 4 位序号，取全表最大号 +1（faq_no 是全局唯一键，不能按天 count） */
    private String nextFaqNo() {
        String max = faqMapper.maxFaqNo();
        int seq = 1;
        if (StringUtils.hasText(max) && max.length() > 3) {
            try {
                seq = Integer.parseInt(max.substring(3)) + 1;
            } catch (NumberFormatException ex) {
                seq = 1;
            }
        }
        return "FAQ" + String.format("%04d", seq);
    }

    private static String cut(String text, int max) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        String value = text.trim();
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static FaqAdminVO toAdminVO(SysFaq faq) {
        FaqAdminVO vo = new FaqAdminVO();
        vo.setId(faq.getId());
        vo.setFaqNo(faq.getFaqNo());
        vo.setCategoryCode(faq.getCategoryCode());
        vo.setCategoryName(faq.getCategoryName());
        vo.setQuestion(faq.getQuestion());
        vo.setAnswer(faq.getAnswer());
        vo.setKeywords(faq.getKeywords());
        vo.setHotFlag(faq.getHotFlag());
        vo.setViewCount(faq.getViewCount());
        vo.setHelpfulCount(faq.getHelpfulCount());
        vo.setUselessCount(faq.getUselessCount());
        vo.setStatus(faq.getStatus());
        vo.setSortOrder(faq.getSortOrder());
        vo.setCreateTime(faq.getCreateTime() == null ? "" : faq.getCreateTime().format(TIME));
        return vo;
    }

    private List<SysFaq> enabledFaqs() {
        List<SysFaq> list = faqMapper.selectList(new LambdaQueryWrapper<SysFaq>()
                .eq(SysFaq::getStatus, 1)
                .orderByAsc(SysFaq::getSortOrder));
        return list == null ? List.of() : list;
    }

    private static int pageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? 1 : pageNum;
    }

    private static int pageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private static FaqListVO toVO(SysFaq faq) {
        FaqListVO vo = new FaqListVO();
        vo.setId(faq.getId());
        vo.setFaqNo(faq.getFaqNo());
        vo.setCategoryCode(faq.getCategoryCode());
        vo.setCategoryName(faq.getCategoryName());
        vo.setQuestion(faq.getQuestion());
        vo.setAnswer(faq.getAnswer());
        vo.setHotFlag(faq.getHotFlag());
        vo.setViewCount(faq.getViewCount());
        return vo;
    }
}
