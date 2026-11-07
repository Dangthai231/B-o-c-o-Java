package com.eventmanager.fe.gui.Admin.common;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * BẢNG MÀU + FONT của toàn bộ phần Admin (lấy theo ảnh thiết kế).
 * Muốn đổi màu cả hệ thống chỉ cần sửa ở đây.
 */
public final class Theme {

    private Theme() {
    }

    // ---------- Màu nền & khung ----------
    public static final Color SIDEBAR = new Color(0x0B2B5C);        // xanh navy thanh bên trái
    public static final Color SIDEBAR_ACTIVE = new Color(0x1F5CB8); // mục đang chọn
    public static final Color SIDEBAR_HOVER = new Color(0x163E7A);
    public static final Color SIDEBAR_TEXT = new Color(0xD5E0F2);
    public static final Color SIDEBAR_LINE = new Color(0x22457A);

    public static final Color BG = new Color(0xF3F5F9);             // nền vùng nội dung
    public static final Color CARD = Color.WHITE;
    public static final Color BORDER = new Color(0xE3E8EF);
    public static final Color ROW_HOVER = new Color(0xF5F8FD);
    public static final Color TABLE_HEAD = new Color(0xF7F9FC);

    // ---------- Màu chữ ----------
    public static final Color TEXT = new Color(0x14284B);
    public static final Color TEXT_2 = new Color(0x3C4D69);
    public static final Color MUTED = new Color(0x7A889E);

    // ---------- Màu nhấn ----------
    public static final Color PRIMARY = new Color(0x1565D8);
    public static final Color PRIMARY_DARK = new Color(0x0F52B5);
    public static final Color PRIMARY_SOFT = new Color(0xE7F0FD);

    public static final Color SUCCESS = new Color(0x1E9E5A);
    public static final Color SUCCESS_SOFT = new Color(0xE4F6EB);
    public static final Color DANGER = new Color(0xD93A3A);
    public static final Color DANGER_SOFT = new Color(0xFDEAEA);
    public static final Color WARNING = new Color(0xE0920F);
    public static final Color WARNING_SOFT = new Color(0xFDF2DD);
    public static final Color PURPLE = new Color(0x6B4FD8);
    public static final Color PURPLE_SOFT = new Color(0xEEEAFD);
    public static final Color GRAY_SOFT = new Color(0xEEF1F5);

    // ---------- Font: ưu tiên Segoe UI (Windows), máy khác thì dùng font có sẵn ----------
    public static final String FONT_NAME = chonFont("Segoe UI", "Inter", "Roboto", "Noto Sans", "DejaVu Sans");

    public static Font font(int style, float size) {
        return new Font(FONT_NAME, style, Math.round(size));
    }

    public static final Font H1 = font(Font.BOLD, 24);      // tiêu đề trang
    public static final Font H2 = font(Font.BOLD, 16);      // tiêu đề khối
    public static final Font BODY = font(Font.PLAIN, 13);
    public static final Font BODY_BOLD = font(Font.BOLD, 13);
    public static final Font SMALL = font(Font.PLAIN, 12);
    public static final Font SMALL_BOLD = font(Font.BOLD, 12);
    public static final Font NUMBER = font(Font.BOLD, 24);  // số to trên thẻ thống kê

    private static String chonFont(String... ten) {
        List<String> coSan = Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String t : ten) {
            if (coSan.contains(t)) return t;
        }
        return Font.SANS_SERIF;
    }

    /** Bật khử răng cưa cho chữ và hình -> giao diện mịn hơn. */
    public static Graphics2D muot(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }
}
