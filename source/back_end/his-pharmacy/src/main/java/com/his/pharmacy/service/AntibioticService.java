package com.his.pharmacy.service;

import com.his.common.base.PageResult;
import com.his.pharmacy.dto.AntibioticAliasUpsertDTO;
import com.his.pharmacy.dto.AntibioticAuthQueryPageDTO;
import com.his.pharmacy.dto.AntibioticAuthUpsertDTO;
import com.his.pharmacy.dto.AntibioticCatalogLevelUpsertDTO;
import com.his.pharmacy.dto.AntibioticCatalogQueryPageDTO;
import com.his.pharmacy.vo.AntibioticAliasVO;
import com.his.pharmacy.vo.AntibioticAuthCheckVO;
import com.his.pharmacy.vo.AntibioticAuthVO;
import com.his.pharmacy.vo.AntibioticCatalogVO;
import com.his.pharmacy.vo.AntibioticDoctorSelectListVO;
import com.his.pharmacy.vo.AntibioticDrugSelectListVO;

import java.util.List;

/**
 * 抗菌药物分级目录 + 医师处方权授权。
 *
 * <p>本模块的"硬"落点只有一个：{@link #checkAuthority(Long, List)} —— 开方时
 * 医师授权级别必须 ≥ 药品分级。目录和授权表本身只是台账，闸不住的话评审一句
 * "系统里谁能开限制级"就答不上来。
 */
public interface AntibioticService {

    /** 分级目录分页 */
    PageResult<AntibioticCatalogVO> catalogListPage(AntibioticCatalogQueryPageDTO query);

    /** 维护药品分级与 DDD 值 */
    AntibioticCatalogVO catalogLevelUpsert(AntibioticCatalogLevelUpsertDTO dto);

    /** 别名列表（drugId 为空=全部；非空=该药品的别名） */
    List<AntibioticAliasVO> aliasList(Long drugId);

    /** 别名新增/修改 */
    AntibioticAliasVO aliasUpsert(AntibioticAliasUpsertDTO dto);

    /** 别名删除（物理删：uk_antibiotic_alias 不含 del_flag） */
    void aliasDeleteById(Long id);

    /** 抗菌药物下拉（selectList 口径） */
    List<AntibioticDrugSelectListVO> antibioticDrugSelectList();

    /** 医师下拉（selectList 口径） */
    List<AntibioticDoctorSelectListVO> doctorSelectList(String keyword);

    /** 处方权授权分页 */
    PageResult<AntibioticAuthVO> authListPage(AntibioticAuthQueryPageDTO query);

    /** 授权新增/修改 */
    AntibioticAuthVO authUpsert(AntibioticAuthUpsertDTO dto);

    /**
     * 开方越权闸：医师授权级别必须 ≥ 药品分级。
     *
     * <p>返回可开/不可开 + 具体药品名与级别，供前端提示与后端阻断共用同一份判定。
     * 前端可以先问一次拿提示文案，但<b>真正的闸在服务端</b>（{@link #assertCanPrescribe}）。
     */
    AntibioticAuthCheckVO checkAuthority(Long doctorId, List<Long> drugIds);

    /**
     * 开方落库前的硬校验：无有效授权直接抛 BusinessException 让整张处方回滚。
     *
     * <p>为什么是抛异常而不是返回码：开方事务里"一张开出去但没权限的处方"比"开不出去"危险得多。
     */
    void assertCanPrescribe(Long doctorId, List<Long> drugIds);

    /** 某医师当前有效的最高授权级别（null=没有任何有效授权） */
    Integer maxValidLevel(Long doctorId);
}
