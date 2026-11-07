package com.eventmanager.fe.gui.Admin.common;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Bộ icon nét mảnh vẽ bằng Graphics2D (không cần file ảnh).
 * Cách dùng: Icons.of(Icons.HOME, 18, Theme.TEXT)
 * Mỗi icon được vẽ trên khung 24x24 rồi phóng to/thu nhỏ theo size.
 */
public final class Icons {

    public static final String HOME = "home", CALENDAR = "calendar", BELL = "bell", USER = "user",
            USERS = "users", SHIELD = "shield", CHART = "chart", GEAR = "gear", SEARCH = "search",
            PLUS = "plus", EYE = "eye", EDIT = "edit", LOCK = "lock", UNLOCK = "unlock", MORE = "more",
            MENU = "menu", CHECK = "check", CHECK_CIRCLE = "checkCircle", X = "x", CLOCK = "clock",
            CHEVRON_DOWN = "chevronDown", CHEVRON_LEFT = "chevronLeft", CHEVRON_RIGHT = "chevronRight",
            LOGOUT = "logout", SEND = "send", TRASH = "trash", USER_PLUS = "userPlus", FILE = "file",
            CAP = "cap", KEY = "key", LIST = "list", PIN = "pin", REFRESH = "refresh";

    private Icons() {
    }

    public static Icon of(String ten, int size, Color mau) {
        return new VectorIcon(ten, size, mau);
    }

    private static final class VectorIcon implements Icon {
        private final String ten;
        private final int size;
        private final Color mau;

        VectorIcon(String ten, int size, Color mau) {
            this.ten = ten;
            this.size = size;
            this.mau = mau;
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = Theme.muot(g);
            g2.translate(x, y);
            g2.scale(size / 24.0, size / 24.0);
            g2.setColor(mau);
            g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            ve(g2);
            g2.dispose();
        }

