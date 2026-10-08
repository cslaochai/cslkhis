package com.his.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.base.PageResult;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.system.dto.WorkbenchRoleConfigUpsertDTO;
import com.his.system.dto.WorkbenchRoleWidgetDTO;
import com.his.system.dto.WorkbenchWidgetQueryPageDTO;
import com.his.system.dto.WorkbenchWidgetUpsertDTO;
import com.his.system.entity.*;
import com.his.system.mapper.SysMenuMapper;
import com.his.system.mapper.SysRoleMapper;
import com.his.system.mapper.SysWorkbenchRoleMapper;
import com.his.system.mapper.SysWorkbenchWidgetMapper;
import com.his.system.provider.WorkbenchMetricProvider;
import com.his.system.service.WorkbenchService;
import com.his.system.utils.UserUtils;
import com.his.system.vo.WorkbenchConfigVO;
import com.his.system.vo.WorkbenchDataVO;
import com.his.system.vo.WorkbenchRoleConfigVO;
import com.his.system.vo.WorkbenchWidgetVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 门户工作台 Service。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkbenchServiceImpl extends ServiceImpl<SysWorkbenchWidgetMapper, SysWorkbenchWidget> implements WorkbenchService {

    /**
     * 卡片区域枚举，与工作台卡片注册表.area 列注释一致
     */
    private static final Set<String> AREAS = Set.of("todo", "notice", "entry", "kpi", "domain");

    private final SysWorkbenchWidgetMapper sysWorkbenchWidgetMapper;
    private final SysWorkbenchRoleMapper sysWorkbenchRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    /**
     * 各业务域的取数 bean。用 {@link ObjectProvider} 而不是直接注入 {@code List}：
     * 一是本模块编译期不认识任何业务域，二是首次取数才解析，避免启动期把各域
     * Service 连带拉起（注册表里 status=0 的占位卡本来就没有 bean）。
     */
    private final ObjectProvider<WorkbenchMetricProvider> metricProviders;

    private volatile Map<String, WorkbenchMetricProvider> providerMap;

    @Override
    public WorkbenchConfigVO getConfig() {
        WorkbenchConfigVO vo = new WorkbenchConfigVO();
        vo.setLandingScope(0);
        vo.setWidgets(Collections.emptyList());

        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            return vo;
        }
        String roleCode = user.getCurrentRole();
        vo.setRoleCode(roleCode);
        Integer landingScope = sysWorkbenchWidgetMapper.selectLandingScope(roleCode);
        vo.setLandingScope(landingScope == null ? 0 : landingScope);

        List<WorkbenchWidgetVO> widgets = sysWorkbenchWidgetMapper.selectRoleWidgets(roleCode);
        if (widgets.isEmpty()) {
            // 新角色漏配时只回落通用三张：它们不依赖角色专属数据，首页不会开天窗
            widgets = sysWorkbenchWidgetMapper.selectFallbackWidgets(roleCode);
        }
        vo.setWidgets(widgets);
        return vo;
    }

    @Override
    public List<WorkbenchDataVO> getData() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            return Collections.emptyList();
        }
        Map<String, WorkbenchMetricProvider> providers = providers();
        return getConfig().getWidgets().stream().map(widget -> {
            WorkbenchDataVO item = new WorkbenchDataVO();
            item.setCode(widget.getWidgetCode());
            WorkbenchMetricProvider provider = providers.get(widget.getWidgetCode());
            if (provider == null) {
                return item;
            }
            try {
                item.setData(provider.summary(user));
            } catch (Exception e) {
                // 一张卡的 SQL 问题不该让整屏 500：前端见 error 就渲染「—」
                log.warn("工作台卡片取数失败, widgetCode={}", widget.getWidgetCode(), e);
                item.setError("取数失败");
            }
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<WorkbenchWidgetVO> widgetListPage(WorkbenchWidgetQueryPageDTO queryDTO) {
        String keyword = queryDTO.getKeyword();
        LambdaQueryWrapper<SysWorkbenchWidget> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(TextUtil.hasText(keyword), w -> w
                        .like(SysWorkbenchWidget::getWidgetCode, keyword)
                        .or()
                        .like(SysWorkbenchWidget::getWidgetName, keyword))
                .eq(TextUtil.hasText(queryDTO.getArea()), SysWorkbenchWidget::getArea, queryDTO.getArea())
                .eq(queryDTO.getStatus() != null, SysWorkbenchWidget::getStatus, queryDTO.getStatus())
                .orderByAsc(SysWorkbenchWidget::getArea)
                .orderByAsc(SysWorkbenchWidget::getSortOrder);
        Page<SysWorkbenchWidget> page = sysWorkbenchWidgetMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
        List<WorkbenchWidgetVO> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), voList);
    }

    @Override
    public void widgetUpsert(WorkbenchWidgetUpsertDTO upsertDTO) {
        if (!AREAS.contains(upsertDTO.getArea())) {
            throw new BusinessException("归属区域只能是 todo/notice/entry/kpi/domain");
        }
        String permission = upsertDTO.getPermission();
        if (TextUtil.hasText(permission) && !permissionExists(permission)) {
            // 凭空造的码 = 任何角色都拿不到 = 卡片永久消失，且现场只会看到"卡不见了"
            throw new BusinessException("权限码在菜单表中不存在：" + permission);
        }
        SysWorkbenchWidget widget = new SysWorkbenchWidget();
        BeanUtils.copyProperties(upsertDTO, widget);
        widget.setRemark(upsertDTO.getRemark());

        Long id = widget.getId();
        if (id == null) {
            Long duplicated = sysWorkbenchWidgetMapper.selectCount(new LambdaQueryWrapper<SysWorkbenchWidget>()
                    .eq(SysWorkbenchWidget::getWidgetCode, widget.getWidgetCode()));
            if (duplicated != null && duplicated > 0) {
                throw new BusinessException("卡片编码已存在：" + widget.getWidgetCode());
            }
            sysWorkbenchWidgetMapper.insert(widget);
            return;
        }
        if (sysWorkbenchWidgetMapper.selectById(id) == null) {
            throw new BusinessException("卡片不存在");
        }
        sysWorkbenchWidgetMapper.updateById(widget);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void widgetDelete(Long widgetId) {
        SysWorkbenchWidget widget = sysWorkbenchWidgetMapper.selectById(widgetId);
        if (widget == null) {
            throw new BusinessException("卡片不存在");
        }
        sysWorkbenchWidgetMapper.purgeById(widgetId);
        // 注册表删了，角色配置里的引用必须一起清掉，否则留下指向已删卡片的孤儿行
        sysWorkbenchRoleMapper.purgeByWidget(widgetId);
    }

    @Override
    public WorkbenchRoleConfigVO roleConfig(Long roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        WorkbenchRoleConfigVO vo = new WorkbenchRoleConfigVO();
        vo.setRoleId(roleId);
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        Integer landingScope = sysWorkbenchWidgetMapper.selectLandingScope(role.getRoleCode());
        vo.setLandingScope(landingScope == null ? 0 : landingScope);
        vo.setWidgets(sysWorkbenchWidgetMapper.selectAllWidgetsForRole(roleId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void roleConfigUpsert(WorkbenchRoleConfigUpsertDTO upsertDTO) {
        SysRole role = sysRoleMapper.selectById(upsertDTO.getRoleId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        List<WorkbenchRoleWidgetDTO> items = upsertDTO.getWidgets() == null
                ? Collections.<WorkbenchRoleWidgetDTO>emptyList() : upsertDTO.getWidgets();
        Integer landingScope = upsertDTO.getLandingScope();
        if (items.isEmpty() && landingScope != 0) {
            // landing_scope 存在角色工作台配置行上（与卡片配置同屏维护，不再开一张表），
            // 一张卡都不挂时没有行可落，落点策略也就无从读取。
            throw new BusinessException("落点策略需随卡片配置一起保存：请先勾选至少一张卡片");
        }
        // 同一张卡重复提交以最后一次为准，避免撞 uk_workbench_role_widget 变成裸 500
        Map<Long, WorkbenchRoleWidgetDTO> unique = new LinkedHashMap<>();
        items.forEach(item -> unique.put(item.getWidgetId(), item));

        // 整体替换：先物理清空再插。用软删会撞 uk_workbench_role_widget（唯一键不含 del_flag），见 Mapper 注释
        sysWorkbenchRoleMapper.purgeByRole(upsertDTO.getRoleId());
        unique.forEach((widgetId, item) -> {
            SysWorkbenchRole row = new SysWorkbenchRole();
            row.setRoleId(upsertDTO.getRoleId());
            row.setWidgetId(widgetId);
            row.setSortOrder(item.getSortOrder());
            row.setVisible(item.getVisible());
            row.setLandingScope(landingScope);
            sysWorkbenchRoleMapper.insert(row);
        });
    }

    private Map<String, WorkbenchMetricProvider> providers() {
        Map<String, WorkbenchMetricProvider> current = providerMap;
        if (current == null) {
            current = new LinkedHashMap<>();
            for (WorkbenchMetricProvider provider : metricProviders) {
                WorkbenchMetricProvider previous = current.putIfAbsent(provider.widgetCode(), provider);
                if (previous != null) {
                    throw new IllegalStateException("工作台卡片取数 bean 编码重复：" + provider.widgetCode());
                }
            }
            providerMap = current;
        }
        return current;
    }

    private boolean permissionExists(String permission) {
        Long count = sysMenuMapper.selectCount(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getPermission, permission));
        return count != null && count > 0;
    }

    private WorkbenchWidgetVO toVO(SysWorkbenchWidget widget) {
        WorkbenchWidgetVO vo = new WorkbenchWidgetVO();
        BeanUtils.copyProperties(widget, vo);
        vo.setSpan(widget.getDefaultSpan());
        return vo;
    }
}
