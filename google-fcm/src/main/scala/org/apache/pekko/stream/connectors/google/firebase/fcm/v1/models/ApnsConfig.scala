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

package org.apache.pekko.stream.connectors.google.firebase.fcm.v1.models

/**
 * ApnsConfig model.
 * @see https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages#ApnsConfig
 */
case class ApnsConfig(
    headers: Option[Map[String, String]] = None,
    payload: Option[String] = None,
    fcm_options: Option[FcmOption] = None) {
  def withHeaders(value: Map[String, String]): ApnsConfig = this.copy(headers = Option(value))
  def withPayload(value: String): ApnsConfig = this.copy(payload = Option(value))
  def withFcmOptions(value: FcmOption): ApnsConfig = this.copy(fcm_options = Option(value))
}
object ApnsConfig {
  val empty: ApnsConfig = ApnsConfig()
  def fromJava(): ApnsConfig = empty
}
