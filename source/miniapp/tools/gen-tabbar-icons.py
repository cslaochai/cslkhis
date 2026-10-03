"""生成小程序 tabBar 的 8 张 PNG 图标（4 个入口 × 常态/选中）。

微信 tabBar 的 iconPath 不支持 SVG，只吃 PNG（<=40KB，建议 81x81），
所以这里用 Pillow 按 Lucide 的线条风格（24 网格、2 线宽、圆头圆角）自绘后降采样。

用法：python source/miniapp/tools/gen-tabbar-icons.py   （依赖 pillow）
"""
import os
from PIL import Image, ImageDraw

SS = 324          # 4x 超采样画布，缩到 81 后自带抗锯齿
OUT = 81
GRAY = (153, 153, 153, 255)     # 与 app.json tabBar.color 一致
BLUE = (43, 108, 176, 255)      # 与 selectedColor 一致
W = 24.0


def s(v):
    return v / W * SS


def stroke(d, pts):
    w = s(2.0)
    pts = [(s(x), s(y)) for x, y in pts]
    if len(pts) > 1:
        d.line(pts, fill=d.fill, width=int(round(w)), joint="curve")
    for x, y in pts:                       # 圆头端点
        d.ellipse([x - w / 2, y - w / 2, x + w / 2, y + w / 2], fill=d.fill)


def rrect(d, box, radius):
    d.rounded_rectangle([s(box[0]), s(box[1]), s(box[2]), s(box[3])],
                        radius=s(radius), outline=d.fill, width=int(round(s(2.0))))


def home(d):
    stroke(d, [(3, 10), (3.71, 8.47), (10.71, 2.47), (13.29, 2.47), (20.29, 8.47),
               (21, 10), (21, 19), (19, 21), (5, 21), (3, 19), (3, 10)])
    stroke(d, [(9, 21), (9, 13), (15, 13), (15, 21)])


def calendar(d):
    rrect(d, (3, 5, 21, 21), 2)
    stroke(d, [(3, 9.5), (21, 9.5)])
    stroke(d, [(8, 3), (8, 7)])
    stroke(d, [(16, 3), (16, 7)])


def chart(d):
    stroke(d, [(4, 3), (4, 20), (21, 20)])
    stroke(d, [(8.5, 20), (8.5, 13)])
    stroke(d, [(13, 20), (13, 8.5)])
    stroke(d, [(17.5, 20), (17.5, 15.5)])


def user(d):
    w = s(2.0)
    d.ellipse([s(7), s(3), s(17), s(13)], outline=d.fill, width=int(round(w)))
    d.arc([s(4), s(13), s(20), s(29)], 180, 360, fill=d.fill, width=int(round(w)))
    for x in (4, 20):
        d.ellipse([s(x) - w / 2, s(21) - w / 2, s(x) + w / 2, s(21) + w / 2], fill=d.fill)


SHAPES = {"home": home, "appointment": calendar, "report": chart, "profile": user}
DESTDIR = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)),
                                        "..", "images", "tabbar"))
os.makedirs(DESTDIR, exist_ok=True)

for name, fn in SHAPES.items():
    for suffix, color in (("", GRAY), ("-active", BLUE)):
        img = Image.new("RGBA", (SS, SS), (0, 0, 0, 0))
        dr = ImageDraw.Draw(img)
        dr.fill = color
        fn(dr)
        path = os.path.join(DESTDIR, name + suffix + ".png")
        img.resize((OUT, OUT), Image.LANCZOS).save(path, optimize=True)
        print(os.path.basename(path), os.path.getsize(path), "bytes")
