package com.yourname.hospital.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;

public class RecordReindexJob {

    public static void main(String[] args) {
        String jdbcUrl = System.getenv("JDBC_URL");
        String jdbcUser = System.getenv("JDBC_USER");
        String jdbcPassword = System.getenv("JDBC_PASSWORD");
        String jdbcDriver = System.getenv("JDBC_DRIVER");
        String esUrl = System.getenv("ES_URL");
        String esIndex = System.getenv("ES_INDEX");
        String esUser = System.getenv("ES_USER");
        String esPassword = System.getenv("ES_PASSWORD");
        String esCaCert = System.getenv("ES_CA_CERT");

        if (jdbcUrl == null || jdbcUrl.isBlank() || esUrl == null || esUrl.isBlank()
                || esIndex == null || esIndex.isBlank()) {
            System.err.println("Missing JDBC_URL / ES_URL / ES_INDEX environment variables.");
            System.exit(1);
            return;
        }
        if (jdbcDriver == null || jdbcDriver.isBlank()) {
            jdbcDriver = "com.mysql.cj.jdbc.Driver";
        }
        try {
            Class.forName(jdbcDriver);
        } catch (ClassNotFoundException ex) {
            System.err.println("Missing JDBC driver: " + jdbcDriver);
            System.exit(1);
            return;
        }

        String esNodes = esUrl.replace("http://", "").replace("https://", "");
        boolean esSsl = esUrl.startsWith("https://");

        SparkSession spark = SparkSession.builder()
                .appName("HospitalReindexJob")
                .getOrCreate();

        Dataset<Row> metadata = spark.read()
                .format("jdbc")
                .option("url", jdbcUrl)
                .option("dbtable", "metadata_ho_so")
                .option("driver", jdbcDriver)
                .option("user", jdbcUser)
                .option("password", jdbcPassword)
                .load();

        Dataset<Row> indexed = metadata.select(
                functions.col("ho_so_id").alias("recordId"),
                functions.col("patient_code").alias("patientCode"),
                functions.col("patient_name").alias("patientName"),
                functions.col("keywords"),
                functions.col("summary"),
                functions.col("updated_at").alias("updatedAt")
        );

        DataFrameWriter<Row> writer = indexed.write()
                .format("org.elasticsearch.spark.sql")
                .option("es.nodes", esNodes)
                .option("es.nodes.wan.only", "true")
                .option("es.resource", esIndex)
                .option("es.mapping.id", "recordId");

        if (esSsl) {
            writer = writer.option("es.net.ssl", "true");
        }
        if (esUser != null && !esUser.isBlank() && esPassword != null) {
            writer = writer.option("es.net.http.auth.user", esUser)
                    .option("es.net.http.auth.pass", esPassword);
        }
        if (esCaCert != null && !esCaCert.isBlank()) {
            writer = writer.option("es.net.ssl.cert.allow.self.signed", "true");
        }

        writer.mode("overwrite").save();

        spark.stop();
    }
}
