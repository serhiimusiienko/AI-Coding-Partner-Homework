# Test Coverage Summary (JaCoCo)

This summarizes the latest JaCoCo coverage generated from the test suite.

- Overall Instruction: 95.4% (1534/1608)
- Overall Branch: 85.4% (111/130)
- Overall Line: 96.7% (320/331)
- Overall Complexity: 87.0% (180/207)
- Overall Method: 94.3% (133/141)
- Overall Class: 100.0% (19/19)
 - Overall Instruction: 95.7% (1538/1608)
 - Overall Branch: 86.2% (112/130)
 - Overall Line: 96.7% (320/331)
 - Overall Complexity: 87.4% (181/207)
 - Overall Method: 94.3% (133/141)
 - Overall Class: 100.0% (19/19)

Full HTML report:
- [homework-2/build/reports/jacoco/test/html/index.html](homework-2/build/reports/jacoco/test/html/index.html)

How to regenerate the report:
- Run: `./gradlew clean test jacocoTestReport`
- HTML: [homework-2/build/reports/jacoco/test/html/index.html](homework-2/build/reports/jacoco/test/html/index.html)
- XML: [homework-2/build/reports/jacoco/test/jacocoTestReport.xml](homework-2/build/reports/jacoco/test/jacocoTestReport.xml)

Notes / Next Steps:
- All coverage types meet the ≥85% target.
- Controller error paths covered (get-by-id 404, delete 404).
- Classifier branches covered across billing/feature/bug/other and priority levels.
- Negative-path tests added for `TicketService.update()` (nulls, empty DTO, missing ID).