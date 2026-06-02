package com.yourname.hospital.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.spark")
public class SparkProperties {

    private boolean enabled = false;
    private String submit = "spark-submit";
    private String master = "local[*]";
    private String jobJar = "./spark-jobs/target/record-reindex-job.jar";
    private String appName = "hospital-reindex-job";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSubmit() {
        return submit;
    }

    public void setSubmit(String submit) {
        this.submit = submit;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getJobJar() {
        return jobJar;
    }

    public void setJobJar(String jobJar) {
        this.jobJar = jobJar;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
