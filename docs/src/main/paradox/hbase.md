# HBase

The connector provides sources, flows and sinks to interact with HBase database.

HBase is a column family NoSQL Database backed by HDFS.
For more information about HBase, please visit the [HBase documentation](https://hbase.apache.org).

@@project-info{ projectId="hbase" }

## Artifacts

@@dependency [sbt,Maven,Gradle] {
  group=org.apache.pekko
  artifact=pekko-connectors-hbase_$scala.binary.version$
  version=$project.version$
  symbol2=PekkoVersion
  value2=$pekko.version$
  group2=org.apache.pekko
  artifact2=pekko-stream_$scala.binary.version$
  version2=PekkoVersion
}

The table below shows direct dependencies of this module and the second tab shows all libraries it depends on transitively.

@@dependencies { projectId="hbase" }


## Converters

Converters map the domain object to a list of HBase mutations (`Append`, `Delete`, `Increment`, `Put`).

### Put

Scala
:   @@snip [snip](/hbase/src/test/scala/docs/scaladsl/HBaseStageSpec.scala) { #create-converter-put }

Java
:   @@snip [snip](/hbase/src/test/java/docs/javadsl/HBaseStageTest.java) { #create-converter-put }

### Append

Scala
:   @@snip [snip](/hbase/src/test/scala/docs/scaladsl/HBaseStageSpec.scala) { #create-converter-append }

Java
:   @@snip [snip](/hbase/src/test/java/docs/javadsl/HBaseStageTest.java) { #create-converter-append }

### Delete

Scala
:   @@snip [snip](/hbase/src/test/scala/docs/scaladsl/HBaseStageSpec.scala) { #create-converter-delete }

Java
:   @@snip [snip](/hbase/src/test/java/docs/javadsl/HBaseStageTest.java) { #create-converter-delete }

### Increment

Scala
:   @@snip [snip](/hbase/src/test/scala/docs/scaladsl/HBaseStageSpec.scala) { #create-converter-increment }

Java
:   @@snip [snip](/hbase/src/test/java/docs/javadsl/HBaseStageTest.java) { #create-converter-increment }

### Complex and noop mutations

To ignore an object return an empty `List` - this will have no effect on HBase.
You can also combine mutations to perform complex business logic:

Scala
:   @@snip [snip](/hbase/src/test/scala/docs/scaladsl/HBaseStageSpec.scala) { #create-converter-complex }

Java
:   @@snip [snip](/hbase/src/test/java/docs/javadsl/HBaseStageTest.java) { #create-converter-complex }

If you return a list of mutations they will be applied in the same order.
The list of mutations are not applied in an transaction, each mutation is independent.

## Settings

HBase combinators require @scaladoc[HTableSettings](org.apache.pekko.stream.connectors.hbase.HTableSettings).
If the table referenced in the settings does not exist, it will be created on demand.

Scala
:   @@snip [snip](/hbase/src/test/scala/docs/scaladsl/HBaseStageSpec.scala) { #create-settings }

Java
:   @@snip [snip](/hbase/src/test/java/docs/javadsl/HBaseStageTest.java) { #create-settings }

## Source

Scala
: @@snip [snip](/hbase/src/test/scala/docs/scaladsl/HBaseStageSpec.scala) { #source }

Java
: @@snip [snip](/hbase/src/test/java/docs/javadsl/HBaseStageTest.java) { #source }

## Flow

Scala
: @@snip [snip](/hbase/src/test/scala/docs/scaladsl/HBaseStageSpec.scala) { #flow }

Java
: @@snip [snip](/hbase/src/test/java/docs/javadsl/HBaseStageTest.java) { #flow }


## Sink

Scala
: @@snip [snip](/hbase/src/test/scala/docs/scaladsl/HBaseStageSpec.scala) { #sink }

Java
: @@snip [snip](/hbase/src/test/java/docs/javadsl/HBaseStageTest.java) { #sink }

## HBase administration commands

To manage HBase database, startup HBase shell (`$HBASE_HOME/bin/shell`), and run following commands:

```
list // list tables
scan "person" // select * from person
disable "person" // Disable table "person", before drop
drop "person"
```
