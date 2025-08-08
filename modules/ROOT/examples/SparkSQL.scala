import com.couchbase.spark.DefaultConstants
import com.couchbase.spark.columnar.ColumnarOptions
import com.couchbase.spark.enterpriseanalytics.EnterpriseAnalyticsOptions
import com.couchbase.spark.kv.KeyValueOptions
import com.couchbase.spark.query.QueryOptions
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
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

      val enterpriseAnalyticsDf = spark.read.format("couchbase.enterprise-analytics").load()

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
      // tag::enterprise-analytics-collection[]
      val airlines = spark.read.format("couchbase.enterprise-analytics")
        .option(EnterpriseAnalyticsOptions.Database, "travel-sample")
        .option(EnterpriseAnalyticsOptions.Scope, "inventory")
        .option(EnterpriseAnalyticsOptions.Collection, "airline")
        .load()
      // end::enterprise-analytics-collection[]
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
      // tag::enterprise-analytics-filter[]
      val airlines = spark.read
        .format("couchbase.enterprise-analytics")
        .option(EnterpriseAnalyticsOptions.Database, "travel-sample")
        .option(EnterpriseAnalyticsOptions.Scope, "inventory")
        .option(EnterpriseAnalyticsOptions.Collection, "airline")
        .option(EnterpriseAnalyticsOptions.Filter, "country = 'United States'")
        .load()
      // end::enterprise-analytics-filter[]

      airlines.printSchema()
    }

    {
      // tag::partitioning[]
      spark.read
        .format("couchbase.query")
        .option(QueryOptions.PartitionColumn, "id")
        .option(QueryOptions.PartitionLowerBound, "1")
        .option(QueryOptions.PartitionUpperBound, "100000")
        .option(QueryOptions.PartitionCount, "100")
        .load()
      // end::partitioning[]
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

      // tag::simplesort[]
      airlines
        .select("name", "callsign")
        .sort(airlines("callsign").desc)
        .show(10)
      // end::simplesort[]
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
        .save()
      // end::kvwrite[]
    }

    {
      val df: DataFrame = null
      // tag::writing[]
      df.write.format("couchbase.kv")
        .option(KeyValueOptions.Bucket, "test-bucket")
        .option(KeyValueOptions.Scope, "test-scope")
        .option(KeyValueOptions.Collection, "test-collection")
        .option(KeyValueOptions.IdFieldName, "YourIdColumn")
        .save()
      // end::writing[]
    }

    {
      // tag::savemode[]
      val airlines = spark.read.format("couchbase.query")
        .option(QueryOptions.Bucket, "travel-sample")
        .option(QueryOptions.Scope, "inventory")
        .option(QueryOptions.Collection, "airline")
        .load()
        .limit(5)

      // Writing using a built-in Spark SaveMode
      airlines.write.format("couchbase.kv")
        .option(KeyValueOptions.Bucket, "test-bucket")
        .option(KeyValueOptions.Scope, "test-scope")
        .option(KeyValueOptions.Collection, "test-collection")
        .mode(SaveMode.Append)
        .save()

      // Writing using one of the Couchbase WriteModes
      airlines.write.format("couchbase.kv")
        .option(KeyValueOptions.Bucket, "test-bucket")
        .option(KeyValueOptions.Scope, "test-scope")
        .option(KeyValueOptions.Collection, "test-collection")
        .option(KeyValueOptions.WriteMode, KeyValueOptions.WriteModeReplace)
        .save()
      // end::savemode[]
    }

    {
      // tag::cas[]
      val airlines = spark.read.format("couchbase.query")
        .option(QueryOptions.Bucket, "travel-sample")
        .option(QueryOptions.Scope, "inventory")
        .option(QueryOptions.Collection, "airline")
        // Adds a field named "__META_CAS" to the DataFrame, with each document's CAS
        .option(QueryOptions.OutputCas, "true")
        .load()
        .limit(5)

      airlines.write.format("couchbase.kv")
        .option(KeyValueOptions.Bucket, "test-bucket")
        .option(KeyValueOptions.Scope, "test-scope")
        .option(KeyValueOptions.Collection, "test-collection")
        // Both enables CAS and specifies the field name to use (the constant here is "__META_CAS")
        .option(KeyValueOptions.CasFieldName, DefaultConstants.DefaultCasFieldName)
        // CAS is only supported with replace operations
        .option(KeyValueOptions.WriteMode, KeyValueOptions.WriteModeReplace)
        .save()
      // end::cas[]
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
