package com.his.patient.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.patient.dto.InpatientOrderItemDTO;
import com.his.patient.dto.InpatientOrderTemplateQueryPageDTO;
import com.his.patient.dto.InpatientOrderTemplateUpsertDTO;
import com.his.patient.entity.BizInpatientOrderTemplate;
import com.his.patient.entity.BizInpatientOrderTemplateItem;
import com.his.patient.enums.OrderClassEnum;
import com.his.patient.enums.OrderTypeEnum;
import com.his.patient.mapper.BizInpatientOrderTemplateItemMapper;
import com.his.patient.mapper.BizInpatientOrderTemplateMapper;
import com.his.patient.service.InpatientOrderTemplateService;
import com.his.patient.support.InpatientOrderItemRules;
import com.his.patient.vo.InpatientOrderTemplateDetailVO;
import com.his.patient.vo.InpatientOrderTemplateItemVO;
import com.his.patient.vo.InpatientOrderTemplateListVO;
import com.his.patient.vo.InpatientOrderTemplateSelectListVO;
import com.his.system.entity.CurrentUser;
import com.his.system.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 住院医嘱模板服务实现。
 *
 * <p>四条必须守住的口径：
 * <ol>
 *   <li><b>归属只认当前登录人</b>：{@code doctorId} 一律服务端覆盖，且 {@code getById}/{@code upsert}/
 *       {@code deleteById} 都要校验模板属于本人 —— 前端传谁的 id 都读不到别人的模板。
 *       （门诊 {@code DoctorTemplateController} 的 {@code getRxById}/{@code deleteRxById} 没做这层，
 *       那是既有缺口，不要照着抄。）</li>
 *   <li><b>明细先删后插</b>：一次提交就是这份模板的全量，逐条 diff 换来的复杂度没有对应收益。</li>
 *   <li><b>保存即校验</b>：明细走 {@link InpatientOrderItemRules}（与开立医嘱同一份硬规则）。
 *       放过一条缺途径的药品明细，模板就成了绕过硬规则的后门。</li>
 *   <li><b>不存开始时间与加急</b>：那是当次临床决定；{@code orderType} 存的是默认值，套用后仍可改。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InpatientOrderTemplateServiceImpl implements InpatientOrderTemplateService {

    /**
     * 下拉候选上限：开立弹窗要一屏能扫完，真正的翻找走「模板管理」分页。
     */
    private static final int SELECT_LIMIT = 50;

    private final BizInpatientOrderTemplateMapper bizInpatientOrderTemplateMapper;
    private final BizInpatientOrderTemplateItemMapper bizInpatientOrderTemplateItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsert(InpatientOrderTemplateUpsertDTO dto) {
        // 保留（类别②）：整个 DTO 为 null 不是字段校验，Bean Validation 覆盖不到；
        // 明细非空已由 DTO 的 @NotEmpty(message="模板至少包含一条医嘱明细") 在入参层拦截
        if (dto == null) {
            throw new BusinessException("模板至少包含一条医嘱明细");
        }
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        List<InpatientOrderItemDTO> items = new ArrayList<>();
        for (InpatientOrderItemDTO item : dto.getItems()) {
            if (item != null && TextUtil.hasText(item.getItemName())) {
                items.add(item);
            }
        }
        if (items.isEmpty()) {
            throw new BusinessException("模板至少包含一条有项目名称的医嘱明细");
        }
        for (InpatientOrderItemDTO item : items) {
            InpatientOrderItemRules.validate(item);
        }

        Integer orderType = dto.getOrderType() != null ? dto.getOrderType() : OrderTypeEnum.TEMP.getCode();

        String templateName = dto.getTemplateName().trim();
        if (templateName.length() > 100) {
            throw new BusinessException("模板名称不能超过 100 字");
        }

        Long doctorId = operatorUser.getEmployeeId();
        if (doctorId == null) {
            throw new BusinessException("未获取到当前登录医生，无法保存模板");
        }
        assertNameNotDuplicated(templateName, dto.getId(), doctorId);

        BizInpatientOrderTemplate template = new BizInpatientOrderTemplate();
        template.setId(dto.getId());
        template.setTemplateName(templateName);
        template.setOrderType(orderType);
        template.setItemCount(items.size());
        template.setRemark(trimRemark(dto.getRemark()));

        if (dto.getId() == null) {
            template.setDoctorId(doctorId);
            template.setDoctorName(operatorUser.getRealName());
            template.setDeptId(currentDeptId());
            bizInpatientOrderTemplateMapper.insert(template);
        } else {
            BizInpatientOrderTemplate owned = requireOwned(dto.getId(), doctorId);
            // 归属与创建时科室不随修改漂移：模板属于谁、在哪个科室建的，是当时的事实
            template.setId(owned.getId());
            bizInpatientOrderTemplateMapper.updateById(template);
        }

        bizInpatientOrderTemplateItemMapper.delete(new LambdaQueryWrapper<BizInpatientOrderTemplateItem>()
                .eq(BizInpatientOrderTemplateItem::getTemplateId, template.getId()));
        int sort = 1;
        for (InpatientOrderItemDTO item : items) {
            bizInpatientOrderTemplateItemMapper.insert(toItemEntity(template.getId(), sort++, item));
        }

        log.info("保存医嘱模板 id={} 名称={} 医生={} 明细={} 条",
                template.getId(), templateName, template.getDoctorName(), items.size());
        return template.getId();
    }

    @Override
    public InpatientOrderTemplateDetailVO getById(Long id) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizInpatientOrderTemplate template = requireOwned(id, operatorUser.getEmployeeId());
        List<BizInpatientOrderTemplateItem> items = bizInpatientOrderTemplateItemMapper.selectList(
                new LambdaQueryWrapper<BizInpatientOrderTemplateItem>()
                        .eq(BizInpatientOrderTemplateItem::getTemplateId, template.getId())
                        .orderByAsc(BizInpatientOrderTemplateItem::getSortNo));

        InpatientOrderTemplateDetailVO vo = new InpatientOrderTemplateDetailVO();
        BeanUtils.copyProperties(template, vo);
        vo.setOrderTypeText(OrderTypeEnum.getText(template.getOrderType()));
        List<InpatientOrderTemplateItemVO> itemVOs = new ArrayList<>(items.size());
        for (BizInpatientOrderTemplateItem item : items) {
            InpatientOrderTemplateItemVO itemVO = new InpatientOrderTemplateItemVO();
            BeanUtils.copyProperties(item, itemVO);
            itemVO.setOrderClassText(OrderClassEnum.getText(item.getOrderClass()));
            itemVOs.add(itemVO);
        }
        vo.setItems(itemVOs);
        return vo;
    }

    @Override
    public IPage<InpatientOrderTemplateListVO> listPage(InpatientOrderTemplateQueryPageDTO query) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        Long doctorId = operatorUser.getEmployeeId();
        String keyword = TextUtil.hasText(query.getKeyword()) ? query.getKeyword().trim() : null;
        LambdaQueryWrapper<BizInpatientOrderTemplate> wrapper = new LambdaQueryWrapper<BizInpatientOrderTemplate>()
                .eq(BizInpatientOrderTemplate::getDoctorId, doctorId)
                .eq(query.getOrderType() != null, BizInpatientOrderTemplate::getOrderType, query.getOrderType())
                .and(keyword != null, w -> w.like(BizInpatientOrderTemplate::getTemplateName, keyword)
                        .or().like(BizInpatientOrderTemplate::getRemark, keyword))
                .orderByDesc(BizInpatientOrderTemplate::getCreateTime);

        return bizInpatientOrderTemplateMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper)
                .convert(entity -> {
                    InpatientOrderTemplateListVO vo = new InpatientOrderTemplateListVO();
                    BeanUtils.copyProperties(entity, vo);
                    vo.setOrderTypeText(OrderTypeEnum.getText(entity.getOrderType()));
                    return vo;
                });
    }

    @Override
    public List<InpatientOrderTemplateSelectListVO> selectList() {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        Long doctorId = operatorUser.getEmployeeId();
        List<BizInpatientOrderTemplate> list = bizInpatientOrderTemplateMapper.selectList(
                new LambdaQueryWrapper<BizInpatientOrderTemplate>()
                        .eq(BizInpatientOrderTemplate::getDoctorId, doctorId)
                        .orderByDesc(BizInpatientOrderTemplate::getCreateTime)
                        .last("LIMIT " + SELECT_LIMIT));
        List<InpatientOrderTemplateSelectListVO> vos = new ArrayList<>(list.size());
        for (BizInpatientOrderTemplate entity : list) {
            InpatientOrderTemplateSelectListVO vo = new InpatientOrderTemplateSelectListVO();
            BeanUtils.copyProperties(entity, vo);
            vo.setOrderTypeText(OrderTypeEnum.getText(entity.getOrderType()));
            vos.add(vo);
        }
        return vos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        CurrentUser operatorUser = UserUtils.getCurrentUser();
        if (operatorUser == null) {
            throw new BusinessException("当前用户信息不存在");
        }
        BizInpatientOrderTemplate template = requireOwned(id, operatorUser.getEmployeeId());
        // 明细无软删列，物理删；主表走 @TableLogic 逻辑删，历史医嘱上"来自模板"的痕迹不受影响
        bizInpatientOrderTemplateItemMapper.delete(new LambdaQueryWrapper<BizInpatientOrderTemplateItem>()
                .eq(BizInpatientOrderTemplateItem::getTemplateId, template.getId()));
        bizInpatientOrderTemplateMapper.deleteById(template.getId());
        log.info("删除医嘱模板 id={} 名称={} 医生={}", template.getId(), template.getTemplateName(), template.getDoctorName());
    }

    // 内部

    /**
     * 取模板并校验归属。故意不区分"不存在"与"不是你的"：
     * 后者若回"模板不存在"以外的话，等于把别人的模板 id 是否有效泄露出去。
     */
    private BizInpatientOrderTemplate requireOwned(Long id, Long doctorId) {
        // 保留（类别②）：入参是普通 Long（GET @RequestParam / 修改路径复用同一私有校验），
        // 无对应 request DTO 字段可挂注解
        if (id == null) {
            throw new BusinessException("模板ID不能为空");
        }
        if (doctorId == null) {
            throw new BusinessException("未获取到当前登录医生");
        }
        BizInpatientOrderTemplate template = bizInpatientOrderTemplateMapper.selectById(id);
        if (template == null || !Objects.equals(doctorId, template.getDoctorId())) {
            throw new BusinessException("医嘱模板不存在");
        }
        return template;
    }

    private void assertNameNotDuplicated(String templateName, Long excludeId, Long doctorId) {
        Long hits = bizInpatientOrderTemplateMapper.selectCount(new LambdaQueryWrapper<BizInpatientOrderTemplate>()
                .eq(BizInpatientOrderTemplate::getDoctorId, doctorId)
                .eq(BizInpatientOrderTemplate::getTemplateName, templateName)
                .ne(excludeId != null, BizInpatientOrderTemplate::getId, excludeId));
        if (hits != null && hits > 0) {
            throw new BusinessException("你已有同名模板「" + templateName + "」，请改个名字或直接修改原模板");
        }
    }

    private BizInpatientOrderTemplateItem toItemEntity(Long templateId, int sortNo, InpatientOrderItemDTO item) {
        BizInpatientOrderTemplateItem entity = new BizInpatientOrderTemplateItem();
        entity.setTemplateId(templateId);
        entity.setSortNo(sortNo);
        entity.setOrderClass(item.getOrderClass());
        entity.setItemCode(TextUtil.hasText(item.getItemCode()) ? item.getItemCode() : null);
        entity.setItemName(item.getItemName().trim());
        entity.setSpec(item.getSpec());
        entity.setUnit(item.getUnit());
        entity.setDosage(item.getDosage());
        entity.setDosageUnit(item.getDosageUnit());
        entity.setRoute(item.getRoute());
        entity.setFrequency(item.getFrequency());
        entity.setQuantity(item.getQuantity() != null ? item.getQuantity() : BigDecimal.ONE);
        entity.setPrice(item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
        return entity;
    }

    /**
     * 备注列宽 500，超长直接写库会报 Data too long，把"保存失败"升级成 500
     */
    private String trimRemark(String remark) {
        if (remark == null) {
            return null;
        }
        String trimmed = remark.trim();
        return trimmed.length() > 500 ? trimmed.substring(0, 500) : trimmed;
    }

    private Long currentDeptId() {
        try {
            CurrentUser user = UserUtils.getCurrentUser();
            return user == null ? null : user.getDeptId();
        } catch (Exception e) {
            return null;
        }
    }
}
