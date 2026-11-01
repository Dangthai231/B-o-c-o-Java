# -*- coding: utf-8 -*-
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

def set_cell_background(cell, fill_hex):
    tcPr = cell._tc.get_or_add_tcPr()
    tcPr.append(parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>'))

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
        node = OxmlElement(f'w:{m}')
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def set_table_borders(table, color="D0D7DE", sz="4", val="single"):
    tblPr = table._tbl.tblPr
    borders = parse_xml(
        f'<w:tblBorders {nsdecls("w")}>'
        f'  <w:top w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'  <w:bottom w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'  <w:left w:val="none"/>'
        f'  <w:right w:val="none"/>'
        f'  <w:insideH w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
        f'  <w:insideV w:val="none"/>'
        f'</w:tblBorders>'
    )
    tblPr.append(borders)

def format_paragraph(p, space_before=0, space_after=6, line_spacing=1.15):
    p.paragraph_format.space_before = Pt(space_before)
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.line_spacing = line_spacing

def add_heading_1(doc, text):
    h = doc.add_paragraph()
    format_paragraph(h, space_before=14, space_after=6)
    run = h.add_run(text)
    run.font.name = 'Times New Roman'
    run.font.size = Pt(16)
    run.font.bold = True
    run.font.color.rgb = RGBColor(15, 76, 129) # Classic Navy
    return h

def add_heading_2(doc, text):
    h = doc.add_paragraph()
    format_paragraph(h, space_before=10, space_after=4)
    run = h.add_run(text)
    run.font.name = 'Times New Roman'
    run.font.size = Pt(13.5)
    run.font.bold = True
    run.font.color.rgb = RGBColor(43, 88, 118) # Steel Blue
    return h

def add_heading_3(doc, text):
    h = doc.add_paragraph()
    format_paragraph(h, space_before=6, space_after=2)
    run = h.add_run(text)
    run.font.name = 'Times New Roman'
    run.font.size = Pt(12)
    run.font.bold = True
    run.font.color.rgb = RGBColor(50, 50, 50)
    return h

def add_body_p(doc, text, bold_prefix="", italic=False):
    p = doc.add_paragraph()
    format_paragraph(p, space_before=0, space_after=4)
    if bold_prefix:
        r_pre = p.add_run(bold_prefix)
        r_pre.font.name = 'Times New Roman'
        r_pre.font.size = Pt(12)
        r_pre.font.bold = True
    r = p.add_run(text)
    r.font.name = 'Times New Roman'
    r.font.size = Pt(12)
    r.font.italic = italic
    return p

def add_callout_box(doc, title, content_list):
    table = doc.add_table(rows=1, cols=1)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = table.cell(0, 0)
    set_cell_background(cell, "F0F4F8")
    set_cell_margins(cell, top=140, bottom=140, left=200, right=200)
    
    # Left border highlight
    tcPr = cell._tc.get_or_add_tcPr()
    borders = parse_xml(
        f'<w:tcBorders {nsdecls("w")}>'
        f'  <w:left w:val="single" w:sz="24" w:space="0" w:color="0F4C81"/>'
        f'  <w:top w:val="none"/>'
        f'  <w:right w:val="none"/>'
        f'  <w:bottom w:val="none"/>'
        f'</w:tcBorders>'
    )
    tcPr.append(borders)
    
    p = cell.paragraphs[0]
    format_paragraph(p, 0, 4)
    r_t = p.add_run(title)
    r_t.font.name = 'Times New Roman'
    r_t.font.size = Pt(12.5)
    r_t.font.bold = True
    r_t.font.color.rgb = RGBColor(15, 76, 129)
    
    for item in content_list:
        p_i = cell.add_paragraph()
        format_paragraph(p_i, 0, 2)
        r_i = p_i.add_run(f"• {item}")
        r_i.font.name = 'Times New Roman'
        r_i.font.size = Pt(11.5)
        r_i.font.color.rgb = RGBColor(40, 40, 40)
    
    doc.add_paragraph() # spacing

def style_table(table, col_widths, headers, data, header_bg="0F4C81", alt_bg="F7F9FB"):
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    set_table_borders(table)
    
    # Header row
    hdr_cells = table.rows[0].cells
    for i, title in enumerate(headers):
        hdr_cells[i].text = title
        set_cell_background(hdr_cells[i], header_bg)
        set_cell_margins(hdr_cells[i], top=120, bottom=120, left=140, right=140)
        p = hdr_cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        format_paragraph(p, 0, 0)
        for run in p.runs:
            run.font.name = 'Times New Roman'
            run.font.size = Pt(11)
            run.font.bold = True
            run.font.color.rgb = RGBColor(255, 255, 255)
            
    # Data rows
    for r_idx, row_data in enumerate(data):
        row_cells = table.add_row().cells
        bg = alt_bg if r_idx % 2 == 1 else "FFFFFF"
        for c_idx, val in enumerate(row_data):
            row_cells[c_idx].text = str(val)
            set_cell_background(row_cells[c_idx], bg)
            set_cell_margins(row_cells[c_idx], top=90, bottom=90, left=120, right=120)
            p = row_cells[c_idx].paragraphs[0]
            format_paragraph(p, 0, 0)
            
            # Căn giữa cho STT, Ngày, Trạng thái, Vai trò
            if c_idx in [0, 2, 3, 4, 5] and len(str(val)) < 25:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            else:
                p.alignment = WD_ALIGN_PARAGRAPH.LEFT
                
            for run in p.runs:
                run.font.name = 'Times New Roman'
                run.font.size = Pt(10.5)
                run.font.color.rgb = RGBColor(30, 30, 30)
                
    # Widths
    for row in table.rows:
        for i, w in enumerate(col_widths):
            row.cells[i].width = Inches(w)

def build_document(file_path):
    doc = Document()
    
    # Margins 2cm (~0.79 inch)
    for section in doc.sections:
        section.top_margin = Inches(0.8)
        section.bottom_margin = Inches(0.8)
        section.left_margin = Inches(0.8)
        section.right_margin = Inches(0.8)
        
    # ================= TITLE BANNER =================
    p_title = doc.add_paragraph()
    format_paragraph(p_title, space_before=10, space_after=4)
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_sub = p_title.add_run("BÁO CÁO KẾ HOẠCH DỰ ÁN & PHÂN CÔNG TIMELINE\n")
    r_sub.font.name = 'Times New Roman'
    r_sub.font.size = Pt(13)
    r_sub.font.bold = True
    r_sub.font.color.rgb = RGBColor(100, 100, 100)
    
    r_main = p_title.add_run("HỆ THỐNG QUẢN LÝ SỰ KIỆN LIÊN CHI ĐOÀN\n(JAVA SWING & SPRING BOOT REST API)")
    r_main.font.name = 'Times New Roman'
    r_main.font.size = Pt(20)
    r_main.font.bold = True
    r_main.font.color.rgb = RGBColor(15, 76, 129)
    
    p_meta = doc.add_paragraph()
    format_paragraph(p_meta, space_before=4, space_after=18)
    p_meta.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_meta = p_meta.add_run("Tài liệu đặc tả tiến độ phát triển phần mềm dựa trên thiết kế Kiến trúc & Sơ đồ Use Case chuẩn")
    r_meta.font.name = 'Times New Roman'
    r_meta.font.size = Pt(11.5)
    r_meta.font.italic = True
    r_meta.font.color.rgb = RGBColor(80, 80, 80)
    
    # ================= 1. THÔNG TIN CHUNG DỰ ÁN =================
    add_heading_1(doc, "I. TỔNG QUAN DỰ ÁN & PHÂN CÔNG NHÂN SỰ")
    
    add_body_p(doc, 
        "Dự án \"Hệ thống Quản lý Sự kiện\" được xây dựng với mục tiêu chuyển đổi số công tác tổ chức, quản lý và theo dõi sự kiện cho Liên Chi đoàn Khoa CNTT. "
        "Hệ thống vận hành theo mô hình kiến trúc phân tầng chuyên biệt Client-Server kết hợp Backend API mạnh mẽ và Giao diện Desktop hiện đại:")
        
    add_callout_box(doc, "Đặc tả Kiến trúc Công nghệ & Nền tảng triển khai:", [
        "Backend (BE): Java 17+, Framework Spring Boot 3.x, Spring Data JPA, Spring Security, Hibernate ORM.",
        "Frontend (FE): Java Swing phong cách hiện đại WinForms (Flat Design, bảng biểu trực quan, KPI Cards, Bar Chart).",
        "Giao thức tích hợp: RESTful API (HTTP Client, Gson JSON Serialization) trên cổng 8080.",
        "Cơ sở dữ liệu: MySQL Server 8.0 (Database: event_management_db) quản lý đồng bộ toàn bộ Entities."
    ])
    
    add_heading_2(doc, "1.1. Ma trận phân công nhân sự chủ chốt")
    add_body_p(doc, "Dựa trên yêu cầu dự án và sơ đồ Use Case của từng nhóm đối tượng, đội ngũ phát triển gồm 3 thành viên phụ trách 3 trụ cột nghiệp vụ cốt lõi:")
    
    headers_ns = ["STT", "Họ và tên", "Vai trò hệ thống", "Module nghiệp vụ phụ trách chính", "Đầu ra bàn giao"]
    widths_ns = [0.5, 1.2, 1.5, 2.5, 1.8]
    data_ns = [
        [
            "1", 
            "Ngọc Anh", 
            "Admin\n(Quản trị viên)", 
            "• Quản lý tài khoản & Phân quyền người dùng\n• Quản lý thông báo toàn hệ thống\n• Quản lý danh mục sự kiện & Phân công cán bộ", 
            "DashboardAdmin.java\nAuthController.java\nSecurityConfig.java\nDB Migration & Seed"
        ],
        [
            "2", 
            "Thái", 
            "Cán bộ phụ trách\n(Event Manager)", 
            "• Dashboard Tổng quan sự kiện & KPI Cards\n• Lập báo cáo & Thống kê (Biểu đồ cột)\n• Quản lý tài liệu sự kiện\n• Quản lý công việc & Tiến độ nhiệm vụ\n• Quản lý người tham gia & Điểm danh\n• Quản lý thông báo sự kiện", 
            "DashboardCanbo.java\nTrangchu.java\nLapbaocaothongke.java\nQuanlytailieusukien.java\nQuanlycongviec.java\nQuanlynguoithamgia.java\nQuanlythongbao.java"
        ],
        [
            "3", 
            "Thúy", 
            "Sinh viên\n(Student/User)", 
            "• Tra cứu sự kiện (Danh sách, tìm kiếm, lọc ngày)\n• Đăng ký tham gia & Hủy đăng ký sự kiện\n• Xem trạng thái tham gia sự kiện của tôi\n• Đánh giá và phản hồi chất lượng sự kiện\n• Quản lý & Cập nhật thông tin cá nhân", 
            "DashboardHocSinh.java\nTraCuuSuKien.java\nSuKienCuaToi.java\nDanhGiaPhanHoi.java\nThongTinCaNhan.java\nHocSinhUI.java"
        ]
    ]
    t_ns = doc.add_table(rows=1, cols=5)
    style_table(t_ns, widths_ns, headers_ns, data_ns, header_bg="1B365D")
    
    doc.add_paragraph() # Spacing
    
    # ================= 2. BẢN ĐỒ USE CASE & MAPPING KỸ THUẬT =================
    add_heading_1(doc, "II. PHÂN RÃ CÔNG VIỆC DỰA TRÊN SƠ ĐỒ USE CASE (WBS)")
    add_body_p(doc, "Bản đồ phân rã công việc (Work Breakdown Structure) được xây dựng bám sát 100% theo 3 sơ đồ Use Case chi tiết đã được thẩm định:")
    
    # Use Case 1: Admin
    add_heading_2(doc, "2.1. Phân hệ Admin (Phụ trách: Ngọc Anh)")
    add_body_p(doc, "Căn cứ theo Sơ đồ Use Case Admin, Ngọc Anh chịu trách nhiệm xây dựng 3 khối chức năng quản trị cấp cao:")
    
    headers_uc_admin = ["Nhóm Use Case", "Use Case chi tiết (extend)", "Thành phần Frontend (FE)", "API Backend & CSDL (BE)"]
    widths_uc_admin = [1.8, 2.3, 1.8, 1.6]
    data_uc_admin = [
        [
            "Phân quyền và Quản lý tài khoản",
            "• Tạo tài khoản mới\n• Duyệt thay đổi thông tin\n• Tra cứu tài khoản\n• Quản lý trạng thái (khóa/mở)\n• Cấp quyền, thay đổi, tra cứu quyền",
            "DashboardAdmin.java\nForm Quản lý User\nForm Phân quyền",
            "POST /api/auth/register\nGET /api/users\nPUT /api/users/{id}\nEntity: TaiKhoan, users"
        ],
        [
            "Quản lý thông báo",
            "• Tạo thông báo hệ thống\n• Duyệt & đăng thông báo\n• Cập nhật thông báo\n• Gỡ thông báo đã đăng\n• Tra cứu lịch sử thông báo",
            "Panel Duyệt thông báo\nForm Đăng tin tức\nBảng thông báo hệ thống",
            "POST /api/thong-bao/tao-moi\nPUT /api/thong-bao/cap-nhat\nDELETE /api/thong-bao/xoa\nEntity: ThongBao"
        ],
        [
            "Quản lý sự kiện",
            "• Tạo sự kiện mới\n• Cập nhật thông tin sự kiện\n• Duyệt kế hoạch sự kiện\n• Theo dõi tiến trình sự kiện\n• Phân công cán bộ phụ trách\n• Tra cứu sự kiện",
            "Panel Quản trị sự kiện\nForm duyệt kế hoạch\nDialog gán cán bộ phụ trách",
            "GET /api/events\nPOST /api/events/create\nPUT /api/events/assign\nTable: events, plans"
        ]
    ]
    t_uc_admin = doc.add_table(rows=1, cols=4)
    style_table(t_uc_admin, widths_uc_admin, headers_uc_admin, data_uc_admin, header_bg="2C3E50")
    
    doc.add_paragraph()

    # Use Case 2: Cán bộ phụ trách
    add_heading_2(doc, "2.2. Phân hệ Cán bộ phụ trách sự kiện (Phụ trách: Thái)")
    add_body_p(doc, "Căn cứ theo Sơ đồ Use Case Cán bộ, Thái chịu trách nhiệm hiện thực hóa toàn bộ luồng điều hành tác nghiệp sự kiện:")
    
    headers_uc_canbo = ["Nhóm Use Case", "Use Case chi tiết (extend)", "Thành phần Frontend (FE)", "API Backend & CSDL (BE)"]
    widths_uc_canbo = [1.8, 2.3, 1.8, 1.6]
    data_uc_canbo = [
        [
            "Tổng quan & Báo cáo thống kê",
            "• Xem 6 thẻ KPI Dashboard tổng quan\n• Lập báo cáo và thống kê sự kiện\n• Lọc số liệu theo khoảng ngày\n• Xem biểu đồ cột số sự kiện 12 tháng\n• Xem tỷ lệ tham dự & trạng thái",
            "Trangchu.java\nLapbaocaothongke.java\nCustom BarChartPanel",
            "GET /api/thongke/tong-quan\nGET /api/thongke/bao-cao\nTrangChuRepository.java"
        ],
        [
            "Quản lý tài liệu sự kiện",
            "• Tải lên tài liệu sự kiện mới\n• Xuất danh sách tài liệu ra file\n• Xóa tài liệu không còn hiệu lực\n• Xem loại file, kích thước, ngày tải",
            "Quanlytailieusukien.java\nJTable danh sách tài liệu\nDialog nạp file",
            "GET /api/tai-lieu/danh-sach\nPOST /api/tai-lieu/them\nEntity: TaiLieuSuKien"
        ],
        [
            "Quản lý công việc",
            "• Lập kế hoạch sự kiện\n• Phân công nhiệm vụ cho nhân sự\n• Theo dõi tiến độ\n• Cập nhật trạng thái công việc",
            "Quanlycongviec.java\nBảng danh sách công việc\nThanh tiến độ ProgressBar",
            "GET /api/cong-viec/danh-sach\nPOST /api/cong-viec/them\nEntity: CongViec"
        ],
        [
            "Quản lý người tham gia",
            "• Xem danh sách sinh viên tham gia\n• Tìm kiếm & hủy tìm kiếm theo từ khóa\n• Điểm danh sinh viên (Có mặt / Vắng)\n• Lưu kết quả điểm danh hàng loạt\n• Thống kê nhanh tổng, vắng, chưa điểm danh",
            "Quanlynguoithamgia.java\nTable Badge Renderer\nComboBox Cell Editor",
            "GET /api/nguoi-tham-gia/danh-sach\nPUT /api/nguoi-tham-gia/cap-nhat\nEntity: NguoiThamGia"
        ],
        [
            "Quản lý thông báo sự kiện",
            "• Tạo thông báo sự kiện (tiêu đề, nội dung)\n• Chọn đối tượng nhận thông báo\n• Cập nhật thông báo đã gửi/lưu nháp\n• Xóa và làm mới form thông báo",
            "Quanlythongbao.java\nLayout Split 2 cột (List + Form)",
            "GET /api/thong-bao/danh-sach\nPOST /api/thong-bao/tao-moi\nEntity: ThongBao"
        ]
    ]
    t_uc_canbo = doc.add_table(rows=1, cols=4)
    style_table(t_uc_canbo, widths_uc_canbo, headers_uc_canbo, data_uc_canbo, header_bg="16A085")
    
    doc.add_paragraph()

    # Use Case 3: Sinh viên
    add_heading_2(doc, "2.3. Phân hệ Sinh viên (Phụ trách: Thúy)")
    add_body_p(doc, "Căn cứ theo Sơ đồ Use Case Sinh viên, Thúy phụ trách giao diện tương tác trực tiếp của người học:")
    
    headers_uc_sv = ["Nhóm Use Case", "Use Case chi tiết (extend)", "Thành phần Frontend (FE)", "API Backend & CSDL (BE)"]
    widths_uc_sv = [1.8, 2.3, 1.8, 1.6]
    data_uc_sv = [
        [
            "Đăng ký tham gia sự kiện",
            "• Tra cứu sự kiện: Xem danh sách, tìm kiếm, lọc theo ngày, xem chi tiết\n• Xem thông tin thể lệ & điều kiện\n• Nhấn Đăng ký tham gia sự kiện\n• Hủy đăng ký nếu bận việc\n• Xem trạng thái tham gia (Đã duyệt/Chờ)",
            "TraCuuSuKien.java\nSuKienCuaToi.java\nDashboardHocSinh.java",
            "GET /api/events/search\nPOST /api/participants/register\nDELETE /api/participants/cancel\nEntity: NguoiThamGia"
        ],
        [
            "Đánh giá và phản hồi",
            "• Gửi đánh giá mức độ hài lòng\n• Viết ý kiến phản hồi về sự kiện\n• Xem các nhận xét đã gửi",
            "DanhGiaPhanHoi.java\nRating Star / Slider\nTextArea phản hồi",
            "POST /api/feedbacks/submit\nEntity: Feedback, reports"
        ],
        [
            "Quản lý thông tin cá nhân",
            "• Xem thông tin hồ sơ sinh viên\n• Cập nhật số điện thoại, email liên hệ\n• Đổi mật khẩu đăng nhập",
            "ThongTinCaNhan.java\nForm cập nhật Profile",
            "GET /api/profile\nPUT /api/profile/update\nEntity: TaiKhoan, NguoiThamGia"
        ]
    ]
    t_uc_sv = doc.add_table(rows=1, cols=4)
    style_table(t_uc_sv, widths_uc_sv, headers_uc_sv, data_uc_sv, header_bg="D35400")
    
    doc.add_paragraph()

    # ================= 3. TIMELINE TỔNG THỂ =================
    add_heading_1(doc, "III. KẾ HOẠCH TIMELINE TỔNG THỂ THEO TUẦN (ROADMAP)")
    add_body_p(doc, 
        "Dự án được triển khai trong vòng 06 tuần theo mô hình Agile/Scrum rút gọn, "
        "đảm bảo các nhánh công việc giữa Backend, Frontend và Database được tiến hành song song, đồng bộ và kiểm soát chất lượng qua từng Sprint:")

    headers_tl = ["Giai đoạn / Tuần", "Khoảng thời gian", "Mục tiêu trọng tâm", "Công việc chính", "Người phụ trách", "Đầu ra (Deliverables)"]
    widths_tl = [1.0, 1.1, 1.4, 2.2, 1.0, 1.5]
    data_tl = [
        [
            "Tuần 1\n(Sprint 1)",
            "13/10 - 19/10",
            "Khởi tạo dự án & Thiết kế CSDL",
            "• Thiết lập Git repo, cấu trúc Backend Gradle, Frontend Maven.\n• Thiết kế CSDL MySQL (bảng users, events, participants, tasks, documents).\n• Viết script Migration V1__init_database.sql và DataSeeder.",
            "Cả nhóm\n(Ngọc Anh chủ trì)",
            "• DB Schema hoàn chỉnh\n• 30 tài khoản mẫu\n• Repo cấu hình chuẩn"
        ],
        [
            "Tuần 2\n(Sprint 2)",
            "20/10 - 26/10",
            "Phát triển Cốt lõi & Module Cán bộ",
            "• Xây dựng REST API Auth, Dashboard, Thống kê, Điểm danh.\n• Thiết kế WinForms UI Dashboard Cán bộ, Trang chủ, Quản lý tài liệu, Người tham gia, Thông báo.\n• Thiết kế giao diện Đăng nhập phân quyền 3 Actor.",
            "Thái (chính)\nNgọc Anh",
            "• Login.java hoàn thiện\n• DashboardCanbo.java\n• API thongke & Auth"
        ],
        [
            "Tuần 3\n(Sprint 3)",
            "27/10 - 02/11",
            "Phát triển Module Sinh viên",
            "• Xây dựng API tra cứu sự kiện, đăng ký tham gia, đánh giá.\n• Thiết kế giao diện Dashboard Sinh viên: Tra cứu sự kiện, Sự kiện của tôi, Đánh giá phản hồi, Hồ sơ cá nhân.\n• Kiểm tra xác thực thông tin đăng ký.",
            "Thúy (chính)\nNgọc Anh",
            "• DashboardHocSinh.java\n• TraCuuSuKien.java\n• SuKienCuaToi.java\n• API đăng ký sự kiện"
        ],
        [
            "Tuần 4\n(Sprint 4)",
            "03/11 - 09/11",
            "Phát triển Module Quản trị Admin",
            "• Xây dựng giao diện Dashboard Admin: quản lý tài khoản, cấp quyền, duyệt kế hoạch sự kiện.\n• Cấu hình Spring Security, mã hóa mật khẩu, kiểm soát vai trò.\n• Viết API phê duyệt và gỡ bỏ thông báo.",
            "Ngọc Anh (chính)\nThái",
            "• DashboardAdmin.java\n• API phân quyền Role\n• Quản trị sự kiện cấp Admin"
        ],
        [
            "Tuần 5\n(Sprint 5)",
            "10/11 - 16/11",
            "Tích hợp Liên thông (End-to-End)",
            "• Tích hợp toàn diện FE Swing với BE Spring Boot qua ApiClient.\n• Kiểm thử luồng nghiệp vụ xuyên suốt: Admin duyệt -> Cán bộ quản lý -> Sinh viên đăng ký -> Cán bộ điểm danh -> Admin xuất báo cáo.\n• Xử lý lỗi ngoại lệ, timeout máy chủ.",
            "Cả nhóm\n(Thái, Thúy, Ngọc Anh)",
            "• Hệ thống chạy mượt mà\n• Dữ liệu đồng bộ realtime\n• Không còn lỗi crash"
        ],
        [
            "Tuần 6\n(Sprint 6)",
            "17/11 - 23/11",
            "Kiểm thử, Đóng gói & Báo cáo",
            "• Kiểm thử chấp nhận người dùng (UAT), fix lỗi giao diện.\n• Đóng gói file thực thi `.jar` và cấu hình chạy terminal bằng Maven Wrapper (`mvnw`).\n• Viết tài liệu báo cáo đề tài, hướng dẫn cài đặt và slide thuyết trình.",
            "Cả nhóm",
            "• File .jar đóng gói hoàn chỉnh\n• Báo cáo tổng kết dự án\n• Slide báo cáo"
        ]
    ]
    t_tl = doc.add_table(rows=1, cols=6)
    style_table(t_tl, widths_tl, headers_tl, data_tl, header_bg="0F4C81")

    doc.add_paragraph()

    # ================= 4. BẢNG PHÂN CÔNG CHI TIẾT THEO TỪNG THÀNH VIÊN =================
    add_heading_1(doc, "IV. BẢNG PHÂN CÔNG NHIỆM VỤ CHI TIẾT (TASK BREAKDOWN)")
    
    # 4.1. Nhiệm vụ của Thái
    add_heading_2(doc, "4.1. Danh mục công việc chi tiết - Cán bộ phụ trách (Thái)")
    headers_t = ["Mã Task", "Tên đầu việc", "Chi tiết công việc & Nghiệp vụ", "Thời gian", "Kết quả đầu ra"]
    widths_t = [0.8, 1.7, 3.1, 1.0, 1.6]
    data_t = [
        ["TK-01", "Giao diện Login", "Thiết kế trang Login chuẩn WinForms, toggle ẩn/hiện mật khẩu, bắt phím Enter", "Tuần 2", "Login.java"],
        ["TK-02", "Dashboard Cán bộ", "Tạo Sidebar màu sắc chuẩn, 5 nút điều hướng, cơ chế chuyển đổi CardPanel", "Tuần 2", "DashboardCanbo.java"],
        ["TK-03", "Trang chủ & KPI", "Tạo 6 Card thống kê số lượng lớn, Footer hiển thị ngày tự động, gọi API tổng quan", "Tuần 2", "Trangchu.java"],
        ["TK-04", "Báo cáo Thống kê", "Vẽ biểu đồ BarChart 12 tháng bằng Graphics2D, 7 ô KPI chỉ số, bộ lọc ngày", "Tuần 2", "Lapbaocaothongke.java"],
        ["TK-05", "Quản lý Tài liệu", "Bảng danh sách tài liệu sự kiện, chức năng Tải lên, Xuất file XLSX, Xóa tài liệu", "Tuần 2-3", "Quanlytailieusukien.java"],
        ["TK-06", "Quản lý Công việc", "Bảng tiến độ công việc sự kiện, phân công nhân sự, hiển thị ProgressBar", "Tuần 3", "Quanlycongviec.java"],
        ["TK-07", "Quản lý Người tham gia", "Bảng người tham gia, Badge màu (Vắng/Đã điểm danh), Dropdown điểm danh, Tìm kiếm", "Tuần 2-3", "Quanlynguoithamgia.java"],
        ["TK-08", "Quản lý Thông báo", "SplitPane 2 cột (Danh sách + Form soạn thảo thông báo), CRUD thông báo", "Tuần 3", "Quanlythongbao.java"],
        ["TK-09", "Tích hợp Backend API", "Kết nối các endpoint /api/thongke, /api/tai-lieu, /api/nguoi-tham-gia", "Tuần 4-5", "ApiClient / SwingWorker"]
    ]
    t_t = doc.add_table(rows=1, cols=5)
    style_table(t_t, widths_t, headers_t, data_t, header_bg="16A085")
    
    doc.add_paragraph()

    # 4.2. Nhiệm vụ của Thúy
    add_heading_2(doc, "4.2. Danh mục công việc chi tiết - Sinh viên (Thúy)")
    headers_s = ["Mã Task", "Tên đầu việc", "Chi tiết công việc & Nghiệp vụ", "Thời gian", "Kết quả đầu ra"]
    widths_s = [0.8, 1.7, 3.1, 1.0, 1.6]
    data_s = [
        ["SV-01", "Dashboard Sinh viên", "Tạo khung điều hướng dành riêng cho Sinh viên, lời chào cá nhân hóa", "Tuần 3", "DashboardHocSinh.java"],
        ["SV-02", "Tra cứu sự kiện", "Xem danh sách sự kiện mở, tìm kiếm sự kiện theo tên, lọc sự kiện theo khoảng ngày", "Tuần 3", "TraCuuSuKien.java"],
        ["SV-03", "Xem chi tiết sự kiện", "Hiển thị thời gian, địa điểm, thể lệ, số lượng slot và yêu cầu tham dự", "Tuần 3", "Dialog chi tiết sự kiện"],
        ["SV-04", "Đăng ký sự kiện", "Xử lý nút Đăng ký, kiểm tra trùng lặp, cập nhật trạng thái tham gia vào DB", "Tuần 3-4", "POST /api/participants"],
        ["SV-05", "Sự kiện của tôi", "Hiển thị danh sách sự kiện đã đăng ký, hỗ trợ nút Hủy đăng ký có xác nhận", "Tuần 3", "SuKienCuaToi.java"],
        ["SV-06", "Đánh giá & Phản hồi", "Form đánh giá chất lượng tổ chức sự kiện, chấm sao và gửi ý kiến đóng góp", "Tuần 4", "DanhGiaPhanHoi.java"],
        ["SV-07", "Thông tin cá nhân", "Xem thông tin sinh viên (MSSV, Họ tên, Lớp, SĐT, Email), form cập nhật thông tin", "Tuần 4", "ThongTinCaNhan.java"],
        ["SV-08", "Kiểm thử trải nghiệm", "Đóng vai người học kiểm thử luồng đăng ký, hủy và nhận thông báo kết quả", "Tuần 5-6", "Kịch bản Test Sinh viên"]
    ]
    t_s = doc.add_table(rows=1, cols=5)
    style_table(t_s, widths_s, headers_s, data_s, header_bg="D35400")

    doc.add_paragraph()

    # 4.3. Nhiệm vụ của Ngọc Anh
    add_heading_2(doc, "4.3. Danh mục công việc chi tiết - Admin / Quản trị viên (Ngọc Anh)")
    headers_a = ["Mã Task", "Tên đầu việc", "Chi tiết công việc & Nghiệp vụ", "Thời gian", "Kết quả đầu ra"]
    widths_a = [0.8, 1.7, 3.1, 1.0, 1.6]
    data_a = [
        ["AD-01", "Cơ sở dữ liệu & Entity", "Thiết kế các bảng MySQL: tai_khoan, events, documents, notifications, reports", "Tuần 1", "V1__init_database.sql"],
        ["AD-02", "Bảo mật & Phân quyền", "Cấu hình Spring Security, phân quyền 3 Role (ADMIN, CAN_BO, HOC_SINH)", "Tuần 1-2", "SecurityConfig.java"],
        ["AD-03", "Data Seeder hệ thống", "Tự động tạo 30 tài khoản kiểm thử (1 Admin, 15 Cán bộ, 14 Học sinh)", "Tuần 1", "DataSeeder.java"],
        ["AD-04", "API Xác thực (Auth)", "Viết API /api/auth/login kiểm tra đăng nhập và phản hồi vai trò tương ứng", "Tuần 2", "AuthController.java"],
        ["AD-05", "Dashboard Admin UI", "Thiết kế giao diện quản trị viên: Quản lý người dùng, phân quyền, trạng thái", "Tuần 4", "DashboardAdmin.java"],
        ["AD-06", "Quản lý sự kiện cấp Admin", "Duyệt kế hoạch sự kiện từ các đơn vị gửi lên, phân công cán bộ phụ trách", "Tuần 4", "Module Quản trị Sự kiện"],
        ["AD-07", "Quản lý & Duyệt thông báo", "Kiểm duyệt nội dung thông báo trước khi phát hành toàn trường, gỡ bỏ thông báo", "Tuần 4-5", "ThongBaoController"],
        ["AD-08", "Quản lý Maven Wrapper", "Cấu hình maven-wrapper (mvnw, mvnw.cmd) hỗ trợ chạy terminal không phụ thuộc cài đặt", "Tuần 2", "frontend/mvnw"],
        ["AD-09", "Đóng gói & Triển khai", "Kiểm thử tích hợp hệ thống, build artifact jar, viết tài liệu kỹ thuật", "Tuần 5-6", "Tài liệu kỹ thuật / Build JAR"]
    ]
    t_a = doc.add_table(rows=1, cols=5)
    style_table(t_a, widths_a, headers_a, data_a, header_bg="2C3E50")

    doc.add_paragraph()

    # ================= 5. KẾ HOẠCH KIỂM THỬ & TIÊU CHÍ NGHIỆM THU =================
    add_heading_1(doc, "V. KẾ HOẠCH KIỂM THỬ & TIÊU CHÍ NGHIỆM THU (TEST PLAN)")
    add_body_p(doc, "Để đảm bảo ứng dụng vận hành chuẩn xác theo yêu cầu thực tế, kế hoạch kiểm thử được phân định rõ ràng:")
    
    headers_tp = ["STT", "Hạng mục kiểm thử", "Mô tả kịch bản kiểm thử", "Kết quả kỳ vọng", "Người thực hiện", "Trạng thái"]
    widths_tp = [0.5, 1.5, 2.5, 1.8, 1.0, 0.9]
    data_tp = [
        [
            "1", 
            "Bảo mật Đăng nhập", 
            "Người dùng chưa đăng nhập cố gắng mở thẳng DashboardCanbo hoặc DashboardHocSinh", 
            "Hệ thống chặn truy cập và tự động redirect về màn hình Login.java", 
            "Thái", 
            "Đạt (Pass)"
        ],
        [
            "2", 
            "Phân quyền Role", 
            "Đăng nhập tài khoản canbo01 (mật khẩu canbo123), admin, hoặc hocsinh01", 
            "Hệ thống điều hướng chuẩn 100% về đúng Dashboard và hiển thị đúng tên trên Sidebar", 
            "Ngọc Anh", 
            "Đạt (Pass)"
        ],
        [
            "3", 
            "Hiển thị Dữ liệu Cán bộ", 
            "Mở Dashboard Cán bộ xem 6 ô KPI, biểu đồ cột 12 tháng, danh sách tài liệu & người tham gia", 
            "Dữ liệu khớp chính xác với CSDL MySQL (12 sự kiện, 30 người tham gia, 33 thông báo)", 
            "Thái", 
            "Đạt (Pass)"
        ],
        [
            "4", 
            "Đăng ký Sự kiện Sinh viên", 
            "Sinh viên chọn sự kiện đang mở và nhấn nút Đăng ký tham gia", 
            "Hệ thống lưu bản ghi vào CSDL, cập nhật trạng thái 'Đã đăng ký' trên SuKienCuaToi", 
            "Thúy", 
            "Đạt (Pass)"
        ],
        [
            "5", 
            "Điểm danh Cán bộ", 
            "Cán bộ chuyển trạng thái sinh viên từ 'Chưa điểm danh' sang 'Đã điểm danh' và bấm Lưu", 
            "CSDL cập nhật tức thì, số liệu thống kê 'Đã tham dự' trên Báo cáo tự động tăng", 
            "Thái", 
            "Đạt (Pass)"
        ],
        [
            "6", 
            "Chạy Terminal độc lập", 
            "Mở terminal Git Bash / PowerShell gõ `./mvnw spring-boot:run` tại frontend", 
            "Giao diện Login xuất hiện mượt mà không cần mở IDE phức tạp", 
            "Ngọc Anh", 
            "Đạt (Pass)"
        ]
    ]
    t_tp = doc.add_table(rows=1, cols=6)
    style_table(t_tp, widths_tp, headers_tp, data_tp, header_bg="1B365D")

    doc.add_paragraph()

    # ================= 6. QUẢN LÝ RỦI RO & GIẢI PHÁP =================
    add_heading_1(doc, "VI. QUẢN LÝ RỦI RO & PHƯƠNG ÁN DỰ PHÒNG (RISK MANAGEMENT)")
    
    headers_rk = ["Mã Rủi ro", "Mô tả rủi ro tiềm ẩn", "Mức độ", "Biện pháp phòng ngừa & Xử lý dự phòng", "Người kiểm soát"]
    widths_rk = [0.8, 2.2, 1.0, 3.2, 1.0]
    data_rk = [
        [
            "RK-01", 
            "Máy chủ Backend tắt hoặc lỗi cổng 8080 khiến Frontend không kết nối được", 
            "Cao", 
            "Xây dựng cơ chế fallback thông minh trong Frontend (tự nạp dữ liệu mẫu offline để giao diện không bị treo / đơ, hiển thị thông báo rõ ràng)", 
            "Thái"
        ],
        [
            "RK-02", 
            "Xung đột phiên bản môi trường Java / Maven giữa các máy tính thành viên", 
            "Trung bình", 
            "Chuẩn hóa Maven Wrapper (mvnw, mvnw.cmd) và Gradle Wrapper (gradlew) đi kèm mã nguồn, cố định phiên bản Java 17-25 trong pom.xml và build.gradle", 
            "Ngọc Anh"
        ],
        [
            "RK-03", 
            "Xung đột mã nguồn khi commit/push Git đồng thời", 
            "Thấp", 
            "Phân tách rõ ràng các thư mục package theo vai trò: com.eventmanager.fe.gui.Canbo, .HocSinh, .Admin; pull code trước khi push", 
            "Cả nhóm"
        ]
    ]
    t_rk = doc.add_table(rows=1, cols=5)
    style_table(t_rk, widths_rk, headers_rk, data_rk, header_bg="8E44AD")

    doc.add_paragraph()

    # ================= KẾT LUẬN & KÝ DUYỆT =================
    add_heading_1(doc, "VII. KẾT LUẬN & CAM KẾT TIẾN ĐỘ")
    add_body_p(doc, 
        "Bản kế hoạch Timeline trên đây là cơ sở pháp lý và kỹ thuật định hướng toàn bộ quá trình phát triển dự án "
        "\"Hệ thống Quản lý Sự kiện Liên Chi đoàn\". Ba thành viên Thái, Thúy và Ngọc Anh cam kết thực hiện đúng tiến độ, "
        "tuân thủ chuẩn kiến trúc đã thiết kế và hoàn thành xuất sắc các mục tiêu đã đề ra.")

    p_sign = doc.add_paragraph()
    format_paragraph(p_sign, space_before=20, space_after=6)
    p_sign.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    r_date = p_sign.add_run("Hà Nội, ngày 01 tháng 11 năm 2026\nĐẠI DIỆN ĐỘI NGŨ PHÁT TRIỂN DỰ ÁN\n\n\n\n")
    r_date.font.name = 'Times New Roman'
    r_date.font.size = Pt(12)
    r_date.font.italic = True
    
    r_name = p_sign.add_run("Thái  —  Thúy  —  Ngọc Anh")
    r_name.font.name = 'Times New Roman'
    r_name.font.size = Pt(13)
    r_name.font.bold = True
    r_name.font.color.rgb = RGBColor(15, 76, 129)

    doc.save(file_path)
    print(f"File saved successfully to: {file_path}")

if __name__ == "__main__":
    out_file = r"D:\BC JAVA\Ke_Hoach_Timeline_Du_An_Quan_Ly_Su_Kien.docx"
    build_document(out_file)
