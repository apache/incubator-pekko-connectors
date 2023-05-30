/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) since 2016 Lightbend Inc. <https://www.lightbend.com>
 */

package docs.scaladsl

import org.apache.pekko
import pekko.stream.connectors.couchbase.scaladsl.CouchbaseSession
import pekko.stream.connectors.couchbase.{ CouchbaseSessionRegistry, CouchbaseSessionSettings }
import pekko.stream.connectors.couchbase.testing.CouchbaseSupport
import pekko.stream.connectors.testkit.scaladsl.LogCapturing
import com.couchbase.client.java.document.JsonDocument
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CouchbaseSessionExamplesSpec
    extends AnyWordSpec
    with CouchbaseSupport
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures
    with LogCapturing {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(10.seconds, 250.millis)

  override def beforeAll(): Unit = super.beforeAll()
  override def afterAll(): Unit = super.afterAll()

  "a Couchbasesession" should {
    "be managed by the registry" in {
      // #registry
      import com.couchbase.client.java.env.{ CouchbaseEnvironment, DefaultCouchbaseEnvironment }

      // Pekko extension (singleton per actor system)
      val registry = CouchbaseSessionRegistry(actorSystem)

      // If connecting to more than one Couchbase cluster, the environment should be shared
      val environment: CouchbaseEnvironment = DefaultCouchbaseEnvironment.create()
      actorSystem.registerOnTermination {
        environment.shutdown()
      }

      val sessionSettings = CouchbaseSessionSettings(actorSystem)
        .withEnvironment(environment)
      val sessionFuture: Future[CouchbaseSession] = registry.sessionFor(sessionSettings, bucketName)
      // #registry
      sessionFuture.futureValue shouldBe a[CouchbaseSession]
    }

    "be created from settings" in {
      // #create

      implicit val ec: ExecutionContext = actorSystem.dispatcher
      val sessionSettings = CouchbaseSessionSettings(actorSystem)
      val sessionFuture: Future[CouchbaseSession] = CouchbaseSession(sessionSettings, bucketName)
      actorSystem.registerOnTermination(sessionFuture.flatMap(_.close()))

      val documentFuture = sessionFuture.flatMap { session =>
        val id = "myId"
        val documentFuture: Future[Option[JsonDocument]] = session.get(id)
        documentFuture.flatMap {
          case Some(jsonDocument) =>
            Future.successful(jsonDocument)
          case None =>
            Future.failed(new RuntimeException(s"document $id wasn't found"))
        }
      }
      // #create
      documentFuture.failed.futureValue shouldBe a[RuntimeException]
    }

    "be created from a bucket" in {
      implicit val ec: ExecutionContext = actorSystem.dispatcher
      // #fromBucket
      import com.couchbase.client.java.auth.PasswordAuthenticator
      import com.couchbase.client.java.{ Bucket, CouchbaseCluster }

      val cluster: CouchbaseCluster = CouchbaseCluster.create("localhost")
      cluster.authenticate(new PasswordAuthenticator("Administrator", "password"))
      val bucket: Bucket = cluster.openBucket("akka")
      val session: CouchbaseSession = CouchbaseSession(bucket)
      actorSystem.registerOnTermination {
        session.close()
      }

      val id = "myId"
      val documentFuture = session.get(id).flatMap {
        case Some(jsonDocument) =>
          Future.successful(jsonDocument)
        case None =>
          Future.failed(new RuntimeException(s"document $id wasn't found"))
      }
      // #fromBucket
      documentFuture.failed.futureValue shouldBe a[RuntimeException]
    }
  }
}
