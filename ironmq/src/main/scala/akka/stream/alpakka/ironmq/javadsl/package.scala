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

package akka.stream.alpakka.ironmq

import java.util.concurrent.CompletionStage

import akka.Done
import akka.stream.alpakka.ironmq.scaladsl.{
  Committable => ScalaCommittable,
  CommittableMessage => ScalaCommittableMessage
}

import scala.compat.java8.FutureConverters
import scala.concurrent.Future

/**
 * This implicit classes allow to convert the Committable and CommittableMessage between scaladsl and javadsl.
 */
package object javadsl {

  import FutureConverters._

  private[javadsl] implicit class RichScalaCommittableMessage(cm: ScalaCommittableMessage) {
    def asJava: CommittableMessage = new CommittableMessage {
      override def message: Message = cm.message
      override def commit(): CompletionStage[Done] = cm.commit().toJava
    }
  }

  private[javadsl] implicit class RichScalaCommittable(cm: ScalaCommittable) {
    def asJava: Committable = new Committable {
      override def commit(): CompletionStage[Done] = cm.commit().toJava
    }
  }

  private[javadsl] implicit class RichCommittableMessage(cm: CommittableMessage) {
    def asScala: ScalaCommittableMessage = new ScalaCommittableMessage {
      override def message: Message = cm.message
      override def commit(): Future[Done] = cm.commit().toScala
    }
  }

  private[javadsl] implicit class RichCommittable(cm: Committable) {
    def asScala: ScalaCommittable = new ScalaCommittable {
      override def commit(): Future[Done] = cm.commit().toScala
    }
  }

}
