# Hadoop Distributed File System - HDFS

The connector offers Flows and Sources that interact with HDFS file systems.

For more information about Hadoop, please visit the [Hadoop documentation](https://hadoop.apache.org/).

@@project-info{ projectId="hdfs" }

## Artifacts

@@dependency [sbt,Maven,Gradle] {
  group=org.apache.pekko
  artifact=pekko-connectors-hdfs_$scala.binary.version$
  version=$project.version$
  symbol2=PekkoVersion
  value2=$pekko.version$
  group2=org.apache.pekko
  artifact2=pekko-stream_$scala.binary.version$
  version2=PekkoVersion
}

The table below shows direct dependencies of this module and the second tab shows all libraries it depends on transitively.

@@dependencies { projectId="hdfs" }


## Specifying a Hadoop Version

By default, HDFS connector uses Hadoop **@var[hadoop.version]**. If you are using a different version of Hadoop,
you should exclude the Hadoop libraries from the connector dependency and add the dependency for your preferred version.

## Set up client

Flows provided by this connector need a prepared `org.apache.hadoop.fs.FileSystem` to 
interact with HDFS.


Scala
: @@snip [snip](/hdfs/src/test/scala/docs/scaladsl//HdfsWriterSpec.scala) { #init-client }

Java
: @@snip [snip](/hdfs/src/test/java/docs/javadsl/HdfsWriterTest.java) { #init-client }


## Writing

The connector provides three Flows. Each flow requires `RotationStrategy` and `SyncStrategy` to run.
@scala[@scaladoc[HdfsFlow](org.apache.pekko.stream.connectors.hdfs.scaladsl.HdfsFlow$).]
@java[@scaladoc[HdfsFlow](org.apache.pekko.stream.connectors.hdfs.javadsl.HdfsFlow$).]

The flows push `OutgoingMessage` to a downstream.

### Data Writer

Use `HdfsFlow.data` to stream with @javadoc[FSDataOutputStream](org.apache.hadoop.fs.FSDataOutputStream) without any compression.


Scala
: @@snip [snip](/hdfs/src/test/scala/docs/scaladsl//HdfsWriterSpec.scala) { #define-data }

Java
: @@snip [snip](/hdfs/src/test/java/docs/javadsl/HdfsWriterTest.java) { #define-data }


### Compressed Data Writer

First, create @javadoc[CompressionCodec](org.apache.hadoop.io.compress.CompressionCodec).


Scala
: @@snip [snip](/hdfs/src/test/scala/docs/scaladsl//HdfsWriterSpec.scala) { #define-codec }

Java
: @@snip [snip](/hdfs/src/test/java/docs/javadsl/HdfsWriterTest.java) { #define-codec }


Then, use `HdfsFlow.compress` to stream with @javadoc[CompressionOutputStream](org.apache.hadoop.io.compress.CompressionOutputStream) and @javadoc[CompressionCodec](org.apache.hadoop.io.compress.CompressionCodec). 


Scala
: @@snip [snip](/hdfs/src/test/scala/docs/scaladsl//HdfsWriterSpec.scala) { #define-compress }

Java
: @@snip [snip](/hdfs/src/test/java/docs/javadsl/HdfsWriterTest.java) { #define-compress }


### Sequence Writer

Use `HdfsFlow.sequence` to stream a flat file consisting of binary key/value pairs.

#### Without Compression


Scala
: @@snip [snip](/hdfs/src/test/scala/docs/scaladsl//HdfsWriterSpec.scala) { #define-sequence }

Java
: @@snip [snip](/hdfs/src/test/java/docs/javadsl/HdfsWriterTest.java) { #define-sequence }


#### With Compression

First, define a codec.


Scala
: @@snip [snip](/hdfs/src/test/scala/docs/scaladsl//HdfsWriterSpec.scala) { #define-codec }

Java
: @@snip [snip](/hdfs/src/test/java/docs/javadsl/HdfsWriterTest.java) { #define-codec }


Then, create a flow.


Scala
: @@snip [snip](/hdfs/src/test/scala/docs/scaladsl//HdfsWriterSpec.scala) { #define-sequence-compressed }

Java
: @@snip [snip](/hdfs/src/test/java/docs/javadsl/HdfsWriterTest.java) { #define-sequence-compressed }

### Passing data through HdfsFlow

Use `HdfsFlow.dataWithPassThrough`, `HdfsFlow.compressedWithPassThrough` or `HdfsFlow.sequenceWithPassThrough`.

When streaming documents from Kafka, you might want to commit to Kafka. The flow will emit two messages.
For every input, it will produce `WrittenMessage` and when it rotates, `RotationMessage`.

Let's say that we have these classes.


Scala
: @@snip [snip](/hdfs/src/test/scala/docs/scaladsl//HdfsWriterSpec.scala) { #define-kafka-classes }

Java
: @@snip [snip](/hdfs/src/test/java/docs/javadsl/HdfsWriterTest.java) { #define-kafka-classes }


Then, we can stream with `passThrough`.


Scala
: @@snip [snip](/hdfs/src/test/scala/docs/scaladsl//HdfsWriterSpec.scala) { #kafka-example }

Java
: @@snip [snip](/hdfs/src/test/java/docs/javadsl/HdfsWriterTest.java) { #kafka-example }


## Configuration

We can configure the sink by `HdfsWritingSettings`. 


Scala
: @@snip [snip](/hdfs/src/test/scala/docs/scaladsl//HdfsWriterSpec.scala) { #define-settings }

Java
: @@snip [snip](/hdfs/src/test/java/docs/javadsl/HdfsWriterTest.java) { #define-settings }


### File path generator

@scaladoc[FilePathGenerator](org.apache.pekko.stream.connectors.hdfs.FilePathGenerator$) provides a functionality to generate rotation path in HDFS. 

Scala
: @@snip [snip](/hdfs/src/test/scala/docs/scaladsl//HdfsWriterSpec.scala) { #define-generator }

Java
: @@snip [snip](/hdfs/src/test/java/docs/javadsl/HdfsWriterTest.java) { #define-generator }


### Rotation Strategy


@scaladoc[RotationStrategy](org.apache.pekko.stream.connectors.hdfs.RotationStrategy$) provides a functionality to decide when to rotate files.

### Sync Strategy


@scaladoc[SyncStrategy](org.apache.pekko.stream.connectors.hdfs.SyncStrategy$) provides a functionality to decide when to synchronize the output.

## Reading

Use `HdfsSource` to read from HDFS.
@scala[@scaladoc[HdfsSource](org.apache.pekko.stream.connectors.hdfs.scaladsl.HdfsSource$).]
@java[@scaladoc[HdfsSource](org.apache.pekko.stream.connectors.hdfs.javadsl.HdfsSource$).]


### Data Reader


Scala
: @@snip [snip](/hdfs/src/test/scala/docs/scaladsl//HdfsReaderSpec.scala) { #define-data-source }

Java
: @@snip [snip](/hdfs/src/test/java/docs/javadsl/HdfsReaderTest.java) { #define-data-source }


### Compressed Data Reader


Scala
: @@snip [snip](/hdfs/src/test/scala/docs/scaladsl//HdfsReaderSpec.scala) { #define-compressed-source }

Java
: @@snip [snip](/hdfs/src/test/java/docs/javadsl/HdfsReaderTest.java) { #define-compressed-source }


### Sequence Reader


Scala
: @@snip [snip](/hdfs/src/test/scala/docs/scaladsl//HdfsReaderSpec.scala) { #define-sequence-source }

Java
: @@snip [snip](/hdfs/src/test/java/docs/javadsl/HdfsReaderTest.java) { #define-sequence-source }


## Running the example code

The code in this guide is part of runnable tests of this project. You are welcome to edit the code and run it in sbt.

Scala
:   ```
    sbt
    > hdfs/testOnly *.HdfsWriterSpec
    > hdfs/testOnly *.HdfsReaderSpec
    ```

Java
:   ```
    sbt
    > hdfs/testOnly *.HdfsWriterTest
    > hdfs/testOnly *.HdfsReaderTest
    ```
