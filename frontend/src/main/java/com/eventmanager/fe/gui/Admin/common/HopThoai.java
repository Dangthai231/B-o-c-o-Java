package com.eventmanager.fe.gui.Admin.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Các hộp thoại dùng chung, cùng phong cách với giao diện:
 *  - FormDialog: khung form (tiêu đề + các ô nhập + nút ở chân)
 *  - xacNhan(): hỏi Có / Không
 *  - nhapLyDo(): bắt nhập lý do (dùng khi Từ chối)
 *  - loi(), thanhCong(): báo kết quả
 */
public final class HopThoai {

    private HopThoai() {
    }

    // ===================== KHUNG FORM =====================
    public static class FormDialog extends JDialog {
        protected final JPanel than = new JPanel(new GridBagLayout());
        protected final JPanel chan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        private int dong = 0;
        private int cotHienTai = 0;

        public FormDialog(Component cha, String tieuDe, String moTa, int rong) {
            super(SwingUtilities.getWindowAncestor(cha), tieuDe, ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            JPanel goc = new JPanel(new BorderLayout());
            goc.setBackground(Color.WHITE);

            // Đầu: tiêu đề + mô tả + nút X
            JPanel dau = new JPanel(new BorderLayout());
            dau.setBackground(Color.WHITE);
            dau.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER), new EmptyBorder(18, 24, 16, 16)));
            JPanel chu = new JPanel();
            chu.setOpaque(false);
            chu.setLayout(new BoxLayout(chu, BoxLayout.Y_AXIS));
            chu.add(UI.nhan(tieuDe, Theme.H2, Theme.TEXT));
            if (moTa != null && !moTa.isEmpty()) {
                chu.add(Box.createVerticalStrut(3));
                chu.add(UI.nhan(moTa, Theme.SMALL, Theme.MUTED));
            }
            dau.add(chu, BorderLayout.CENTER);
            JButton dong = UI.nutIcon(Icons.X, "Đóng");
            dong.addActionListener(e -> dispose());
            JPanel boc = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            boc.setOpaque(false);
            boc.add(dong);
            dau.add(boc, BorderLayout.EAST);
            goc.add(dau, BorderLayout.NORTH);

            than.setBackground(Color.WHITE);
            than.setBorder(new EmptyBorder(18, 24, 8, 24));
            JScrollPane sp = UI.cuon(than);
            sp.getViewport().setOpaque(true);
            sp.getViewport().setBackground(Color.WHITE);
            goc.add(sp, BorderLayout.CENTER);

