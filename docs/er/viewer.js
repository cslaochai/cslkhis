/* cslk_his E-R 连线图查看器：数据来自 index.html 内联的 DATA，与 PowerDesigner 脚本同源 */
(function () {
  'use strict';
  var $ = function (s) { return document.querySelector(s); };
  var stage = $('#stage'), canvas = $('#canvas'), side = $('#side'), status = $('#status');
  var view = { s: 1, x: 0, y: 0 };
  var state = { tab: 'domain', domain: DATA.domains[0].n, table: 'biz_patient', seq: 0, proven: false, allChildren: false, info: '' };

  mermaid.initialize({
    startOnLoad: false,
    securityLevel: 'loose',
    logLevel: 'fatal',
    theme: 'base',
    themeVariables: {
      fontFamily: '"Microsoft YaHei","PingFang SC",sans-serif',
      primaryColor: '#eef6fc', primaryBorderColor: '#1269B5', primaryTextColor: '#14303f',
      lineColor: '#7d99ab', edgeLabelBackground: '#ffffff', clusterBkg: '#f7fbfe', clusterBorder: '#cfe0ec',
    },
    flowchart: { useMaxWidth: false, htmlLabels: true, curve: 'linear', nodeSpacing: 40, rankSpacing: 90 },
    er: { useMaxWidth: false, minEntityWidth: 130, minEntityHeight: 55, entityPadding: 14, stroke: '#1269B5', fill: '#eef6fc' },
  });

  function apply() {
    canvas.style.transform = 'translate(' + view.x + 'px,' + view.y + 'px) scale(' + view.s + ')';
  }

  function fit() {
    var svg = canvas.querySelector('svg');
    if (!svg) return;
    var vb = svg.viewBox && svg.viewBox.baseVal;
    var w = (vb && vb.width) || svg.getBoundingClientRect().width / view.s;
    var h = (vb && vb.height) || svg.getBoundingClientRect().height / view.s;
    var k = Math.min((stage.clientWidth - 48) / w, (stage.clientHeight - 48) / h, 1.4);
    // 全域图动辄 3000+ 像素宽，真按比例缩到 12% 就没法读了：低于 5 成时按 5 成显示，靠拖动/双击看其余部分
    view.s = Math.max(k, 0.5);
    view.x = (stage.clientWidth - w * view.s) / 2;
    view.y = k < 0.5 ? 24 : (stage.clientHeight - h * view.s) / 2;
    apply();
    paint();
  }

  function zoomAt(cx, cy, k) {
    var r = stage.getBoundingClientRect();
    var px = cx - r.left, py = cy - r.top;
    var s2 = Math.min(4, Math.max(0.05, view.s * k));
    view.x = px - (px - view.x) * (s2 / view.s);
    view.y = py - (py - view.y) * (s2 / view.s);
    view.s = s2;
    apply(); paint();
  }

  function paint() { $('#reset').textContent = Math.round(view.s * 100) + '%'; }

  function legend(extra) {
    status.innerHTML = (extra || state.info) +
      '<span class="legend"><i>||--o{ 1:N 可空</i><i>||--|{ 1:N 必填</i><i>||--|| 1:1</i>' +
      '<span>A 数据全命中 · A* 人工口径 · B 部分命中 · C 有值全悬空 · D 无数据可验</span></span>';
  }

  function dom(n) { for (var i = 0; i < DATA.domains.length; i++) if (DATA.domains[i].n === n) return DATA.domains[i]; return null; }
  var DOMOFPK = {};
  DATA.tables.forEach(function (t) { DOMOFPK[t[0]] = t[2]; });
  function domOf(t) { return DOMOFPK[t] || 99; }

  function tblRow(t, cls) {
    var b = document.createElement('button');
    b.className = 'item ' + (cls || '');
    b.innerHTML = '<span class="nm">' + t[0] + '</span><span class="ct">' + (t[1] || '') + '</span>';
    return b;
  }

  function renderSideDomain() {
    side.innerHTML = '<div class="side-h">23 个领域（表数 / 关系数）</div>';
    DATA.domains.forEach(function (d) {
      var b = document.createElement('button');
      b.className = 'item' + (d.n === state.domain ? ' on' : '');
      b.innerHTML = '<span class="no">' + (d.n < 10 ? '0' + d.n : d.n) + '</span>' +
        '<span class="nm">' + d.title + '</span>' +
        '<span class="ct">' + d.tables + '表/' + (state.proven ? d.proven : d.edges) + '线</span>';
      b.onclick = function () { state.domain = d.n; renderSideDomain(); showDomain(); };
      side.appendChild(b);
    });
  }

  function neigh(t) {
    var up = DATA.edges.filter(function (e) { return e[0] === t; });
    var down = DATA.edges.filter(function (e) { return e[2] === t; });
    return { up: up, down: down };
  }

  function renderSideFocus() {
    side.innerHTML = '<div class="side-h">全部 302 张表（↑父 ↓子）</div>';
    DATA.tables.forEach(function (t) {
      var g = neigh(t[0]);
      var b = document.createElement('button');
      b.className = 'item' + (t[0] === state.table ? ' on' : '');
      b.innerHTML = '<span class="no">' + (t[2] < 10 ? '0' + t[2] : t[2]) + '</span>' +
        '<span class="nm"><b>' + (shortCn(t[0]) || '（无中文名）') + '</b><i>' + t[0] + '</i></span>' +
        '<span class="ct">↑' + g.up.length + ' ↓' + g.down.length + '</span>';
      b.onclick = function () { state.table = t[0]; renderSideFocus(); showFocus(); };
      side.appendChild(b);
    });
  }

  function entityLines(name, cols) {
    var out = ['  ' + name + aliasOf(name) + ' {'];
    cols.forEach(function (c) { out.push('    ' + c[0] + ' ' + c[1] + (c[2] ? ' "' + c[2] + '"' : '')); });
    out.push('  }');
    return out;
  }

  var CN = {};
  DATA.tables.forEach(function (t) { CN[t[0]] = t[1]; });
  function shortCn(n) {
    var c = (CN[n] || '').replace(/["\[\]{}]/g, ' ').trim();
    return c.length > 16 ? c.slice(0, 16) : c;
  }
  function aliasOf(n) { var c = shortCn(n); return c ? '["' + c + ' · ' + n + '"]' : ''; }

  function isKey(t, col) { return (DATA.pk[t] || []).indexOf(col) >= 0; }

  var CAP = 24;
  function focusText(t) {
    var g = neigh(t);
    var shown = state.allChildren ? g.down : g.down.slice(0, CAP);
    var set = {}; set[t] = 1;
    g.up.forEach(function (e) { set[e[2]] = 1; });
    shown.forEach(function (e) { set[e[0]] = 1; });
    var names = Object.keys(set).sort(function (a, b) { return domOf(a) - domOf(b) || a.localeCompare(b); });
    var lines = ['erDiagram'];
    names.forEach(function (x) {
      var cols;
      if (x === t) {
        cols = DATA.keycols[x].slice(0, 40);
      } else {
        cols = DATA.keycols[x].filter(function (c) {
          return isKey(x, c[1]) || g.up.some(function (e) { return e[2] === x && c[1] === e[1]; }) ||
            shown.some(function (e) { return e[0] === x && c[1] === e[1]; });
        }).slice(0, 24);
        if (!cols.length) cols = DATA.keycols[x].filter(function (c) { return isKey(x, c[1]); });
      }
      entityLines(x, cols).forEach(function (l) { lines.push(l); });
    });
    g.up.forEach(function (e) { lines.push('  ' + e[2] + ' ' + e[3] + ' ' + e[0] + ' : "' + e[1] + ' [' + e[4] + ']"'); });
    shown.forEach(function (e) { lines.push('  ' + e[2] + ' ' + e[3] + ' ' + e[0] + ' : "' + e[1] + ' [' + e[4] + ']"'); });
    return { text: lines.join('\n'), ent: names.length, rel: g.up.length + shown.length, cut: g.down.length - shown.length };
  }

  var token = 0;
  function draw(text, after) {
    var my = ++token;
    var id = 'er-' + (++state.seq);
    mermaid.render(id, text).then(function (r) {
      if (my !== token) return;
      canvas.innerHTML = r.svg;
      after && after();
      fit();
    }, function (e) {
      if (my !== token) return;
      canvas.innerHTML = '<div class="err">渲染失败：' + (e && e.message ? e.message : e) + '</div>';
      view = { s: 1, x: 20, y: 20 }; apply();
    });
  }

  function showDomain() {
    var d = dom(state.domain);
    var src = state.proven ? d.kept : d.all;
    if (!src) { canvas.innerHTML = '<div class="err">该域没有可画的关系</div>'; return; }
    state.info = '<b>' + (d.n < 10 ? '0' + d.n : d.n) + ' ' + d.title + '</b> · 本域 ' + d.tables +
      ' 表 + 上游参照 ' + d.ctx + ' 表 · ' + (state.proven ? d.proven : d.edges) + ' 条连线' +
      (state.proven ? '（已隐藏 ' + (d.edges - d.proven) + ' 条 C/D 级）' : '');
    legend();
    draw(src);
  }

  function showOverview() {
    state.info = '<b>全域关系总览</b> · 23 个领域之间 ' + DATA.ovFlows +
      ' 条连线（只显示 ≥3 条的域间关系，节点里的「内部」= 同域自环条数）';
    legend();
    draw(DATA.overview);
  }

  function showFocus() {
    var r = focusText(state.table);
    var poly = DATA.poly.filter(function (p) { return p[0] === state.table; });
    var g = neigh(state.table);
    state.info = '<b>' + (shortCn(state.table) || '（无中文名）') + '</b> ' + state.table +
      ' · 父表 ' + g.up.length + ' 个 / 子表 ' + g.down.length + ' 个 · 图上 ' + r.ent + ' 实体 ' + r.rel + ' 连线' +
      (r.cut > 0 ? '（子表按域序只显示前 ' + CAP + ' 张，还有 ' + r.cut + ' 张未画 → 勾选「展开全部子表」）' : '') +
      (poly.length ? ' · 多态列 ' + poly.map(function (p) { return p[1]; }).join(',') + '（不画线）' : '');
    legend();
    draw(r.text, function () { locate(state.table); });
  }

  function locate(name) {
    var nodes = canvas.querySelectorAll('text, tspan');
    for (var i = 0; i < nodes.length; i++) {
      var txt = (nodes[i].textContent || '').trim();
      if (txt !== name && txt.indexOf('· ' + name) < 0) continue;
      var g = nodes[i].closest('g') || nodes[i];
      g.classList.add('flash');
      var er = canvas.querySelector('svg').getBoundingClientRect();
      var nr = g.getBoundingClientRect();
      view.x += stage.clientWidth / 2 - (nr.left + nr.width / 2);
      view.y += stage.clientHeight / 2 - (nr.top + nr.height / 2);
      apply(); paint();
      setTimeout(function () { g.classList.remove('flash'); }, 2600);
      void er;
      return;
    }
  }

  function setTab(tab) {
    state.tab = tab;
    document.querySelectorAll('.tabs button').forEach(function (b) {
      b.classList.toggle('on', b.dataset.tab === tab);
    });
    $('#onlyProven').parentElement.style.display = tab === 'overview' ? 'none' : '';
    $('#allChildWrap').style.display = tab === 'focus' ? '' : 'none';
    if (tab === 'domain') { renderSideDomain(); showDomain(); }
    else if (tab === 'overview') { side.innerHTML = '<div class="side-h">阅读顺序：按患者流转主线 01→23</div>' +
      DATA.domains.map(function (d) {
        return '<div class="item"><span class="no">' + (d.n < 10 ? '0' + d.n : d.n) + '</span><span class="nm">' + d.title + '</span><span class="ct">' + d.tables + '表</span></div>';
      }).join(''); showOverview(); }
    else { renderSideFocus(); showFocus(); }
  }

  document.querySelectorAll('.tabs button').forEach(function (b) {
    b.onclick = function () { setTab(b.dataset.tab); };
  });
  $('#onlyProven').onchange = function (e) {
    state.proven = e.target.checked;
    if (state.tab === 'domain') { renderSideDomain(); showDomain(); }
  };
  $('#allChildren').onchange = function (e) {
    state.allChildren = e.target.checked;
    if (state.tab === 'focus') showFocus();
  };
  $('#fit').onclick = fit;
  $('#zin').onclick = function () { var r = stage.getBoundingClientRect(); zoomAt(r.left + r.width / 2, r.top + r.height / 2, 1.25); };
  $('#zout').onclick = function () { var r = stage.getBoundingClientRect(); zoomAt(r.left + r.width / 2, r.top + r.height / 2, 0.8); };
  $('#reset').onclick = function () { view.s = 1; view.x = 20; view.y = 20; apply(); paint(); };

  stage.addEventListener('wheel', function (e) { e.preventDefault(); zoomAt(e.clientX, e.clientY, e.deltaY < 0 ? 1.12 : 0.89); }, { passive: false });
  stage.addEventListener('dblclick', function (e) { e.preventDefault(); zoomAt(e.clientX, e.clientY, e.shiftKey ? 0.62 : 1.7); });
  var drag = null;
  stage.addEventListener('pointerdown', function (e) { drag = { x: e.clientX, y: e.clientY, vx: view.x, vy: view.y }; stage.classList.add('drag'); stage.setPointerCapture(e.pointerId); });
  stage.addEventListener('pointermove', function (e) { if (!drag) return; view.x = drag.vx + e.clientX - drag.x; view.y = drag.vy + e.clientY - drag.y; apply(); });
  stage.addEventListener('pointerup', function () { drag = null; stage.classList.remove('drag'); });
  window.addEventListener('resize', function () { if (canvas.querySelector('svg')) fit(); });

  var dl = $('#tlist');
  DATA.tables.forEach(function (t) {
    var o = document.createElement('option');
    o.value = t[0]; o.label = t[1];
    dl.appendChild(o);
  });
  var box = $('#search');
  box.type = 'text';
  function jump() {
    var v = (box.value || '').trim().toLowerCase();
    if (!v) return;
    var hit = DATA.tables.filter(function (t) { return t[0] === v; })[0] ||
      DATA.tables.filter(function (t) { return t[0].indexOf(v) === 0; })[0] ||
      DATA.tables.filter(function (t) { return t[0].indexOf(v) > 0; })[0];
    if (!hit) { state.info = '没有匹配的表：' + v; legend(); return; }
    state.table = hit[0];
    setTab('focus');
  }
  box.addEventListener('keydown', function (e) { if (e.key === 'Enter') jump(); });
  box.addEventListener('change', jump);

  setTab('domain');
  legend();
})();
