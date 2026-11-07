package com.eventmanager.fe.gui.HocSinh;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Use case "Đánh giá và phản hồi sự kiện".
 * Trái: form gửi đánh giá bằng 5 ngôi sao + nhận xét. Phải: các đánh giá đã gửi (bấm để sửa).
 * Chỉ các sự kiện "Đã tham gia" mới được đánh giá (backend cũng kiểm tra lại).
 */
public class DanhGiaPhanHoi extends TrangSinhVien {

    private static final int TOI_DA_KY_TU = 1000;

    private final JComboBox<SuKienItem> cbSuKien = UI.hopChon(new SuKienItem[0], Icons.CALENDAR);
    private final SaoDanhGia sao = new SaoDanhGia(34, true);
    private final JLabel lblMoTaSao = UI.nhan("Bấm vào ngôi sao để chọn", Theme.BODY_BOLD, Theme.MUTED);
    private final JTextArea txtNhanXet = UI.vungChu(5);
    private final JLabel lblDem = UI.nhan("0 / " + TOI_DA_KY_TU, Theme.SMALL, Theme.MUTED);
    private final UI.FlatButton btnGui = UI.nut("Gửi đánh giá", Icons.SEND, UI.Kieu.CHINH);
    private final BangDep bang = new BangDep("id", "Sự kiện", "Số sao", "Nhận xét", "Ngày gửi");
    private final JLabel lblSoLuong = UI.nhan(" ", Theme.SMALL, Theme.MUTED);
    private final List<JsonObject> daGui = new ArrayList<>();
    private long chonSanSuKien;

    // Mục trong hộp chọn: hiện tên, giữ id
    private record SuKienItem(long id, String ten, boolean daDanhGia) {
        @Override
        public String toString() {
            return daDanhGia ? ten + "  (đã đánh giá)" : ten;
        }
    }

