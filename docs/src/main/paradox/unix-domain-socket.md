# Unix Domain Socket

[From Wikipedia](https://en.wikipedia.org/wiki/Unix_domain_socket), _A Unix domain socket or IPC socket (inter-process communication socket) is a data communications endpoint for exchanging data between processes executing on the same host operating system._ Unix Domain Sockets leverage files and so operating system level access control can be utilized. This is a security advantage over using TCP/UDP where IPC is required without a more complex [Transport Layer Security (TLS)](https://en.wikipedia.org/wiki/Transport_Layer_Security). Performance also favors Unix Domain Sockets over TCP/UDP given that the Operating System's network stack is bypassed.

This connector provides an implementation of a Unix Domain Socket with interfaces modelled on the conventional `Tcp` Apache Pekko Streams class. The connector uses JNI and so there are no native dependencies.

The binding and connecting APIs are extremely similar to the `Tcp` Apache Pekko Streams class. `UnixDomainSocket` is generally substitutable for `Tcp` except that the `SocketAddress` is different (Unix Domain Sockets requires a `java.io.File` as opposed to a host and port). Please read the following for details:

* @extref:[Scala user reference for `Tcp`](pekko:stream/stream-io.html?language=scala)
* @extref:[Java user reference for `Tcp`](pekko:stream/stream-io.html?language=java)


> Note that Unix Domain Sockets, as the name implies, do not apply to Windows.

@@project-info{ projectId="unix-domain-socket" }


## Artifacts

@@dependency [sbt,Maven,Gradle] {
  group=org.apache.pekko
  artifact=pekko-connectors-unix-domain-socket_$scala.binary.version$
  version=$project.version$
  symbol2=PekkoVersion
  value2=$pekko.version$
  group2=org.apache.pekko
  artifact2=pekko-stream_$scala.binary.version$
  version2=PekkoVersion
}

The table below shows direct dependencies of this module and the second tab shows all libraries it depends on transitively.

@@dependencies { projectId="unix-domain-socket" }

## Binding to a file

Scala
: @@snip [snip](/unix-domain-socket/src/test/scala/docs/scaladsl/UnixDomainSocketSpec.scala) { #binding }

Java
: @@snip [snip](/unix-domain-socket/src/test/java/docs/javadsl/UnixDomainSocketTest.java) { #binding }

## Connecting to a file

Scala
: @@snip [snip](/unix-domain-socket/src/test/scala/docs/scaladsl/UnixDomainSocketSpec.scala) { #outgoingConnection }

Java
: @@snip [snip](/unix-domain-socket/src/test/java/docs/javadsl/UnixDomainSocketTest.java) { #outgoingConnection }

