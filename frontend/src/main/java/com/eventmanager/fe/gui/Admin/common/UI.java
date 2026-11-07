package com.eventmanager.fe.gui.Admin.common;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * "Hộp đồ nghề" giao diện: các hàm tạo nút, ô nhập, thẻ, nhãn trạng thái... đã được làm đẹp sẵn.
 * Màn hình nào cũng gọi UI.xxx() nên giao diện luôn đồng bộ.
 */
public final class UI {

    private UI() {
    }

    // =====================================================================
    // 1. PANEL BO GÓC (dùng làm thẻ / card)
    // =====================================================================
    public static class RoundPanel extends JPanel {
        private final int radius;
        private Color nen;
        private final Color vien;

        public RoundPanel(LayoutManager layout, int radius, Color nen, Color vien) {
            super(layout);
            this.radius = radius;
            this.nen = nen;
            this.vien = vien;
            setOpaque(false);
        }

        public void setNen(Color nen) {
            this.nen = nen;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Theme.muot(g);
            g2.setColor(nen);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            if (vien != null) {
                g2.setColor(vien);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Thẻ trắng bo góc 12px, có viền nhạt và lề trong. */
    public static RoundPanel card(LayoutManager layout, int padding) {
        RoundPanel p = new RoundPanel(layout, 14, Theme.CARD, Theme.BORDER);
        p.setBorder(new EmptyBorder(padding, padding, padding, padding));
        return p;
    }

    // =====================================================================
    // 2. NÚT BẤM
    // =====================================================================
    public enum Kieu { CHINH, VIEN, NGUY_HIEM, THANH_CONG, NHAT }

    public static class FlatButton extends JButton {
        private final Kieu kieu;
        private boolean hover;

        public FlatButton(String text, Icon icon, Kieu kieu) {
            super(text, icon);
            this.kieu = kieu;
            setFont(Theme.BODY_BOLD);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setIconTextGap(6);
            setBorder(new EmptyBorder(8, 14, 8, 14));
            setForeground(switch (kieu) {
                case CHINH, NGUY_HIEM, THANH_CONG -> Color.WHITE;
                case VIEN -> Theme.TEXT_2;
                case NHAT -> Theme.PRIMARY;
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Theme.muot(g);
            Color nen;
            Color vien = null;
            switch (kieu) {
                case CHINH -> nen = hover ? Theme.PRIMARY_DARK : Theme.PRIMARY;
                case NGUY_HIEM -> nen = hover ? Theme.DANGER.darker() : Theme.DANGER;
                case THANH_CONG -> nen = hover ? Theme.SUCCESS.darker() : Theme.SUCCESS;
                case NHAT -> nen = hover ? Theme.PRIMARY_SOFT : new Color(0, 0, 0, 0);
                default -> {
                    nen = hover ? Theme.ROW_HOVER : Color.WHITE;
                    vien = Theme.BORDER;
                }
            }
            if (!isEnabled()) {
                nen = kieu == Kieu.VIEN ? Theme.GRAY_SOFT : new Color(0xB9C3D3);
            }
            g2.setColor(nen);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 9, 9));
            if (vien != null) {
                g2.setColor(vien);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 9, 9));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static FlatButton nut(String text, Kieu kieu) {
        return new FlatButton(text, null, kieu);
    }

    public static FlatButton nut(String text, String icon, Kieu kieu) {
        Color mau = (kieu == Kieu.VIEN) ? Theme.TEXT_2 : (kieu == Kieu.NHAT ? Theme.PRIMARY : Color.WHITE);
        return new FlatButton(text, Icons.of(icon, 15, mau), kieu);
    }

    /** Nút vuông nhỏ chỉ có icon (như nút bút chì, ổ khóa trong bảng). */
    public static FlatButton nutIcon(String icon, String tooltip) {
        FlatButton b = new FlatButton(null, Icons.of(icon, 15, Theme.TEXT_2), Kieu.VIEN);
        b.setBorder(new EmptyBorder(6, 7, 6, 7));
        b.setToolTipText(tooltip);
        return b;
    }

    // =====================================================================
    // 3. Ô NHẬP LIỆU
    // =====================================================================

    /** Viền bo góc, đổi sang màu xanh khi ô đang được chọn (focus). */
    public static class VienBoGoc extends AbstractBorder {
        private final int padTrai;
        private final int padPhai;

        public VienBoGoc(int padTrai, int padPhai) {
            this.padTrai = padTrai;
            this.padPhai = padPhai;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = Theme.muot(g);
            g2.setColor(c.hasFocus() ? Theme.PRIMARY : Theme.BORDER);
            g2.draw(new RoundRectangle2D.Double(x, y, w - 1, h - 1, 9, 9));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, padTrai, 8, padPhai);
        }
    }

    /** Ô chữ có chữ mờ gợi ý (placeholder) và icon phía trước (tùy chọn). */
    public static class TextFieldDep extends JTextField {
        private final String goiY;
        private final Icon icon;

        public TextFieldDep(String goiY, String tenIcon) {
            this.goiY = goiY;
            this.icon = tenIcon == null ? null : Icons.of(tenIcon, 16, Theme.MUTED);
            setFont(Theme.BODY);
            setForeground(Theme.TEXT);
            setCaretColor(Theme.PRIMARY);
            setBorder(new VienBoGoc(icon == null ? 11 : 36, 11));
            setOpaque(false);
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Theme.muot(g);
            g2.setColor(isEditable() ? Color.WHITE : Theme.TABLE_HEAD);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 9, 9));
            if (icon != null) icon.paintIcon(this, g2, 12, (getHeight() - 16) / 2);
            g2.dispose();
            super.paintComponent(g);
            if (getText().isEmpty() && goiY != null) {
                Graphics2D g3 = Theme.muot(g);
                g3.setColor(Theme.MUTED);
                g3.setFont(getFont());
                Insets in = getInsets();
                FontMetrics fm = g3.getFontMetrics();
                g3.drawString(goiY, in.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g3.dispose();
            }
        }
    }

    public static TextFieldDep oNhap(String goiY) {
        return new TextFieldDep(goiY, null);
    }

    public static TextFieldDep oTimKiem(String goiY) {
        return new TextFieldDep(goiY, Icons.SEARCH);
    }

    public static JPasswordField oMatKhau() {
        JPasswordField p = new JPasswordField();
        p.setFont(Theme.BODY);
        p.setBorder(new VienBoGoc(11, 11));
        p.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                p.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                p.repaint();
            }
        });
        return p;
    }

    public static JTextArea vungChu(int dong) {
        JTextArea a = new JTextArea(dong, 20);
        a.setFont(Theme.BODY);
        a.setForeground(Theme.TEXT);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(new EmptyBorder(8, 10, 8, 10));
        return a;
    }

    public static JScrollPane cuonVungChu(JTextArea a) {
        JScrollPane sp = new JScrollPane(a);
        sp.setBorder(new VienBoGoc(0, 0) {
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(1, 1, 1, 1);
            }
        });
        return sp;
    }

    /** Hộp chọn (combobox) phẳng, có mũi tên dạng chevron. Item có thể kèm icon phía trước. */
    public static <T> JComboBox<T> hopChon(T[] items, String tenIcon) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(Theme.BODY);
        cb.setForeground(Theme.TEXT);
        cb.setBackground(Color.WHITE);
        cb.setBorder(new VienBoGoc(tenIcon == null ? 6 : 30, 4));
        cb.setFocusable(false);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Icon icon = tenIcon == null ? null : Icons.of(tenIcon, 16, Theme.MUTED);
        cb.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton(Icons.of(Icons.CHEVRON_DOWN, 16, Theme.MUTED));
                b.setBorder(new EmptyBorder(0, 6, 0, 8));
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
                return b;
            }

            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(Color.WHITE);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }

            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = Theme.muot(g);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(0, 0, c.getWidth() - 1, c.getHeight() - 1, 9, 9));
                if (icon != null) icon.paintIcon(c, g2, 11, (c.getHeight() - 16) / 2);
                g2.dispose();
                super.paint(g, c);
            }
        });
        cb.setOpaque(false);
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setBorder(new EmptyBorder(6, 8, 6, 8));
                l.setFont(Theme.BODY);
                if (index >= 0) {
                    l.setBackground(isSelected ? Theme.PRIMARY_SOFT : Color.WHITE);
                    l.setForeground(Theme.TEXT);
                } else {
                    l.setOpaque(false);
                }
                return l;
            }
        });
        return cb;
    }

    // =====================================================================
    // 4. NHÃN CHỮ & NHÃN TRẠNG THÁI (badge)
    // =====================================================================
    public static JLabel nhan(String text, Font font, Color mau) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(mau);
        return l;
    }

    /** Trả về {màu chữ, màu nền} cho mỗi loại trạng thái -> nhìn màu là biết tình trạng. */
    public static Color[] mauTrangThai(String s) {
        if (s == null) s = "";
        return switch (s) {
            case "Hoạt động", "Đã duyệt", "Đã đăng", "Đang diễn ra", "Đã tham gia" -> new Color[]{Theme.SUCCESS, Theme.SUCCESS_SOFT};
            case "Bị khóa", "Từ chối", "Đã hủy", "Vắng mặt" -> new Color[]{Theme.DANGER, Theme.DANGER_SOFT};
            case "Chờ duyệt" -> new Color[]{Theme.WARNING, Theme.WARNING_SOFT};
            case "Sắp diễn ra", "Quản trị viên" -> new Color[]{Theme.PRIMARY, Theme.PRIMARY_SOFT};
            case "Cán bộ sự kiện" -> new Color[]{Theme.PURPLE, Theme.PURPLE_SOFT};
            case "Sinh viên" -> new Color[]{new Color(0x0E7C86), new Color(0xE1F4F5)};
            default -> new Color[]{Theme.TEXT_2, Theme.GRAY_SOFT};
        };
    }

    /** Có chấm tròn phía trước: dùng cho trạng thái hoạt động / duyệt. */
    public static boolean coCham(String s) {
        return switch (s == null ? "" : s) {
            case "Hoạt động", "Bị khóa", "Chờ duyệt", "Đã duyệt", "Từ chối", "Đã đăng", "Đã gỡ",
                 "Sắp diễn ra", "Đang diễn ra", "Đã kết thúc", "Đã hủy" -> true;
            default -> false;
        };
    }

    /** Viên thuốc (pill) hiển thị trạng thái. */
    public static class Badge extends JComponent {
        private String text = "";

        public Badge() {
            setFont(Theme.SMALL);
        }

        public void setText(String t) {
            text = t == null ? "" : t;
            revalidate();
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            int w = fm.stringWidth(text) + 20 + (coCham(text) ? 12 : 0);
            return new Dimension(w, 24);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (text.isEmpty()) return;
            Graphics2D g2 = Theme.muot(g);
            Color[] m = mauTrangThai(text);
            Dimension d = getPreferredSize();
            int y = (getHeight() - d.height) / 2;
            g2.setColor(m[1]);
            g2.fill(new RoundRectangle2D.Double(0, y, d.width, d.height, d.height, d.height));
            int x = 10;
            if (coCham(text)) {
                g2.setColor(m[0]);
                g2.fillOval(x, y + d.height / 2 - 3, 6, 6);
                x += 12;
            }
            g2.setColor(m[0]);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, x, y + (d.height - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // =====================================================================
    // 5. THẺ THỐNG KÊ (4 ô số liệu trên đầu trang)
    // =====================================================================
    public static class StatCard extends RoundPanel {
        private final JLabel lblSo = nhan("–", Theme.NUMBER, Theme.TEXT);

        public StatCard(String tieuDe, String icon, Color mau, Color mauNen) {
            super(new BorderLayout(16, 0), 14, Theme.CARD, Theme.BORDER);
            setBorder(new EmptyBorder(14, 16, 14, 16));
            JComponent tron = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = Theme.muot(g);
                    g2.setColor(mauNen);
                    g2.fillOval(0, 0, 48, 48);
                    g2.dispose();
                    Icons.of(icon, 24, mau).paintIcon(this, g, 12, 12);
                }
            };
            tron.setPreferredSize(new Dimension(48, 48));
            JPanel trai = new JPanel(new GridBagLayout());
            trai.setOpaque(false);
            trai.add(tron);
            add(trai, BorderLayout.WEST);

            JPanel chu = new JPanel();
            chu.setOpaque(false);
            chu.setLayout(new BoxLayout(chu, BoxLayout.Y_AXIS));
            chu.add(nhan(tieuDe, Theme.BODY, Theme.TEXT_2));
            chu.add(Box.createVerticalStrut(2));
            chu.add(lblSo);
            add(chu, BorderLayout.CENTER);
        }

        public void setSo(long so) {
            lblSo.setText(String.valueOf(so));
        }

        public void setChu(String s) {
            lblSo.setText(s);
        }
    }

    // =====================================================================
    // 6. THANH TAB (gạch chân màu xanh ở tab đang chọn)
    // =====================================================================
    public static class TabBar extends JPanel {
        private final List<JLabel> tabs = new ArrayList<>();
        private int chon = 0;
        private final IntConsumer khiDoi;

        public TabBar(IntConsumer khiDoi, String... ten) {
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            this.khiDoi = khiDoi;
            setOpaque(false);
            for (int i = 0; i < ten.length; i++) {
                final int idx = i;
                JLabel l = new JLabel(ten[i]);
                l.setFont(Theme.BODY_BOLD);
                l.setBorder(new EmptyBorder(10, 16, 12, 16));
                l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                l.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        chonTab(idx);
                    }
                });
                tabs.add(l);
                add(l);
            }
            capNhatMau();
        }

        public void chonTab(int idx) {
            chon = idx;
            capNhatMau();
            repaint();
            if (khiDoi != null) khiDoi.accept(idx);
        }

        public void doiTen(int idx, String ten) {
            tabs.get(idx).setText(ten);
            revalidate();
            repaint();
        }

        public int getChon() {
            return chon;
        }

        private void capNhatMau() {
            for (int i = 0; i < tabs.size(); i++) {
                tabs.get(i).setForeground(i == chon ? Theme.PRIMARY : Theme.TEXT_2);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = Theme.muot(g);
            g2.setColor(Theme.BORDER);
            g2.fillRect(0, getHeight() - 1, getWidth(), 1);
            if (!tabs.isEmpty()) {
                Rectangle r = tabs.get(chon).getBounds();
                g2.setColor(Theme.PRIMARY);
                g2.fillRoundRect(r.x + 6, getHeight() - 3, r.width - 12, 3, 3, 3);
            }
            g2.dispose();
        }
    }

    // =====================================================================
    // 7. PHÂN TRANG:  Hiển thị 1 - 10 trong tổng số 128      < 1 2 3 4 5 >
    // =====================================================================
    public static class PhanTrang extends JPanel {
        private final JLabel lblThongTin = nhan("", Theme.SMALL, Theme.MUTED);
        private final JPanel nutTrang = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        private final IntConsumer khiChonTrang;

        public PhanTrang(IntConsumer khiChonTrang) {
            super(new BorderLayout());
            this.khiChonTrang = khiChonTrang;
            setOpaque(false);
            nutTrang.setOpaque(false);
            add(lblThongTin, BorderLayout.WEST);
            add(nutTrang, BorderLayout.EAST);
        }

        /** trang bắt đầu từ 0. */
        public void capNhat(int trang, int tongTrang, long tongSo, int kichThuoc, String donVi) {
            long tu = tongSo == 0 ? 0 : (long) trang * kichThuoc + 1;
            long den = Math.min(tongSo, (long) (trang + 1) * kichThuoc);
            lblThongTin.setText("Hiển thị " + tu + " - " + den + " trong tổng số " + tongSo + " " + donVi);

            nutTrang.removeAll();
            nutTrang.add(oTrang(null, Icons.CHEVRON_LEFT, trang > 0, false, () -> khiChonTrang.accept(trang - 1)));
            int batDau = Math.max(0, Math.min(trang - 2, tongTrang - 5));
            int ketThuc = Math.min(tongTrang, batDau + 5);
            for (int i = batDau; i < ketThuc; i++) {
                final int t = i;
                nutTrang.add(oTrang(String.valueOf(i + 1), null, true, i == trang, () -> khiChonTrang.accept(t)));
            }
            nutTrang.add(oTrang(null, Icons.CHEVRON_RIGHT, trang < tongTrang - 1, false, () -> khiChonTrang.accept(trang + 1)));
            nutTrang.revalidate();
            nutTrang.repaint();
        }

        private JComponent oTrang(String so, String icon, boolean bat, boolean dangChon, Runnable onClick) {
            JLabel l = new JLabel(so, icon == null ? null : Icons.of(icon, 14, bat ? Theme.TEXT_2 : Theme.BORDER),
                    SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = Theme.muot(g);
                    g2.setColor(dangChon ? Theme.PRIMARY : Color.WHITE);
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                    if (!dangChon) {
                        g2.setColor(Theme.BORDER);
                        g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            l.setFont(Theme.SMALL_BOLD);
            l.setForeground(dangChon ? Color.WHITE : Theme.TEXT_2);
            l.setPreferredSize(new Dimension(30, 30));
            if (bat && !dangChon) {
                l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                l.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        onClick.run();
                    }
                });
            }
            return l;
        }
    }

    // =====================================================================
    // 8. TIỆN ÍCH KHÁC
    // =====================================================================

    /** Khung cuộn không viền, cuộn mượt hơn mặc định, thanh cuộn mảnh. */
    public static JScrollPane cuon(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.getVerticalScrollBar().setUI(new ThanhCuonManh());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(8, 8));
        sp.getHorizontalScrollBar().setUI(new ThanhCuonManh());
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(8, 8));
        return sp;
    }

    private static class ThanhCuonManh extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(0xC9D2DF);
            trackColor = new Color(0, 0, 0, 0);
        }

        @Override
        protected JButton createDecreaseButton(int o) {
            return nutRong();
        }

        @Override
        protected JButton createIncreaseButton(int o) {
            return nutRong();
        }

        private JButton nutRong() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = Theme.muot(g);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        }
    }

    /** Ảnh đại diện tròn có chữ viết tắt (VD: "AD"). */
    public static Icon avatar(String chu, int size, Color nen) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = Theme.muot(g);
                g2.setColor(nen);
                g2.fillOval(x, y, size, size);
                g2.setColor(Color.WHITE);
                g2.setFont(Theme.font(Font.BOLD, size * 0.36f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(chu, x + (size - fm.stringWidth(chu)) / 2, y + (size - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    /** "Nguyễn Hải Nam" -> "HN" (2 chữ cái cuối) */
    public static String vietTat(String hoTen) {
        if (hoTen == null || hoTen.isBlank()) return "?";
        String[] p = hoTen.trim().split("\\s+");
        String s = p.length == 1 ? p[0].substring(0, 1)
                : p[p.length - 2].substring(0, 1) + p[p.length - 1].substring(0, 1);
        return s.toUpperCase();
    }

    /** Hàng ngang chứa các thành phần, khoảng cách đều nhau. */
    public static JPanel hang(int khoangCach, Component... cs) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, khoangCach, 0));
        p.setOpaque(false);
        for (Component c : cs) p.add(c);
        return p;
    }

    public static JPanel hangPhai(int khoangCach, Component... cs) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, khoangCach, 0));
        p.setOpaque(false);
        for (Component c : cs) p.add(c);
        return p;
    }

    /** Khối "Tiêu đề trang + dòng mô tả" ở đầu mỗi màn hình. */
    public static JPanel tieuDeTrang(String tieuDe, String moTa) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = nhan(tieuDe, Theme.H1, Theme.TEXT);
        JLabel m = nhan(moTa, Theme.BODY, Theme.MUTED);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        m.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(4));
        p.add(m);
        return p;
    }

    /** Thanh tiến độ bo tròn. */
    public static JComponent thanhTienDo(double tiLe, Color mau) {
        JComponent c = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Theme.muot(g);
                int h = 8;
                int y = (getHeight() - h) / 2;
                g2.setColor(Theme.GRAY_SOFT);
                g2.fillRoundRect(0, y, getWidth(), h, h, h);
                g2.setColor(mau);
                g2.fillRoundRect(0, y, (int) (getWidth() * Math.max(0, Math.min(1, tiLe))), h, h, h);
                g2.dispose();
            }
        };
        c.setPreferredSize(new Dimension(120, 12));
        return c;
    }
}
