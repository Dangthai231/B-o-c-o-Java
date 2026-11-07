import java.io.Console;
import java.sql.*;
import java.util.Arrays;
import java.util.Properties;

/** Standalone read-only probe. Never starts Spring, Flyway or Hibernate. */
public class JdbcCheck {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Driver driver = DriverManager.getDriver("jdbc:mysql://127.0.0.1:3306/event_management_db");
            System.out.println("Driver loaded: " + driver.getClass().getName()
                    + " " + driver.getMajorVersion() + "." + driver.getMinorVersion());
            if (args.length > 0 && args[0].equals("--driver-only")) return;
            String password = System.getenv("SPRING_DATASOURCE_PASSWORD");
            if (password == null || password.isEmpty()) {
                Console console = System.console();
                if (console == null) {
                    System.err.println("Credentials unavailable. Run in an interactive terminal to enter a hidden password, or set SPRING_DATASOURCE_PASSWORD.");
                    System.exit(2);
                }
                char[] entered = console.readPassword("MySQL password (hidden): ");
                if (entered == null || entered.length == 0) System.exit(2);
                password = new String(entered);
                Arrays.fill(entered, '\0');
            }
            Properties settings = new Properties();
            settings.setProperty("user", System.getenv().getOrDefault("DB_USERNAME", "root"));
            settings.setProperty("password", password);
            settings.setProperty("connectTimeout", "5000");
            settings.setProperty("socketTimeout", "10000");
            DriverManager.setLoginTimeout(10);
            try (Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/event_management_db", settings)) {
                settings.remove("password");
                password = null;
                connection.setReadOnly(true);
                System.out.println("Connected: " + connection.getMetaData().getDatabaseProductVersion());
                for (String sql : new String[]{"SELECT 1", "SELECT DATABASE()",
                        "SELECT USER(), CURRENT_USER()", "SHOW TABLES",
                        "SELECT COUNT(*) FROM tai_khoan"}) {
                    System.out.println(sql);
                    try (Statement statement = connection.createStatement()) {
                        statement.setQueryTimeout(10);
                        try (ResultSet rows = statement.executeQuery(sql)) {
                            while (rows.next()) {
                                for (int i = 1; i <= rows.getMetaData().getColumnCount(); i++) {
                                    if (i > 1) System.out.print(" | ");
                                    System.out.print(rows.getString(i));
                                }
                                System.out.println();
                            }
                        }
                    } catch (SQLException failure) {
                        report(failure);
                        System.exit(1);
                    }
                }
            }
        } catch (SQLException failure) {
            report(failure);
            System.exit(1);
        } catch (ClassNotFoundException failure) {
            System.err.println("Driver missing: add mysql-connector-j JAR to --class-path.");
            System.exit(1);
        }
    }

    private static void report(SQLException failure) {
        System.err.println("Type=" + failure.getClass().getSimpleName()
                + "; SQLState=" + failure.getSQLState() + "; code=" + failure.getErrorCode());
        // Deliberately omit exception messages: they can contain connection secrets.
        String state = failure.getSQLState();
        if (failure.getErrorCode() == 1045)
            System.err.println("Authentication rejected: verify credentials for this server/account.");
        else if (failure.getErrorCode() == 1049)
            System.err.println("Database not found on this server.");
        else if (failure.getErrorCode() == 1146)
            System.err.println("Connection succeeded but the requested table is absent.");
        else if (state != null && state.startsWith("08"))
            System.err.println("Connection/handshake failure: check listener, TLS and authentication exchange.");
        else System.err.println("Check account permissions, driver and server configuration.");
    }
}
