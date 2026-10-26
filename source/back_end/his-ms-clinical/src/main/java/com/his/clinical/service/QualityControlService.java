package com.his.clinical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.his.common.base.PageResult;
import com.his.clinical.dto.QcCandidatePageQueryDTO;
import com.his.clinical.dto.QcExecuteDTO;
import com.his.clinical.dto.QcPageQueryDTO;
import com.his.clinical.dto.QcSystemExecuteDTO;
import com.his.clinical.entity.BizQualityControl;
import com.his.clinical.support.QcSnapshot;
import com.his.clinical.vo.QcIssueVO;
import com.his.clinical.vo.*;

import java.util.List;

/**
 * 病案质控服务接口。
 */
public interface QualityControlService extends IService<BizQualityControl> {

    /**
     * 质控单分页
     */
    PageResult<BizQualityControlVO> selectQcPage(QcPageQueryDTO query);

    /**
     * 质控单详情（含问题明细）
     */
    BizQualityControlVO getQcDetail(Long qcId);

    /**
     * 对一份病历执行质控并落库
     */
    BizQualityControlVO executeQc(QcExecuteDTO dto);

    /**
     * 以系统发起的身份对一份病历执行质控并落库：操作人与触发来源由调用方显式给出。
     *
     * <p>用于门诊结诊收尾、出院收尾与定时补扫这些没有登录态的线程 —— 它们跑在事务提交之后的
     * 独立线程池里，取不到当前登录人；质控单上的质控人必须是发起时点的那个人，否则事后无法追溯。
     */
    BizQualityControlVO executeQcBySystem(QcSystemExecuteDTO dto);

    /**
     * 结诊定稿预检：对「即将定稿」的病历内容只挑否决项，不落库、不出质控单，返回空即可以结诊。
     *
     * <p>用于医生工作站、急诊工作站点「结诊」的那一次同步拦一道：医生此刻还坐在病历前面，
     * 缺陷当场改得动；结诊之后才跑的自动质控只能靠站内信和撤回补救，等于错过整改窗口。
     */
    List<QcIssueVO> inspectSubmitVeto(QcSnapshot snapshot);

    /**
     * 补扫漏掉自动质控的门诊病历：结诊收尾丢失后的兜底自愈，返回实际补跑的质控单数。
     *
     * <p>用于病案质控的定时补扫任务。结诊收尾跑在事务提交后的独立线程池里，线程池打满被拒、
     * 进程重启都会让整批收尾丢掉且库里不留痕；这里按「这段时间结诊、却没有自动质控单」反查病历补跑，
     * 落库触发来源记 3-定时补扫，与结诊那一次（2-结诊自动）分得开。
     *
     * @param limit 单次补扫的病历数上限
     */
    int autoRescanOutpatientQc(int limit);

    /**
     * 批量执行质控（病案室月末批量质控）
     */
    List<BizQualityControlVO> executeQcBatch(String recordSource, List<Long> recordIds, Integer qcType);

    /**
     * 处理质控问题
     */
    boolean handleQc(Long qcId, boolean ignore, String remark);

    /**
     * 质控概览
     */
    QcOverviewVO getOverview();

    /**
     * 规则清单与命中统计（含从未命中的规则）
     */
    List<QcRuleMetricVO> listRuleMetric(Integer dimension);

    /**
     * 某质控单的问题明细
     */
    List<QcIssueVO> listIssueByQc(Long qcId);

    /**
     * 待质控病历候选分页
     */
    PageResult<QcCandidateVO> listCandidatePage(QcCandidatePageQueryDTO query);

    /**
     * 维度字典
     */
    List<QcDimensionSelectListVO> dimensionDict();

    /**
     * 质控类型字典（0 综合 / 1~3 三维度 / 4 AI 内涵质控）
     */
    List<QcTypeSelectListVO> qcTypeDict();
}
