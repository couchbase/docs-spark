import com.couchbase.spark.kv.KeyValueOptions
import com.couchbase.spark.query.QueryOptions
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.types.{StringType, StructField, StructType}

object SparkSQL {

  def main(args: Array[String]): Unit = {

    // tag::context[]
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("Spark SQL")
      .config("spark.couchbase.connectionString", "couchbase://127.0.0.1")
      .config("spark.couchbase.username", "username")
      .config("spark.couchbase.password", "password")
      .config("spark.couchbase.implicitBucket", "travel-sample")
      .getOrCreate()
    // end::context[]

    {
      // tag::context-with-all-implicit-options[]
      val spark = SparkSession
        .builder()
        .master("local[*]")
        .config("spark.couchbase.connectionString", "couchbase://127.0.0.1")
        .config("spark.couchbase.username", "username")
        .config("spark.couchbase.password", "password")
        .config("spark.couchbase.implicitBucket", "travel-sample")
        .config("spark.couchbase.implicitScope", "inventory")
        .config("spark.couchbase.implicitCollection", "airline")
        .getOrCreate()
      // end::context-with-all-implicit-options[]

    }

    {
      // tag::simpledf[]
      val queryDf = spark.read.format("couchbase.query").load()

      val analyticsDf = spark.read.format("couchbase.analytics").load()

      val columnarDf = spark.read.format("couchbase.columnar").load()
      // end::simpledf[]
    }

    {
      // tag::query-collection[]
      val airlines = spark.read.format("couchbase.query")
        .option(QueryOptions.Bucket, "travel-sample")
        .option(QueryOptions.Scope, "inventory")
        .option(QueryOptions.Collection, "airline")
        .load()
       // end::query-collection[]
    }

    {
      // tag::columnar-collection[]
      val airlines = spark.read.format("couchbase.columnar")
        .option(ColumnarOptions.Database, "travel-sample")
        .option(ColumnarOptions.Scope, "inventory")
        .option(ColumnarOptions.Collection, "airline")
        .load()
      // end::columnar-collection[]
    }

    {
      // tag::queryfilter[]
      val airlines = spark.read
        .format("couchbase.query")
        .option(ColumnarOptions.Database, "travel-sample")
        .option(ColumnarOptions.Scope, "inventory")
        .option(ColumnarOptions.Collection, "airline")
        .option(QueryOptions.Filter, "version = 2")
        .load()
      // end::queryfilter[]

      airlines.printSchema()
    }

    {
      // tag::manualschema[]
      val airlines = spark.read
        .format("couchbase.query")
        .schema(StructType(
          StructField("name", StringType) ::
            StructField("type", StringType) :: Nil
        ))
        .load()
      // end::manualschema[]
    }

    {
      // tag::kvwrite[]
      val airlines = spark.read.format("couchbase.query")
        .option(QueryOptions.Bucket, "travel-sample")
        .option(QueryOptions.Scope, "inventory")
        .option(QueryOptions.Collection, "airline")
        .load()
        .limit(5)

      airlines.write.format("couchbase.kv")
        .option(KeyValueOptions.Bucket, "test-bucket")
        .option(KeyValueOptions.Scope, "test-scope")
        .option(KeyValueOptions.Collection, "test-collection")
        .option(KeyValueOptions.Durability, KeyValueOptions.MajorityDurability)
        .save()
      // end::kvwrite[]
    }

    {
      // tag::caseclass[]
      case class Airline(name: String, iata: String, icao: String, country: String)
      // end::caseclass[]

      import spark.implicits._

      // tag::ds[]
      val airlines = spark.read.format("couchbase.query")
        .option(QueryOptions.Bucket, "travel-sample")
        .option(QueryOptions.Scope, "inventory")
        .option(QueryOptions.Collection, "airline")
        .load()
        .as[Airline]
      // end::ds[]

      // tag::dsfetch[]
      airlines
        .map(_.name)
        .filter(_.toLowerCase.startsWith("a"))
        .foreach(println(_))
      // end::dsfetch[]

    }

  }

}
