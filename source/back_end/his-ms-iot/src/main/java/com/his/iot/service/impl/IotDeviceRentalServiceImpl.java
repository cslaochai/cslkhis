package com.his.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.his.common.entity.CurrentUser;
import com.his.common.enums.AdmitStatusEnum;
import com.his.common.enums.DeviceRentalItemStatusEnum;
import com.his.common.enums.DeviceRentalOrderStatusEnum;
import com.his.common.enums.DeviceRentalUrgencyEnum;
import com.his.common.exception.BusinessException;
import com.his.common.util.TextUtil;
import com.his.common.utils.UserUtils;
import com.his.iot.dto.*;
import com.his.iot.entity.IotDevice;
import com.his.iot.entity.IotDeviceRentalItem;
import com.his.iot.entity.IotDeviceRentalOrder;
import com.his.iot.mapper.IotDeviceMapper;
import com.his.iot.mapper.IotDeviceRentalItemMapper;
import com.his.iot.mapper.IotDeviceRentalOrderMapper;
import com.his.iot.service.IotDeviceBindService;
import com.his.iot.service.IotDeviceRentalService;
import com.his.iot.vo.DeviceRentalCatalogVO;
import com.his.iot.vo.DeviceRentalItemVO;
import com.his.iot.vo.DeviceRentalOrderVO;
import com.his.iot.vo.IotDeviceBindVO;
import com.his.spi.service.EquipmentRentalCatalogService;
import com.his.spi.service.RedisSequenceService;
import com.his.spi.vo.AdmissionActiveBriefVO;
import com.his.spi.vo.EquipmentRentalCatalogVO;
import com.his.spi.service.AdmissionQueryFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备共享调配实现。
 * <p>
 * 设备科共享池 → 临床申请 → 审批 → 发放（IoT 设备同时绑定患者）→ 归还申请 → 设备科验收。
 * 申请单是业务事实；{@code biz_iot_device_bind} 只是 IoT 设备在发放后产生的底层绑定关系。
 */
