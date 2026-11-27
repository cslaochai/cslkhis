import { createRequire } from 'module';
const require = createRequire('C:/Users/10431/.workbuddy/binaries/node/workspace/package.json');
const smcrypto = require('E:/work_source/his_cslk/cslk/source/front/node_modules/sm-crypto');
const BASE='http://localhost:8080/api';
const j=async(p,o={})=>(await(await fetch(BASE+p,o)).json().catch(()=>({})));
const pk=await j('/auth/publicKey');
const login=await j('/auth/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username:'admin',password:smcrypto.sm2.doEncrypt('123456',pk.data.publicKey,1)})});
const token=login.data.token; const H={Authorization:'Bearer '+token,'Content-Type':'application/json'};
const types=['infusion','bed','coldchain','asset','robot','env','gas','cabinet','uv','dialysis'];
const map=new Map();
for (const t of types){ const r=await j('/iot/device/selectList',{method:'POST',headers:H,body:JSON.stringify({deviceType:t})}); for(const d of r.data||[]) map.set(d.deviceNo,d.deptName); }
const pad=n=>String(n).padStart(2,'0'); const f=d=>`${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
const now=new Date(), start=new Date(now.getTime()-15*60*1000);
const page=await j('/iot/event/listPage',{method:'POST',headers:H,body:JSON.stringify({pageNum:1,pageSize:50,minLevel:2,handledFlag:0,startTime:f(start),endTime:f(now)})});
const rows=(page.data?.records||[]).map(r=>({ts:r.ts,lv:r.level,no:r.deviceNo,dept:map.get(r.deviceNo)||'(设备无档案)',code:r.eventCodeText}));
console.log('近15分钟二级以上事件',rows.length);
for(const r of rows) console.log(' ',r.ts,'|',r.lv,'|',r.no,'|',r.dept,'|',r.code);
