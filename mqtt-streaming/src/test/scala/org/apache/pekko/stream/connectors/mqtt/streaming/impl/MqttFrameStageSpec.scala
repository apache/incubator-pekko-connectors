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

package org.apache.pekko.stream.connectors.mqtt.streaming
package impl

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.connectors.testkit.scaladsl.LogCapturing
import org.apache.pekko.stream.scaladsl.{ Keep, Source }
import org.apache.pekko.stream.testkit.javadsl.TestSink
import org.apache.pekko.stream.testkit.scaladsl.TestSource
import org.apache.pekko.testkit.TestKit
import org.apache.pekko.util.ByteString
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class MqttFrameStageSpec
    extends TestKit(ActorSystem("MqttFrameStageSpec"))
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with LogCapturing {

  val MaxPacketSize = 100

  "framing" should {
    "frame a packet with just a fixed header" in {
      val bytes = ByteString.newBuilder.putByte(0).putByte(0).result()
      Source
        .single(bytes)
        .via(new MqttFrameStage(MaxPacketSize))
        .runWith(TestSink.probe(system))
        .request(1)
        .expectNext(bytes)
        .expectComplete()
    }

    "frame a packet with a fixed and variable header" in {
      val bytes = ByteString.newBuilder.putByte(0).putByte(1).putByte(0).result()
      Source
        .single(bytes)
        .via(new MqttFrameStage(MaxPacketSize))
        .runWith(TestSink.probe(system))
        .request(1)
        .expectNext(bytes)
        .expectComplete()
    }

    "frame two packets from bytes" in {
      val bytes = ByteString.newBuilder.putByte(0).putByte(1).putByte(0).result()
      Source
        .single(bytes ++ bytes)
        .via(new MqttFrameStage(MaxPacketSize))
        .runWith(TestSink.probe(system))
        .request(2)
        .expectNext(bytes, bytes)
        .expectComplete()
    }

    "frame a packet where its length bytes are split" in {
      val bytes0 = ByteString.newBuilder.putByte(0).putByte(0x80.toByte).result()
      val bytes1 = ByteString.newBuilder.putByte(1).putBytes(Array.ofDim(0x80)).result()

      val (pub, sub) =
        TestSource
          .probe(system)
          .via(new MqttFrameStage(MaxPacketSize * 2))
          .toMat(TestSink.probe(system))(Keep.both)
          .run()

      pub.sendNext(bytes0)

      sub.request(1)

      pub.sendNext(bytes1).sendComplete()

      sub
        .expectNext(bytes0 ++ bytes1)
        .expectComplete()
    }

    "fail if packet size exceeds max" in {
      val bytes = ByteString.newBuilder.putByte(0).putByte(MaxPacketSize.toByte).putByte(0).result()
      val ex =
        Source
          .single(bytes)
          .via(new MqttFrameStage(MaxPacketSize))
          .runWith(TestSink.probe(system))
          .request(1)
          .expectError()
      ex.getMessage shouldBe s"Max packet size of $MaxPacketSize exceeded with ${MaxPacketSize + 2}"
    }
  }

  override def afterAll(): Unit =
    TestKit.shutdownActorSystem(system)
}
