/**
 * 门诊诊查费职级档位（与后端 ClinicDiagnosisTierEnum 一一对应）。
 *
 * <p>依据：湘医保发〔2025〕58号附件1（长沙市医保局同文，2026-01-01 执行）四档加收价。
 * 之前这里只有一个 isExpert 开关，把副主任和主任压成同一档，是 dev 库里
 * 63 名副主任医师与 57 名主任医师的号源全部按 70 元挂的直接原因。
 */
export const DIAGNOSIS_TIER_LIST = [
  {value: 1, label: '普通（主治及以下）', amount: 9, ybCode: '011102020010000'},
  {value: 2, label: '副主任医师', amount: 22, amountText: '9 + 加收 13', ybCode: '011102020010001'},
  {value: 3, label: '主任医师', amount: 31, amountText: '9 + 加收 22', ybCode: '011102020010002'},
  {value: 4, label: '知名专家', amount: 70, amountText: '9 + 加收 61', ybCode: '011102020010003'}
];

/** 档位码 → 文案；未知码回落成「档位X」，不空白（前端字典缺失时也能看出异常） */
export const tierLabel = (tier) =>
  DIAGNOSIS_TIER_LIST.find((t) => Number(t.value) === Number(tier))?.label || (tier ? `档位${tier}` : '');

/** 档位码 → 政策全额（元）；未知码返回 0，调用方自行决定是否拦截 */
export const tierAmount = (tier) =>
  DIAGNOSIS_TIER_LIST.find((t) => Number(t.value) === Number(tier))?.amount || 0;

/**
 * 员工职称码（字典 sys_hospital_title）→ 诊查费档位。
 *
 * 🔑 白名单精确匹配，**不能用 startsWith('3') 之类的前缀**：字典里 302副主任药师、
 * 303副主任护师、304副主任技师紧挨着 301副主任医师，前缀匹配会把药事/护理/医技人员
 * 全算成「副主任医师加收」，挂错号就是错账。
 */
const TITLE_TIER_MAP = {
  '201': 1, // 主治（主管）医师
  '301': 2, // 副主任医师
  '401': 3  // 主任医师
};

/** 知名专家只对主任医师开放选项，但**不自动选中**：它是名单制认定，不该由职称推导 */
const EXPERT_ALLOWED_TITLES = ['401'];

/**
 * 按员工档案推导号源默认档位与价格。
 *
 * <p>知名专家（档位 4）刻意<b>不从职称推导</b>：它每期名单会调整，由系统按职称猜等于
 * 把定价权交给一份永远过期的名单。主任医师默认给档位 3，排班人员可手工改成 4。
 *
 * @param emp 员工档案（EmployeeVO），需含 title 字段
 * @param currentTier 当前已选档位；非空且人工调过则保留，不被职称覆盖
 * @returns {{diagnosisTier:number, diagnosisFee:number, isExpert:number, expertFee:number}}
 */
export const tierDefaultOf = (emp, currentTier) => {
  const derived = TITLE_TIER_MAP[String(emp?.title ?? '')] || 1;
  // 已人工选过档位就尊重人工选择 —— 知名专家、名额分配等都不是职称能推导的
  const tier = Number(currentTier) >= 1 && Number(currentTier) <= 4 ? Number(currentTier) : derived;
  const amount = tierAmount(tier);
  return {
    diagnosisTier: tier,
    diagnosisFee: amount,
    // isExpert / expertFee 仅作展示标记，不再参与计价（后端已标 @Deprecated）
    isExpert: tier >= 2 ? 1 : 0,
    expertFee: 0
  };
};

/** 该职称是否允许选「知名专家」档（前端据此禁用/隐藏该选项） */
export const expertTierAllowed = (emp) => EXPERT_ALLOWED_TITLES.includes(String(emp?.title ?? ''));