            chan.setBackground(Color.WHITE);
            chan.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER), new EmptyBorder(14, 24, 14, 24)));
            goc.add(chan, BorderLayout.SOUTH);

            setContentPane(goc);
            getRootPane().registerKeyboardAction(e -> dispose(),
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
            setMinimumSize(new Dimension(rong, 200));
        }

        /** Thêm 1 trường: nhãn ở trên, ô nhập ở dưới. caHang = true thì chiếm cả 2 cột. */
        public void truong(String nhan, JComponent o, boolean caHang) {
            if (caHang && cotHienTai == 1) {
                dong++;
                cotHienTai = 0;
            }
            JPanel cum = new JPanel(new BorderLayout(0, 6));
            cum.setOpaque(false);
            if (nhan != null) cum.add(UI.nhan(nhan, Theme.SMALL_BOLD, Theme.TEXT_2), BorderLayout.NORTH);
            if (o instanceof JTextField || o instanceof JPasswordField || o instanceof JComboBox) {
                o.setPreferredSize(new Dimension(o.getPreferredSize().width, 38)); // các ô cao bằng nhau
            }
            cum.add(o, BorderLayout.CENTER);

            GridBagConstraints g = new GridBagConstraints();
            g.gridx = cotHienTai;
            g.gridy = dong;
            g.gridwidth = caHang ? 2 : 1;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(0, cotHienTai == 1 ? 8 : 0, 14, caHang || cotHienTai == 1 ? 0 : 8);
            than.add(cum, g);

            if (caHang || cotHienTai == 1) {
                dong++;
                cotHienTai = 0;
            } else {
                cotHienTai = 1;
            }
        }

        /** Thêm khối tự do (VD: bảng so sánh) chiếm cả hàng. */
        public void khoi(JComponent c) {
            truong(null, c, true);
        }

        public void ketThucThan() {
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = 0;
            g.gridy = dong + 1;
            g.weighty = 1;
            than.add(Box.createGlue(), g);
        }

        public void nut(JButton b) {
            chan.add(b);
        }

        public void hien() {
            ketThucThan();
            pack();
            Dimension d = getSize();
            setSize(Math.max(d.width, getMinimumSize().width), Math.min(d.height, 680));
            setLocationRelativeTo(getOwner());
            setVisible(true);
        }
    }

    /** Ô chỉ để xem (không sửa được) với nền xám nhạt. */
    public static JComponent oChiXem(String giaTri) {
        UI.TextFieldDep t = UI.oNhap(null);
        t.setText(giaTri);
        t.setEditable(false);
        return t;
    }

    // ===================== HỎI XÁC NHẬN =====================
    public static boolean xacNhan(Component cha, String tieuDe, String noiDung, String chuNut, UI.Kieu kieu) {
        final boolean[] ok = {false};
        FormDialog d = new FormDialog(cha, tieuDe, null, 420);
        JLabel l = new JLabel("<html><div style='width:330px'>" + noiDung + "</div></html>");
        l.setFont(Theme.BODY);
        l.setForeground(Theme.TEXT_2);
        d.khoi(l);
        JButton huy = UI.nut("Hủy", UI.Kieu.VIEN);
        huy.addActionListener(e -> d.dispose());
        JButton dongY = UI.nut(chuNut, kieu);
        dongY.addActionListener(e -> {
            ok[0] = true;
            d.dispose();
        });
        d.nut(huy);
        d.nut(dongY);
        d.getRootPane().setDefaultButton(dongY);
        d.hien();
        return ok[0];
    }

    /** Bắt buộc nhập lý do. Trả về null nếu người dùng bấm Hủy. */
    public static String nhapLyDo(Component cha, String tieuDe, String goiY) {
        final String[] kq = {null};
        FormDialog d = new FormDialog(cha, tieuDe, "Lý do sẽ được gửi kèm cho người liên quan", 460);
        JTextArea a = UI.vungChu(4);
        d.truong("Lý do *", UI.cuonVungChu(a), true);
        JLabel loi = UI.nhan(" ", Theme.SMALL, Theme.DANGER);
        d.khoi(loi);
        JButton huy = UI.nut("Hủy", UI.Kieu.VIEN);
        huy.addActionListener(e -> d.dispose());
        JButton ok = UI.nut("Xác nhận từ chối", UI.Kieu.NGUY_HIEM);
        ok.addActionListener(e -> {
            if (a.getText().trim().isEmpty()) {
                loi.setText("Vui lòng nhập lý do" + (goiY == null ? "" : " (" + goiY + ")"));
                return;
            }
            kq[0] = a.getText().trim();
            d.dispose();
        });
        d.nut(huy);
        d.nut(ok);
        d.hien();
        return kq[0];
    }

    public static void loi(Component cha, String noiDung) {
        JOptionPane.showMessageDialog(cha, noiDung, "Không thực hiện được", JOptionPane.WARNING_MESSAGE);
    }

    // ===================== THÔNG BÁO NHỎ (toast) GÓC TRÊN PHẢI =====================
    public static void thanhCong(Component cha, String noiDung) {
        Window w = cha instanceof Window win ? win : SwingUtilities.getWindowAncestor(cha);
        if (w == null || GraphicsEnvironment.isHeadless()) {
            return;
        }
        JWindow t = new JWindow(w);
        JPanel p = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Theme.muot(g);
                g2.setColor(new Color(0x14284B));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(12, 14, 12, 18));
        p.add(new JLabel(Icons.of(Icons.CHECK_CIRCLE, 20, Theme.SUCCESS)), BorderLayout.WEST);
        p.add(UI.nhan(noiDung, Theme.BODY_BOLD, Color.WHITE), BorderLayout.CENTER);
        t.setContentPane(p);
        try {
            // Một số máy không hỗ trợ cửa sổ trong suốt -> bỏ qua, vẫn hiện bình thường
            if (w.getGraphicsConfiguration().getDevice()
                    .isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT)) {
                t.setBackground(new Color(0, 0, 0, 0));
            }
        } catch (RuntimeException ignored) {
        }
        t.pack();
        Point goc = w.getLocationOnScreen();
        t.setLocation(goc.x + w.getWidth() - t.getWidth() - 28, goc.y + 70);
        t.setVisible(true);
        Timer timer = new Timer(2400, e -> t.dispose());
        timer.setRepeats(false);
        timer.start();
    }
}
