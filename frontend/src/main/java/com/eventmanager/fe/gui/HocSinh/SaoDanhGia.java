package com.eventmanager.fe.gui.HocSinh;

import com.eventmanager.fe.gui.Admin.common.BangDep;
import com.eventmanager.fe.gui.Admin.common.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.function.IntConsumer;

/**
 * 5 ngôi sao đánh giá.
 *  - choSua = true : rê chuột để xem trước, bấm để chọn số sao (dùng trong form đánh giá)
 *  - choSua = false: chỉ hiển thị (dùng trong bảng, chi tiết sự kiện)
 */
public class SaoDanhGia extends JComponent {

    public static final Color VANG = new Color(0xF5B301);
    public static final Color XAM = new Color(0xD6DCE5);
    private static final String[] MO_TA = {"Chưa chọn", "Rất tệ", "Chưa hài lòng", "Bình thường", "Hài lòng", "Rất hài lòng"};

    private final int co;
    private final int khoangCach;
    private int giaTri;
    private int hover;
    private IntConsumer khiDoi;

    public SaoDanhGia(int co, boolean choSua) {
        this.co = co;
        this.khoangCach = Math.max(2, co / 5);
        setOpaque(false);
        if (choSua) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            MouseAdapter chuot = new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    hover = saoTaiDiem(e.getX());
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = 0;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    setGiaTri(saoTaiDiem(e.getX()));
                }
            };
            addMouseListener(chuot);
            addMouseMotionListener(chuot);
        }
    }

    public int getGiaTri() {
        return giaTri;
    }

    public void setGiaTri(int giaTri) {
        this.giaTri = Math.max(0, Math.min(5, giaTri));
        repaint();
        if (khiDoi != null) khiDoi.accept(this.giaTri);
    }

    public void setKhiDoi(IntConsumer khiDoi) {
        this.khiDoi = khiDoi;
    }

    public static String moTa(int soSao) {
        return MO_TA[Math.max(0, Math.min(5, soSao))];
    }

    private int saoTaiDiem(int x) {
        return Math.max(1, Math.min(5, x / (co + khoangCach) + 1));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(5 * co + 4 * khoangCach, co + 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Theme.muot(g);
        int hien = hover > 0 ? hover : giaTri;
        int y = (getHeight() - co) / 2;
        for (int i = 0; i < 5; i++) {
            g2.setColor(i < hien ? VANG : XAM);
            g2.fill(ngoiSao(i * (co + khoangCach), y, co));
        }
        g2.dispose();
    }

    /** Ngôi sao 5 cánh nằm gọn trong ô vuông (x, y, size). */
    private static Shape ngoiSao(double x, double y, double size) {
        double cx = x + size / 2, cy = y + size / 2 + size * 0.05;
        double rNgoai = size / 2, rTrong = rNgoai * 0.45;
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double r = i % 2 == 0 ? rNgoai : rTrong;
            double goc = -Math.PI / 2 + i * Math.PI / 5;
            double px = cx + r * Math.cos(goc), py = cy + r * Math.sin(goc);
            if (i == 0) p.moveTo(px, py);
            else p.lineTo(px, py);
        }
        p.closePath();
        return p;
    }

    /** Cột trong bảng hiển thị số sao (giá trị ô là số 1-5). */
    public static void cotSao(BangDep bang, int cot) {
        bang.getColumnModel().getColumn(cot).setCellRenderer((t, v, chon, f, r, c) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 15));
            p.setBackground(bang.nenDong(r, chon));
            SaoDanhGia s = new SaoDanhGia(15, false);
            s.giaTri = v instanceof Number n ? n.intValue() : 0;
            p.add(s);
            return p;
        });
    }
}
