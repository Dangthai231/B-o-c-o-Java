package com.eventmanager.fe.gui.Admin.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * JTable đã làm đẹp: dòng cao, không kẻ dọc, đổi màu khi rê chuột,
 * hỗ trợ cột "nhãn trạng thái" và cột "thao tác" chứa các nút bấm.
 *
 * Mẹo: mỗi dòng của bảng lưu kèm id ở cột 0 (ẩn đi), khi bấm nút sẽ biết đang thao tác với bản ghi nào.
 */
public class BangDep extends JTable {

    private int dongHover = -1;
    private final DefaultTableModel model;

    public BangDep(String... tenCot) {
        model = new DefaultTableModel(tenCot, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return getColumnClass(column) == Boolean.class;
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return getRowCount() > 0 && getValueAt(0, c) instanceof Boolean ? Boolean.class : Object.class;
            }
        };
        setModel(model);
        setRowHeight(46);
        setShowVerticalLines(false);
        setShowHorizontalLines(true);
        setGridColor(new Color(0xEEF1F5));
        setIntercellSpacing(new Dimension(0, 1));
        setFont(Theme.BODY);
        setForeground(Theme.TEXT);
        setSelectionBackground(Theme.PRIMARY_SOFT);
        setSelectionForeground(Theme.TEXT);
        setFillsViewportHeight(true);
        setBackground(Color.WHITE);
        setFocusable(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader h = getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 42));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                l.setFont(Theme.SMALL_BOLD);
                l.setForeground(Theme.TEXT_2);
                l.setBackground(Theme.TABLE_HEAD);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER), new EmptyBorder(0, 10, 0, 6)));
                return l;
            }
        });

        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, s, false, r, c);
                l.setBorder(new EmptyBorder(0, 10, 0, 6));
                l.setFont(Theme.BODY);
                l.setForeground(Theme.TEXT);
                l.setBackground(nenDong(r, s));
                return l;
            }
        });

        // Ô tích chọn (cột Boolean)
        setDefaultRenderer(Boolean.class, (t, v, s, f, r, c) -> {
            JCheckBox cb = new JCheckBox();
            cb.setSelected(Boolean.TRUE.equals(v));
            cb.setHorizontalAlignment(SwingConstants.CENTER);
            cb.setOpaque(true);
            cb.setBackground(nenDong(r, s));
            return cb;
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int r = rowAtPoint(e.getPoint());
                if (r != dongHover) {
                    dongHover = r;
                    repaint();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                dongHover = -1;
                repaint();
            }
        });
    }

    public Color nenDong(int r, boolean chon) {
        if (chon) return Theme.PRIMARY_SOFT;
        return r == dongHover ? Theme.ROW_HOVER : Color.WHITE;
    }

    public DefaultTableModel getModelBang() {
        return model;
    }

    public void xoaHet() {
        model.setRowCount(0);
    }

    public void themDong(Object... giaTri) {
        model.addRow(giaTri);
    }

    /** Ẩn cột (thường là cột id). */
    public void anCot(int cot) {
        getColumnModel().getColumn(cot).setMinWidth(0);
        getColumnModel().getColumn(cot).setMaxWidth(0);
        getColumnModel().getColumn(cot).setPreferredWidth(0);
    }

    public void doRongCot(int cot, int rong) {
        getColumnModel().getColumn(cot).setPreferredWidth(rong);
    }

    public void coDinhCot(int cot, int rong) {
        getColumnModel().getColumn(cot).setPreferredWidth(rong);
        getColumnModel().getColumn(cot).setMinWidth(rong);
        getColumnModel().getColumn(cot).setMaxWidth(rong);
    }

    /** Lấy id (cột 0) của dòng hiển thị r. */
    public long idDong(int r) {
        Object v = model.getValueAt(convertRowIndexToModel(r), 0);
        return v instanceof Number n ? n.longValue() : Long.parseLong(v.toString());
    }

    // ---------------- Cột hiển thị nhãn trạng thái ----------------
    public void cotNhan(int cot) {
        getColumnModel().getColumn(cot).setMinWidth(112); // đủ chỗ cho nhãn, không bị cắt chữ
        getColumnModel().getColumn(cot).setCellRenderer((t, v, s, f, r, c) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 11));
            p.setBackground(nenDong(r, s));
            UI.Badge b = new UI.Badge();
            b.setText(v == null ? "" : v.toString());
            p.add(b);
            return p;
        });
    }

    /** Cột 2 dòng: dòng chính đậm + dòng phụ mờ. Giá trị ô dạng "chính\nphụ". */
    public void cotHaiDong(int cot) {
        getColumnModel().getColumn(cot).setCellRenderer((t, v, s, f, r, c) -> {
            String[] p = (v == null ? "" : v.toString()).split("\n", 2);
            JPanel box = new JPanel();
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.setBorder(new EmptyBorder(5, 10, 5, 6));
            box.setBackground(nenDong(r, s));
            JLabel a = UI.nhan(p[0], Theme.BODY_BOLD, Theme.TEXT);
            box.add(Box.createVerticalGlue());
            box.add(a);
            if (p.length > 1) box.add(UI.nhan(p[1], Theme.SMALL, Theme.MUTED));
            box.add(Box.createVerticalGlue());
            return box;
        });
    }

    // ---------------- Cột chứa các nút thao tác ----------------

    /** Mô tả 1 nút trong cột thao tác. */
    public record NutThaoTac(String ma, String chu, String icon, String goiY) {
    }

    /**
     * @param hien   (mã nút, dòng model) -> có hiện nút ở dòng này không
     * @param khiBam (mã nút, dòng model) -> xử lý khi bấm
     */
    public void cotThaoTac(int cot, List<NutThaoTac> nuts, BiPredicate<String, Integer> hien,
                           BiConsumer<String, Integer> khiBam) {
        TableCellRenderer renderer = (t, v, s, f, r, c) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 8));
            p.setBackground(nenDong(r, s));
            int dongModel = convertRowIndexToModel(r);
            for (NutThaoTac n : nuts) {
                if (hien != null && !hien.test(n.ma(), dongModel)) continue;
                UI.FlatButton b = n.chu() == null ? UI.nutIcon(n.icon(), n.goiY())
                        : UI.nut(n.chu(), n.icon(), UI.Kieu.VIEN);
                if (n.chu() != null) {
                    b.setFont(Theme.SMALL);
                    b.setBorder(new EmptyBorder(5, 8, 5, 8));
                }
                b.setName(n.ma());
                p.add(b);
            }
            return p;
        };
        getColumnModel().getColumn(cot).setCellRenderer(renderer);

        // Bấm chuột: dựng lại ô để biết điểm bấm rơi vào nút nào
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String ma = nutTaiDiem(e.getPoint(), cot, renderer);
                if (ma != null) {
                    int r = rowAtPoint(e.getPoint());
                    khiBam.accept(ma, convertRowIndexToModel(r));
                }
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                String ma = nutTaiDiem(e.getPoint(), cot, renderer);
                setCursor(Cursor.getPredefinedCursor(ma != null ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                setToolTipText(ma == null ? null : goiYCuaMa(nuts, ma));
            }
        });
    }

    private String goiYCuaMa(List<NutThaoTac> nuts, String ma) {
        for (NutThaoTac n : nuts) if (n.ma().equals(ma)) return n.goiY();
        return null;
    }

    private String nutTaiDiem(Point pt, int cot, TableCellRenderer renderer) {
        int r = rowAtPoint(pt);
        int c = columnAtPoint(pt);
        if (r < 0 || c != convertColumnIndexToView(cot)) return null;
        Rectangle o = getCellRect(r, c, false);
        Component comp = renderer.getTableCellRendererComponent(this, null, false, false, r, c);
        comp.setBounds(0, 0, o.width, o.height);
        comp.doLayout();
        if (comp instanceof Container ct) {
            for (Component con : ct.getComponents()) {
                Rectangle b = con.getBounds();
                if (b.contains(pt.x - o.x, pt.y - o.y)) return con.getName();
            }
        }
        return null;
    }

    /** Đặt bảng vào khung cuộn không viền. */
    public JScrollPane trongKhungCuon() {
        JScrollPane sp = UI.cuon(this);
        sp.getViewport().setBackground(Color.WHITE);
        sp.getViewport().setOpaque(true);
        return sp;
    }
}
