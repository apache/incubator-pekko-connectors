# Google Cloud BigQuery

The BigQuery connector provides Apache Pekko Stream sources and sinks to connect to [Google Cloud BigQuery](https://cloud.google.com/bigquery/).
BigQuery is a serverless data warehouse for storing and analyzing massive datasets.
This connector is primarily intended for streaming data into and out of BigQuery tables and running SQL queries, although it also provides basic support for managing datasets and tables and flexible access to the BigQuery REST API.

@@project-info{ projectId="google-cloud-bigquery" }

@@@warning { title="API may change" }

Apache Pekko Connectors Google Cloud BigQuery is marked as "API may change". Please try it out and suggest improvements. [PR #2548](https://github.com/akka/alpakka/pull/2548)

@@@

## Artifacts

@@dependency [sbt,Maven,Gradle] {
  group=org.apache.pekko
  artifact=pekko-connectors-google-cloud-bigquery_$scala.binary.version$
  version=$project.version$
  symbol2=PekkoVersion
  value2=$pekko.version$
  group2=org.apache.pekko
  artifact2=pekko-stream_$scala.binary.version$
  version2=PekkoVersion
  symbol3=PekkoHttpVersion
  value3=$pekko-http.version$
  group3=org.apache.pekko
  artifact3=pekko-http_$scala.binary.version$
  version3=PekkoHttpVersion
  group4=org.apache.pekko
  artifact4=pekko-http-spray-json_$scala.binary.version$
  version4=PekkoHttpVersion
}

To use the [Jackson JSON library](https://github.com/FasterXML/jackson) for marshalling you must also add the Apache Pekko HTTP module for Jackson support.

@@dependency [sbt,Maven,Gradle] {
  symbol3=PekkoHttpVersion
  value3=$pekko-http.version$
  group5=org.apache.pekko
  artifact5=pekko-http-jackson_$scala.binary.version$
  version5=PekkoHttpVersion
}

The table below shows direct dependencies of this module and the second tab shows all libraries that it depends on transitively.

@@dependencies { projectId="google-cloud-bigquery" }

## Configuration

The BigQuery connector @ref[shares its basic configuration](google-common.md) with all the Google connectors in Apache Pekko Connectors.
Additional BigQuery-specific configuration settings can be found in its @github[reference.conf](/google-cloud-bigquery/src/main/resources/reference.conf).

## Imports

All of the examples below assume the following imports are in scope.

Scala
: @@snip [snip](/google-cloud-bigquery/src/test/scala/docs/scaladsl/BigQueryDoc.scala) { #imports }

Java
: @@snip [snip](/google-cloud-bigquery/src/test/java/docs/javadsl/BigQueryDoc.java) { #imports }

## Setup data classes

As a working example throughout this documentation, we will use the `Person` @scala[case] class to model the data in our BigQuery tables.

Scala
: @@snip [snip](/google-cloud-bigquery/src/test/scala/docs/scaladsl/BigQueryDoc.scala) { #setup }

Java
: @@snip [snip](/google-cloud-bigquery/src/test/java/docs/javadsl/BigQueryDoc.java) { #setup }

@scala[
  To enable automatic support for (un)marshalling `User` and `Address` as BigQuery table rows and query results we create implicit @scaladoc[BigQueryRootJsonFormat[T]](org.apache.pekko.stream.connectors.googlecloud.bigquery.scaladsl.spray.BigQueryRootJsonFormat) instances.
  The `bigQueryJsonFormatN` methods are imported from @scaladoc[BigQueryJsonProtocol](org.apache.pekko.stream.connectors.googlecloud.bigquery.scaladsl.spray.BigQueryJsonProtocol$), analogous to Spray’s @scaladoc[DefaultJsonProtocol](spray.json.DefaultJsonProtocol).
]
@java[
  To enable support for (un)marshalling `User` and `Address` as BigQuery table rows and query results we use Jackson’s @javadoc[@JsonCreator](com.fasterxml.jackson.annotation.JsonCreator) and @javadoc[@JsonProperty](com.fasterxml.jackson.annotation.JsonProperty) annotations.
  Note that a custom @javadoc[@JsonCreator](com.fasterxml.jackson.annotation.JsonCreator) constructor is necessary due to BigQuery’s unusual encoding of rows as “a series of JSON f,v objects for indicating fields and values” ([reference documentation](https://cloud.google.com/bigquery/docs/reference/rest/v2/jobs/getQueryResults#body.GetQueryResultsResponse.FIELDS.rows)).
  In addition, we also define `NameAddressesPair` to model the result of the query in the @ref[next section](google-cloud-bigquery.md#run-a-query).
]

## Run a query

You can run a SQL query and stream the unmarshalled results with the @scala[@apidoc[BigQuery.query[Out]](BigQuery$)] @java[@apidoc[BigQuery.<Out>query](BigQuery$)] method.
@scala[
  The output type `Out` can be a tuple or any user-defined class for which an implicit @scaladoc[BigQueryRootJsonFormat[Out]](org.apache.pekko.stream.connectors.googlecloud.bigquery.scaladsl.spray.BigQueryRootJsonFormat) is available.
  Note that the order and presence of fields in `Out` must strictly match your SQL query.
]
@java[To create the unmarshaller, use the @scaladoc[BigQueryMarshallers.<Out>queryResponseUnmarshaller](org.apache.pekko.stream.connectors.googlecloud.bigquery.javadsl.jackson.BigQueryMarshallers$) method.]

Scala
: @@snip [snip](/google-cloud-bigquery/src/test/scala/docs/scaladsl/BigQueryDoc.scala) { #run-query }

Java
: @@snip [snip](/google-cloud-bigquery/src/test/java/docs/javadsl/BigQueryDoc.java) { #run-query }

Notice that the source materializes a @scala[`Future[QueryResponse[(String, Seq[Address])]]`] @java[`CompletionStage<QueryResponse<NameAddressesTuple>>`] which can be used to retrieve metadata related to the query.
For example, you can use a dry run to estimate the number of bytes that will be read by a query.

Scala
: @@snip [snip](/google-cloud-bigquery/src/test/scala/docs/scaladsl/BigQueryDoc.scala) { #dry-run-query }

Java
: @@snip [snip](/google-cloud-bigquery/src/test/java/docs/javadsl/BigQueryDoc.java) { #dry-run-query }

Finally, you can also stream all of the rows in a table without the expense of running a query with the @scala[@apidoc[BigQuery.tableData[Out]](BigQuery$)] @java[@apidoc[BigQuery.<Out>listTableData](BigQuery$)] method.

Scala
: @@snip [snip](/google-cloud-bigquery/src/test/scala/docs/scaladsl/BigQueryDoc.scala) { #table-data }

Java
: @@snip [snip](/google-cloud-bigquery/src/test/java/docs/javadsl/BigQueryDoc.java) { #table-data }

## Load data into BigQuery

The BigQuery connector enables loading data into tables via real-time streaming inserts or batch loading.
For an overview of these strategies see the [BigQuery documentation](https://cloud.google.com/bigquery/docs/loading-data).

The @scala[@apidoc[BigQuery.insertAll[In]](BigQuery$)] @java[@apidoc[BigQuery.<In>insertAll](BigQuery$)] method creates a sink that accepts batches of @scala[`Seq[In]`] @java[`List<In>`]
(for example created via the @extref:[`batch`](pekko:stream/operators/Source-or-Flow/batch.html) operator) and streams them directly into a table.
To enable/disable BigQuery’s best-effort deduplication feature use the appropriate @apidoc[InsertAllRetryPolicy$].

Scala
: @@snip [snip](/google-cloud-bigquery/src/test/scala/docs/scaladsl/BigQueryDoc.scala) { #streaming-insert }

Java
: @@snip [snip](/google-cloud-bigquery/src/test/java/docs/javadsl/BigQueryDoc.java) { #streaming-insert }

As a cost-saving alternative to streaming inserts, you can also add data to a table via asynchronous load jobs.
The @scala[@apidoc[BigQuery.insertAllAsync[In]](BigQuery$)] @java[@apidoc[BigQuery.<In>insertAllAsync](BigQuery$)] method creates a flow that starts a series of batch load jobs.
By default, a new load job is created every minute to attempt to emulate near-real-time streaming inserts, although there is no guarantee when the job will actually run.
The frequency with which new load jobs are created is controlled by the `pekko.connectors.google.bigquery.load-job-per-table-quota` configuration setting.

@@@warning

Pending the resolution of [Google BigQuery issue 176002651](https://issuetracker.google.com/176002651), the `BigQuery.insertAllAsync` API may not work as expected.

As a workaround, you can use the config setting `pekko.http.parsing.conflicting-content-type-header-processing-mode = first` with Apache Pekko HTTP v1.0.0 or later.

@@@

Scala
: @@snip [snip](/google-cloud-bigquery/src/test/scala/docs/scaladsl/BigQueryDoc.scala) { #async-insert }

Java
: @@snip [snip](/google-cloud-bigquery/src/test/java/docs/javadsl/BigQueryDoc.java) { #async-insert }

To check the status of the load jobs use the @scala[@apidoc[BigQuery.job](BigQuery$)] @java[@apidoc[BigQuery.getJob](BigQuery$)] method.

Scala
: @@snip [snip](/google-cloud-bigquery/src/test/scala/docs/scaladsl/BigQueryDoc.scala) { #job-status }

Java
: @@snip [snip](/google-cloud-bigquery/src/test/java/docs/javadsl/BigQueryDoc.java) { #job-status }

## Managing datasets and tables

The BigQuery connector provides methods for basic management of datasets and tables.

Scala
: @@snip [snip](/google-cloud-bigquery/src/test/scala/docs/scaladsl/BigQueryDoc.scala) { #dataset-methods #table-methods }

Java
: @@snip [snip](/google-cloud-bigquery/src/test/java/docs/javadsl/BigQueryDoc.java) { #dataset-methods #table-methods }

Creating a table requires a little more work to specify the schema.
@scala[To enable automatic schema generation, you can bring implicit @scaladoc[TableSchemaWriter[T]](org.apache.pekko.stream.connectors.googlecloud.bigquery.scaladsl.schema.TableSchemaWriter) instances for your classes into scope via the `bigQuerySchemaN` methods in @scaladoc[BigQuerySchemas](org.apache.pekko.stream.connectors.googlecloud.bigquery.scaladsl.schema.BigQuerySchemas$).]

Scala
: @@snip [snip](/google-cloud-bigquery/src/test/scala/docs/scaladsl/BigQueryDoc.scala) { #create-table }

Java
: @@snip [snip](/google-cloud-bigquery/src/test/java/docs/javadsl/BigQueryDoc.java) { #create-table }

## Apply custom settings to a part of the stream

In certain situations it may be desirable to modify the @apidoc[org.apache.pekko.stream.connectors.google.GoogleSettings] applied to a part of the stream, for example to change the project ID or use different @apidoc[org.apache.pekko.stream.connectors.google.RetrySettings].

Scala
: @@snip [snip](/google-cloud-bigquery/src/test/scala/docs/scaladsl/BigQueryDoc.scala) { #custom-settings }

Java
: @@snip [snip](/google-cloud-bigquery/src/test/java/docs/javadsl/BigQueryDoc.java) { #custom-settings }

## Make raw API requests

If you would like to interact with the BigQuery REST API beyond what the BigQuery connector supports, you can make authenticated raw requests via the @apidoc[BigQuery.singleRequest](BigQuery$) and @scala[@apidoc[BigQuery.paginatedRequest[Out]](BigQuery$)] @java[@apidoc[BigQuery.<Out>paginatedRequest](BigQuery$)] methods.
