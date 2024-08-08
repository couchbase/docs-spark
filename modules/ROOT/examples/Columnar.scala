import org.apache.spark.sql.SparkSession

object Columnar {
  // tag::init[]
  val spark = SparkSession
    .builder()
    .appName("CouchbaseColumnarSample") // your app name
    .master("local[*]") // your local or remote Spark master node
    .config("spark.couchbase.connectionString", "couchbases://your-columnar-endpoint.cloud.couchbase.com")
    .config("spark.couchbase.username", "username")
    .config("spark.couchbase.password", "password")
    .getOrCreate()
  // end::init[]

  // tag::reading-dataframe[]
  val airlines = spark.read
    .format("couchbase.columnar")
    .option(ColumnarOptions.Database, "travel-sample")
    .option(ColumnarOptions.Scope, "inventory")
    .option(ColumnarOptions.Collection, "airline")
    .load
  // end::reading-dataframe[]


  // tag::reading-dataset[]
  case class Airline(id: String, name: String, country: String) // (1)

  val sparkSession = spark
  import sparkSession.implicits._ // (2)

  val airlinesDataset = spark.read
    .format("couchbase.columnar")
    .option(ColumnarOptions.Database, "travel-sample")
    .option(ColumnarOptions.Scope, "inventory")
    .option(ColumnarOptions.Collection, "airline")
    .load
    .as[Airline] // (3)
  // end::reading-dataset[]

  // tag::processing-dataframe[]
  println(airlines.count)

  airlines.foreach(row => {
    val id = row.getAs[String]("id")
    val name = row.getAs[String]("name")
    println(s"Row: id=${id} name=${name}")
  })
  // end::processing-dataframe[]

  // tag::writing-dataframe[]
  airlines.write
    .format("couchbase.columnar")
    .option(ColumnarOptions.Database, "targetDatabase")
    .option(ColumnarOptions.Scope, "targetScope")
    .option(ColumnarOptions.Collection, "targetCollection")
    .save()
  // end::writing-dataframe[]

  // tag::sql[]
  airlines.createOrReplaceTempView("airlinesView")
  val airlinesFromView = spark.sql("SELECT * FROM airlinesView")
  // end::sql[]

}