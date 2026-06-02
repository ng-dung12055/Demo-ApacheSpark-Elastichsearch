package com.yourname.hospital.dto;

import java.util.List;

public class SearchRecordPageResponse {

    private final List<SearchRecordResponse> items;
    private final long total;
    private final int page;
    private final int size;

    public SearchRecordPageResponse(List<SearchRecordResponse> items, long total, int page, int size) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<SearchRecordResponse> getItems() {
        return items;
    }

    public long getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }
}
