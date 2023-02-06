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

package org.apache.pekko.stream.connectors.jms.javadsl

import javax.jms.Message
import org.apache.pekko.NotUsed
import org.apache.pekko.stream.connectors.jms._
import org.apache.pekko.stream.javadsl.Source

import scala.jdk.CollectionConverters._

/**
 * Factory methods to create JMS consumers.
 */
object JmsConsumer {

  /**
   * Creates a source emitting [[javax.jms.Message]] instances, and materializes a
   * control instance to shut down the consumer.
   */
  def create(settings: JmsConsumerSettings): org.apache.pekko.stream.javadsl.Source[Message, JmsConsumerControl] =
    org.apache.pekko.stream.connectors.jms.scaladsl.JmsConsumer.apply(settings).mapMaterializedValue(
      toConsumerControl).asJava

  /**
   * Creates a source emitting Strings, and materializes a
   * control instance to shut down the consumer.
   */
  def textSource(settings: JmsConsumerSettings): org.apache.pekko.stream.javadsl.Source[String, JmsConsumerControl] =
    org.apache.pekko.stream.connectors.jms.scaladsl.JmsConsumer.textSource(settings).mapMaterializedValue(
      toConsumerControl).asJava

  /**
   * Creates a source emitting byte arrays, and materializes a
   * control instance to shut down the consumer.
   */
  def bytesSource(
      settings: JmsConsumerSettings): org.apache.pekko.stream.javadsl.Source[Array[Byte], JmsConsumerControl] =
    org.apache.pekko.stream.connectors.jms.scaladsl.JmsConsumer.bytesSource(settings).mapMaterializedValue(
      toConsumerControl).asJava

  /**
   * Creates a source emitting maps, and materializes a
   * control instance to shut down the consumer.
   */
  def mapSource(
      settings: JmsConsumerSettings)
      : org.apache.pekko.stream.javadsl.Source[java.util.Map[String, Any], JmsConsumerControl] =
    org.apache.pekko.stream.connectors.jms.scaladsl.JmsConsumer
      .mapSource(settings)
      .map(_.asJava)
      .mapMaterializedValue(toConsumerControl)
      .asJava

  /**
   * Creates a source emitting de-serialized objects, and materializes a
   * control instance to shut down the consumer.
   */
  def objectSource(
      settings: JmsConsumerSettings): org.apache.pekko.stream.javadsl.Source[java.io.Serializable, JmsConsumerControl] =
    org.apache.pekko.stream.connectors.jms.scaladsl.JmsConsumer.objectSource(settings).mapMaterializedValue(
      toConsumerControl).asJava

  /**
   * Creates a source emitting [[org.apache.pekko.stream.connectors.jms.AckEnvelope AckEnvelope]] instances, and materializes a
   * control instance to shut down the consumer.
   * It requires explicit acknowledgements on the envelopes. The acknowledgements must be called on the envelope and not on the message inside.
   */
  def ackSource(
      settings: JmsConsumerSettings): org.apache.pekko.stream.javadsl.Source[AckEnvelope, JmsConsumerControl] =
    org.apache.pekko.stream.connectors.jms.scaladsl.JmsConsumer.ackSource(settings).mapMaterializedValue(
      toConsumerControl).asJava

  /**
   * Creates a source emitting [[org.apache.pekko.stream.connectors.jms.TxEnvelope TxEnvelope]] instances, and materializes a
   * control instance to shut down the consumer.
   * It requires explicit committing or rollback on the envelopes.
   */
  def txSource(settings: JmsConsumerSettings): org.apache.pekko.stream.javadsl.Source[TxEnvelope, JmsConsumerControl] =
    org.apache.pekko.stream.connectors.jms.scaladsl.JmsConsumer.txSource(settings).mapMaterializedValue(
      toConsumerControl).asJava

  /**
   * Creates a source browsing a JMS destination (which does not consume the messages)
   * and emitting [[javax.jms.Message]] instances.
   */
  def browse(settings: JmsBrowseSettings): org.apache.pekko.stream.javadsl.Source[Message, NotUsed] =
    org.apache.pekko.stream.connectors.jms.scaladsl.JmsConsumer.browse(settings).asJava

  private def toConsumerControl(scalaControl: scaladsl.JmsConsumerControl) = new JmsConsumerControl {

    override def connectorState(): Source[JmsConnectorState, NotUsed] =
      scalaControl.connectorState.map(_.asJava).asJava

    override def shutdown(): Unit = scalaControl.shutdown()

    override def abort(ex: Throwable): Unit = scalaControl.abort(ex)
  }
}
