#!/usr/bin/env node
/**
 * 斗地主扑克牌 SVG 资源生成器
 *
 * 用法：  node scripts/gen-cards.mjs
 * 输出：  public/cards/*.svg   （54 张牌 + back.svg 牌背）
 *         public/cards/preview.html （本地预览页，dev 起来后访问 /cards/preview.html）
 *
 * 命名规则与后端 Card#code 完全一致，前端拼 URL 时无需任何大小写转换：
 *   普通牌 = 花色字母 + 点数       S3  H10  C2  D2
 *   大小王 = JOKER_ + S/B         JOKER_S（小王）  JOKER_B（大王）
 * 花色字母：S=♠黑桃  H=♥红桃  C=♣梅花  D=♦方块
 *
 * 设计约束：
 *   - 纯矢量、无滤镜、无渐变、无外部字体依赖（Georgia/Times 缺失时回落 serif 仍能看）
 *   - 单文件 < 3 KB，60 张牌同屏时不会拖慢渲染（对应立项书风险 R6）
 *   - 颜色取自 src/style.css 的 --color-card-red / --color-card-ink
 */
import { mkdir, writeFile, readdir } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const HERE = path.dirname(fileURLToPath(import.meta.url))
const OUT_DIR = path.resolve(HERE, '../public/cards')

/* ------------------------------------------------------------------ 基础常量 */

const W = 120
const H = 168
const RED = '#d92b2b'
const INK = '#1f2937'
const BORDER = '#c9ced8'
const SERIF = "Georgia,'Times New Roman',serif"

/** 只保留两位小数，避免 SVG 里出现一长串浮点 */
const n = (v) => Number(v.toFixed(2))

/* ------------------------------------------------------ 花色矢量（100×100 盒） */

const SUIT_GEOM = {
  // ♠ 尖朝上，下方带短柄
  S: '<path d="M50 4c6 10 42 30 42 54 0 14-10 24-22 24-6 0-12-3-16-8 1 8 4 16 10 22H36c6-6 9-14 10-22-4 5-10 8-16 8C18 82 8 72 8 58 8 34 44 14 50 4Z"/>',
  // ♥ 双瓣朝上，尖端朝下
  H: '<path d="M50 92S6 62 6 34C6 18 18 8 32 8c9 0 15 4 18 10 3-6 9-10 18-10 14 0 26 10 26 26 0 28-44 58-44 58Z"/>',
  // ♦ 菱形
  D: '<path d="M50 4 92 50 50 96 8 50Z"/>',
  // ♣ 三圆一柄（同色填充后视觉上是一个整体）
  C: '<circle cx="50" cy="27" r="19"/><circle cx="27" cy="61" r="19"/><circle cx="73" cy="61" r="19"/><path d="M50 45c4 16 7 32 11 47H39c4-15 7-31 11-47Z"/>',
}

/**
 * 在 (x,y) 处画一个尺寸为 size 的花色。
 * 花色坐标系是 100×100，中心在 (50,50)，所以先平移到左上角再缩放。
 */
function pip(suit, x, y, size, color) {
  const k = n(size / 100)
  const tx = n(x - size / 2)
  const ty = n(y - size / 2)
  return `<g transform="translate(${tx} ${ty}) scale(${k})" fill="${color}">${SUIT_GEOM[suit]}</g>`
}

/* -------------------------------------------------------------- 点数与布局 */

/** 斗地主点数顺序：3 最小，2 最大（王单独处理） */
const RANKS = ['3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K', 'A', '2']

const COL_L = 46
const COL_R = 74
const ROW3 = [38, 84, 130]
const ROW4 = [34, 66, 102, 134]
const ROW5 = [30, 57, 84, 111, 138]

/**
 * 中间花色区布局：[x, y, size]，y > 84 的点会被旋转 180°（模拟真牌下半部分的倒花）
 * 数值都经过手工校对，保证任何两张花色之间至少留 1px 间隙。
 */
const PIPS = {
  2: [[60, 44, 34], [60, 124, 34]],
  3: [[60, 42, 30], [60, 84, 30], [60, 126, 30]],
  4: [[COL_L, 42, 30], [COL_R, 42, 30], [COL_L, 126, 30], [COL_R, 126, 30]],
  5: [[COL_L, 42, 30], [COL_R, 42, 30], [60, 84, 30], [COL_L, 126, 30], [COL_R, 126, 30]],
  6: [...ROW3.flatMap((y) => [[COL_L, y, 24], [COL_R, y, 24]])],
  7: [...ROW3.flatMap((y) => [[COL_L, y, 24], [COL_R, y, 24]]), [60, 61, 20]],
  8: [...ROW3.flatMap((y) => [[COL_L, y, 24], [COL_R, y, 24]]), [60, 61, 20], [60, 107, 20]],
  9: [...ROW4.flatMap((y) => [[COL_L, y, 20], [COL_R, y, 20]]), [60, 84, 16]],
  10: [...ROW5.flatMap((y) => [[COL_L, y, 18], [COL_R, y, 18]])],
  A: [[60, 84, 54]],
}

