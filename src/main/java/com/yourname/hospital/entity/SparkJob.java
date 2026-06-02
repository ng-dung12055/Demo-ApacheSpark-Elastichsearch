package com.yourname.hospital.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_ma_hoa_spark")
public class SparkJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobName;
    private String status;
    private LocalDateTime runAt = LocalDateTime.now();

    public SparkJob() {
    }

    public SparkJob(String jobName, String status) {
        this.jobName = jobName;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRunAt() {
        return runAt;
    }
}
