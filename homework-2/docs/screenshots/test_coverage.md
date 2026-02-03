# Test Coverage Summary (JaCoCo)

This summarizes the latest JaCoCo coverage generated from the test suite.

- Overall Instruction: 86.9% (1398/1608)
- Overall Branch: 68.5% (89/130)
- Overall Line: 89.4% (296/331)
- Overall Complexity: 71.0% (147/207)
- Overall Method: 83.0% (117/141)
- Overall Class: 100.0% (19/19)

Full HTML report:
- [homework-2/build/reports/jacoco/test/html/index.html](homework-2/build/reports/jacoco/test/html/index.html)

How to regenerate the report:
- Run: `./gradlew clean test jacocoTestReport`
- HTML: [homework-2/build/reports/jacoco/test/html/index.html](homework-2/build/reports/jacoco/test/html/index.html)
- XML: [homework-2/build/reports/jacoco/test/jacocoTestReport.xml](homework-2/build/reports/jacoco/test/jacocoTestReport.xml)

Notes / Next Steps:
- Controller error paths now covered (get-by-id 404, delete 404).
- Classifier branch coverage improved with billing/feature/bug/other cases.
- Negative-path tests added for `TicketService.update()` (nulls, empty DTO, missing ID).