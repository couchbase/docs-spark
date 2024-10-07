from pyspark.sql import SparkSession

# tag::init[]
spark = (SparkSession.builder
    .appName("Couchbase Spark Connector Columnar Example")
    # Note whether you need the .master(...) and .config("spark.jars"...) lines depends on how you are using Spark.
    # See our PySpark documentation for more details.
    .master("local[*]")
    .config("spark.jars", "/path/to/spark-connector-assembly-<version>.jar")
    .config("spark.couchbase.connectionString", "couchbases://cb.your.columnar.connection.string.com")
    .config("spark.couchbase.username", "YourColumnarUsername")
    .config("spark.couchbase.password", "YourColumnarPassword")
    .getOrCreate())
# end::init[]

# tag::reading-dataframe[]
airlines = (spark.read
    .format("couchbase.columnar")
    .option("database", "travel-sample")
    .option("scope", "inventory")
    .option("collection", "airline")
    .load())
# end::reading-dataframe[]


# tag::processing-dataframe[]
airlines.show()
print(airlines.count())

collected = airlines.collect()

for airline in collected:
    print(airline)
    id = airline["id"]
    name = airline["name"]
    print(f"Airline: id={id} name={name}")
# end::processing-dataframe[]

# tag::sql[]
airlines.createOrReplaceTempView("airlinesView")
airlinesFromView = spark.sql("SELECT * FROM airlinesView")
print(airlinesFromView.count())
# end::sql[]
