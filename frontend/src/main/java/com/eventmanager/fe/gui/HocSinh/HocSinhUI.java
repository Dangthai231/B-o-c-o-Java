package com.eventmanager.fe.gui.HocSinh;

import com.eventmanager.fe.gui.Admin.common.BangDep;
import com.eventmanager.fe.gui.Admin.common.Theme;
import com.eventmanager.fe.gui.Admin.common.UI;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Các hàm dùng chung cho phần Sinh viên: đọc ngày giờ, nhãn trạng thái, thẻ có tiêu đề.
 * Màu sắc, font, nút bấm... dùng chung bộ Theme / UI của nhóm (gói Admin.common).
 */
public final class HocSinhUI {

    private static final DateTimeFormatter FMT_THOI_GIAN = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FMT_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private HocSinhUI() {
    }

    // ---------------- Ngày giờ ----------------

    public static LocalDateTime docThoiGian(String iso) {
        try {
            return iso == null || iso.isEmpty() ? null : LocalDateTime.parse(iso);
        } catch (RuntimeException e) {
            return null;
        }
    }

    // "2026-11-05T08:00:00" -> "05/11/2026 08:00"
    public static String thoiGian(String iso) {
        LocalDateTime t = docThoiGian(iso);
        return t == null ? (iso == null ? "" : iso) : t.format(FMT_THOI_GIAN);
    }

    // "2004-03-12" -> "12/03/2004"
    public static String ngay(String iso) {
        try {
            return iso == null || iso.isEmpty() ? "" : LocalDate.parse(iso).format(FMT_NGAY);
        } catch (RuntimeException e) {
            return iso;
        }
    }

    public static boolean daBatDau(String iso) {
        LocalDateTime t = docThoiGian(iso);
        return t != null && t.isBefore(LocalDateTime.now());
    }

    // "5 phút trước", "3 ngày trước", quá 30 ngày thì ghi ngày
    public static String truoc(String iso) {
        LocalDateTime t = docThoiGian(iso);
        if (t == null) return "";
        long phut = Duration.between(t, LocalDateTime.now()).toMinutes();
        if (phut < 1) return "Vừa xong";
        if (phut < 60) return phut + " phút trước";
        if (phut < 60 * 24) return (phut / 60) + " giờ trước";
        if (phut < 60 * 24 * 30) return (phut / 60 / 24) + " ngày trước";
        return t.format(FMT_NGAY);
    }

    public static String isoNgay(Date d) {
        return d == null ? null : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
    }

    public static Date tuIsoNgay(String iso) {
        try {
            return iso == null || iso.isEmpty() ? null
                    : Date.from(LocalDate.parse(iso).atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Ô chọn ngày (lịch) cao bằng các ô nhập khác. */
    public static JDateChooser oChonNgay() {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");
        dc.setFont(Theme.BODY);
        dc.setPreferredSize(new Dimension(150, 38));
        dc.getDateEditor().getUiComponent().setFont(Theme.BODY);
        return dc;
    }

    // ---------------- Nhãn trạng thái ----------------

    /** {màu chữ, màu nền} cho các trạng thái của sinh viên; trạng thái khác dùng bảng màu chung. */
    public static Color[] mau(String s) {
        return switch (s == null ? "" : s) {
            case "Đã đăng ký", "Sắp diễn ra" -> new Color[]{Theme.PRIMARY, Theme.PRIMARY_SOFT};
            case "Đã tham gia", "Đang diễn ra" -> new Color[]{Theme.SUCCESS, Theme.SUCCESS_SOFT};
            case "Vắng mặt", "Đã hủy" -> new Color[]{Theme.DANGER, Theme.DANGER_SOFT};
            case "Chờ đánh giá" -> new Color[]{Theme.WARNING, Theme.WARNING_SOFT};
            case "Đã kết thúc", "Chưa đăng ký", "" -> new Color[]{Theme.TEXT_2, Theme.GRAY_SOFT};
            default -> UI.mauTrangThai(s);
        };
    }

    /** Viên thuốc (pill) có chấm tròn và chữ trạng thái. */
    public static class Nhan extends JComponent {
        private String chu = "";

        public Nhan(String chu) {
            setFont(Theme.SMALL_BOLD);
            setChu(chu);
        }

        public void setChu(String chu) {
            this.chu = chu == null ? "" : chu;
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(chu.isEmpty() ? 0 : fm.stringWidth(chu) + 32, 24);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (chu.isEmpty()) return;
            Graphics2D g2 = Theme.muot(g);
            Color[] m = mau(chu);
            Dimension d = getPreferredSize();
            int y = (getHeight() - d.height) / 2;
            g2.setColor(m[1]);
            g2.fill(new RoundRectangle2D.Double(0, y, d.width, d.height, d.height, d.height));
            g2.setColor(m[0]);
            g2.fillOval(10, y + d.height / 2 - 3, 6, 6);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(chu, 22, y + (d.height - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Cột trong bảng hiển thị nhãn trạng thái theo màu của phần Sinh viên. */
    public static void cotNhan(BangDep bang, int cot) {
        bang.getColumnModel().getColumn(cot).setMinWidth(118);
        bang.getColumnModel().getColumn(cot).setCellRenderer((t, v, chon, f, r, c) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 11));
            p.setBackground(bang.nenDong(r, chon));
            p.add(new Nhan(v == null ? "" : v.toString()));
            return p;
        });
    }

    // ---------------- Thẻ có tiêu đề ----------------

    /** Thẻ trắng bo góc: tiêu đề (và nút phụ bên phải nếu có) ở trên, nội dung ở dưới. */
    public static UI.RoundPanel the(String tieuDe, JComponent benPhai, JComponent noiDung) {
        UI.RoundPanel the = UI.card(new BorderLayout(0, 12), 0);
        the.setBorder(new EmptyBorder(16, 18, 16, 18));
        JPanel dau = new JPanel(new BorderLayout());
        dau.setOpaque(false);
        dau.add(UI.nhan(tieuDe, Theme.H2, Theme.TEXT), BorderLayout.WEST);
        if (benPhai != null) dau.add(benPhai, BorderLayout.EAST);
        the.add(dau, BorderLayout.NORTH);
        the.add(noiDung, BorderLayout.CENTER);
        return the;
    }

    /** Dòng chữ mờ ở giữa khi danh sách trống. */
    public static JComponent trong(String chu) {
        JLabel l = UI.nhan(chu, Theme.BODY, Theme.MUTED);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        l.setBorder(new EmptyBorder(24, 0, 24, 0));
        return l;
    }
}