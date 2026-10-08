package com.his.miniapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.DateFormats;
import com.his.common.util.TextUtil;
import com.his.miniapp.dto.FaqPageQueryDTO;
import com.his.miniapp.dto.FaqUpsertDTO;
import com.his.miniapp.entity.SysFaq;
import com.his.miniapp.mapper.MiniappFaqMapper;
import com.his.miniapp.service.MiniappFaqService;
import com.his.miniapp.support.FaqSearchSupport;
import com.his.miniapp.vo.FaqCategoryListVO;
import com.his.miniapp.vo.FaqListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 患者端常见问题实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MiniappFaqServiceImpl extends ServiceImpl<MiniappFaqMapper, SysFaq> implements MiniappFaqService {

    private static final int DEFAULT_HOT_LIMIT = 8;
    private static final int MAX_HOT_LIMIT = 20;

    private final MiniappFaqMapper miniappFaqMapper;

    private static FaqListVO toAdminVO(SysFaq faq) {
        FaqListVO vo = new FaqListVO();
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
        vo.setCreateTime(faq.getCreateTime() == null ? "" : faq.getCreateTime().format(DateFormats.DATETIME));
        return vo;
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

    @Override
    public List<FaqCategoryListVO> categories() {
        Map<String, FaqCategoryListVO> map = new LinkedHashMap<>();
        for (SysFaq faq : enabledFaqs()) {
            FaqCategoryListVO vo = map.computeIfAbsent(faq.getCategoryCode(), code -> {
                FaqCategoryListVO item = new FaqCategoryListVO();
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
    public PageResult<FaqListVO> search(FaqPageQueryDTO dto) {
        List<SysFaq> all = enabledFaqs();
        if (dto != null && TextUtil.hasText(dto.getCategoryCode())) {
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

        int size = dto.getPageSize();
        int current = dto.getPageNum();
        long total = matched.size();
        int from = Math.min((current - 1) * size, matched.size());
        int to = Math.min(from + size, matched.size());
        List<FaqListVO> records = matched.subList(from, to).stream().map(MiniappFaqServiceImpl::toVO).toList();
        return PageResult.of(total, current, size, (total + size - 1) / size, records);
    }

    @Override
    public FaqListVO getById(Long faqId) {
        SysFaq faq = miniappFaqMapper.selectOne(new LambdaQueryWrapper<SysFaq>()
                .eq(SysFaq::getId, faqId)
                .eq(SysFaq::getStatus, 1));
        if (faq == null) {
            throw new BusinessException("常见问题不存在或已停用");
        }
        // 查看次数自增：用 SQL 自增而不是 updateById 回写对象值，避免并发下互相覆盖
        miniappFaqMapper.update(null, new LambdaUpdateWrapper<SysFaq>()
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
        boolean useful = helpful == null || helpful != 0;
        boolean updated = miniappFaqMapper.update(null, new LambdaUpdateWrapper<SysFaq>()
                .eq(SysFaq::getId, faqId)
                .eq(SysFaq::getStatus, 1)
                .setSql(useful ? "helpful_count = helpful_count + 1" : "useless_count = useless_count + 1")) > 0;
        if (!updated) {
            throw new BusinessException("常见问题不存在或已停用");
        }
        return 1;
    }

    @Override
    public PageResult<FaqListVO> adminPage(FaqPageQueryDTO dto) {
        List<SysFaq> all = miniappFaqMapper.selectList(new LambdaQueryWrapper<SysFaq>()
                .orderByAsc(SysFaq::getSortOrder)
                .orderByAsc(SysFaq::getId));
        if (dto != null && TextUtil.hasText(dto.getCategoryCode())) {
            all = all.stream().filter(f -> dto.getCategoryCode().equals(f.getCategoryCode())).toList();
        }
        String keyword = dto == null ? null : dto.getKeyword();
        if (TextUtil.hasText(keyword)) {
            all = all.stream()
                    .filter(f -> (f.getQuestion() != null && f.getQuestion().contains(keyword.trim()))
                            || (f.getKeywords() != null && f.getKeywords().contains(keyword.trim())))
                    .toList();
        }
        int size = dto.getPageSize();
        int current = dto.getPageNum();
        long total = all.size();
        int from = Math.min((current - 1) * size, all.size());
        int to = Math.min(from + size, all.size());
        List<FaqListVO> records = all.subList(from, to).stream().map(MiniappFaqServiceImpl::toAdminVO).toList();
        return PageResult.of(total, current, size, (total + size - 1) / size, records);
    }

    @Override
    public FaqListVO adminGetById(Long faqId) {
        SysFaq faq = miniappFaqMapper.selectById(faqId);
        if (faq == null) {
            throw new BusinessException("常见问题不存在");
        }
        return toAdminVO(faq);
    }

    // 私有

    @Override
    public String adminUpsert(FaqUpsertDTO dto) {
        boolean isNew = dto.getId() == null;
        SysFaq entity = isNew ? new SysFaq() : miniappFaqMapper.selectById(dto.getId());
        if (entity == null) {
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
        entity.setQuestion(TextUtil.cut(dto.getQuestion(), 200));
        entity.setAnswer(TextUtil.cut(dto.getAnswer(), 1000));
        entity.setKeywords(TextUtil.cut(dto.getKeywords(), 500));
        entity.setHotFlag(dto.getHotFlag() == null ? 0 : dto.getHotFlag());
        entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        entity.setSortOrder(dto.getSortOrder() == null ? 999 : dto.getSortOrder());
        if (isNew) {
            miniappFaqMapper.insert(entity);
        } else {
            miniappFaqMapper.updateById(entity);
        }
        return String.valueOf(entity.getId());
    }

    @Override
    public void adminDelete(Long faqId) {
        miniappFaqMapper.purgeById(faqId);
    }

    /**
     * 编号 FAQ + 4 位序号，取全表最大号 +1（faq_no 是全局唯一键，不能按天 count）
     */
    private String nextFaqNo() {
        String max = miniappFaqMapper.maxFaqNo();
        int seq = 1;
        if (TextUtil.hasText(max) && max.length() > 3) {
            try {
                seq = Integer.parseInt(max.substring(3)) + 1;
            } catch (NumberFormatException ex) {
                seq = 1;
            }
        }
        return "FAQ" + String.format("%04d", seq);
    }

    private List<SysFaq> enabledFaqs() {
        List<SysFaq> list = miniappFaqMapper.selectList(new LambdaQueryWrapper<SysFaq>()
                .eq(SysFaq::getStatus, 1)
                .orderByAsc(SysFaq::getSortOrder));
        return list == null ? List.of() : list;
    }
}
