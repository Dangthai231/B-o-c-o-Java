# Read-only MySQL connection check (Java 17)

This probe is separate from Spring Boot, Flyway and Hibernate. It never reads
application.properties or IntelliJ Password Safe. No password is passed on the
command line or printed. It uses SPRING_DATASOURCE_PASSWORD when present;
otherwise an interactive Java console prompts without echoing characters.

In an IntelliJ PowerShell Terminal, from the backend directory, run:

```powershell
$jdbcProbeJar = Get-ChildItem -LiteralPath "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\com.mysql\mysql-connector-j\9.7.0" -Filter 'mysql-connector-j-9.7.0.jar' -Recurse | Select-Object -First 1
java --class-path $jdbcProbeJar.FullName tools\JdbcCheck.java
```

The direct Java command works even when Windows disables PowerShell scripts.
If scripts are allowed, `./tools/Test-Jdbc.ps1` discovers a cached driver.
`-DriverOnly` checks driver loading without credentials.

The probe uses the same basic URL as the IDE:
`jdbc:mysql://127.0.0.1:3306/event_management_db`.
It sends the user (DB_USERNAME or root) and password using JDBC Properties,
sets the connection read-only, then runs only:

```sql
SELECT 1;
SELECT DATABASE();
SELECT USER(), CURRENT_USER();
SHOW TABLES;
SELECT COUNT(*) FROM tai_khoan;
```

Exit codes: 0 = checks passed, 1 = driver/SQL failure, 2 = credentials unavailable.
Errors report exception type, SQLState and vendor code without raw exception messages.
If the last query reports error 1146, connectivity succeeded but that table is absent.
If JDBC succeeds but the IDE fails, re-enter the password in the IDE data source.
If JDBC fails too, compare SQLState/code before changing any driver options.

Do not restart the backend just to test connectivity: its current configuration
enables Flyway and Hibernate schema updates. A listening HTTP port alone does
not establish that the backend's datasource is healthy.

No existing configuration was modified. In particular the backend's literal
password was not migrated because SPRING_DATASOURCE_PASSWORD was not available
in the diagnostic process; replacing it would risk breaking startup. Configure
that variable in the backend's actual launch environment before migration.
