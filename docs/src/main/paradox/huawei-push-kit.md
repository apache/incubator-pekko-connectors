# Huawei Push Kit

@@@ note { title="Huawei Push Kit" }

Huawei Push Kit is a messaging service provided for you. It establishes a messaging channel from the cloud to devices. By integrating Push Kit, you can send messages to your apps on users' devices in real time.

@@@

The Apache Pekko Connectors Huawei Push Kit connector provides a way to send notifications with [Huawei Push Kit](https://developer.huawei.com/consumer/en/hms/huawei-pushkit).

@@project-info{ projectId="huawei-push-kit" }

## Artifacts

@@dependency [sbt,Maven,Gradle] {
group=org.apache.pekko
artifact=pekko-connectors-huawei-push-kit_$scala.binary.version$
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

@@dependencies { projectId="huawei-push-kit" }

## Settings

All of the configuration settings for Huawei Push Kit can be found in the @github[reference.conf](/huawei-push-kit/src/main/resources/reference.conf).

@@snip [snip](/huawei-push-kit/src/test/resources/application.conf) { #init-credentials }

The `test` and `maxConcurrentConnections`  parameters in @scaladoc[HmsSettings](org.apache.pekko.stream.connectors.huawei.pushkit.HmsSettings) are the predefined values.
You can send test notifications [(so called validate only).](https://developer.huawei.com/consumer/en/doc/development/HMSCore-References-V5/https-send-api-0000001050986197-V5)
And you can set the number of maximum concurrent connections.

## Sending notifications

To send a notification message create your notification object, and send it!

Scala
: @@snip [snip](/huawei-push-kit/src/test/scala/docs/scaladsl/PushKitExamples.scala) { #imports #asFlow-send }

Java
: @@snip [snip](/huawei-push-kit/src/test/java/docs/javadsl/PushKitExamples.java) { #imports #asFlow-send }

With this type of send you can get responses from the server.
These responses can be @scaladoc[PushKitResponse](org.apache.pekko.stream.connectors.huawei.pushkit.PushKitResponse) or @scaladoc[ErrorResponse](org.apache.pekko.stream.connectors.huawei.pushkit.ErrorResponse).
You can choose what you want to do with this information, but keep in mind
if you try to resend the failed messages you will need to use exponential backoff! (see @extref[[Apache Pekko docs `RestartFlow.onFailuresWithBackoff`](pekko:stream/operators/RestartFlow/onFailuresWithBackoff.html))

If you don't care if the notification was sent successfully, you may use `fireAndForget`.

Scala
: @@snip [snip](/huawei-push-kit/src/test/scala/docs/scaladsl/PushKitExamples.scala) { #imports #simple-send }

Java
: @@snip [snip](/huawei-push-kit/src/test/java/docs/javadsl/PushKitExamples.java) { #imports #simple-send }

With fire and forget you will just send messages and ignore all the errors.

To help the integration and error handling or logging, there is a variation of the flow where you can send data beside your notification.

## Scala only

You can build notification described in the original documentation.
It can be done by hand, or using some builder method.
See an example of the condition builder below.

Scala
: @@snip [snip](/huawei-push-kit/src/test/scala/docs/scaladsl/PushKitExamples.scala) { #condition-builder }
