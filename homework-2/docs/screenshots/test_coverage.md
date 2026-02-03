# Test Coverage Summary (JaCoCo)

This summarizes the latest JaCoCo coverage generated from the test suite.

- Overall Instruction: 71.4% (1148/1608)
- Overall Branch: 50.8% (66/130)
- Overall Line: 78.2% (259/331)
- Overall Complexity: 59.4% (123/207)
- Overall Method: 73.8% (104/141)
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