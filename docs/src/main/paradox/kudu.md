# Apache Kudu

The Apache Pekko Connectors Kudu connector supports writing to [Apache Kudu](https://kudu.apache.org) tables.

Apache Kudu is a free and open source column-oriented data store in the Apache Hadoop ecosystem.

@@project-info{ projectId="kudu" }


## Artifacts

@@dependency [sbt,Maven,Gradle] {
  group=org.apache.pekko
  artifact=pekko-connectors-kudu_$scala.binary.version$
  version=$project.version$
  symbol2=PekkoVersion
  value2=$pekko.version$
  group2=org.apache.pekko
  artifact2=pekko-stream_$scala.binary.version$
  version2=PekkoVersion
}

The table below shows direct dependencies of this module and the second tab shows all libraries it depends on transitively.

@@dependencies { projectId="kudu" }

## Configuration

To connect to Kudu you need:

1. Describe the Kudu @javadoc[Schema](org.apache.kudu.Schema)
1. Define a converter function to map your data type to a @javadoc[PartialRow](org.apache.kudu.client.PartialRow)
1. Specify Kudu @javadoc[CreateTableOptions](org.apache.kudu.client.CreateTableOptions)
1. Set up Apache Pekko Connectors' @scaladoc[KuduTableSettings](org.apache.pekko.stream.connectors.kudu.KuduTableSettings)

Scala
:   @@snip [snip](/kudu/src/test/scala/docs/scaladsl/KuduTableSpec.scala) { #configure }

Java
:   @@snip [snip](/kudu/src/test/java/docs/javadsl/KuduTableTest.java) { #configure }

The @javadoc[KuduClient](org.apache.kudu.client.KuduClient) by default is automatically managed by the connector.
Settings for the client are read from the @github[reference.conf](/kudu/src/main/resources/reference.conf) file.
A manually initialized client can be injected to the stream using @scaladoc[KuduAttributes](org.apache.pekko.stream.connectors.kudu.KuduAttributes$)

Scala
:   @@snip [snip](/kudu/src/test/scala/docs/scaladsl/KuduTableSpec.scala) { #attributes }

Java
:   @@snip [snip](/kudu/src/test/java/docs/javadsl/KuduTableTest.java) { #attributes }

## Writing to Kudu in a Flow

Scala
: @@snip [snip](/kudu/src/test/scala/docs/scaladsl/KuduTableSpec.scala) { #flow }

Java
: @@snip [snip](/kudu/src/test/java/docs/javadsl/KuduTableTest.java) { #flow }


## Writing to Kudu with a Sink

Scala
: @@snip [snip](/kudu/src/test/scala/docs/scaladsl/KuduTableSpec.scala) { #sink }

Java
: @@snip [snip](/kudu/src/test/java/docs/javadsl/KuduTableTest.java) { #sink }
