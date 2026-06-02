package com.yourname.hospital.dto;

public class SearchReindexResponse {

    private int indexed;
    private String status;
    private String message;

    public SearchReindexResponse(int indexed, String status, String message) {
        this.indexed = indexed;
        this.status = status;
        this.message = message;
    }

    public int getIndexed() {
        return indexed;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
