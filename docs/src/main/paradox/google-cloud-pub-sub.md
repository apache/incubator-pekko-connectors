# Google Cloud Pub/Sub

@@@ note
Google Cloud Pub/Sub provides many-to-many, asynchronous messaging that decouples senders and receivers.

Further information at the official [Google Cloud documentation website](https://cloud.google.com/pubsub/docs/overview).
@@@

This connector communicates to Pub/Sub via HTTP requests (i.e. `https://pubsub.googleapis.com`). For a connector that uses gRPC for the communication, take a look at the alternative @ref[Apache Pekko Connectors Google Cloud Pub/Sub gRPC](google-cloud-pub-sub-grpc.md) connector.

@@project-info{ projectId="google-cloud-pub-sub" }

## Artifacts

@@dependency [sbt,Maven,Gradle] {
  group=org.apache.pekko
  artifact=pekko-connectors-google-cloud-pub-sub_$scala.binary.version$
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

The table below shows direct dependencies of this module and the second tab shows all libraries it depends on transitively.

@@dependencies { projectId="google-cloud-pub-sub" }


## Usage

The Pub/Sub connector @ref[shares its basic configuration](google-common.md) with all the Google connectors in Apache Pekko Connectors.
Additional Pub/Sub-specific configuration settings can be found in its own @github[reference.conf](/google-cloud-pub-sub/src/main/resources/reference.conf).

And prepare the actor system.

Scala
: @@snip [snip](/google-cloud-pub-sub/src/test/scala/docs/scaladsl/ExampleUsage.scala) { #init-system }

Java
: @@snip [snip](/google-cloud-pub-sub/src/test/java/docs/javadsl/ExampleUsageJava.java) { #init-system }

To publish a single request, build the message with a base64 data payload and put it in a @scaladoc[PublishRequest](org.apache.pekko.stream.connectors.googlecloud.pubsub.PublishRequest). Publishing creates a flow taking the messages and returning the accepted message ids.

Scala
: @@snip [snip](/google-cloud-pub-sub/src/test/scala/docs/scaladsl/ExampleUsage.scala) { #publish-single }

Java
: @@snip [snip](/google-cloud-pub-sub/src/test/java/docs/javadsl/ExampleUsageJava.java) { #publish-single }

To get greater performance you can batch messages together, here we send batches with a maximum size of 1000 or at a maximum of 1 minute apart depending on the source.

Scala
: @@snip [snip](/google-cloud-pub-sub/src/test/scala/docs/scaladsl/ExampleUsage.scala) { #publish-fast }

Java
: @@snip [snip](/google-cloud-pub-sub/src/test/java/docs/javadsl/ExampleUsageJava.java) { #publish-fast }

To consume the messages from a subscription you must subscribe then acknowledge the received messages. @scaladoc[PublishRequest](org.apache.pekko.stream.connectors.googlecloud.pubsub.ReceivedMessage)

Scala
: @@snip [snip](/google-cloud-pub-sub/src/test/scala/docs/scaladsl/ExampleUsage.scala) { #subscribe }

Java
: @@snip [snip](/google-cloud-pub-sub/src/test/java/docs/javadsl/ExampleUsageJava.java) { #subscribe }

If you want to automatically acknowledge the messages and send the ReceivedMessages to your own sink you can create a graph.

Scala
: @@snip [snip](/google-cloud-pub-sub/src/test/scala/docs/scaladsl/ExampleUsage.scala) { #subscribe-auto-ack }

Java
: @@snip [snip](/google-cloud-pub-sub/src/test/java/docs/javadsl/ExampleUsageJava.java) { #subscribe-auto-ack }

## Running the examples

To run the example code you will need to configure a project and pub/sub in google cloud and provide your own credentials.
