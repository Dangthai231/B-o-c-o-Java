package com.eventmanager.fe.gui.Admin;

import com.eventmanager.fe.api.Api;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * MÀN HÌNH "GIAO CÔNG VIỆC" của Admin (Admin -> Cán bộ)
 *
 * Admin tạo công việc, chọn cán bộ phụ trách -> lưu bảng cong_viec với nguoiGiao = "admin".
 * Cán bộ mở "Quản lý công việc" sẽ thấy việc đó (tick "Việc giao cho tôi" để chỉ xem việc của mình)
 * và tự cập nhật tiến độ / trạng thái; Admin xem lại tiến độ ở đây.
 */
public class QuanLyCongViecPanel extends TrangAdmin {

    private static final String[] UU_TIEN = {"Trung bình", "Cao", "Thấp"};

    private final BangDep bang = new BangDep("ID", "Mã CV", "Công việc", "Sự kiện", "Cán bộ phụ trách",
            "Hạn chót", "Tiến độ", "Trạng thái", "Thao tác");
    private final JComboBox<String> cbLocCanBo = UI.hopChon(new String[]{"Tất cả cán bộ"}, Icons.USER);
    private final UI.TextFieldDep oTim = UI.oTimKiem("Tìm theo mã, tên công việc...");

    private final List<JsonObject> tatCa = new ArrayList<>();
    private final List<JsonObject> dangHien = new ArrayList<>();
    private final List<String> dsCanBo = new ArrayList<>();      // "Họ tên (canbo01)"
    private final List<long[]> idSuKien = new ArrayList<>();
    private final List<String> tenSuKien = new ArrayList<>();

    public QuanLyCongViecPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        JPanel goc = new JPanel(new BorderLayout(0, 16));
        goc.setOpaque(false);

        // Tiêu đề + nút giao việc
        JPanel dau = new JPanel(new BorderLayout());
        dau.setOpaque(false);
        dau.add(UI.tieuDeTrang("Giao công việc",
                "Giao việc cho cán bộ và theo dõi tiến độ"), BorderLayout.WEST);
        UI.FlatButton btnGiao = UI.nut("Giao việc mới", Icons.PLUS, UI.Kieu.CHINH);
        btnGiao.addActionListener(e -> formCongViec(null));
        UI.FlatButton btnLamMoi = UI.nut("Làm mới", Icons.REFRESH, UI.Kieu.VIEN);
        btnLamMoi.addActionListener(e -> taiLai());
        dau.add(UI.hangPhai(10, btnLamMoi, btnGiao), BorderLayout.EAST);
        goc.add(dau, BorderLayout.NORTH);

