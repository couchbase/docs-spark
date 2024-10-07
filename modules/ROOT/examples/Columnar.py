from pyspark.sql import SparkSession

# tag::init[]
spark = (SparkSession.builder
    .appName("Couchbase Spark Connector Columnar Example")
    # Note whether you need the .master(...) and .config("spark.jars"...) lines depends on how you are using Spark.
    # See our PySpark documentation for more details.
    .master("local[*]")
    .config("spark.jars", """C:\\Users\\Graham Pople\\Downloads\\Couchbase-Spark-Connector_2.12-3.5.1\\spark-connector-assembly-3.5.1.jar""")
    .config("spark.couchbase.connectionString", "couchbases://cb.kiofzclm8ogfldf.customsubdomain.nonprod-project-avengers.com")
    .config("spark.couchbase.username", "test")
    .config("spark.couchbase.password", "Password!1")
    .config("spark.ssl.insecure", "true")
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
