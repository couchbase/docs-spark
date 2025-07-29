from pyspark.sql import SparkSession, DataFrame
import os

spark = SparkSession.builder \
    .appName("Couchbase Spark Connector Columnar Example") \
    .config("spark.couchbase.connectionString", os.getenv('COUCHBASE_CONNECTION_STRING')) \
    .config("spark.couchbase.username", os.getenv('COUCHBASE_USERNAME')) \
    .config("spark.couchbase.password", os.getenv('COUCHBASE_PASSWORD')) \
    .config("spark.ssl.insecure", "true") \
    .getOrCreate()

# tag::simpledf[]
queryDf = spark.read.format("couchbase.query").load()

analyticsDf = spark.read.format("couchbase.analytics").load()

enterpriseAnalyticsDf = spark.read.format("couchbase.enterprise-analytics").load()

columnarDf = spark.read.format("couchbase.columnar").load()
# end::simpledf[]

# tag::query-collection[]
airlines = (spark.read.format("couchbase.query")
            .option("bucket", "travel-sample")
            .option("scope", "inventory")
            .option("collection", "airline")
            .load())
# end::query-collection[]

# tag::columnar-collection[]
airlines = (spark.read.format("couchbase.columnar")
            .option("database", "travel-sample")
            .option("scope", "inventory")
            .option("collection", "airline")
            .load())
# end::columnar-collection[]

# tag::enterprise-analytics-collection[]
airlines = (spark.read.format("couchbase.enterprise-analytics")
            .option("database", "travel-sample")
            .option("scope", "inventory")
            .option("collection", "airline")
            .load())
# end::enterprise-analytics-collection[]

# tag::queryfilter[]
airlines = (spark.read
            .format("couchbase.query")
            .option("database", "travel-sample")
            .option("scope", "inventory")
            .option("collection", "airline")
            .option("filter", "version = 2")
            .load())
# end::queryfilter[]

# tag::enterprise-analytics-filter[]
airlines = (spark.read
            .format("couchbase.enterprise-analytics")
            .option("database", "travel-sample")
            .option("scope", "inventory")
            .option("collection", "airline")
            .option("filter", "country = 'United States'")
            .load())
# end::enterprise-analytics-filter[]

airlines.printSchema()

# tag::simplesort[]
(airlines
 .select("name", "callsign")
 .sort(airlines("callsign").desc)
 .show(10))
# end::simplesort[]

# tag::manualschema[]
# todo
# airlines = (spark.read
# .format("couchbase.query")
# .schema(StructType(
#     StructField("name", StringType)::
# StructField("type", StringType):: Nil
# ))
# .load())
# end::manualschema[]

# tag::kvwrite[]
airlines = (spark.read.format("couchbase.query")
            .option("bucket", "travel-sample")
            .option("scope", "inventory")
            .option("scope", "airline")
            .load()
            .limit(5))

(airlines.write.format("couchbase.kv")
 .option("bucket", "test-bucket")
 .option("scope", "test-scope")
 .option("collection", "test-collection")
 .save())
# end::kvwrite[]


df: DataFrame = None
# tag::writing[]
(df.write.format("couchbase.kv")
 .option("bucket", "test-bucket")
 .option("scope", "test-scope")
 .option("collection", "test-collection")
 .option("idFieldName", "YourIdColumn")
 .save())
# end::writing[]

# tag::sql[]
airlines.createOrReplaceTempView("airlinesView")
airlinesFromView = spark.sql("SELECT * FROM airlinesView")
# end::sql[]


# tag::partitioning[]
(spark.read
 .format("couchbase.query")
 .option("partitionColumn", "id")
 .option("partitionLowerBound", "1")
 .option("partitionUpperBound", "100000")
 .option("partitionCount", "100")
 .load())
# end::partitioning[]
