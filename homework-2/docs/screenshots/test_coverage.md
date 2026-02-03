# Test Coverage Summary (JaCoCo)

This summarizes the latest JaCoCo coverage generated from the test suite.

- Overall Instruction: 82.8% (1331/1608)
- Overall Branch: 63.1% (82/130)
- Overall Line: 86.1% (285/331)
- Overall Complexity: 67.1% (139/207)
- Overall Method: 80.8% (114/141)
- Overall Class: 94.7% (18/19)

Full HTML report:
- [homework-2/build/reports/jacoco/test/html/index.html](homework-2/build/reports/jacoco/test/html/index.html)

How to regenerate the report:
- Run: `./gradlew clean test jacocoTestReport`
- HTML: [homework-2/build/reports/jacoco/test/html/index.html](homework-2/build/reports/jacoco/test/html/index.html)
- XML: [homework-2/build/reports/jacoco/test/jacocoTestReport.xml](homework-2/build/reports/jacoco/test/jacocoTestReport.xml)

Notes / Next Steps:
- Expand tests to cover remaining controller error paths (`getTicket`, `deleteTicket`).
- Increase classifier branch coverage by exercising additional keyword branches.
- Add negative-path tests for `TicketService.update()` (invalid DTO combinations).