package com.eventmanager.fe.gui.HocSinh;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * THÔNG BÁO CHO SINH VIÊN (liên kết với phần Admin):
 * Cán bộ tạo thông báo -> Admin duyệt / đăng -> sinh viên thấy ở đây.
 * Chỉ hiện thông báo gửi cho "Tất cả người tham gia" hoặc "Sinh viên" (backend đã lọc).
 * Thông báo chưa mở có nhãn "Mới"; trạng thái đã đọc lưu trên máy theo từng tài khoản.
 */
public class ThongBaoSinhVien extends TrangSinhVien {

    private final UI.TextFieldDep oTim = UI.oTimKiem("Tìm theo tiêu đề, nội dung...");
    private final JLabel lblSoLuong = UI.nhan(" ", Theme.SMALL, Theme.MUTED);
    private final JPanel danhSach = new JPanel();
    private final List<JsonObject> tatCa = new ArrayList<>();

    public ThongBaoSinhVien(DieuHuong dieuHuong) {
        super(dieuHuong);
        add(UI.tieuDeTrang("Thông báo", "Thông báo của Liên Chi đoàn CNTT đã được duyệt gửi tới sinh viên"), BorderLayout.NORTH);

        UI.FlatButton btnDocHet = UI.nut("Đánh dấu đã đọc tất cả", Icons.CHECK, UI.Kieu.VIEN);
        btnDocHet.addActionListener(e -> {
            List<Long> ids = new ArrayList<>();
            for (JsonObject o : tatCa) ids.add(Api.n(o, "id"));
            danhDauDaXem(dieuHuong.tenDangNhap(), ids);
            hienThi();
            dieuHuong.capNhatChuong();
        });
        JPanel loc = new JPanel(new BorderLayout(12, 0));
        loc.setOpaque(false);
        oTim.setPreferredSize(new Dimension(320, 38));
        loc.add(oTim, BorderLayout.CENTER);
        loc.add(btnDocHet, BorderLayout.EAST);

        danhSach.setOpaque(false);
        danhSach.setLayout(new BoxLayout(danhSach, BoxLayout.Y_AXIS));

        UI.RoundPanel the = UI.card(new BorderLayout(0, 12), 0);
        the.setBorder(new EmptyBorder(16, 16, 14, 16));
        the.add(loc, BorderLayout.NORTH);
        the.add(UI.cuon(danhSach), BorderLayout.CENTER);
        the.add(lblSoLuong, BorderLayout.SOUTH);
        add(the, BorderLayout.CENTER);

        oTim.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { hienThi(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { hienThi(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { hienThi(); }
        });
    }

    /** GET /api/sinh-vien/thong-bao (backend chỉ trả thông báo đã duyệt / đã đăng dành cho sinh viên). */
    @Override
    public void taiLai() {
        Api.chayNen(this, () -> Api.get("/api/sinh-vien/thong-bao"), kq -> {
            tatCa.clear();
            for (JsonElement el : Api.arr(kq)) tatCa.add(el.getAsJsonObject());
            hienThi();
        });
    }

    private void hienThi() {
        String kw = oTim.getText().trim().toLowerCase();
        Set<Long> daXem = daXem(dieuHuong.tenDangNhap());
        danhSach.removeAll();
        int hien = 0, chuaDoc = 0;
        for (JsonObject o : tatCa) {
            boolean moi = !daXem.contains(Api.n(o, "id"));
            if (moi) chuaDoc++;
            String tim = (Api.s(o, "tieuDe") + " " + Api.s(o, "noiDung")).toLowerCase();
            if (!kw.isEmpty() && !tim.contains(kw)) continue;
            danhSach.add(dongThongBao(o, moi));
            danhSach.add(Box.createVerticalStrut(8));
            hien++;
        }
        if (tatCa.isEmpty()) danhSach.add(HocSinhUI.trong("Chưa có thông báo nào dành cho sinh viên"));
        else if (hien == 0) danhSach.add(HocSinhUI.trong("Không tìm thấy thông báo phù hợp"));
        lblSoLuong.setText(tatCa.size() + " thông báo  •  " + chuaDoc + " chưa đọc  •  Bấm vào một thông báo để đọc");
        danhSach.revalidate();
        danhSach.repaint();
    }

    /** Một thông báo: chuông | tiêu đề (+ nhãn Mới), trích nội dung, ngày và đối tượng. Bấm để đọc. */
    private JComponent dongThongBao(JsonObject o, boolean moi) {
        UI.RoundPanel dong = new UI.RoundPanel(new BorderLayout(14, 0), 12, moi ? Theme.PRIMARY_SOFT : Color.WHITE, Theme.BORDER);
        dong.setBorder(new EmptyBorder(12, 14, 12, 14));
        dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        dong.setAlignmentX(LEFT_ALIGNMENT);

        JLabel chuong = new JLabel(Icons.of(Icons.BELL, 20, moi ? Theme.PRIMARY : Theme.MUTED));
        chuong.setVerticalAlignment(SwingConstants.TOP);
        dong.add(chuong, BorderLayout.WEST);

        JPanel chu = new JPanel();
        chu.setOpaque(false);
        chu.setLayout(new BoxLayout(chu, BoxLayout.Y_AXIS));
        JPanel dongTieuDe = UI.hang(8, UI.nhan(Api.s(o, "tieuDe"), Theme.BODY_BOLD, Theme.TEXT));
        ((FlowLayout) dongTieuDe.getLayout()).setHgap(0);
        if (moi) {
            dongTieuDe.add(Box.createHorizontalStrut(8));
            dongTieuDe.add(new HocSinhUI.Nhan("Mới"));
        }
        String noiDung = Api.s(o, "noiDung");
        JLabel trich = UI.nhan(noiDung.length() > 110 ? noiDung.substring(0, 110) + "..." : noiDung, Theme.SMALL, Theme.TEXT_2);
        JLabel meta = UI.nhan(HocSinhUI.thoiGian(Api.s(o, "ngay")) + "  •  Gửi tới: " + Api.s(o, "doiTuong"), Theme.SMALL, Theme.MUTED);
        for (JComponent c : new JComponent[]{dongTieuDe, trich, meta}) c.setAlignmentX(LEFT_ALIGNMENT);
        chu.add(dongTieuDe);
        chu.add(Box.createVerticalStrut(4));
        chu.add(trich);
        chu.add(Box.createVerticalStrut(4));
        chu.add(meta);
        dong.add(chu, BorderLayout.CENTER);

        dong.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dong.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                docThongBao(o);
            }
        });
        return dong;
    }

    /** Hộp thoại đọc toàn bộ nội dung, đồng thời đánh dấu đã đọc. */
    private void docThongBao(JsonObject o) {
        HopThoai.FormDialog d = new HopThoai.FormDialog(this, Api.s(o, "tieuDe"),
                Api.s(o, "maThongBao") + "  •  " + HocSinhUI.thoiGian(Api.s(o, "ngay")), 520);
        d.truong("Gửi tới", HopThoai.oChiXem(Api.s(o, "doiTuong")), true);
        JTextArea nd = UI.vungChu(7);
        nd.setText(Api.s(o, "noiDung"));
        nd.setEditable(false);
        nd.setCaretPosition(0);
        d.truong("Nội dung", UI.cuonVungChu(nd), true);
        JButton dong = UI.nut("Đóng", UI.Kieu.CHINH);
        dong.addActionListener(e -> d.dispose());
        d.nut(dong);

        danhDauDaXem(dieuHuong.tenDangNhap(), List.of(Api.n(o, "id")));
        hienThi();
        dieuHuong.capNhatChuong();
        d.hien();
    }

    // ---------------- Đã đọc / chưa đọc (lưu trên máy, riêng từng tài khoản) ----------------

    private static Preferences luuTru(String tenDangNhap) {
        return Preferences.userNodeForPackage(ThongBaoSinhVien.class).node("thongbao_" + tenDangNhap);
    }

    public static Set<Long> daXem(String tenDangNhap) {
        Set<Long> kq = new HashSet<>();
        for (String s : luuTru(tenDangNhap).get("daXem", "").split(",")) {
            if (!s.isBlank()) kq.add(Long.parseLong(s.trim()));
        }
        return kq;
    }

    public static void danhDauDaXem(String tenDangNhap, Collection<Long> ids) {
        Set<Long> tap = daXem(tenDangNhap);
        tap.addAll(ids);
        StringJoiner sj = new StringJoiner(",");
        for (Long id : tap) sj.add(String.valueOf(id));
        luuTru(tenDangNhap).put("daXem", sj.toString());
    }
}