/* ---------------------------------------------------------------- 绘制片段 */

/** 左上角点数 + 花色（右下角由同一个片段旋转 180° 得到） */
function cornerIndex(rank, suit, color) {
  const isTen = rank === '10'
  const fs = isTen ? 20 : 25
  return [
    `<text x="17" y="26" text-anchor="middle" font-family="${SERIF}" font-size="${fs}" font-weight="700" fill="${color}">${rank}</text>`,
    pip(suit, 17, 41, 15, color),
  ].join('')
}

/** 中间花色区 */
function centerPips(suit, color, layout) {
  return layout
    .map(([x, y, size]) => {
      const glyph = pip(suit, x, y, size, color)
      // 下半部分倒过来，和真牌一致
      return y > 84 ? `<g transform="rotate(180 ${x} ${y})">${glyph}</g>` : glyph
    })
    .join('')
}

/** J / Q / K：内框 + 大字母 + 上下小花色 */
function faceCenter(rank, suit, color) {
  return [
    `<rect x="36" y="36" width="48" height="96" rx="6" fill="${color}" fill-opacity="0.06" stroke="${color}" stroke-opacity="0.28" stroke-width="1"/>`,
    pip(suit, 60, 50, 15, color),
    `<text x="60" y="92" text-anchor="middle" font-family="${SERIF}" font-size="44" font-weight="700" fill="${color}">${rank}</text>`,
    pip(suit, 60, 116, 15, color),
  ].join('')
}

/* ------------------------------------------------------------------ 卡面 */

function cardFrame(fill = '#ffffff') {
  // 描边以矩形边线为中心，故内缩半个线宽，避免被 viewBox 裁掉
  return `<rect x="0.8" y="0.8" width="${n(W - 1.6)}" height="${n(H - 1.6)}" rx="9" fill="${fill}" stroke="${BORDER}" stroke-width="1.6"/>`
}

function normalCard(rank, suit, color) {
  let center
  if (rank === 'J' || rank === 'Q' || rank === 'K') {
    center = faceCenter(rank, suit, color)
  } else {
    center = centerPips(suit, color, PIPS[rank])
  }

  return svg(
    cardFrame() +
      center +
      cornerIndex(rank, suit, color) +
      // 右下角：整体旋转 180°，省掉一份重复的属性
      `<g transform="translate(${W} ${H}) rotate(180)">${cornerIndex(rank, suit, color)}</g>`,
  )
}

/** 大小王：淡底 + 双线内框 + 竖排 JOKER + 大「王」字 */
function jokerCard(isBig) {
  const color = isBig ? RED : INK
  const bg = isBig ? '#fff6f6' : '#f7f8fa'
  const label = isBig ? '大' : '小'

  return svg(
    cardFrame(bg) +
      `<rect x="5" y="5" width="${W - 10}" height="${H - 10}" rx="6" fill="none" stroke="${color}" stroke-opacity="0.22" stroke-width="1"/>` +
      `<text x="13" y="108" text-anchor="middle" transform="rotate(-90 13 108)" font-family="${SERIF}" font-size="10" letter-spacing="2.5" fill="${color}" fill-opacity="0.55">JOKER</text>` +
      `<text x="64" y="64" text-anchor="middle" font-family="${SERIF}" font-size="22" font-weight="700" fill="${color}" fill-opacity="0.75">${label}</text>` +
      `<text x="64" y="112" text-anchor="middle" font-family="'Microsoft YaHei','PingFang SC',serif" font-size="48" font-weight="700" fill="${color}">王</text>` +
      // 角标：小王用空心星、大王用实心星，缩小后也能一眼分辨
      `<text x="17" y="27" text-anchor="middle" font-size="20" fill="${color}">${isBig ? '★' : '☆'}</text>` +
      `<g transform="translate(${W} ${H}) rotate(180)"><text x="17" y="27" text-anchor="middle" font-size="20" fill="${color}">${isBig ? '★' : '☆'}</text></g>`,
  )
}

/** 牌背：红底 + 斜格纹 + 中央「斗」字菱形 */
function backCard() {
  return svg(
    `<rect x="0.8" y="0.8" width="${n(W - 1.6)}" height="${n(H - 1.6)}" rx="9" fill="#b91c2c" stroke="#8f1523" stroke-width="1.6"/>` +
      `<rect x="6" y="6" width="${W - 12}" height="${H - 12}" rx="6" fill="none" stroke="#ffffff" stroke-opacity="0.55" stroke-width="1.2"/>` +
      `<g stroke="#ffffff" stroke-opacity="0.16" stroke-width="1.4">` +
      Array.from({ length: 11 }, (_, i) => {
        const d = -80 + i * 22
        return `<path d="M${d} 0 L${d + 120} 168"/><path d="M${d + 120} 0 L${d} 168"/>`
      }).join('') +
      `</g>` +
      `<rect x="6" y="6" width="${W - 12}" height="${H - 12}" rx="6" fill="#b91c2c" fill-opacity="0.72"/>` +
      `<rect x="6" y="6" width="${W - 12}" height="${H - 12}" rx="6" fill="none" stroke="#ffffff" stroke-opacity="0.55" stroke-width="1.2"/>` +
      `<path d="M60 46 92 84 60 122 28 84Z" fill="#ffffff" fill-opacity="0.92"/>` +
      `<text x="60" y="99" text-anchor="middle" font-family="'Microsoft YaHei','PingFang SC',serif" font-size="30" font-weight="700" fill="#b91c2c">斗</text>`,
  )
}

