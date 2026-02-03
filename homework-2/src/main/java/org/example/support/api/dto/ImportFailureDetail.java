package org.example.support.api.dto;

public class ImportFailureDetail {
    private int index;
    private String error;

    public ImportFailureDetail() {}
    public ImportFailureDetail(int index, String error) {
        this.index = index;
        this.error = error;
    }

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
