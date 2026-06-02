package com.yourname.hospital.dto;

import com.yourname.hospital.entity.MedicalRecord;
import java.util.List;

public class PatientHistoryPageResponse {

    private List<MedicalRecord> items;
    private long total;
    private int page;
    private int size;

    public PatientHistoryPageResponse() {
    }

    public PatientHistoryPageResponse(List<MedicalRecord> items, long total, int page, int size) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<MedicalRecord> getItems() {
        return items;
    }

    public void setItems(List<MedicalRecord> items) {
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
