package com.yourname.hospital.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "dich_vu_tim_kiem")
public class SearchServiceStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String indexName;
    private String connectionStatus;

    public SearchServiceStatus() {
    }

    public SearchServiceStatus(String indexName, String connectionStatus) {
        this.indexName = indexName;
        this.connectionStatus = connectionStatus;
    }

    public Long getId() {
        return id;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }
}
