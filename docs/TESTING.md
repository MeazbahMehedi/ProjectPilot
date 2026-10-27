# Verification record

Product: ProjectPilot-Java 2.0.0  
Date: 2026-10-27  
Environment: Linux; OpenJDK 17; virtual X11 display for GUI checks.

## Completed checks

| Check | Result |
| --- | --- |
| Business-rule regression suite | PASS — 33 service assertions, including process locking and corrupted-file preservation |
| Login and logout screens | PASS |
| Supervisor Overview, Projects, Students, Activity, Settings | PASS |
| Student Overview, Projects, My selection, Activity, Settings | PASS |
| Live project search | PASS |
| Dark and light themes | PASS |
| Minimum supported window layout | PASS |
| Real launch entry point with a new temporary workspace | PASS |
| Six running-UI screenshots | Captured and embedded in README |

Tests use temporary workspaces and do not ship test databases. Persistence uses locked, atomically replaced JSON storage.

## Practical limits of verification

These checks were run on Linux. Windows batch launchers were reviewed but were not executed on Windows. The application uses native GUI libraries, so fonts and control rendering can differ by operating system. Automated page checks do not click every possible native file-picker path. Exports and backups are covered directly at the service layer. No remote-server, multi-device, encryption, or disaster-recovery guarantees are claimed.

## Reproduce

```bash
java tools/Build.java --test
java tools/Build.java --screenshots
```
