// 从 Iconify 拉取 Lucide 开源图标（ISC 许可，https://lucide.dev）替换小程序自绘图标。
// 输出保持既有文件名与颜色变体（业务蓝 / 白色 -w / 语义色），页面引用零改动。
// 用法：node tools/gen-icons-lucide.mjs   （需可访问 api.iconify.design）
import { writeFileSync, mkdirSync } from 'fs';

const OUT = 'images/icons';
mkdirSync(OUT, { recursive: true });

const BLUE = '#2b6cb0';

// 我们的文件名 -> lucide 图标名
const MAP = {
  appointment: 'calendar-plus',
  payment: 'receipt-text',
  report: 'chart-no-axes-combined',
  bed: 'bed-double',
  robot: 'bot',
  clock: 'clock',
  filetext: 'file-text',
  pill: 'pill',
  shield: 'shield-plus',
  money: 'circle-dollar-sign',
  user: 'user-round',
  lock: 'lock',
  eye: 'eye',
  eyeoff: 'eye-off',
  search: 'search',
  hospital: 'hospital',
  bell: 'bell',
  menu: 'menu',
  alert: 'triangle-alert',
  check: 'circle-check',
  users: 'users-round',
  gear: 'settings',
  // 二期新增
  clipboard: 'clipboard-list',
  logout: 'log-out',
  home: 'house',
  // 注册页表单字段
  smartphone: 'smartphone',
  sms: 'message-square-code',
  // 首页 banner 联系信息
  mappin: 'map-pin',
  phone: 'phone',
};

const tpl = (inner) =>
  `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">\n  ${inner}\n</svg>\n`;

// currentColor -> 目标色；stroke-width 与圆角样式在模板里统一给
const colorize = (body, color) => body.replace(/currentColor/g, color).trim();

async function fetchBody(name) {
  const url = `https://api.iconify.design/lucide/${name}.svg?format=none`;
  const resp = await fetch(url);
  if (!resp.ok) throw new Error(`${name}: HTTP ${resp.status}`);
  const svg = await resp.text();
  const m = svg.match(/<svg[^>]*>([\s\S]*)<\/svg>/);
  if (!m) throw new Error(`${name}: 响应不是 svg`);
  return m[1];
}

let n = 0;
for (const [file, lucideName] of Object.entries(MAP)) {
  const body = await fetchBody(lucideName);
  writeFileSync(`${OUT}/${file}.svg`, tpl(colorize(body, BLUE)));
  n++;
}
// 白色版（蓝底 banner / fab 用）
for (const name of ['hospital', 'bell', 'robot', 'menu', 'mappin', 'phone']) {
  const body = await fetchBody(MAP[name]);
  writeFileSync(`${OUT}/${name}-w.svg`, tpl(colorize(body, '#ffffff')));
  n++;
}
// 语义色（聊天提示条用）
for (const [name, color] of Object.entries({ alert: '#e6a23c', check: '#67c23a' })) {
  const body = await fetchBody(MAP[name]);
  writeFileSync(`${OUT}/${name}.svg`, tpl(colorize(body, color)));
  n++;
}
console.log(`generated ${n} lucide icons -> ${OUT}/ (ISC license, via api.iconify.design)`);
