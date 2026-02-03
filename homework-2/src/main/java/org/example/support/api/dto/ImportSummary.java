package org.example.support.api.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportSummary {
    private int totalRecords;
    private int successful;
    private int failed;
    private List<ImportFailureDetail> failures = new ArrayList<>();

    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
    public int getSuccessful() { return successful; }
    public void setSuccessful(int successful) { this.successful = successful; }
    public int getFailed() { return failed; }
    public void setFailed(int failed) { this.failed = failed; }
    public List<ImportFailureDetail> getFailures() { return failures; }
    public void setFailures(List<ImportFailureDetail> failures) { this.failures = failures; }
    public void addFailure(ImportFailureDetail failure) { this.failures.add(failure); }
}
