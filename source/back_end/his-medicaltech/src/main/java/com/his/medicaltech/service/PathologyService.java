package com.his.medicaltech.service;

import com.his.common.base.PageResult;
import com.his.medicaltech.dto.PathologyDTO;
import com.his.medicaltech.entity.BizPathologyBlock;
import com.his.medicaltech.entity.BizPathologyOrder;
import com.his.medicaltech.vo.PathologyVO;

import java.util.List;

/**
 * 病理亚专业服务：病理单登记、标本接收、蜡块流转与初诊-审核-发布这条病理链的业务收口。
 */
public interface PathologyService extends com.baomidou.mybatisplus.extension.service.IService<BizPathologyOrder> {

    /**
     * 病理单分页：按病理号、患者、检查类型、状态与送检日期区间检索。
     * 用于病理工作站的病理单查询表格。
     */
    PageResult<PathologyVO.ListVO> pageVO(PathologyDTO.Query q);

    /**
     * 病理工作量统计：待接收、处理中、待审核、已发布的单据数与今日冰冻数量。
     * 用于病理工作站顶部的统计卡。
     */
    PathologyVO.StatsVO stats();

    /**
     * 病理单详情，含名下全部蜡块与流程时间节点。
     * 用于病理工作站的详情弹框与初诊弹框回显。
     */
    PathologyVO.DetailVO getDetail(Long orderId);

    /**
     * 按病理单取其名下蜡块明细列表。
     * 供蜡块流转环节在单据上下文之外单独核对蜡块进度时取数。
     */
    List<PathologyVO.BlockVO> listBlocks(Long orderId);

    /**
     * 新增或修改病理单；仅已登记状态的单据可改，返回落库后的单据详情。
     * 用于病理工作站的病理单登记与修改。
     */
    PathologyVO.DetailVO upsertOrder(PathologyDTO.OrderUpsert dto);

    /**
     * 由内镜检查记录派生病理送检单，按活检部位与取材块数初始化蜡块，返回新建单据。
     * 供内镜模块在活检送检环节调用，送检成功后由内镜侧回填病理号。
     */
    BizPathologyOrder createFromEndoscopy(PathologyDTO.FromEndoscopy dto);

    /**
     * 接收送检标本：登记接收人与接收状态，把病理单推进到已接收。
     * 用于病理工作站待接收标本的接收动作。
     */
    void receive(PathologyDTO.Receive dto);

    /**
     * 推进病理主单流程到已取材或已制片。
     * 供大体取材与制片环节回写主单状态。
     */
    void process(PathologyDTO.Process dto);

    /**
     * 为病理单补登记一枚蜡块，返回新建蜡块。
     * 用于病理工作站的蜡块补录。
     */
    BizPathologyBlock addBlock(PathologyDTO.BlockUpsert dto);

    /**
     * 推进单个蜡块的流转状态（取材、包埋、切片），返回所属病理单ID。
     * 用于病理单详情弹框内蜡块行的流转操作。
     */
    Long blockAction(PathologyDTO.BlockAction dto);

    /**
     * 提交病理初诊：落镜下所见与病理诊断，单据进入待审核。
     * 用于病理工作站的初诊报告录入。
     */
    void report(PathologyDTO.Report dto);

    /**
     * 审核病理初诊，审核人不得是初诊人本人。
     * 用于病理工作站初诊完成后的上级医师审核。
     */
    void audit(PathologyDTO.Audit dto);

    /**
     * 发布已审核的病理报告，使其对临床可见。
     * 用于病理工作站审核通过后的发布动作。
     */
    void publish(Long orderId);

    /**
     * 取消病理单；报告已发布的单据不可取消。
     * 用于病理工作站作废录错或重复的病理单。
     */
    void cancel(PathologyDTO.Cancel dto);

    /**
     * 按病理单ID取其病理号。
     * 供送检来源单据回写病理号、以及按病理号对外沟通的环节取数。
     */
    String orderNoById(Long id);

    /**
     * 判断某来源检查记录是否已生成过病理单。
     * 供活检送检环节的重复送检校验，避免同一检查记录开出两张病理单。
     */
    boolean existsBySource(String sourceRecordNo);
}
