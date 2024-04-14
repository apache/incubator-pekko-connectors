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

package org.apache.pekko.stream.connectors.udp

import java.net.InetSocketAddress

import org.apache.pekko.util.ByteString

final class Datagram private (val data: ByteString, val remote: InetSocketAddress) {

  def withData(data: ByteString): Datagram = copy(data = data)

  def withRemote(remote: InetSocketAddress): Datagram = copy(remote = remote)

  /**
   * Java API
   */
  def getData(): ByteString = data

  /**
   * Java API
   */
  def getRemote(): InetSocketAddress = remote

  private def copy(data: ByteString = data, remote: InetSocketAddress = remote) =
    new Datagram(data, remote)

  override def toString: String =
    s"""Datagram(
       |  data   = $data
       |  remote = $remote
       |)""".stripMargin
}

object Datagram {
  def apply(data: ByteString, remote: InetSocketAddress): Datagram = new Datagram(data, remote)

  /**
   * Java API
   */
  def create(data: ByteString, remote: InetSocketAddress): Datagram = Datagram(data, remote)
}
