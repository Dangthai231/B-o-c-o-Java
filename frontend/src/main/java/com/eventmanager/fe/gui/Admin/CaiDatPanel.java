package com.eventmanager.fe.gui.Admin;

import com.eventmanager.fe.api.ApiClient;
import com.eventmanager.fe.gui.Admin.common.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** MÀN HÌNH "CÀI ĐẶT": đổi mật khẩu Quản trị viên + thông tin hệ thống. */
public class CaiDatPanel extends TrangAdmin {

    private final JPasswordField mkCu = UI.oMatKhau();
    private final JPasswordField mkMoi = UI.oMatKhau();
    private final JPasswordField mkMoi2 = UI.oMatKhau();
    private final JLabel loi = UI.nhan(" ", Theme.SMALL, Theme.DANGER);

    public CaiDatPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        add(UI.tieuDeTrang("Cài đặt", "Bảo mật tài khoản và thông tin hệ thống"), BorderLayout.NORTH);

        JPanel hai = new JPanel(new GridBagLayout());
        hai.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.NORTHWEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 0, 18);
        g.weightx = 0.55;
        hai.add(theDoiMatKhau(), g);
        g.weightx = 0.45;
        g.insets = new Insets(0, 0, 0, 0);
        hai.add(theThongTin(), g);
        g.gridy = 1;
        g.weighty = 1;
        hai.add(Box.createGlue(), g);
        add(hai, BorderLayout.CENTER);
    }

    private JPanel theDoiMatKhau() {
        UI.RoundPanel p = UI.card(new GridBagLayout(), 22);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 4, 0);
        p.add(UI.nhan("Đổi mật khẩu", Theme.H2, Theme.TEXT), g);
        g.insets = new Insets(0, 0, 16, 0);
        p.add(UI.nhan("Nên dùng mật khẩu từ 8 ký tự, có cả chữ và số", Theme.SMALL, Theme.MUTED), g);
        themO(p, g, "Mật khẩu hiện tại", mkCu);
        themO(p, g, "Mật khẩu mới", mkMoi);
        themO(p, g, "Nhập lại mật khẩu mới", mkMoi2);
        p.add(loi, g);
        UI.FlatButton luu = UI.nut("Cập nhật mật khẩu", Icons.KEY, UI.Kieu.CHINH);
        luu.addActionListener(e -> doiMatKhau());
        g.fill = GridBagConstraints.NONE;
        g.anchor = GridBagConstraints.WEST;
        p.add(luu, g);
        return p;
    }

    private void themO(JPanel p, GridBagConstraints g, String nhan, JComponent o) {
        g.insets = new Insets(0, 0, 6, 0);
        p.add(UI.nhan(nhan, Theme.SMALL_BOLD, Theme.TEXT_2), g);
        g.insets = new Insets(0, 0, 14, 0);
        o.setPreferredSize(new Dimension(0, 38));
        p.add(o, g);
    }

    private JPanel theThongTin() {
        UI.RoundPanel p = UI.card(new GridLayout(0, 1, 0, 12), 22);
        p.add(UI.nhan("Thông tin hệ thống", Theme.H2, Theme.TEXT));
        p.add(dong("Tài khoản đăng nhập", dieuHuong.tenDangNhap()));
        p.add(dong("Vai trò", "Quản trị viên"));
        p.add(dong("Máy chủ API", ApiClient.BASE_URL));
        p.add(dong("Đơn vị", "BCH Liên chi đoàn CNS - ĐH Điện lực"));
        return p;
    }

    private JPanel dong(String nhan, String giaTri) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(0, 0, 10, 0)));
        p.add(UI.nhan(nhan, Theme.BODY, Theme.MUTED), BorderLayout.WEST);
        p.add(UI.nhan(giaTri, Theme.BODY_BOLD, Theme.TEXT), BorderLayout.EAST);
        return p;
    }

    private void doiMatKhau() {
        String cu = new String(mkCu.getPassword());
        String moi = new String(mkMoi.getPassword());
        if (cu.isEmpty()) { loi.setText("Vui lòng nhập mật khẩu hiện tại"); return; }
        if (moi.length() < 6) { loi.setText("Mật khẩu mới phải có ít nhất 6 ký tự"); return; }
        if (!moi.equals(new String(mkMoi2.getPassword()))) { loi.setText("Hai lần nhập mật khẩu mới không khớp"); return; }
        loi.setText(" ");
        Api.chayNen(this, () -> Api.put("/api/admin/doi-mat-khau",
                Api.body("tenDangNhap", dieuHuong.tenDangNhap(), "matKhauCu", cu, "matKhauMoi", moi)), r -> {
            mkCu.setText("");
            mkMoi.setText("");
            mkMoi2.setText("");
            HopThoai.thanhCong(this, "Đổi mật khẩu thành công");
        });
    }

    @Override
    public void taiLai() {
        loi.setText(" ");
    }
}
