package com.his.patient.service.impl;
import com.his.patient.enums.OrderClassEnum;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.exception.BusinessException;
import com.his.patient.dto.InpatientOrderItemDTO;
import com.his.patient.dto.OrderSetQueryPageDTO;
import com.his.patient.dto.OrderSetUpsertDTO;
import com.his.patient.entity.BizInpatientOrderTemplate;
import com.his.patient.entity.BizInpatientOrderTemplateItem;
import com.his.patient.enums.OrderTypeEnum;
import com.his.patient.enums.TemplateScopeEnum;
import com.his.patient.mapper.BizInpatientOrderTemplateItemMapper;
import com.his.patient.mapper.BizInpatientOrderTemplateMapper;
import com.his.patient.service.InpatientOrderSetService;
import com.his.patient.support.InpatientOrderItemRules;
import com.his.patient.vo.InpatientOrderTemplateItemVO;
import com.his.patient.vo.OrderSetDetailVO;
import com.his.patient.vo.OrderSetListVO;
import com.his.patient.vo.OrderSetSelectListVO;
import com.his.security.UserUtils;
import com.his.security.entity.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * 医嘱组套模板服务实现（sql/142）。
 *
 * <p>五条必须守住的口径：
 * <ol>
 *   <li><b>可见集先切一刀，再谈过滤</b>：所有读写都先落在「全院 ∪ 本科室 ∪ 本人」里，
 *       前端传任何 scope / id 都越不出这个集合。范围条件整体包在一个 {@code and(...)} 里 ——
 *       拆成平铺的 {@code or} 会把 {@code del_flag=0} 吞掉，软删组套会诈尸。</li>
 *   <li><b>归属服务端定</b>：{@code doctorId} / {@code deptId} 一律按当前登录人覆盖，DTO 里没有这两个字段。</li>
 *   <li><b>scope 不可改</b>：范围与归属是创建时的事实（个人→本人 / 科室→当前科室 / 全院→不归属任何人），
 *       要换范围就新建一份。放开修改等于允许把全院公共组套改成自己私有的。</li>
 *   <li><b>明细先删后插 + 保存即校验</b>：与个人模板同一套做法、同一份
 *       {@link InpatientOrderItemRules} 硬规则，组套不能成为绕过校验的后门。</li>
 *   <li><b>不落开始时间与加急标志</b>：那是当次临床决定，不该由组套固化。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InpatientOrderSetServiceImpl implements InpatientOrderSetService {

    /**
     * 下拉候选上限：开立弹窗要一屏能扫完，翻找走管理页分页
     */
    private static final int SELECT_LIMIT = 50;

    private final BizInpatientOrderTemplateMapper templateMapper;
    private final BizInpatientOrderTemplateItemMapper itemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long upsert(OrderSetUpsertDTO dto) {
        // 保留（类别②）：整个 DTO 为 null 不是字段校验，Bean Validation 覆盖不到
        if (dto == null) {
            throw new BusinessException("组套内容不能为空");
        }
        Integer scope = dto.getScope();
        if (!Objects.equals(TemplateScopeEnum.PERSONAL.getCode(), scope) && !Objects.equals(TemplateScopeEnum.DEPT.getCode(), scope) && !Objects.equals(TemplateScopeEnum.HOSPITAL.getCode(), scope)) {
            throw new BusinessException("共享范围取值不合法（应为 1-个人 2-科室 3-全院）");
        }

        List<InpatientOrderItemDTO> items = new ArrayList<>();
        if (dto.getItems() != null) {
            for (InpatientOrderItemDTO item : dto.getItems()) {
                if (item != null && StringUtils.hasText(item.getItemName())) {
                    items.add(item);
                }
            }
        }
        if (items.isEmpty()) {
            throw new BusinessException("组套至少包含一条有项目名称的医嘱明细");
        }
        // 与个人模板同一份硬规则：放过一条缺途径的药品明细，组套就成了绕过校验的后门
        for (InpatientOrderItemDTO item : items) {
            InpatientOrderItemRules.validate(item);
        }

        Integer orderType = dto.getOrderType() != null ? dto.getOrderType() : OrderTypeEnum.TEMP.getCode();
        if (!Objects.equals(OrderTypeEnum.LONG.getCode(), orderType) && !Objects.equals(OrderTypeEnum.TEMP.getCode(), orderType)) {
            throw new BusinessException("医嘱类型取值不合法（应为 1-长期 2-临时）");
        }

        String templateName = dto.getTemplateName().trim();
        if (templateName.length() > 100) {
            throw new BusinessException("组套名称不能超过 100 字");
        }

        CurrentUser user = requireUser();
        Long empId = currentEmpId(user);
        Long deptId = user.getDeptId();

        BizInpatientOrderTemplate template = new BizInpatientOrderTemplate();
        template.setId(dto.getId());
        template.setTemplateName(templateName);
        template.setOrderType(orderType);
        template.setItemCount(items.size());
        template.setRemark(trimRemark(dto.getRemark()));

        if (dto.getId() == null) {
            template.setScope(scope);
            applyOwner(template, scope, empId, deptId);
            assertNameNotDuplicated(templateName, null, scope, empId, deptId);
            templateMapper.insert(template);
        } else {
            BizInpatientOrderTemplate owned = requireWritable(dto.getId());
            if (!Objects.equals(scope, owned.getScope())) {
                throw new BusinessException("组套的共享范围不能修改（要换范围请新建一份）");
            }
            // 归属不随修改漂移：组套属于谁、在哪个科室建的，是当时的事实
            applyOwner(template, scope, owned.getDoctorId(), owned.getDeptId());
            assertNameNotDuplicated(templateName, owned.getId(), scope, empId, deptId);
            templateMapper.updateById(template);
        }

        itemMapper.delete(new LambdaQueryWrapper<BizInpatientOrderTemplateItem>()
                .eq(BizInpatientOrderTemplateItem::getTemplateId, template.getId()));
        int sort = 1;
        for (InpatientOrderItemDTO item : items) {
            itemMapper.insert(toItemEntity(template.getId(), sort++, item));
        }

        log.info("保存医嘱组套 id={} 名称={} scope={} 明细={} 条",
                template.getId(), templateName, scope, items.size());
        return template.getId();
    }

    @Override
    public OrderSetDetailVO getDetailById(Long id) {
        BizInpatientOrderTemplate template = requireVisible(id);
        List<BizInpatientOrderTemplateItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<BizInpatientOrderTemplateItem>()
                        .eq(BizInpatientOrderTemplateItem::getTemplateId, template.getId())
                        .orderByAsc(BizInpatientOrderTemplateItem::getSortNo));

        OrderSetDetailVO vo = new OrderSetDetailVO();
        fill(vo, template);
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
    public IPage<OrderSetListVO> listPage(OrderSetQueryPageDTO query) {
        CurrentUser user = currentUserOrNull();
        String keyword = StringUtils.hasText(query.getKeyword()) ? query.getKeyword().trim() : null;
        LambdaQueryWrapper<BizInpatientOrderTemplate> wrapper = visibleWrapper(user)
                .eq(query.getScope() != null, BizInpatientOrderTemplate::getScope, query.getScope())
                .eq(query.getOrderType() != null, BizInpatientOrderTemplate::getOrderType, query.getOrderType())
                .and(keyword != null, w -> w.like(BizInpatientOrderTemplate::getTemplateName, keyword)
                        .or().like(BizInpatientOrderTemplate::getRemark, keyword))
                // 分页必须补唯一二级键：同一秒建的组套在翻页时会在两页重复出现
                .orderByDesc(BizInpatientOrderTemplate::getCreateTime)
                .orderByDesc(BizInpatientOrderTemplate::getId);

        Map<Long, String> deptNames = new HashMap<>();
        IPage<OrderSetListVO> page = templateMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper)
                .convert(entity -> {
                    OrderSetListVO vo = new OrderSetListVO();
                    fill(vo, entity);
                    if (entity.getDeptId() != null) {
                        vo.setDeptName(deptNames.computeIfAbsent(entity.getDeptId(), k -> {
                            try {
                                return templateMapper.selectDeptName(k);
                            } catch (Exception e) {
                                return null;
                            }
                        }));
                    }
                    return vo;
                });
        return page;
    }

    @Override
    public List<OrderSetSelectListVO> selectList() {
        CurrentUser user = currentUserOrNull();
        List<BizInpatientOrderTemplate> list = templateMapper.selectList(visibleWrapper(user)
                // 全院在前、科室其次、自己的最后：最常用的共享口径先被看到
                .orderByDesc(BizInpatientOrderTemplate::getScope)
                .orderByDesc(BizInpatientOrderTemplate::getCreateTime)
                .last("LIMIT " + SELECT_LIMIT));
        List<OrderSetSelectListVO> vos = new ArrayList<>(list.size());
        for (BizInpatientOrderTemplate entity : list) {
            OrderSetSelectListVO vo = new OrderSetSelectListVO();
            BeanUtils.copyProperties(entity, vo);
            vo.setScopeText(TemplateScopeEnum.getText(entity.getScope()));
            vo.setOrderTypeText(OrderTypeEnum.getText(entity.getOrderType()));
            if (entity.getDeptId() != null) {
                try {
                    vo.setDeptName(templateMapper.selectDeptName(entity.getDeptId()));
                } catch (Exception e) {
                    vo.setDeptName(null);
                }
            }
            vos.add(vo);
        }
        return vos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        BizInpatientOrderTemplate template = requireWritable(id);
        // 明细无软删列，物理删；主表逻辑删，历史医嘱上「来自组套」的痕迹不受影响
        itemMapper.delete(new LambdaQueryWrapper<BizInpatientOrderTemplateItem>()
                .eq(BizInpatientOrderTemplateItem::getTemplateId, template.getId()));
        templateMapper.deleteById(template.getId());
        log.info("删除医嘱组套 id={} 名称={} scope={}", template.getId(), template.getTemplateName(), template.getScope());
    }

    // 内部

    /**
     * 可见集：全院 ∪ 本科室 ∪ 本人。
     *
     * <p>整段包在一个 {@code and(...)} 里 —— 拆成平铺的 or 会与 {@code del_flag=0} 平级，
     * 软删组套会被捞回来（软删行的 scope 也满足条件）。
     */
    private LambdaQueryWrapper<BizInpatientOrderTemplate> visibleWrapper(CurrentUser user) {
        Long empId = user == null ? null : currentEmpId(user);
        Long deptId = user == null ? null : user.getDeptId();
        return new LambdaQueryWrapper<BizInpatientOrderTemplate>()
                .and(w -> {
                    w.eq(BizInpatientOrderTemplate::getScope, TemplateScopeEnum.HOSPITAL.getCode());
                    if (empId != null) {
                        w.or(x -> x.eq(BizInpatientOrderTemplate::getScope, TemplateScopeEnum.PERSONAL.getCode())
                                .eq(BizInpatientOrderTemplate::getDoctorId, empId));
                    }
                    if (deptId != null) {
                        w.or(x -> x.eq(BizInpatientOrderTemplate::getScope, TemplateScopeEnum.DEPT.getCode())
                                .eq(BizInpatientOrderTemplate::getDeptId, deptId));
                    }
                });
    }

    /**
     * 取组套并校验可见（不可见的与不存在的回同一句话，避免泄露「这个 id 存在但你看不到」）
     */
    private BizInpatientOrderTemplate requireVisible(Long id) {
        // 保留（类别②）：入参是普通 Long（GET @RequestParam / 修改路径复用同一私有校验），
        // 无对应 request DTO 字段可挂注解
        if (id == null) {
            throw new BusinessException("组套ID不能为空");
        }
        BizInpatientOrderTemplate template = templateMapper.selectOne(visibleWrapper(currentUserOrNull())
                .eq(BizInpatientOrderTemplate::getId, id));
        if (template == null) {
            throw new BusinessException("医嘱组套不存在");
        }
        return template;
    }

    /**
     * 取组套并校验可写：全院级凭权限码（能进这页即有码），科室级限本科室，个人级限本人
     */
    private BizInpatientOrderTemplate requireWritable(Long id) {
        BizInpatientOrderTemplate template = requireVisible(id);
        if (Boolean.FALSE.equals(editable(template, currentUserOrNull()))) {
            throw new BusinessException("只能修改或删除自己的个人组套、本科室的科室组套，或全院组套");
        }
        return template;
    }

    private boolean editable(BizInpatientOrderTemplate template, CurrentUser user) {
        Integer scope = template.getScope();
        if (Objects.equals(TemplateScopeEnum.HOSPITAL.getCode(), scope)) {
            return true;
        }
        if (user == null) {
            return false;
        }
        if (Objects.equals(TemplateScopeEnum.PERSONAL.getCode(), scope)) {
            return Objects.equals(currentEmpId(user), template.getDoctorId());
        }
        if (Objects.equals(TemplateScopeEnum.DEPT.getCode(), scope)) {
            return template.getDeptId() != null && Objects.equals(user.getDeptId(), template.getDeptId());
        }
        return false;
    }

    /**
     * 归属一律服务端定：个人→本人，科室→当前科室，全院→不归属任何个人/科室
     */
    private void applyOwner(BizInpatientOrderTemplate template, Integer scope, Long fallbackEmpId, Long fallbackDeptId) {
        if (Objects.equals(TemplateScopeEnum.PERSONAL.getCode(), scope)) {
            Long empId = fallbackEmpId != null ? fallbackEmpId : currentEmpId(requireUser());
            if (empId == null) {
                throw new BusinessException("未获取到当前登录医生，无法保存个人组套");
            }
            template.setDoctorId(empId);
            template.setDoctorName(currentName(requireUser()));
            // 创建时科室是快照，修改时保持原值（fallbackDeptId 传的就是 owned 的 deptId）
            template.setDeptId(fallbackDeptId);
        } else if (Objects.equals(TemplateScopeEnum.DEPT.getCode(), scope)) {
            Long deptId = fallbackDeptId != null ? fallbackDeptId : requireUser().getDeptId();
            if (deptId == null) {
                throw new BusinessException("当前账号没有归属科室，无法创建科室组套");
            }
            template.setDoctorId(null);
            template.setDoctorName(null);
            template.setDeptId(deptId);
        } else {
            template.setDoctorId(null);
            template.setDoctorName(null);
            template.setDeptId(null);
        }
    }

    /**
     * 同名校验按「归属范围」算：全院看全表，科室看本科室，个人看本人
     */
    private void assertNameNotDuplicated(String templateName, Long excludeId, Integer scope, Long empId, Long deptId) {
        LambdaQueryWrapper<BizInpatientOrderTemplate> wrapper = new LambdaQueryWrapper<BizInpatientOrderTemplate>()
                .eq(BizInpatientOrderTemplate::getScope, scope)
                .eq(BizInpatientOrderTemplate::getTemplateName, templateName)
                .ne(excludeId != null, BizInpatientOrderTemplate::getId, excludeId);
        if (Objects.equals(TemplateScopeEnum.PERSONAL.getCode(), scope)) {
            wrapper.eq(BizInpatientOrderTemplate::getDoctorId, empId);
        } else if (Objects.equals(TemplateScopeEnum.DEPT.getCode(), scope)) {
            wrapper.eq(BizInpatientOrderTemplate::getDeptId, deptId);
        }
        Long hits = templateMapper.selectCount(wrapper);
        if (hits != null && hits > 0) {
            throw new BusinessException("同范围内已有同名组套「" + templateName + "」，请改个名字或直接修改原组套");
        }
    }

    private void fill(OrderSetListVO vo, BizInpatientOrderTemplate entity) {
        BeanUtils.copyProperties(entity, vo);
        vo.setScopeText(TemplateScopeEnum.getText(entity.getScope()));
        vo.setOrderTypeText(OrderTypeEnum.getText(entity.getOrderType()));
        vo.setEditable(editable(entity, currentUserOrNull()));
    }

    private BizInpatientOrderTemplateItem toItemEntity(Long templateId, int sortNo, InpatientOrderItemDTO item) {
        BizInpatientOrderTemplateItem entity = new BizInpatientOrderTemplateItem();
        entity.setTemplateId(templateId);
        entity.setSortNo(sortNo);
        entity.setOrderClass(item.getOrderClass());
        entity.setItemCode(StringUtils.hasText(item.getItemCode()) ? item.getItemCode() : null);
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
     * 备注列宽 500，超长直接写库会报 Data too long，把「保存失败」升级成 500
     */
    private String trimRemark(String remark) {
        if (remark == null) {
            return null;
        }
        String trimmed = remark.trim();
        return trimmed.length() > 500 ? trimmed.substring(0, 500) : trimmed;
    }

    private CurrentUser requireUser() {
        CurrentUser user = currentUserOrNull();
        if (user == null) {
            throw new BusinessException("未获取到当前登录人");
        }
        return user;
    }

    private CurrentUser currentUserOrNull() {
        try {
            return UserUtils.getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 组套归属一律用**员工ID**（不是用户的ID），与医嘱行 doctor_id 同一口径
     */
    private Long currentEmpId(CurrentUser user) {
        return user.getEmployeeId() != null ? user.getEmployeeId() : user.getUserId();
    }

    private String currentName(CurrentUser user) {
        if (StringUtils.hasText(user.getEmployeeName())) {
            return user.getEmployeeName();
        }
        if (StringUtils.hasText(user.getRealName())) {
            return user.getRealName();
        }
        return user.getUsername();
    }
}