@Service
@RequiredArgsConstructor
public class IotDeviceRentalServiceImpl extends ServiceImpl<IotDeviceRentalOrderMapper, IotDeviceRentalOrder>
        implements IotDeviceRentalService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final IotDeviceRentalItemMapper itemMapper;
    private final IotDeviceMapper iotDeviceMapper;
    private final IotDeviceBindService iotDeviceBindService;
    private final EquipmentRentalCatalogService equipmentRentalCatalogService;
    private final AdmissionQueryFacadeService admissionQueryFacadeService;
    private final RedisSequenceService redisSequenceService;

    @Override
    public List<DeviceRentalCatalogVO> listCatalog(String keyword) {
        List<EquipmentRentalCatalogVO> catalog = equipmentRentalCatalogService.listRentable(keyword);
        if (catalog.isEmpty()) {
            return List.of();
        }
        Set<String> activeBindDevices = activeBindDeviceNos();
        Set<String> activeRentalDevices = activeRentalDeviceNos();
        // 一次 IN 把全部目录项的关联设备查回来，别在 toCatalogVo 里逐项查（那是 2N 次往返）
        Map<Long, List<IotDevice>> linkedByEquipment = devicesOf(
                catalog.stream().map(EquipmentRentalCatalogVO::getId).toList());
        return catalog.stream()
                .map(e -> toCatalogVo(e, linkedByEquipment.getOrDefault(e.getId(), List.of()),
                        activeBindDevices, activeRentalDevices))
                .toList();
    }

    @Override
    public IPage<DeviceRentalOrderVO> listPage(DeviceRentalPageQueryDTO query) {
        CurrentUser user = requireUser();
        boolean all = canSeeAll(user);
        String kw = TextUtil.trim(query.getKeyword());
        LambdaQueryWrapper<IotDeviceRentalOrder> w = new LambdaQueryWrapper<IotDeviceRentalOrder>()
                .eq(query.getStatus() != null, IotDeviceRentalOrder::getStatus, query.getStatus())
                .and(TextUtil.hasText(kw), x -> x
                        .like(IotDeviceRentalOrder::getOrderNo, kw)
                        .or().like(IotDeviceRentalOrder::getPatientName, kw)
                        .or().like(IotDeviceRentalOrder::getBedNo, kw)
                        .or().like(IotDeviceRentalOrder::getApplicantName, kw)
                        .or().like(IotDeviceRentalOrder::getApplicantDeptName, kw))
                .eq(!all, IotDeviceRentalOrder::getApplicantDeptId, user.getDeptId())
                .orderByDesc(IotDeviceRentalOrder::getCreateTime)
                .orderByDesc(IotDeviceRentalOrder::getId);
        Page<IotDeviceRentalOrder> page = baseMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), w);
        // 明细整页一次查回来：逐行 itemsOf 会让 10 行的分页打 11 次 SQL
        Map<Long, List<IotDeviceRentalItem>> itemsByOrder =
                itemsOf(page.getRecords().stream().map(IotDeviceRentalOrder::getId).toList());
        Page<DeviceRentalOrderVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream()
                .map(o -> toOrderVo(o, itemsByOrder.getOrDefault(o.getId(), List.of())))
                .toList());
        return result;
    }

    @Override
    public DeviceRentalOrderVO getDetail(Long id) {
        CurrentUser user = requireUser();
        IotDeviceRentalOrder order = requireOrder(id);
        if (!canSeeAll(user) && !Objects.equals(order.getApplicantDeptId(), user.getDeptId())) {
            throw new BusinessException(403, "无权查看其他科室的申请单");
        }
        return toOrderVo(order, itemsOf(order.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(DeviceRentalCreateDTO dto) {
        CurrentUser user = requireUser();
        if (user.getDeptId() == null || !TextUtil.hasText(user.getDeptName())) {
            throw new BusinessException(400, "当前岗位没有科室，不能发起设备借用");
        }

        IotDeviceRentalOrder order = new IotDeviceRentalOrder();
        order.setOrderNo(nextOrderNo());
        order.setApplicantDeptId(user.getDeptId());
        order.setApplicantDeptName(user.getDeptName());
        order.setApplicantId(user.getEmployeeId());
        order.setApplicantName(user.getRealName());
        order.setPurpose(TextUtil.trim(dto.getPurpose()));
        order.setUrgency(dto.getUrgency());
        order.setExpectReturnTime(dto.getExpectReturnTime());
        order.setDeliveryPlace(TextUtil.trim(dto.getDeliveryPlace()));
        order.setRemark(TextUtil.trim(dto.getRemark()));
        order.setStatus(DeviceRentalOrderStatusEnum.PENDING_APPROVE.getCode());

        if (dto.getAdmissionId() != null) {
            AdmissionActiveBriefVO admission = admissionQueryFacadeService.findAdmissionBrief(dto.getAdmissionId());
            if (admission == null || !Objects.equals(admission.getAdmitStatus(), AdmitStatusEnum.IN_HOSPITAL.getCode())) {
                throw new BusinessException(400, "患者不在院，不能发起设备借用");
            }
            order.setPatientId(admission.getPatientId());
            order.setAdmissionId(admission.getAdmissionId());
            order.setWardId(admission.getWardId());
            order.setPatientName(TextUtil.trim(dto.getPatientName()));
            order.setBedNo(TextUtil.trim(dto.getBedNo()));
            order.setWardName(TextUtil.trim(dto.getWardName()));
        }

        // 同一设备可借数量不足时直接拒，避免审批后才发现发不出。
        // 在用绑定/租赁集合各取一次；原来在循环里取，3 个申请项就要扫 6 次全表
        Map<Long, Integer> requested = new HashMap<>();
        Map<Long, EquipmentRentalCatalogVO> catalogs = new HashMap<>();
        Map<Long, List<IotDevice>> linkedByEquipment = new HashMap<>();
        Set<String> activeBindDevices = activeBindDeviceNos();
        Set<String> activeRentalDevices = activeRentalDeviceNos();
        for (DeviceRentalItemDTO item : dto.getItems()) {
            EquipmentRentalCatalogVO catalog = catalogs.computeIfAbsent(item.getEquipmentId(), this::requireCatalog);
            List<IotDevice> linked = linkedByEquipment.computeIfAbsent(item.getEquipmentId(), this::linkedDevices);
            int available = availableCount(linked, catalog.getEquipmentCode(), activeBindDevices, activeRentalDevices);
            int already = requested.getOrDefault(item.getEquipmentId(), 0);
            if (already + 1 > available) {
                throw new BusinessException(400, "「" + catalog.getEquipmentName() + "」当前可借数量不足");
            }
            requested.put(item.getEquipmentId(), already + 1);
        }

        baseMapper.insert(order);
        for (DeviceRentalItemDTO dtoItem : dto.getItems()) {
            EquipmentRentalCatalogVO catalog = catalogs.get(dtoItem.getEquipmentId());
            IotDeviceRentalItem item = new IotDeviceRentalItem();
            item.setOrderId(order.getId());
            item.setEquipmentId(catalog.getId());
            item.setEquipmentCode(catalog.getEquipmentCode());
            item.setEquipmentName(catalog.getEquipmentName());
            item.setRequestedBrand(TextUtil.trim(dtoItem.getRequestedBrand()));
            item.setRequestedModel(TextUtil.trim(dtoItem.getRequestedModel()));
            item.setRemark(TextUtil.trim(dtoItem.getRemark()));
            item.setStatus(DeviceRentalItemStatusEnum.PENDING_ISSUE.getCode());
            itemMapper.insert(item);
        }
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(DeviceRentalApproveDTO dto) {
        CurrentUser user = requireUser();
        IotDeviceRentalOrder order = requireOrder(dto.getId());
        if (!Objects.equals(order.getStatus(), DeviceRentalOrderStatusEnum.PENDING_APPROVE.getCode())) {
            throw new BusinessException(400, "申请单不在待审批状态");
        }
        order.setStatus(dto.getApproved()
                ? DeviceRentalOrderStatusEnum.PENDING_ISSUE.getCode()
                : DeviceRentalOrderStatusEnum.REJECTED.getCode());
        order.setApproveBy(user.getRealName());
        order.setApproveById(user.getEmployeeId());
        order.setApproveTime(LocalDateTime.now());
        order.setApproveRemark(TextUtil.trim(dto.getRemark()));
        baseMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void issue(DeviceRentalActionDTO dto) {
        CurrentUser user = requireUser();
        IotDeviceRentalOrder order = requireOrder(dto.getId());
        if (!Objects.equals(order.getStatus(), DeviceRentalOrderStatusEnum.PENDING_ISSUE.getCode())) {
            throw new BusinessException(400, "申请单不在待发放状态");
        }
        if (order.getAdmissionId() != null) {
            AdmissionActiveBriefVO admission = admissionQueryFacadeService.findAdmissionBrief(order.getAdmissionId());
            if (admission == null || !Objects.equals(admission.getAdmitStatus(), AdmitStatusEnum.IN_HOSPITAL.getCode())) {
                throw new BusinessException(400, "患者已出院，不能发放设备；请驳回或取消申请");
            }
        }

        List<IotDeviceRentalItem> items = itemsOf(order.getId());
        Set<String> activeBindDevices = activeBindDeviceNos();
        Set<String> activeRentalDevices = activeRentalDeviceNos();
        for (IotDeviceRentalItem item : items) {
            if (!Objects.equals(item.getStatus(), DeviceRentalItemStatusEnum.PENDING_ISSUE.getCode())) {
                continue;
            }
            EquipmentRentalCatalogVO catalog = requireCatalog(item.getEquipmentId());
            List<IotDevice> linked = linkedDevices(catalog.getId());
            if (linked.isEmpty()) {
                if (activeRentalDevices.contains(catalog.getEquipmentCode())) {
                    throw new BusinessException(400, "「" + catalog.getEquipmentName() + "」已全部借出，无法发放");
                }
                item.setDeviceNo(catalog.getEquipmentCode());
                item.setDeviceType(null);
            } else {
                IotDevice device = linked.stream()
                        .filter(d -> !activeBindDevices.contains(d.getDeviceNo())
                                && !activeRentalDevices.contains(d.getDeviceNo()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessException(400,
                                "「" + catalog.getEquipmentName() + "」当前没有可发放设备"));
                item.setDeviceNo(device.getDeviceNo());
                item.setDeviceType(device.getDeviceType());
                if (order.getAdmissionId() != null) {
                    IotDeviceBindDTO bind = new IotDeviceBindDTO();
                    bind.setDeviceNo(device.getDeviceNo());
                    bind.setDeviceType(device.getDeviceType());
                    bind.setAdmissionId(order.getAdmissionId());
                    bind.setBindSource(com.his.common.enums.IotBindSourceEnum.DISPATCH.getCode());
                    bind.setRemark("设备共享调配 " + order.getOrderNo());
                    item.setBindId(iotDeviceBindService.bindDevice(bind));
                }
                activeRentalDevices.add(device.getDeviceNo());
                activeBindDevices.add(device.getDeviceNo());
            }
            item.setStatus(DeviceRentalItemStatusEnum.IN_USE.getCode());
            item.setIssueTime(LocalDateTime.now());
            itemMapper.updateById(item);
        }

        order.setStatus(DeviceRentalOrderStatusEnum.IN_USE.getCode());
        order.setIssueBy(user.getRealName());
        order.setIssueById(user.getEmployeeId());
        order.setIssueTime(LocalDateTime.now());
        baseMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markReturn(DeviceRentalReturnDTO dto) {
        CurrentUser user = requireUser();
        IotDeviceRentalOrder order = requireOrder(dto.getId());
        if (!Objects.equals(order.getStatus(), DeviceRentalOrderStatusEnum.IN_USE.getCode())) {
            throw new BusinessException(400, "申请单不在使用中状态");
        }
        order.setStatus(DeviceRentalOrderStatusEnum.PENDING_ACCEPT.getCode());
        order.setReturnBy(user.getRealName());
        order.setReturnById(user.getEmployeeId());
        order.setReturnTime(LocalDateTime.now());
        order.setReturnReason(TextUtil.trim(dto.getReason()));
        baseMapper.updateById(order);
        for (IotDeviceRentalItem item : itemsOf(order.getId())) {
            if (Objects.equals(item.getStatus(), DeviceRentalItemStatusEnum.IN_USE.getCode())) {
                item.setStatus(DeviceRentalItemStatusEnum.PENDING_ACCEPT.getCode());
                item.setReturnTime(LocalDateTime.now());
                itemMapper.updateById(item);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void accept(DeviceRentalActionDTO dto) {
        CurrentUser user = requireUser();
        IotDeviceRentalOrder order = requireOrder(dto.getId());
        if (!Objects.equals(order.getStatus(), DeviceRentalOrderStatusEnum.PENDING_ACCEPT.getCode())) {
            throw new BusinessException(400, "申请单不在待验收状态");
        }
        for (IotDeviceRentalItem item : itemsOf(order.getId())) {
            if (!Objects.equals(item.getStatus(), DeviceRentalItemStatusEnum.PENDING_ACCEPT.getCode())) {
                continue;
            }
            if (item.getBindId() != null && TextUtil.hasText(item.getDeviceNo())) {
                IotDeviceUnbindDTO unbind = new IotDeviceUnbindDTO();
                unbind.setDeviceNo(item.getDeviceNo());
                unbind.setReason("设备共享调配验收：" + order.getOrderNo());
                iotDeviceBindService.unbindDevice(unbind);
            }
            item.setStatus(DeviceRentalItemStatusEnum.RETURNED.getCode());
            item.setAcceptTime(LocalDateTime.now());
            itemMapper.updateById(item);
        }
        order.setStatus(DeviceRentalOrderStatusEnum.COMPLETED.getCode());
        order.setAcceptBy(user.getRealName());
        order.setAcceptById(user.getEmployeeId());
        order.setAcceptTime(LocalDateTime.now());
        order.setAcceptRemark(TextUtil.trim(dto.getRemark()));
        baseMapper.updateById(order);
    }

    // ---------------- 内部 ----------------

    private CurrentUser requireUser() {
        CurrentUser user = UserUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException(401, "未获取到登录用户");
        }
        return user;
    }

    private boolean canSeeAll(CurrentUser user) {
        if ("admin".equals(user.getCurrentRole())) {
            return true;
        }
        List<String> permissions = user.getPermissions();
        return permissions != null && (permissions.contains("asset:deviceRental:issue")
                || permissions.contains("asset:deviceRental:accept")
                || permissions.contains("asset:deviceRental:manage"));
    }

    private IotDeviceRentalOrder requireOrder(Long id) {
        IotDeviceRentalOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(400, "申请单不存在：" + id);
        }
        return order;
    }

    private EquipmentRentalCatalogVO requireCatalog(Long equipmentId) {
        EquipmentRentalCatalogVO catalog = equipmentRentalCatalogService.getRentableById(equipmentId);
        if (catalog == null) {
            throw new BusinessException(400, "设备不在共享池或已停用：" + equipmentId);
        }
        return catalog;
    }

    private List<IotDeviceRentalItem> itemsOf(Long orderId) {
        return itemMapper.selectList(new LambdaQueryWrapper<IotDeviceRentalItem>()
                .eq(IotDeviceRentalItem::getOrderId, orderId)
                .orderByAsc(IotDeviceRentalItem::getId));
    }

    /**
     * 整页明细一次查回。groupingBy 保留查询顺序，明细仍按 id 升序。
     */
    private Map<Long, List<IotDeviceRentalItem>> itemsOf(List<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return Map.of();
        }
        return itemMapper.selectList(new LambdaQueryWrapper<IotDeviceRentalItem>()
                        .in(IotDeviceRentalItem::getOrderId, orderIds)
                        .orderByAsc(IotDeviceRentalItem::getId))
                .stream().collect(Collectors.groupingBy(IotDeviceRentalItem::getOrderId));
    }

    private List<IotDevice> linkedDevices(Long equipmentId) {
        return iotDeviceMapper.selectList(new LambdaQueryWrapper<IotDevice>()
                .eq(IotDevice::getEquipmentId, equipmentId)
                .eq(IotDevice::getEnabled, 1)
                .eq(IotDevice::getDelFlag, 0)
                .orderByAsc(IotDevice::getDeviceNo));
    }

    private Set<String> activeBindDeviceNos() {
        IotBindQueryDTO query = new IotBindQueryDTO();
        return iotDeviceBindService.listActiveBinds(query).stream()
                .map(IotDeviceBindVO::getDeviceNo)
                .filter(TextUtil::hasText)
                .collect(Collectors.toSet());
    }

    private Set<String> activeRentalDeviceNos() {
        return itemMapper.selectList(new LambdaQueryWrapper<IotDeviceRentalItem>()
                        .eq(IotDeviceRentalItem::getStatus, DeviceRentalItemStatusEnum.IN_USE.getCode())
                        .isNotNull(IotDeviceRentalItem::getDeviceNo))
                .stream().map(IotDeviceRentalItem::getDeviceNo).filter(TextUtil::hasText)
                .collect(Collectors.toSet());
    }

    private int availableCount(List<IotDevice> linked, String equipmentCode, Set<String> activeBinds,
                               Set<String> activeRentals) {
        if (linked.isEmpty()) {
            return activeRentals.contains(equipmentCode) ? 0 : 1;
        }
        int free = 0;
        for (IotDevice d : linked) {
            if (!activeBinds.contains(d.getDeviceNo()) && !activeRentals.contains(d.getDeviceNo())) {
                free++;
            }
        }
        return free;
    }

    private DeviceRentalCatalogVO toCatalogVo(EquipmentRentalCatalogVO e, List<IotDevice> linked,
                                              Set<String> activeBinds, Set<String> activeRentals) {
        DeviceRentalCatalogVO vo = new DeviceRentalCatalogVO();
        vo.setId(e.getId());
        vo.setEquipmentCode(e.getEquipmentCode());
        vo.setEquipmentName(e.getEquipmentName());
        vo.setCategory(e.getCategory());
        vo.setDeptName(e.getDeptName());
        vo.setBrand(e.getBrand());
        vo.setModel(e.getModel());
        vo.setStatus(e.getStatus());
        vo.setLastMaintainDate(e.getLastMaintainDate());
        vo.setTotalCount(linked.isEmpty() ? 1 : linked.size());
        vo.setAvailableCount(availableCount(linked, e.getEquipmentCode(), activeBinds, activeRentals));
        return vo;
    }

    /**
     * 一批设备档案一次查回（按 equipment_id 分组），目录页不再逐项查库
     */
    private Map<Long, List<IotDevice>> devicesOf(Collection<Long> equipmentIds) {
        if (equipmentIds.isEmpty()) {
            return Map.of();
        }
        return iotDeviceMapper.selectList(new LambdaQueryWrapper<IotDevice>()
                        .in(IotDevice::getEquipmentId, equipmentIds)
                        .eq(IotDevice::getEnabled, 1)
                        .eq(IotDevice::getDelFlag, 0)
                        .orderByAsc(IotDevice::getDeviceNo))
                .stream().collect(Collectors.groupingBy(IotDevice::getEquipmentId));
    }

    private String nextOrderNo() {
        long seq = redisSequenceService.next("DEVICE_RENTAL");
        return "DR" + LocalDate.now().format(DAY) + String.format("%05d", seq % 100000);
    }

    private DeviceRentalOrderVO toOrderVo(IotDeviceRentalOrder order, List<IotDeviceRentalItem> items) {
        DeviceRentalOrderVO vo = new DeviceRentalOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setApplicantDeptId(order.getApplicantDeptId());
        vo.setApplicantDeptName(order.getApplicantDeptName());
        vo.setApplicantId(order.getApplicantId());
        vo.setApplicantName(order.getApplicantName());
        vo.setPatientId(order.getPatientId());
        vo.setAdmissionId(order.getAdmissionId());
        vo.setPatientName(order.getPatientName());
        vo.setBedNo(order.getBedNo());
        vo.setWardId(order.getWardId());
        vo.setWardName(order.getWardName());
        vo.setPurpose(order.getPurpose());
        vo.setUrgency(order.getUrgency());
        vo.setUrgencyText(DeviceRentalUrgencyEnum.getLabel(order.getUrgency()));
        vo.setExpectReturnTime(order.getExpectReturnTime());
        vo.setDeliveryPlace(order.getDeliveryPlace());
        vo.setStatus(order.getStatus());
        vo.setStatusText(DeviceRentalOrderStatusEnum.getLabel(order.getStatus()));
        vo.setApproveBy(order.getApproveBy());
        vo.setApproveTime(order.getApproveTime());
        vo.setApproveRemark(order.getApproveRemark());
        vo.setIssueBy(order.getIssueBy());
        vo.setIssueTime(order.getIssueTime());
        vo.setReturnBy(order.getReturnBy());
        vo.setReturnTime(order.getReturnTime());
        vo.setReturnReason(order.getReturnReason());
        vo.setAcceptBy(order.getAcceptBy());
        vo.setAcceptTime(order.getAcceptTime());
        vo.setAcceptRemark(order.getAcceptRemark());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());
        vo.setItems(items.stream().map(this::toItemVo).toList());
        return vo;
    }

    private DeviceRentalItemVO toItemVo(IotDeviceRentalItem item) {
        DeviceRentalItemVO vo = new DeviceRentalItemVO();
        vo.setId(item.getId());
        vo.setEquipmentId(item.getEquipmentId());
        vo.setEquipmentCode(item.getEquipmentCode());
        vo.setEquipmentName(item.getEquipmentName());
        vo.setRequestedBrand(item.getRequestedBrand());
        vo.setRequestedModel(item.getRequestedModel());
        vo.setDeviceNo(item.getDeviceNo());
        vo.setDeviceType(item.getDeviceType());
        vo.setStatus(item.getStatus());
        vo.setStatusText(DeviceRentalItemStatusEnum.getLabel(item.getStatus()));
        vo.setIssueTime(item.getIssueTime());
        vo.setReturnTime(item.getReturnTime());
        vo.setAcceptTime(item.getAcceptTime());
        vo.setBindId(item.getBindId());
        vo.setRemark(item.getRemark());
        return vo;
    }
}
