/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) since 2016 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.stream.connectors.jakartams

import com.typesafe.config.{ Config, ConfigValueType }
import org.apache.pekko
import org.apache.pekko.actor.{ ActorSystem, ClassicActorSystemProvider }

/**
 * Settings for [[pekko.stream.connectors.jakartams.scaladsl.JmsConsumer.browse]] and [[pekko.stream.connectors.jakartams.javadsl.JmsConsumer.browse]].
 */
final class JmsBrowseSettings private (
    val connectionFactory: jakarta.jms.ConnectionFactory,
    val connectionRetrySettings: ConnectionRetrySettings,
    val destination: Option[Destination],
    val credentials: Option[Credentials],
    val selector: Option[String],
    val acknowledgeMode: AcknowledgeMode) {

  /** Factory to use for creating JMS connections. */
  def withConnectionFactory(value: jakarta.jms.ConnectionFactory): JmsBrowseSettings = copy(connectionFactory = value)

  /** Configure connection retrying. */
  def withConnectionRetrySettings(value: ConnectionRetrySettings): JmsBrowseSettings =
    copy(connectionRetrySettings = value)

  /** Set a queue name to browse from. */
  def withQueue(name: String): JmsBrowseSettings = copy(destination = Some(Queue(name)))

  /** Set a JMS to subscribe to. Allows for custom handling with [[pekko.stream.connectors.jakartams.CustomDestination CustomDestination]]. */
  def withDestination(value: Destination): JmsBrowseSettings = copy(destination = Option(value))

  /** Set JMS broker credentials. */
  def withCredentials(value: Credentials): JmsBrowseSettings = copy(credentials = Option(value))

  /**
   * JMS selector expression.
   *
   * @see https://docs.oracle.com/cd/E19798-01/821-1841/bncer/index.html
   */
  def withSelector(value: String): JmsBrowseSettings = copy(selector = Option(value))

  /** Set an explicit acknowledge mode. (Consumers have specific defaults.) */
  def withAcknowledgeMode(value: AcknowledgeMode): JmsBrowseSettings = copy(acknowledgeMode = value)

  private def copy(
      connectionFactory: jakarta.jms.ConnectionFactory = connectionFactory,
      connectionRetrySettings: ConnectionRetrySettings = connectionRetrySettings,
      destination: Option[Destination] = destination,
      credentials: Option[Credentials] = credentials,
      selector: Option[String] = selector,
      acknowledgeMode: AcknowledgeMode = acknowledgeMode): JmsBrowseSettings = new JmsBrowseSettings(
    connectionFactory = connectionFactory,
    connectionRetrySettings = connectionRetrySettings,
    destination = destination,
    credentials = credentials,
    selector = selector,
    acknowledgeMode = acknowledgeMode)

  override def toString =
    "JmsBrowseSettings(" +
    s"connectionFactory=$connectionFactory," +
    s"connectionRetrySettings=$connectionRetrySettings," +
    s"destination=$destination," +
    s"credentials=$credentials," +
    s"selector=$selector," +
    s"acknowledgeMode=${AcknowledgeMode.asString(acknowledgeMode)}" +
    ")"
}

object JmsBrowseSettings {

  val configPath = "pekko.connectors.jakartams.browse"

  /**
   * Reads from the given config.
   *
   * @param c Config instance read configuration from
   * @param connectionFactory Factory to use for creating JMS connections.
   */
  def apply(c: Config, connectionFactory: jakarta.jms.ConnectionFactory): JmsBrowseSettings = {
    def getOption[A](path: String, read: Config => A): Option[A] =
      if (c.hasPath(path) && (c.getValue(path).valueType() != ConfigValueType.STRING || c.getString(path) != "off"))
        Some(read(c))
      else None
    def getStringOption(path: String): Option[String] =
      if (c.hasPath(path) && c.getString(path).nonEmpty) Some(c.getString(path)) else None

    val connectionRetrySettings = ConnectionRetrySettings(c.getConfig("connection-retry"))
    val destination = None
    val credentials = getOption("credentials", c => Credentials(c.getConfig("credentials")))
    val selector = getStringOption("selector")
    val acknowledgeMode = AcknowledgeMode.from(c.getString("acknowledge-mode"))
    new JmsBrowseSettings(
      connectionFactory,
      connectionRetrySettings,
      destination,
      credentials,
      selector,
      acknowledgeMode)
  }

  /**
   * Java API: Reads from the given config.
   *
   * @param c Config instance read configuration from
   * @param connectionFactory Factory to use for creating JMS connections.
   */
  def create(c: Config, connectionFactory: jakarta.jms.ConnectionFactory): JmsBrowseSettings =
    apply(c, connectionFactory)

  /**
   * Reads from the default config provided by the actor system at `pekko.connectors.jakartams.browse`.
   *
   * @param actorSystem The actor system
   * @param connectionFactory Factory to use for creating JMS connections.
   */
  def apply(actorSystem: ActorSystem, connectionFactory: jakarta.jms.ConnectionFactory): JmsBrowseSettings =
    apply(actorSystem.settings.config.getConfig(configPath), connectionFactory)

  /**
   * Reads from the default config provided by the actor system at `pekko.connectors.jakartams.browse`.
   *
   * @param actorSystem The actor system
   * @param connectionFactory Factory to use for creating JMS connections.
   */
  def apply(actorSystem: ClassicActorSystemProvider,
      connectionFactory: jakarta.jms.ConnectionFactory): JmsBrowseSettings =
    apply(actorSystem.classicSystem, connectionFactory)

  /**
   * Java API: Reads from the default config provided by the actor system at `pekko.connectors.jakartams.browse`.
   *
   * @param actorSystem The actor system
   * @param connectionFactory Factory to use for creating JMS connections.
   */
  def create(actorSystem: ActorSystem, connectionFactory: jakarta.jms.ConnectionFactory): JmsBrowseSettings =
    apply(actorSystem, connectionFactory)

  /**
   * Java API: Reads from the default config provided by the actor system at `pekko.connectors.jakartams.browse`.
   *
   * @param actorSystem The actor system
   * @param connectionFactory Factory to use for creating JMS connections.
   */
  def create(actorSystem: ClassicActorSystemProvider,
      connectionFactory: jakarta.jms.ConnectionFactory): JmsBrowseSettings =
    apply(actorSystem.classicSystem, connectionFactory)

}
