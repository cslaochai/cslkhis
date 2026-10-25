/**
 * 门诊公共工位字典共用常量（sql/277，菜单 2960 / 权限 opd:station:*）。
 *
 * 工位类别与 OpdStationKindEnum（his-core-common）逐值对应：
 * 1-总导诊 2-门诊分诊台 3-输液室 4-注射室 5-采血室 9-其他。
 *  OpdStationView（门诊工位派岗）/ OpdStationEdit（增改弹框）/ ShiftDictView
 * （排班字典管理·公共工位字典 tab）三处共用，禁止各处再写一份。
 */
export const STATION_KIND_OPTIONS = [
  {label: '总导诊', value: 1},
  {label: '门诊分诊台', value: 2},
  {label: '输液室', value: 3},
  {label: '注射室', value: 4},
  {label: '采血室', value: 5},
  {label: '其他', value: 9},
]