        // Thẻ chứa bộ lọc + bảng
        UI.RoundPanel the = UI.card(new BorderLayout(0, 12), 16);
        cbLocCanBo.setPreferredSize(new Dimension(260, 36));
        cbLocCanBo.addActionListener(e -> hienBang());
        oTim.setPreferredSize(new Dimension(280, 36));
        oTim.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { hienBang(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { hienBang(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { hienBang(); }
        });
        the.add(UI.hang(10, cbLocCanBo, oTim), BorderLayout.NORTH);

        bang.setRowHeight(52);
        bang.anCot(0);
        bang.coDinhCot(1, 78);
        bang.cotHaiDong(2);
        bang.doRongCot(2, 240);
        bang.doRongCot(3, 170);
        bang.doRongCot(4, 170);
        bang.coDinhCot(5, 100);
        bang.coDinhCot(6, 80);
        bang.cotNhan(7);
        bang.coDinhCot(8, 110);
        bang.cotThaoTac(8, List.of(
                        new BangDep.NutThaoTac("sua", null, Icons.EDIT, "Sửa / đổi cán bộ"),
                        new BangDep.NutThaoTac("xoa", null, Icons.TRASH, "Xóa công việc")),
                null,
                (ma, dong) -> {
                    JsonObject cv = dangHien.get(dong);
                    if ("sua".equals(ma)) formCongViec(cv);
                    else xoa(cv);
                });
        the.add(UI.cuon(bang), BorderLayout.CENTER);
        goc.add(the, BorderLayout.CENTER);

        vungNoiDung.add(goc, BorderLayout.CENTER);
    }

    // ======================= TẢI DỮ LIỆU =======================
    @Override
    public void taiLai() {
        Api.chayNen(this, () -> Api.get("/api/cong-viec/nhan-su"), kq -> {
            Object chon = cbLocCanBo.getSelectedItem();
            dsCanBo.clear();
            cbLocCanBo.removeAllItems();
            cbLocCanBo.addItem("Tất cả cán bộ");
            for (JsonElement el : Api.arr(kq)) {
                String ht = Api.s(el.getAsJsonObject(), "hienThi");
                dsCanBo.add(ht);
                cbLocCanBo.addItem(ht);
            }
            if (chon != null) cbLocCanBo.setSelectedItem(chon);
        });
        Api.chayNen(this, () -> Api.get("/api/cong-viec/su-kien"), kq -> {
            idSuKien.clear();
            tenSuKien.clear();
            for (JsonElement el : Api.arr(kq)) {
                JsonObject o = el.getAsJsonObject();
                idSuKien.add(new long[]{Api.n(o, "id")});
                tenSuKien.add(Api.s(o, "tenSuKien"));
            }
        });
        Api.chayNen(this, () -> Api.get("/api/cong-viec/danh-sach"), kq -> {
            tatCa.clear();
            for (JsonElement el : Api.arr(kq)) tatCa.add(el.getAsJsonObject());
            hienBang();
        });
    }

    private void hienBang() {
        String canBo = cbLocCanBo.getSelectedIndex() > 0 ? String.valueOf(cbLocCanBo.getSelectedItem()) : null;
        String tu = oTim.getText().trim().toLowerCase();
        dangHien.clear();
        bang.xoaHet();
        for (JsonObject o : tatCa) {
            if (canBo != null && !canBo.equals(Api.s(o, "nhanSu"))) continue;
            String chu = (Api.s(o, "maCv") + " " + Api.s(o, "tenCongViec")).toLowerCase();
            if (!tu.isEmpty() && !chu.contains(tu)) continue;
            dangHien.add(o);
            String nguoiGiao = "admin".equalsIgnoreCase(Api.s(o, "nguoiGiao")) ? "Admin giao" : "Cán bộ tự lập";
            String ns = Api.s(o, "nhanSu");
            String hc = Api.s(o, "hanChot");
            bang.themDong(Api.n(o, "id"), Api.s(o, "maCv"),
                    Api.s(o, "tenCongViec") + "\n" + nguoiGiao + "  |  Ưu tiên: " + Api.s(o, "mucDoUuTien"),
                    Api.s(o, "tenSuKien").isEmpty() ? "—" : Api.s(o, "tenSuKien"),
                    ns.isEmpty() ? "Chưa phân công" : ns,
                    hc.isEmpty() ? "—" : hc,
                    Api.n(o, "tienDo") + "%",
                    Api.s(o, "trangThai"), "");
        }
        hienSoKetQua(dangHien.size());
    }

    // ======================= GIAO / SỬA =======================
    private void formCongViec(JsonObject cv) {
        boolean sua = cv != null;
        HopThoai.FormDialog d = new HopThoai.FormDialog(this, sua ? "Sửa công việc " + Api.s(cv, "maCv") : "Giao việc mới",
                "Cán bộ được chọn sẽ thấy công việc này trong mục Quản lý công việc", 600);

        UI.TextFieldDep tfTen = UI.oNhap("VD: Chuẩn bị hội trường");
        JComboBox<String> cbSuKien = UI.hopChon(new String[]{"(Không gắn sự kiện)"}, Icons.CALENDAR);
        for (String t : tenSuKien) cbSuKien.addItem(t);
        JComboBox<String> cbCanBo = UI.hopChon(dsCanBo.toArray(new String[0]), Icons.USER);
        JComboBox<String> cbUuTien = UI.hopChon(UU_TIEN, Icons.LIST);
        UI.TextFieldDep tfHan = UI.oNhap("yyyy-MM-dd, VD: " + LocalDate.now().plusDays(7));
        JTextArea taMoTa = UI.vungChu(4);

        if (sua) {
            tfTen.setText(Api.s(cv, "tenCongViec"));
            int i = tenSuKien.indexOf(Api.s(cv, "tenSuKien"));
            cbSuKien.setSelectedIndex(i >= 0 ? i + 1 : 0);
            cbCanBo.setSelectedItem(Api.s(cv, "nhanSu"));
            cbUuTien.setSelectedItem(Api.s(cv, "mucDoUuTien"));
            tfHan.setText(Api.s(cv, "hanChot"));
            taMoTa.setText(Api.s(cv, "moTa"));
        }

        d.truong("Tên công việc *", tfTen, true);
        d.truong("Cán bộ phụ trách *", cbCanBo, false);
        d.truong("Sự kiện", cbSuKien, false);
        d.truong("Mức ưu tiên", cbUuTien, false);
        d.truong("Hạn chót", tfHan, false);
        d.truong("Mô tả / yêu cầu", UI.cuonVungChu(taMoTa), true);

        UI.FlatButton huy = UI.nut("Hủy", UI.Kieu.VIEN);
        huy.addActionListener(e -> d.dispose());
        UI.FlatButton luu = UI.nut(sua ? "Lưu" : "Giao việc", Icons.SEND, UI.Kieu.CHINH);
        luu.addActionListener(e -> {
            String ten = tfTen.getText().trim();
            String han = tfHan.getText().trim();
            if (ten.isEmpty()) { HopThoai.loi(d, "Vui lòng nhập tên công việc"); return; }
            if (cbCanBo.getSelectedItem() == null) { HopThoai.loi(d, "Chưa có cán bộ nào để giao việc"); return; }
            if (!han.isEmpty()) {
                try { LocalDate.parse(han); } catch (Exception ex) {
                    HopThoai.loi(d, "Hạn chót phải có dạng yyyy-MM-dd"); return;
                }
            }
            JsonObject body = new JsonObject();
            body.addProperty("tenCongViec", ten);
            body.addProperty("nhanSu", String.valueOf(cbCanBo.getSelectedItem()));
            body.addProperty("mucDoUuTien", String.valueOf(cbUuTien.getSelectedItem()));
            body.addProperty("moTa", taMoTa.getText().trim());
            if (!han.isEmpty()) body.addProperty("hanChot", han);
            int sk = cbSuKien.getSelectedIndex();
            if (sk > 0) body.addProperty("suKienId", idSuKien.get(sk - 1)[0]);
            if (sua) {
                // cap-nhat ghi đè toàn bộ -> giữ nguyên mã, trạng thái, tiến độ cán bộ đã cập nhật
                body.addProperty("maCv", Api.s(cv, "maCv"));
                body.addProperty("trangThai", Api.s(cv, "trangThai"));
                body.addProperty("tienDo", Api.n(cv, "tienDo"));
            } else {
                body.addProperty("nguoiGiao", "admin");
            }
            Api.chayThaoTac(d, () -> sua ? Api.put("/api/cong-viec/cap-nhat/" + Api.n(cv, "id"), body)
                    : Api.post("/api/cong-viec/them", body), kq -> {
                d.dispose();
                HopThoai.thanhCong(this, sua ? "Đã lưu công việc"
                        : "Đã giao " + Api.s(Api.obj(kq), "maCv") + " cho " + cbCanBo.getSelectedItem());
                taiLai();
            });
        });
        d.nut(huy);
        d.nut(luu);
        d.hien();
    }

    private void xoa(JsonObject cv) {
        if (!HopThoai.xacNhan(this, "Xóa công việc", "Xóa công việc <b>" + Api.s(cv, "tenCongViec") + "</b>?",
                "Xóa", UI.Kieu.NGUY_HIEM)) return;
        Api.chayThaoTac(this, () -> Api.delete("/api/cong-viec/xoa/" + Api.n(cv, "id")), r -> {
            HopThoai.thanhCong(this, "Đã xóa công việc");
            taiLai();
        });
    }
}
