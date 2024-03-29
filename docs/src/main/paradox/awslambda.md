# AWS Lambda

The AWS Lambda connector provides Apache Pekko Flow for AWS Lambda integration.

For more information about AWS Lambda please visit the [AWS lambda documentation](https://docs.aws.amazon.com/lambda/index.html).

@@project-info{ projectId="awslambda" }

## Artifacts

@@dependency [sbt,Maven,Gradle] {
  group=org.apache.pekko
  artifact=pekko-connectors-awslambda_$scala.binary.version$
  version=$project.version$
  symbol2=PekkoVersion
  value2=$pekko.version$
  group2=org.apache.pekko
  artifact2=pekko-stream_$scala.binary.version$
  version2=PekkoVersion
}

The table below shows direct dependencies of this module and the second tab shows all libraries it depends on transitively.

@@dependencies { projectId="awslambda" }

## Setup

The flow provided by this connector needs a prepared @javadoc[LambdaAsyncClient](software.amazon.awssdk.services.lambda.LambdaAsyncClient) to be able to invoke lambda functions.

Scala
: @@snip (/awslambda/src/test/scala/docs/scaladsl/Examples.scala) { #init-client }

Java
: @@snip (/awslambda/src/test/java/docs/javadsl/Examples.java) { #init-client }

The example above uses @extref:[Apache Pekko HTTP](pekko-http:) as the default HTTP client implementation. For more details about the HTTP client, configuring request retrying and best practices for credentials, see @ref[AWS client configuration](aws-shared-configuration.md) for more details.

We will need an @apidoc[org.apache.pekko.actor.ActorSystem].

Scala
: @@snip (/awslambda/src/test/scala/docs/scaladsl/Examples.scala) { #init-sys }

Java
: @@snip (/awslambda/src/test/java/docs/javadsl/Examples.java) { #init-sys }

This is all preparation that we are going to need.

## Sending messages

Now we can stream AWS Java SDK Lambda `InvokeRequest` to AWS Lambda functions
@apidoc[AwsLambdaFlow$] factory.

Scala
: @@snip (/awslambda/src/test/scala/docs/scaladsl/Examples.scala) { #run }

Java
: @@snip (/awslambda/src/test/java/docs/javadsl/Examples.java) { #run }

## AwsLambdaFlow configuration

Options:

 - `parallelism` - Number of parallel executions. Should be less or equal to number of threads in ExecutorService for LambdaAsyncClient 

@@@ index

* [retry conf](aws-shared-configuration.md)

@@@