function svg(inner) {
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${W} ${H}" width="${W}" height="${H}">${inner}</svg>\n`
}

/* ------------------------------------------------------------------ 生成 */

const SUITS = [
  { code: 'S', color: INK },
  { code: 'H', color: RED },
  { code: 'C', color: INK },
  { code: 'D', color: RED },
]

async function main() {
  await mkdir(OUT_DIR, { recursive: true })

  /** @type {{code:string, svg:string}[]} */
  const cards = []

  for (const rank of RANKS) {
    for (const suit of SUITS) {
      cards.push({ code: `${suit.code}${rank}`, svg: normalCard(rank, suit.code, suit.color) })
    }
  }
  cards.push({ code: 'JOKER_S', svg: jokerCard(false) })
  cards.push({ code: 'JOKER_B', svg: jokerCard(true) })

  for (const { code, svg: content } of cards) {
    await writeFile(path.join(OUT_DIR, `${code}.svg`), content, 'utf8')
  }
  await writeFile(path.join(OUT_DIR, 'back.svg'), backCard(), 'utf8')
  await writeFile(path.join(OUT_DIR, 'preview.html'), previewHTML(cards), 'utf8')

  const files = await readdir(OUT_DIR)
  const svgs = files.filter((f) => f.endsWith('.svg'))
  const total = cards.reduce((s, c) => s + Buffer.byteLength(c.svg), 0)
  console.log(`✔ 输出目录 ${OUT_DIR}`)
  console.log(`✔ 卡牌 SVG ${cards.length} 个 + 牌背 1 个（目录内 .svg 共 ${svgs.length} 个）`)
  console.log(`✔ 卡面总体积 ${(total / 1024).toFixed(1)} KB，平均 ${(total / cards.length / 1024 * 1024).toFixed(0)} B/张`)
}

/* ---------------------------------------------------------------- 预览页 */

function previewHTML(cards) {
  const item = (code, label) =>
    `<figure class="item"><img src="./${code}.svg" alt="${label}" loading="lazy"><figcaption>${code}</figcaption></figure>`

  const list = cards
    .map((c) => item(c.code, c.code === 'JOKER_S' ? '小王' : c.code === 'JOKER_B' ? '大王' : c.code))
    .join('')

  return `<!doctype html>
<html lang="zh-CN">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>斗地主 · 54 张卡牌预览</title>
<style>
  :root { --ink:#1f2937; --muted:#6b7280; --line:#e5e7eb; }
  * { box-sizing: border-box; }
  body { margin:0; padding:28px 32px 64px; background:#f5f6f8; color:var(--ink);
         font-family: system-ui,-apple-system,"Segoe UI","Microsoft YaHei",sans-serif; }
  h1 { font-size:19px; margin:0 0 4px; font-weight:700; }
  .sub { font-size:13px; color:var(--muted); margin-bottom:22px; }
  .bar { position:sticky; top:0; z-index:5; display:flex; align-items:center; gap:14px;
         padding:12px 0 14px; margin-bottom:6px; background:linear-gradient(#f5f6f8 72%,rgba(245,246,248,0));
         font-size:13px; color:var(--muted); }
  input[type=range] { width:220px; accent-color:#d92b2b; }
  .grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(calc(var(--w) * 1px + 16px),max-content)); gap:14px 12px; }
  .item { margin:0; display:flex; flex-direction:column; align-items:center; gap:6px; }
  .item img { width:calc(var(--w) * 1px); height:auto; display:block;
              border-radius:9px; background:#fff; box-shadow:0 1px 2px rgba(16,24,40,.10),0 4px 10px rgba(16,24,40,.06); }
  figcaption { font-size:10px; color:var(--muted); font-variant-numeric:tabular-nums; }
  .split { grid-column:1/-1; height:1px; background:var(--line); margin:10px 0 4px; }
</style>
</head>
<body>
  <h1>斗地主 · 卡牌资源预览</h1>
  <div class="sub">共 54 张（52 普通牌 + 大小王），另含 <code>back.svg</code> 牌背。拖动滑块可检查小尺寸下的可辨识度。</div>
  <div class="bar"><span>卡牌宽度</span><input id="w" type="range" min="40" max="180" value="120"><span id="wv">120 px</span></div>
  <div class="grid" id="grid">${list}${item('back', '牌背')}</div>
  <script>
    const r = document.getElementById('w'), v = document.getElementById('wv'), g = document.getElementById('grid')
    g.style.setProperty('--w', r.value)
    r.addEventListener('input', () => { g.style.setProperty('--w', r.value); v.textContent = r.value + ' px' })
  </script>
</body>
</html>
`
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