        private void ve(Graphics2D g) {
            switch (ten) {
                case HOME -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(3.5, 11);
                    p.lineTo(12, 4);
                    p.lineTo(20.5, 11);
                    g.draw(p);
                    Path2D q = new Path2D.Double();
                    q.moveTo(5.5, 9.5);
                    q.lineTo(5.5, 20);
                    q.lineTo(18.5, 20);
                    q.lineTo(18.5, 9.5);
                    g.draw(q);
                    g.draw(new Line2D.Double(10, 20, 10, 14.5));
                    g.draw(new Line2D.Double(10, 14.5, 14, 14.5));
                    g.draw(new Line2D.Double(14, 14.5, 14, 20));
                }
                case CALENDAR -> {
                    g.draw(new RoundRectangle2D.Double(3.5, 5, 17, 15.5, 3, 3));
                    g.draw(new Line2D.Double(3.5, 9.5, 20.5, 9.5));
                    g.draw(new Line2D.Double(8, 3, 8, 6.5));
                    g.draw(new Line2D.Double(16, 3, 16, 6.5));
                }
                case BELL -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(6, 16.5);
                    p.lineTo(6, 10.5);
                    p.curveTo(6, 7, 8.7, 4.5, 12, 4.5);
                    p.curveTo(15.3, 4.5, 18, 7, 18, 10.5);
                    p.lineTo(18, 16.5);
                    p.lineTo(19.5, 18);
                    p.lineTo(4.5, 18);
                    p.closePath();
                    g.draw(p);
                    g.draw(new Arc2D.Double(9.8, 18.5, 4.4, 3.2, 180, 180, Arc2D.OPEN));
                }
                case USER -> {
                    g.draw(new Ellipse2D.Double(8, 3.8, 8, 8));
                    g.draw(new Arc2D.Double(4.5, 13.5, 15, 13, 0, 180, Arc2D.OPEN));
                }
                case USERS -> {
                    g.draw(new Ellipse2D.Double(5, 5, 7, 7));
                    g.draw(new Arc2D.Double(2, 14, 13, 11, 0, 180, Arc2D.OPEN));
                    g.draw(new Arc2D.Double(13, 5, 6, 7, -90, 180, Arc2D.OPEN));
                    g.draw(new Arc2D.Double(15, 14, 7, 11, 0, 90, Arc2D.OPEN));
                }
                case USER_PLUS -> {
                    g.draw(new Ellipse2D.Double(5, 4, 7.5, 7.5));
                    g.draw(new Arc2D.Double(1.8, 13.5, 14, 12, 0, 180, Arc2D.OPEN));
                    g.draw(new Line2D.Double(19, 8, 19, 14));
                    g.draw(new Line2D.Double(16, 11, 22, 11));
                }
                case SHIELD -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(12, 3.5);
                    p.lineTo(19, 6.5);
                    p.lineTo(19, 11.5);
                    p.curveTo(19, 16, 16, 19, 12, 20.5);
                    p.curveTo(8, 19, 5, 16, 5, 11.5);
                    p.lineTo(5, 6.5);
                    p.closePath();
                    g.draw(p);
                    Path2D t = new Path2D.Double();
                    t.moveTo(9, 12);
                    t.lineTo(11.2, 14.2);
                    t.lineTo(15.2, 10);
                    g.draw(t);
                }
                case CHART -> {
                    g.draw(new RoundRectangle2D.Double(3.5, 3.5, 17, 17, 3, 3));
                    g.draw(new Line2D.Double(8, 16.5, 8, 12));
                    g.draw(new Line2D.Double(12, 16.5, 12, 8));
                    g.draw(new Line2D.Double(16, 16.5, 16, 10.5));
                }
                case GEAR -> {
                    g.draw(new Ellipse2D.Double(9, 9, 6, 6));
                    for (int i = 0; i < 8; i++) {
                        double a = Math.toRadians(i * 45);
                        g.draw(new Line2D.Double(12 + Math.cos(a) * 6.4, 12 + Math.sin(a) * 6.4,
                                12 + Math.cos(a) * 8.6, 12 + Math.sin(a) * 8.6));
                    }
                    g.draw(new Ellipse2D.Double(5.6, 5.6, 12.8, 12.8));
                }
                case SEARCH -> {
                    g.draw(new Ellipse2D.Double(4, 4, 12, 12));
                    g.draw(new Line2D.Double(14.6, 14.6, 20, 20));
                }
                case PLUS -> {
                    g.draw(new Line2D.Double(12, 5, 12, 19));
                    g.draw(new Line2D.Double(5, 12, 19, 12));
                }
                case EYE -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(2.5, 12);
                    p.curveTo(5, 7, 8.5, 5.5, 12, 5.5);
                    p.curveTo(15.5, 5.5, 19, 7, 21.5, 12);
                    p.curveTo(19, 17, 15.5, 18.5, 12, 18.5);
                    p.curveTo(8.5, 18.5, 5, 17, 2.5, 12);
                    g.draw(p);
                    g.draw(new Ellipse2D.Double(9, 9, 6, 6));
                }
                case EDIT -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(15.5, 4.5);
                    p.lineTo(19.5, 8.5);
                    p.lineTo(8.5, 19.5);
                    p.lineTo(4, 20);
                    p.lineTo(4.5, 15.5);
                    p.closePath();
                    g.draw(p);
                }
                case LOCK, UNLOCK -> {
                    g.draw(new RoundRectangle2D.Double(5, 10.5, 14, 10, 3, 3));
                    if (LOCK.equals(ten)) {
                        g.draw(new Arc2D.Double(8, 4, 8, 11, 0, 180, Arc2D.OPEN));
                        g.draw(new Line2D.Double(8, 9.5, 8, 10.5));
                        g.draw(new Line2D.Double(16, 9.5, 16, 10.5));
                    } else {
                        g.draw(new Arc2D.Double(8, 3, 8, 11, 20, 160, Arc2D.OPEN));
                        g.draw(new Line2D.Double(8, 8.5, 8, 10.5));
                    }
                    g.draw(new Line2D.Double(12, 14.5, 12, 16.5));
                }
                case MORE -> {
                    for (int i = 0; i < 3; i++) g.fill(new Ellipse2D.Double(10.6, 4.6 + i * 6, 2.8, 2.8));
                }
                case MENU -> {
                    g.draw(new Line2D.Double(4, 7, 20, 7));
                    g.draw(new Line2D.Double(4, 12, 20, 12));
                    g.draw(new Line2D.Double(4, 17, 20, 17));
                }
                case CHECK -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(5, 12.5);
                    p.lineTo(10, 17.5);
                    p.lineTo(19, 7);
                    g.draw(p);
                }
                case CHECK_CIRCLE -> {
                    g.fill(new Ellipse2D.Double(3, 3, 18, 18));
                    g.setColor(Color.WHITE);
                    g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    Path2D p = new Path2D.Double();
                    p.moveTo(8, 12.3);
                    p.lineTo(11, 15.2);
                    p.lineTo(16.3, 9.3);
                    g.draw(p);
                }
                case X -> {
                    g.draw(new Line2D.Double(6, 6, 18, 18));
                    g.draw(new Line2D.Double(18, 6, 6, 18));
                }
                case CLOCK -> {
                    g.draw(new Ellipse2D.Double(3.5, 3.5, 17, 17));
                    Path2D p = new Path2D.Double();
                    p.moveTo(12, 7.5);
                    p.lineTo(12, 12);
                    p.lineTo(15, 14);
                    g.draw(p);
                }
                case CHEVRON_DOWN -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(6.5, 9.5);
                    p.lineTo(12, 15);
                    p.lineTo(17.5, 9.5);
                    g.draw(p);
                }
                case CHEVRON_LEFT -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(14.5, 6);
                    p.lineTo(8.5, 12);
                    p.lineTo(14.5, 18);
                    g.draw(p);
                }
                case CHEVRON_RIGHT -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(9.5, 6);
                    p.lineTo(15.5, 12);
                    p.lineTo(9.5, 18);
                    g.draw(p);
                }
                case LOGOUT -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(10, 4);
                    p.lineTo(5, 4);
                    p.lineTo(5, 20);
                    p.lineTo(10, 20);
                    g.draw(p);
                    g.draw(new Line2D.Double(10, 12, 20, 12));
                    Path2D a = new Path2D.Double();
                    a.moveTo(16, 8);
                    a.lineTo(20, 12);
                    a.lineTo(16, 16);
                    g.draw(a);
                }
                case SEND -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(20.5, 3.5);
                    p.lineTo(3.5, 10.5);
                    p.lineTo(10.5, 13.5);
                    p.lineTo(13.5, 20.5);
                    p.closePath();
                    g.draw(p);
                    g.draw(new Line2D.Double(10.5, 13.5, 20.5, 3.5));
                }
                case TRASH -> {
                    g.draw(new Line2D.Double(4, 6.5, 20, 6.5));
                    g.draw(new Line2D.Double(9.5, 6.5, 9.5, 4));
                    g.draw(new Line2D.Double(9.5, 4, 14.5, 4));
                    g.draw(new Line2D.Double(14.5, 4, 14.5, 6.5));
                    Path2D p = new Path2D.Double();
                    p.moveTo(6, 6.5);
                    p.lineTo(7, 20);
                    p.lineTo(17, 20);
                    p.lineTo(18, 6.5);
                    g.draw(p);
                }
                case FILE -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(14, 3.5);
                    p.lineTo(6, 3.5);
                    p.lineTo(6, 20.5);
                    p.lineTo(18, 20.5);
                    p.lineTo(18, 7.5);
                    p.closePath();
                    g.draw(p);
                    g.draw(new Line2D.Double(9, 12, 15, 12));
                    g.draw(new Line2D.Double(9, 16, 15, 16));
                }
                case CAP -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(2.5, 9.5);
                    p.lineTo(12, 5);
                    p.lineTo(21.5, 9.5);
                    p.lineTo(12, 14);
                    p.closePath();
                    g.draw(p);
                    Path2D q = new Path2D.Double();
                    q.moveTo(6.5, 11.5);
                    q.lineTo(6.5, 16);
                    q.curveTo(9, 18.5, 15, 18.5, 17.5, 16);
                    q.lineTo(17.5, 11.5);
                    g.draw(q);
                }
                case KEY -> {
                    g.draw(new Ellipse2D.Double(3.5, 9, 7, 7));
                    g.draw(new Line2D.Double(10.5, 12.5, 20.5, 12.5));
                    g.draw(new Line2D.Double(17.5, 12.5, 17.5, 16));
                    g.draw(new Line2D.Double(20.5, 12.5, 20.5, 15));
                }
                case LIST -> {
                    for (int i = 0; i < 3; i++) {
                        g.fill(new Ellipse2D.Double(4, 5.8 + i * 6, 2.4, 2.4));
                        g.draw(new Line2D.Double(9, 7 + i * 6, 20, 7 + i * 6));
                    }
                }
                case PIN -> {
                    Path2D p = new Path2D.Double();
                    p.moveTo(12, 21);
                    p.curveTo(7, 15.5, 5.5, 12.5, 5.5, 10);
                    p.curveTo(5.5, 6.3, 8.4, 3.5, 12, 3.5);
                    p.curveTo(15.6, 3.5, 18.5, 6.3, 18.5, 10);
                    p.curveTo(18.5, 12.5, 17, 15.5, 12, 21);
                    g.draw(p);
                    g.draw(new Ellipse2D.Double(9.5, 7.5, 5, 5));
                }
                case REFRESH -> {
                    g.draw(new Arc2D.Double(4.5, 4.5, 15, 15, 40, 280, Arc2D.OPEN));
                    Path2D p = new Path2D.Double();
                    p.moveTo(18.5, 3.5);
                    p.lineTo(18.2, 8.2);
                    p.lineTo(13.6, 7.8);
                    g.draw(p);
                }
                default -> g.draw(new Ellipse2D.Double(5, 5, 14, 14));
            }
        }
    }
}
