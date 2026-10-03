// 批量生成小程序用 SVG 线性图标（feather 风格：24 viewBox / stroke 2 / round）
import { writeFileSync, mkdirSync } from 'fs';

const OUT = 'images/icons';
mkdirSync(OUT, { recursive: true });

// 图标 path 定义（stroke 元素）
const ICONS = {
  // ---- 业务功能 ----
  appointment: [
    '<rect x="3" y="4" width="18" height="18" rx="2"/>',
    '<line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>',
    '<line x1="3" y1="10" x2="21" y2="10"/>',
  ],
  payment: [
    '<rect x="1" y="4" width="22" height="16" rx="2"/>',
    '<line x1="1" y1="10" x2="23" y2="10"/>',
  ],
  report: [
    '<line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>',
  ],
  bed: [
    '<path d="M2 19v-8"/><path d="M2 15h20v4"/>',
    '<path d="M22 15v-3a3 3 0 0 0-3-3h-8v6"/>',
    '<circle cx="6.5" cy="11.5" r="1.5"/>',
  ],
  robot: [
    '<rect x="5" y="8" width="14" height="11" rx="2.5"/>',
    '<path d="M12 8V5.5"/><circle cx="12" cy="4" r="1.2"/>',
    '<circle cx="9.5" cy="13" r="1.3" fill="{c}" stroke="none"/><circle cx="14.5" cy="13" r="1.3" fill="{c}" stroke="none"/>',
    '<path d="M9.5 16.3h5"/>',
  ],
  clock: [
    '<circle cx="12" cy="12" r="9"/><polyline points="12 7 12 12 15.5 14"/>',
  ],
  filetext: [
    '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>',
    '<polyline points="14 2 14 8 20 8"/>',
    '<line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>',
  ],
  pill: [
    '<g transform="rotate(45 12 12)"><rect x="4.5" y="9" width="15" height="6" rx="3"/><line x1="12" y1="9" x2="12" y2="15"/></g>',
  ],
  shield: [
    '<path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>',
  ],
  money: [
    '<path d="M8 5h8"/><path d="M12 5v15"/><path d="M8 11h8"/><path d="M9.5 5 12 8l2.5-3"/>',
  ],
  // ---- 表单 ----
  user: [
    '<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>',
  ],
  lock: [
    '<rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>',
  ],
  eye: [
    '<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>',
  ],
  eyeoff: [
    '<path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>',
  ],
  search: [
    '<circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>',
  ],
  // ---- 顶栏（白色版另出）----
  hospital: [
    '<rect x="4" y="7" width="16" height="14" rx="1.5"/>',
    '<path d="M12 10.5v5"/><path d="M9.5 13h5"/>',
    '<path d="M10 21v-3.5h4V21"/>',
  ],
  bell: [
    '<path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>',
    '<path d="M13.73 21a2 2 0 0 1-3.46 0"/>',
  ],
  menu: [
    '<line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/>',
  ],
  alert: [
    '<path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>',
    '<line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>',
  ],
  check: [
    '<circle cx="12" cy="12" r="10"/>',
    '<path d="m9 12 2 2 4-4"/>',
  ],
  users: [
    '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>',
    '<circle cx="9" cy="7" r="4"/>',
    '<path d="M23 21v-2a4 4 0 0 0-3-3.87"/>',
    '<path d="M16 3.13a4 4 0 0 1 0 7.75"/>',
  ],
  gear: [
    '<circle cx="12" cy="12" r="3"/>',
    '<path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/>',
  ],
};

const tpl = (c, inner) =>
  `<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="${c}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">\n  ${inner}\n</svg>\n`;

const BLUE = '#2b6cb0';
let n = 0;
for (const [name, paths] of Object.entries(ICONS)) {
  const inner = paths.map((p) => p.replace('{c}', BLUE)).join('\n  ');
  writeFileSync(`${OUT}/${name}.svg`, tpl(BLUE, inner));
  n++;
}
// 白色版（蓝底 banner / fab 用）
for (const name of ['hospital', 'bell', 'robot', 'menu']) {
  const inner = ICONS[name].map((p) => p.replace('{c}', '#ffffff')).join('\n  ');
  writeFileSync(`${OUT}/${name}-w.svg`, tpl('#ffffff', inner));
  n++;
}
console.log(`generated ${n} svg icons -> ${OUT}/`);

// 语义色图标（聊天提示条用）
const SEMANTIC = { alert: '#e6a23c', check: '#67c23a' };
for (const [name, color] of Object.entries(SEMANTIC)) {
  const inner = ICONS[name].map((p) => p.replace('{c}', color)).join('\n  ');
  writeFileSync(`${OUT}/${name}.svg`, tpl(color, inner));
}
