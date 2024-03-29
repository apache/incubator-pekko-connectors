# Google FCM

@@@ note { title="Google Firebase Cloud Messaging" }

Google Firebase Cloud Messaging (FCM) is a cross-platform messaging solution that lets you reliably deliver messages at no cost.

Using FCM, you can notify a client app that new email or other data is available to sync. You can send notification messages to drive user re-engagement and retention. For use cases such as instant messaging, a message can transfer a payload of up to 4KB to a client app.

@@@

The Apache Pekko Connectors Google Firebase Cloud Messaging connector provides a way to send notifications with [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging/).

@@project-info{ projectId="google-fcm" }

## Artifacts

@@dependency [sbt,Maven,Gradle] {
  group=org.apache.pekko
  artifact=pekko-connectors-google-fcm_$scala.binary.version$
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

@@dependencies { projectId="google-fcm" }


## Settings

The FCM connector @ref[shares its basic configuration](google-common.md) with all the Google connectors in Apache Pekko Connectors.
Additional FCM-specific configuration settings can be found in its own @github[reference.conf](/google-fcm/src/main/resources/reference.conf).
You can send test notifications [(so called validate only).](https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages/send)
And you can set the number of maximum concurrent connections.
There is a limitation in the docs; from one IP you can have maximum 1k pending connections,
and you may need to configure `pekko.http.host-connection-pool.max-open-requests` in your application.conf.


## Sending notifications

To send a notification message create your notification object, and send it!

Scala
: @@snip [snip](/google-fcm/src/test/scala/docs/scaladsl/FcmExamples.scala) { #imports #asFlow-send }

Java
: @@snip [snip](/google-fcm/src/test/java/docs/javadsl/FcmExamples.java) { #imports #asFlow-send }

With this type of send you can get responses from the server.
These responses can be @scaladoc[FcmSuccessResponse](org.apache.pekko.stream.connectors.google.firebase.fcm.FcmSuccessResponse) or @scaladoc[FcmErrorResponse](org.apache.pekko.stream.connectors.google.firebase.fcm.FcmErrorResponse).
You can choose what you want to do with this information, but keep in mind
if you try to resend the failed messages you will need to use exponential backoff! (see @extref:[Apache Pekko docs `RestartFlow.onFailuresWithBackoff`](pekko:stream/operators/RestartFlow/onFailuresWithBackoff.html))

If you don't care if the notification was sent successfully, you may use `fireAndForget`.

Scala
: @@snip [snip](/google-fcm/src/test/scala/docs/scaladsl/FcmExamples.scala) { #imports #simple-send }

Java
: @@snip [snip](/google-fcm/src/test/java/docs/javadsl/FcmExamples.java) { #imports #simple-send }

With fire and forget you will just send messages and ignore all the errors.

To help the integration and error handling or logging, there is a variation of the flow where you can send data beside your notification.

Scala
: @@snip [snip](/google-fcm/src/test/scala/docs/scaladsl/FcmExamples.scala) { #imports #withData-send }

Java
: @@snip [snip](/google-fcm/src/test/java/docs/javadsl/FcmExamples.java) { #imports #withData-send }

Here I send a simple string, but you could use any type.

## Scala only

You can build any notification described in the original documentation.
It can be done by hand, or using some builder method.
If you build your notification from scratch with options (and not with the provided builders), worth to check isSendable before sending.

Scala
: @@snip [snip](/google-fcm/src/test/scala/docs/scaladsl/FcmExamples.scala) { #noti-create }

There is a condition builder too.

Scala
: @@snip [snip](/google-fcm/src/test/scala/docs/scaladsl/FcmExamples.scala) { #condition-builder }
