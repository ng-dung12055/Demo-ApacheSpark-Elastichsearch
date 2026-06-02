# Spark Jobs

This module contains a simple Spark job that reindexes metadata records into
Elasticsearch for high-performance search.

## Build

```bash
mvn -q -DskipTests package
```

The jar is produced at `spark-jobs/target/record-reindex-job.jar`.

## Run

Make sure Elasticsearch is running and the Spring Boot app database is ready.
Then run:

```bash
export JDBC_URL="jdbc:mysql://localhost:3306/hospitaldb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export JDBC_USER="root"
export JDBC_PASSWORD="your_password"
export ES_URL="http://localhost:9200"
export ES_INDEX="medical_records"
export ES_USER="elastic"
export ES_PASSWORD="change_me"
export ES_CA_CERT="/path/to/ca.crt"

spark-submit --master local[*] --class com.yourname.hospital.spark.RecordReindexJob \
  spark-jobs/target/record-reindex-job.jar
```

The job reads `metadata_ho_so` and writes the index to Elasticsearch.
