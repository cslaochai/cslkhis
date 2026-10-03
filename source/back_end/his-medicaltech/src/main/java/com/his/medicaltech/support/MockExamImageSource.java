package com.his.medicaltech.support;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * 模拟 DICOM 影像源（学习阶段不接真设备时的替身）。
 *
 * <p><b>为什么是「画」而不是塞一张静态图：</b>阅片器要验证的是多帧翻页、放大平移、
 * 窗宽窗位（CSS filter）三件事，一份所有帧长得一样的图根本测不出「翻到第 3 帧」，
 * 也看不出窗位有没有真的作用到像素上。所以这里按 (申请单, 帧号) 播种生成
 * 一批<b>互不相同</b>的灰阶层面：同一张申请单每次导入结果一致（可复现），
 * 不同申请单之间的解剖形态不同（不会看起来像同一病人）。
 *
 * <p>真接 PACS 时换掉本类的实现即可，表结构与接口不动。
 */
@Component
public class MockExamImageSource {

    private static final int SIZE = 512;
    private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 14);

    /**
     * 生成第 seq 帧的 PNG 字节。
     *
     * @param seedText 参与播种的文本（申请单号），决定「这个人的片子长什么样」
     */
    public byte[] frame(String seedText, int seq, int totalFrames, String modalityText,
                        String itemName, String patientName) throws IOException {
        long seed = (seedText == null ? "NA" : seedText).hashCode() * 31L + seq;
        Random rnd = new Random(seed);

        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, SIZE, SIZE);

        int cx = SIZE / 2;
        int cy = SIZE / 2 + 8;
        // 层越往头侧身体截面越小：用帧号把外轮廓收一点，翻页时能看出「在往上走」
        double shrink = 1.0 - 0.055 * (totalFrames <= 1 ? 0 : (double) (seq - 1) / (totalFrames - 1));
        int bodyRx = (int) (170 * shrink);
        int bodyRy = (int) (120 * shrink);

        g.setColor(new Color(96, 96, 96));
        g.fillOval(cx - bodyRx, cy - bodyRy, bodyRx * 2, bodyRy * 2);

        // 纵隔/器官：位置随帧漂移，保证相邻帧不相同
        for (int i = 0; i < 5; i++) {
            int rx = 26 + rnd.nextInt(46);
            int ox = cx - 90 + rnd.nextInt(180);
            int oy = cy - 60 + rnd.nextInt(120) + (seq * 7) % 24;
            int shade = 40 + rnd.nextInt(90);
            g.setColor(new Color(shade, shade, shade));
            g.fillOval(ox - rx / 2, oy - rx / 2, rx, rx);
        }
        // 高亮灶：只有部分帧出现，模拟病灶在层面上的出现/消失
        if (rnd.nextDouble() < 0.55) {
            int shade = 200 + rnd.nextInt(50);
            g.setColor(new Color(shade, shade, shade));
            int r = 10 + rnd.nextInt(14);
            g.fillOval(cx - r / 2 + rnd.nextInt(90) - 45, cy - r / 2 + rnd.nextInt(60) - 30, r, r);
        }
        // 伪影噪点
        for (int i = 0; i < 2600; i++) {
            int x = rnd.nextInt(SIZE);
            int y = rnd.nextInt(SIZE);
            int v = rnd.nextInt(46);
            g.setColor(new Color(v, v, v));
            g.fillRect(x, y, 1, 1);
        }

        drawOverlay(g, seq, totalFrames, modalityText, itemName, patientName);
        g.dispose();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return out.toByteArray();
    }

    private void drawOverlay(Graphics2D g, int seq, int totalFrames,
                             String modalityText, String itemName, String patientName) {
        g.setFont(LABEL_FONT);
        g.setColor(new Color(230, 230, 230));
        String head = "SIMULATED (非真实影像)";
        g.drawString(head, 12, 22);
        g.drawString("SEQ " + seq + "/" + totalFrames, 12, 42);
        if (modalityText != null) {
            g.drawString("MOD " + modalityText, 12, 62);
        }
        if (patientName != null) {
            g.drawString(cut(patientName, 12), SIZE - 150, 22);
        }
        if (itemName != null) {
            g.drawString(cut(itemName, 14), SIZE - 180, 42);
        }
        // 标尺：10cm 对应 100px，用来目测窗宽窗位是否生效
        g.setStroke(new BasicStroke(2f));
        g.drawLine(12, SIZE - 24, 112, SIZE - 24);
        g.drawLine(12, SIZE - 30, 12, SIZE - 18);
        g.drawLine(112, SIZE - 30, 112, SIZE - 18);
        g.drawString("10cm", 128, SIZE - 18);
    }

    private static String cut(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
