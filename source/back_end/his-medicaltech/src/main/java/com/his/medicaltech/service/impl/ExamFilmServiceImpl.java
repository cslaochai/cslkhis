package com.his.medicaltech.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.his.common.base.PageResult;
import com.his.common.enums.FeeSourceTypeEnum;
import com.his.common.enums.PaymentItemTypeEnum;
import com.his.common.exception.BusinessException;
import com.his.emr.entity.BizInspectionApply;
import com.his.emr.mapper.BizInspectionApplyMapper;
import com.his.charge.dto.FeeBookDTO;
import com.his.charge.entity.BizFeeRecord;
import com.his.charge.service.FeeRecordService;
import com.his.medicaltech.entity.BizInspectionRecord;
import com.his.medicaltech.dto.ExamFilmQueryPageDTO;
import com.his.medicaltech.dto.ExamFilmSpecUpsertDTO;
import com.his.medicaltech.dto.ExamFilmUpsertDTO;
import com.his.medicaltech.entity.BizExamFilm;
import com.his.medicaltech.entity.BizFilmSpec;
import com.his.medicaltech.enums.FilmStatusEnum;
import com.his.medicaltech.mapper.BizExamFilmMapper;
import com.his.medicaltech.mapper.BizFilmSpecMapper;
import com.his.medicaltech.service.ExamFilmService;
import com.his.medicaltech.vo.ExamFilmVO;
import com.his.medicaltech.vo.FilmSpecSelectListVO;
import com.his.medicaltech.mapper.BizInspectionRecordMapper;
import com.his.system.service.DictCacheService;
import com.his.system.utils.UserUtils;
import com.his.system.service.SysAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 检查胶片量方与发放（sql/138）。
 *
 * <p>三件容易被做错、这里明确钉死的事：
 * <ol>
 *   <li><b>金额一律服务端现算</b>（单价 × 张数），不收前端传的金额 ——
 *       收了就等于把应收交给调用方定义；</li>
 *   <li><b>已记账的胶片不能作废</b>，只能走收费红冲。作废一张已经收了钱的行，
 *       等于让账面上凭空少一笔，而收费侧那笔账还在；</li>
 *   <li><b>用量与记账分两步</b>：登记用量是临床事实，随时可记；记账是财务动作，
 *       只在点「记账」时写一条应收，未记的行在页面上一直显示「未记账」而不是假装记过了。</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamFilmServiceImpl implements ExamFilmService {
    /** 分页每页条数上限（技术阈值，防止前端传入超大值把库拖垮；DTO 迁 PageParam 后在此夹取） */
    private static final int MAX_PAGE_SIZE = 200;


    private static final int AMOUNT_SCALE = 2;

    private final BizExamFilmMapper filmMapper;
    private final BizFilmSpecMapper specMapper;
    private final BizInspectionRecordMapper recordMapper;
    private final BizInspectionApplyMapper applyMapper;
    private final DictCacheService subDictText;
    private final SysAuditLogService sysAuditLogService;
    private final FeeRecordService feeRecordService;

    // 查询

    @Override
    public PageResult<ExamFilmVO> listPage(ExamFilmQueryPageDTO query) {
        int pageNum = Math.max(query.getPageNum(), 1);
        int pageSize = Math.min(Math.max(query.getPageSize(), 1), MAX_PAGE_SIZE);
        IPage<ExamFilmVO> page = new Page<>(pageNum, pageSize);
        List<ExamFilmVO> list = filmMapper.selectFilmPage(page,
                trim(query.getKeyword()), query.getRecordId(), query.getFilmStatus(),
                query.getChargeFlag(), trim(query.getStartDate()), trim(query.getEndDate()));
        list.forEach(this::fillText);
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getPages(), list);
    }

    @Override
    public List<ExamFilmVO> listByRecordId(Long recordId) {
        if (recordId == null) {
            return new ArrayList<>();
        }
        List<ExamFilmVO> list = filmMapper.selectList(new LambdaQueryWrapper<BizExamFilm>()
                        .eq(BizExamFilm::getRecordId, recordId)
                        .orderByDesc(BizExamFilm::getId))
                .stream().map(this::toVO).collect(Collectors.toList());
        list.forEach(this::fillText);
        return list;
    }

    @Override
    public ExamFilmVO.FilmStats stats(String startDate, String endDate) {
        String from = StringUtils.hasText(startDate) ? startDate.trim() : LocalDate.now().toString();
        String to = StringUtils.hasText(endDate) ? endDate.trim() : from;
        return filmMapper.selectStats(from, to);
    }

    // 写

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamFilmVO upsert(ExamFilmUpsertDTO dto) {
        BizInspectionRecord record = recordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException("检查记录不存在或已被删除");
        }
        BizFilmSpec spec = specMapper.selectById(dto.getSpecId());
        if (spec == null) {
            throw new BusinessException("胶片规格不存在或已被删除");
        }
        if (!Objects.equals(1, spec.getStatus())) {
            throw new BusinessException("规格「" + spec.getSpecName() + "」已停用，请先启用");
        }

        BizExamFilm film;
        if (dto.getId() == null) {
            film = new BizExamFilm();
            film.setFilmNo(newFilmNo());
            film.setFilmStatus(FilmStatusEnum.REGISTERED.getCode());
            film.setChargeFlag(0);
            fillSnapshot(film, record, spec);
        } else {
            film = filmMapper.selectById(dto.getId());
            if (film == null) {
                throw new BusinessException("胶片记录不存在或已被删除");
            }
            if (Objects.equals(1, film.getChargeFlag())) {
                throw new BusinessException("该胶片已记账，不能改张数：请先到收费台红冲那笔费用");
            }
            if (Objects.equals(FilmStatusEnum.INVALID.getCode(), film.getFilmStatus())) {
                throw new BusinessException("该胶片已作废，不能修改");
            }
            // 改张数时规格也允许换（录错了很常见），但快照要跟着刷新
            fillSnapshot(film, record, spec);
        }
        film.setQuantity(dto.getQuantity());
        // 金额服务端现算：单价取**当时的规格价**并快照到本行，改价不影响历史账单
        BigDecimal price = spec.getUnitPrice() == null ? BigDecimal.ZERO : spec.getUnitPrice();
        film.setAmount(price.multiply(BigDecimal.valueOf(dto.getQuantity()))
                .setScale(AMOUNT_SCALE, RoundingMode.HALF_UP));
        film.setRemark(cut(dto.getRemark(), 500));

        if (film.getId() == null) {
            filmMapper.insert(film);
        } else {
            filmMapper.updateById(film);
        }
        return fillText(toVO(film));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamFilmVO charge(Long filmId) {
        BizExamFilm film = loadFilm(filmId);
        if (Objects.equals(1, film.getChargeFlag())) {
            throw new BusinessException("该胶片已记账（流水号 " + film.getFeeNo() + "），不要重复记账");
        }
        if (Objects.equals(FilmStatusEnum.INVALID.getCode(), film.getFilmStatus())) {
            throw new BusinessException("已作废的胶片不能记账");
        }

        BizInspectionRecord record = recordMapper.selectById(film.getRecordId());
        // 就诊标识只能从申请单上取（检查记录自身没有挂号ID）：取不到就拒绝，
        // 而不是塞一个 0 进去 —— 记出一笔「不知道是谁的就诊」的费用，比报错更糟。
        BizInspectionApply apply = film.getApplyId() == null ? null : applyMapper.selectById(film.getApplyId());
        if (apply == null || apply.getRegistId() == null) {
            throw new BusinessException("找不到该检查对应的就诊/挂号信息，无法记账（申请单缺失或已清理）");
        }

        FeeBookDTO dto = new FeeBookDTO();
        dto.setPatientId(film.getPatientId());
        dto.setPatientNo(film.getPatientNo());
        dto.setPatientName(film.getPatientName());
        dto.setEncounterType(1);
        dto.setEncounterId(apply.getRegistId());
        // 申请单上没有挂号单号这一列（只有 regist_id），这里留空而不是拿申请单号顶替：
        // encounter_no 是给人对账看的，填成 INS… 会让人以为这是挂号流水号。
        dto.setEncounterNo(null);
        dto.setDeptId(record == null ? null : record.getInspectionDeptId());
        dto.setDeptName(record == null ? null : record.getInspectionDeptName());
        dto.setDoctorId(UserUtils.getCurrentEmployeeId());
        dto.setDoctorName(UserUtils.getCurrentEmployeeName());
        // 胶片是耗材：项目类型 8-耗材材料，费用来源 7-耗材使用
        dto.setItemType(PaymentItemTypeEnum.CONSUMABLE.getCode());
        dto.setItemCode(film.getSpecCode());
        dto.setItemName(film.getSpecName());
        dto.setSpecification(film.getSpecName());
        dto.setUnit(film.getUnit() == null ? "张" : film.getUnit());
        dto.setPrice(film.getUnitPrice());
        dto.setQuantity(BigDecimal.valueOf(film.getQuantity()));
        dto.setSourceType(FeeSourceTypeEnum.CONSUMABLE.getCode());
        // sourceId = 胶片行ID：记账幂等键，也是「这笔钱是哪张胶片产生的」的唯一答案
        dto.setSourceId(film.getId());
        dto.setSourceNo(film.getFilmNo());
        // 胶片按自费记：各地医保对胶片是否报销口径不一，宁可按自费记进账单，
        // 由结算层按当地政策处理，也不要在记账层写一个可能错的报销类别。
        dto.setCatalogType(0);
        dto.setRemark(cut("检查胶片 " + film.getFilmNo() + "（记录号 " + film.getRecordNo() + "）", 500));

        BizFeeRecord fee = feeRecordService.book(dto);
        film.setChargeFlag(1);
        film.setFeeId(fee.getId());
        film.setFeeNo(fee.getFeeNo());
        filmMapper.updateById(film);

        sysAuditLogService.record(UserUtils.getCurrentEmployeeId(), UserUtils.getCurrentEmployeeName(),
                "检查胶片", "胶片记账", "biz_exam_film", film.getId(),
                cut("胶片单号=" + film.getFilmNo() + " 规格=" + film.getSpecName()
                        + " 张数=" + film.getQuantity() + " 金额=" + film.getAmount()
                        + " 记账流水=" + fee.getFeeNo(), 2000),
                true, null);
        return fillText(toVO(film));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamFilmVO markPrinted(Long filmId) {
        BizExamFilm film = loadFilm(filmId);
        if (Objects.equals(FilmStatusEnum.INVALID.getCode(), film.getFilmStatus())) {
            throw new BusinessException("已作废的胶片不能再打印");
        }
        if (Objects.equals(FilmStatusEnum.DELIVERED.getCode(), film.getFilmStatus())) {
            throw new BusinessException("该胶片已发放给患者，重复打印请先登记一行新的");
        }
        film.setFilmStatus(FilmStatusEnum.PRINTED.getCode());
        film.setPrintBy(UserUtils.getCurrentEmployeeName());
        film.setPrintTime(LocalDateTime.now());
        filmMapper.updateById(film);
        return fillText(toVO(film));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamFilmVO deliver(Long filmId) {
        BizExamFilm film = loadFilm(filmId);
        if (Objects.equals(FilmStatusEnum.INVALID.getCode(), film.getFilmStatus())) {
            throw new BusinessException("已作废的胶片不能发放");
        }
        if (Objects.equals(FilmStatusEnum.DELIVERED.getCode(), film.getFilmStatus())) {
            throw new BusinessException("该胶片已发放，不要重复发放");
        }
        film.setFilmStatus(FilmStatusEnum.DELIVERED.getCode());
        film.setDeliverBy(UserUtils.getCurrentEmployeeName());
        film.setDeliverTime(LocalDateTime.now());
        filmMapper.updateById(film);
        return fillText(toVO(film));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long filmId, String reason) {
        BizExamFilm film = loadFilm(filmId);
        if (Objects.equals(1, film.getChargeFlag())) {
            throw new BusinessException("已记账的胶片不能作废（记账流水 " + film.getFeeNo()
                    + "）：请先在收费台红冲那笔费用，再回来作废");
        }
        if (Objects.equals(FilmStatusEnum.DELIVERED.getCode(), film.getFilmStatus())) {
            throw new BusinessException("已发放给患者的胶片不能作废：实物已经出去了");
        }
        // 留痕必须在改状态之前：作废后这行的快照还在，但"是谁因为什么作废的"只有审计里有
        sysAuditLogService.record(UserUtils.getCurrentEmployeeId(), UserUtils.getCurrentEmployeeName(),
                "检查胶片", "作废胶片", "biz_exam_film", film.getId(),
                cut("胶片单号=" + film.getFilmNo() + " 规格=" + film.getSpecName()
                        + " 张数=" + film.getQuantity() + " 金额=" + film.getAmount()
                        + " 原因=" + (StringUtils.hasText(reason) ? reason : "未填写"), 2000),
                true, null);
        BizExamFilm update = new BizExamFilm();
        update.setId(film.getId());
        update.setFilmStatus(FilmStatusEnum.INVALID.getCode());
        update.setRemark(cut("作废：" + (StringUtils.hasText(reason) ? reason : "未填写原因"), 500));
        return filmMapper.updateById(update) > 0;
    }

    // 规格价目

    @Override
    public List<FilmSpecSelectListVO> specSelectList() {
        return specMapper.selectList(new LambdaQueryWrapper<BizFilmSpec>()
                        .eq(BizFilmSpec::getStatus, 1)
                        .orderByAsc(BizFilmSpec::getSortOrder)
                        .orderByAsc(BizFilmSpec::getId))
                .stream().map(this::toSpecVO).collect(Collectors.toList());
    }

    @Override
    public FilmSpecSelectListVO upsertSpec(ExamFilmSpecUpsertDTO dto) {
        BizFilmSpec spec = new BizFilmSpec();
        spec.setSpecCode(cut(dto.getSpecCode(), 32));
        spec.setSpecName(cut(dto.getSpecName(), 100));
        spec.setUnitPrice(dto.getUnitPrice());
        spec.setUnit(StringUtils.hasText(dto.getUnit()) ? cut(dto.getUnit(), 20) : "张");
        spec.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        spec.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        if (dto.getId() == null) {
            long dup = specMapper.selectCount(new LambdaQueryWrapper<BizFilmSpec>()
                    .eq(BizFilmSpec::getSpecCode, spec.getSpecCode()));
            if (dup > 0) {
                throw new BusinessException("规格编码 " + spec.getSpecCode() + " 已存在");
            }
            specMapper.insert(spec);
        } else {
            spec.setId(dto.getId());
            specMapper.updateById(spec);
        }
        return toSpecVO(specMapper.selectById(spec.getId()));
    }

    @Override
    public boolean deleteSpec(Long id) {
        if (id == null) {
            throw new BusinessException("缺少规格ID");
        }
        // 已经用过的规格不允许删：删掉之后历史胶片行的 spec_name 还在，
        // 但「这个规格当年多少钱」这条链就断了，对账时会查不到价。
        long used = filmMapper.selectCount(new LambdaQueryWrapper<BizExamFilm>()
                .eq(BizExamFilm::getSpecId, id));
        if (used > 0) {
            throw new BusinessException("该规格已被 " + used + " 条胶片用量引用，不能删除；请改为「停用」");
        }
        return specMapper.purgeById(id) > 0;
    }

    // 内部

    private BizExamFilm loadFilm(Long filmId) {
        BizExamFilm film = filmId == null ? null : filmMapper.selectById(filmId);
        if (film == null) {
            throw new BusinessException("胶片记录不存在或已被删除");
        }
        return film;
    }

    /** 把检查记录与规格的当前值快照进胶片行（改名/改价不影响已发出的行） */
    private void fillSnapshot(BizExamFilm film, BizInspectionRecord record, BizFilmSpec spec) {
        film.setRecordId(record.getId());
        film.setRecordNo(record.getRecordNo());
        film.setApplyId(record.getApplyId());
        film.setApplyNo(record.getApplyNo());
        film.setPatientId(record.getPatientId());
        film.setPatientNo(record.getPatientNo());
        film.setPatientName(record.getPatientName());
        film.setVisitDate(record.getVisitDate());
        film.setItemCode(record.getInspectionItemCode());
        film.setItemName(record.getInspectionItemName());
        film.setBodyPart(record.getBodyPart());
        film.setSpecId(spec.getId());
        film.setSpecCode(spec.getSpecCode());
        film.setSpecName(spec.getSpecName());
        film.setUnitPrice(spec.getUnitPrice());
        film.setUnit(spec.getUnit());
    }

    /**
     * 胶片单号：FM + yyyyMMdd + 5 位序号。
     *
     * <p><b>序号必须取当天「最大已用序号 + 1」，绝不能取「当天行数 + 1」</b>：
     * 只要有人删掉一张片（或历史上作废行被清掉），行数就回退，下一个单号会撞上
     * 现存记录 → {@code uk_film_no} 直接 Duplicate entry → 登记胶片 500，
     * 表现是「前面登记得好好的，第二张就失败了」。单号是唯一键，不是计数器。
     *
     * <p>并发下仍可能撞（两个请求同时读到同一个 max），靠 uk_film_no 兜底：
     * 撞了就抛错让人重试，好过静默生成两个同号胶片。
     */
    private String newFilmNo() {
        String day = LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        String prefix = "FM" + day;
        long seq = filmMapper.selectMaxSeqOfDay(prefix) + 1;
        return prefix + String.format("%05d", seq);
    }

    private ExamFilmVO toVO(BizExamFilm e) {
        ExamFilmVO vo = new ExamFilmVO();
        BeanUtils.copyProperties(e, vo);
        return vo;
    }

    private ExamFilmVO fillText(ExamFilmVO vo) {
        if (vo == null) {
            return null;
        }
        vo.setFilmStatusText(subDictText.getDicDataLabel("his_film_status", vo.getFilmStatus()));
        vo.setModalityText(vo.getModality() == null ? null
                : subDictText.getDicDataLabel("his_exam_device_type", vo.getModality()));
        return vo;
    }

    private FilmSpecSelectListVO toSpecVO(BizFilmSpec e) {
        FilmSpecSelectListVO vo = new FilmSpecSelectListVO();
        BeanUtils.copyProperties(e, vo);
        return vo;
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static String cut(String s, int max) {
        if (s == null) {
            return null;
        }
        String v = s.trim();
        return v.length() <= max ? v : v.substring(0, max);
    }
}