    public DanhGiaPhanHoi(DieuHuong dieuHuong) {
        super(dieuHuong);
        add(UI.tieuDeTrang("Đánh giá và phản hồi", "Chấm sao và góp ý cho các sự kiện bạn đã tham gia"), BorderLayout.NORTH);

        JPanel giua = new JPanel(new BorderLayout(18, 0));
        giua.setOpaque(false);
        giua.add(taoForm(), BorderLayout.WEST);

        bang.anCot(0);
        bang.cotHaiDong(1);
        bang.doRongCot(1, 220);
        bang.coDinhCot(2, 120);
        SaoDanhGia.cotSao(bang, 2);
        bang.doRongCot(3, 260);
        bang.coDinhCot(4, 130);
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && bang.getSelectedRow() >= 0) {
                napVaoForm(daGui.get(bang.convertRowIndexToModel(bang.getSelectedRow())));
            }
        });
        JPanel bangVaDem = new JPanel(new BorderLayout(0, 10));
        bangVaDem.setOpaque(false);
        bangVaDem.add(bang.trongKhungCuon(), BorderLayout.CENTER);
        bangVaDem.add(lblSoLuong, BorderLayout.SOUTH);
        giua.add(HocSinhUI.the("Đánh giá bạn đã gửi", null, bangVaDem), BorderLayout.CENTER);
        add(giua, BorderLayout.CENTER);
    }

    private JComponent taoForm() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        // BoxLayout chỉ thẳng hàng khi mọi thành phần cùng căn trái
        cbSuKien.setAlignmentX(LEFT_ALIGNMENT);
        cbSuKien.setPreferredSize(new Dimension(320, 38));
        cbSuKien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        sao.setKhiDoi(so -> {
            lblMoTaSao.setText(SaoDanhGia.moTa(so));
            lblMoTaSao.setForeground(so == 0 ? Theme.MUTED : so <= 2 ? Theme.DANGER : so == 3 ? Theme.WARNING : Theme.SUCCESS);
        });
        txtNhanXet.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { demKyTu(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { demKyTu(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { demKyTu(); }
        });
        btnGui.addActionListener(e -> guiDanhGia());

        form.add(nhanTruong("Sự kiện đã tham gia"));
        form.add(cbSuKien);
        form.add(Box.createVerticalStrut(18));
        form.add(nhanTruong("Mức độ hài lòng"));
        JPanel hangSao = new JPanel(new BorderLayout(14, 0));
        hangSao.setOpaque(false);
        hangSao.add(sao, BorderLayout.WEST);
        hangSao.add(lblMoTaSao, BorderLayout.CENTER);
        hangSao.setAlignmentX(LEFT_ALIGNMENT);
        hangSao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        form.add(hangSao);
        form.add(Box.createVerticalStrut(18));
        form.add(nhanTruong("Nhận xét / góp ý"));
        JScrollPane cuon = UI.cuonVungChu(txtNhanXet);
        cuon.setAlignmentX(LEFT_ALIGNMENT);
        cuon.setPreferredSize(new Dimension(320, 130));
        cuon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        form.add(cuon);
        form.add(Box.createVerticalStrut(6));
        lblDem.setAlignmentX(LEFT_ALIGNMENT);
        form.add(lblDem);
        form.add(Box.createVerticalStrut(16));
        JPanel hangNut = UI.hang(0, btnGui);
        ((FlowLayout) hangNut.getLayout()).setHgap(0);
        hangNut.setAlignmentX(LEFT_ALIGNMENT);
        hangNut.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        form.add(hangNut);
        form.add(Box.createVerticalGlue());

        UI.RoundPanel the = HocSinhUI.the("Gửi đánh giá", null, form);
        the.setPreferredSize(new Dimension(380, 0));
        return the;
    }

    private static JComponent nhanTruong(String chu) {
        JLabel l = UI.nhan(chu, Theme.SMALL_BOLD, Theme.TEXT_2);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 0, 6, 0));
        return l;
    }

    private void demKyTu() {
        int n = txtNhanXet.getText().length();
        lblDem.setText(n + " / " + TOI_DA_KY_TU);
        lblDem.setForeground(n > TOI_DA_KY_TU ? Theme.DANGER : Theme.MUTED);
    }

    /** Trang Sự kiện của tôi bấm "Đánh giá" -> mở trang này và chọn sẵn sự kiện đó. */
    public void chonSuKienKhiTai(long suKienId) {
        chonSanSuKien = suKienId;
    }

    /** Sự kiện được đánh giá = "Đã tham gia" (hoặc đã đăng ký và sự kiện đã diễn ra) + các đánh giá đã gửi. */
    @Override
    public void taiLai() {
        String ten = Api.e(dieuHuong.tenDangNhap());
        Api.chayNen(this, () -> {
            JsonObject kq = new JsonObject();
            kq.add("dangKy", Api.get("/api/dang-ky/cua-toi?tenDangNhap=" + ten));
            kq.add("danhGia", Api.get("/api/danh-gia/cua-toi?tenDangNhap=" + ten));
            return kq;
        }, kq -> {
            daGui.clear();
            bang.xoaHet();
            java.util.Set<Long> idDaGui = new java.util.HashSet<>();
            for (JsonElement el : Api.arr(kq, "danhGia")) {
                JsonObject o = el.getAsJsonObject();
                daGui.add(o);
                idDaGui.add(Api.n(o, "suKienId"));
                bang.themDong(Api.n(o, "suKienId"), Api.s(o, "tenSuKien"), (int) Api.n(o, "soSao"),
                        Api.s(o, "noiDung"), HocSinhUI.thoiGian(Api.s(o, "ngayDanhGia")));
            }
            lblSoLuong.setText(daGui.isEmpty() ? "Bạn chưa gửi đánh giá nào"
                    : daGui.size() + " đánh giá  •  Chọn một dòng để sửa lại");

            long muonChon = chonSanSuKien;
            chonSanSuKien = 0;
            cbSuKien.removeAllItems();
            for (JsonElement el : Api.arr(kq, "dangKy")) {
                JsonObject o = el.getAsJsonObject();
                String tt = Api.s(o, "trangThai");
                boolean duoc = "Đã tham gia".equals(tt)
                        || ("Đã đăng ký".equals(tt) && HocSinhUI.daBatDau(Api.s(o, "thoiGianBatDau")));
                if (!duoc) continue;
                long id = Api.n(o, "suKienId");
                SuKienItem item = new SuKienItem(id, Api.s(o, "tenSuKien"), idDaGui.contains(id));
                cbSuKien.addItem(item);
                if (id == muonChon) cbSuKien.setSelectedItem(item);
            }
            boolean coSuKien = cbSuKien.getItemCount() > 0;
            cbSuKien.setEnabled(coSuKien);
            btnGui.setEnabled(coSuKien);
            if (!coSuKien) lblMoTaSao.setText("Bạn chưa tham gia sự kiện nào để đánh giá");

            // Được chuyển tới từ "Sự kiện của tôi" và đã đánh giá rồi -> nạp đánh giá cũ để sửa
            for (JsonObject o : daGui) {
                if (Api.n(o, "suKienId") == muonChon) napVaoForm(o);
            }
        });
    }

    private void napVaoForm(JsonObject dg) {
        long id = Api.n(dg, "suKienId");
        for (int i = 0; i < cbSuKien.getItemCount(); i++) {
            if (cbSuKien.getItemAt(i).id() == id) cbSuKien.setSelectedIndex(i);
        }
        sao.setGiaTri((int) Api.n(dg, "soSao"));
        txtNhanXet.setText(Api.s(dg, "noiDung"));
        btnGui.setText("Cập nhật đánh giá");
    }

    /** POST /api/danh-gia/gui (gửi lại cho cùng sự kiện = sửa đánh giá cũ). */
    private void guiDanhGia() {
        SuKienItem sk = (SuKienItem) cbSuKien.getSelectedItem();
        if (sk == null) {
            HopThoai.loi(this, "Bạn chưa có sự kiện nào đã tham gia để đánh giá");
            return;
        }
        if (sao.getGiaTri() == 0) {
            HopThoai.loi(this, "Hãy bấm chọn số sao (1 - 5) trước khi gửi");
            return;
        }
        String noiDung = txtNhanXet.getText().trim();
        if (noiDung.length() > TOI_DA_KY_TU) {
            HopThoai.loi(this, "Nhận xét tối đa " + TOI_DA_KY_TU + " ký tự");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("suKienId", sk.id());
        body.addProperty("tenDangNhap", dieuHuong.tenDangNhap());
        body.addProperty("soSao", sao.getGiaTri());
        body.addProperty("noiDung", noiDung);
        Api.chayNen(this, () -> Api.post("/api/danh-gia/gui", body), kq -> {
            HopThoai.thanhCong(this, "Cảm ơn bạn đã gửi đánh giá!");
            sao.setGiaTri(0);
            lblMoTaSao.setText("Bấm vào ngôi sao để chọn");
            txtNhanXet.setText("");
            btnGui.setText("Gửi đánh giá");
            taiLai();
        });
    }
}