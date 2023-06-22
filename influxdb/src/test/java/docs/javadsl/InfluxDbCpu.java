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

package docs.javadsl;

import java.time.Instant;

import org.influxdb.annotation.Measurement;

// #define-class
@Measurement(name = "cpu", database = "InfluxDbTest")
public class InfluxDbCpu extends Cpu {

  public InfluxDbCpu() {}

  public InfluxDbCpu(
      Instant time,
      String hostname,
      String region,
      Double idle,
      Boolean happydevop,
      Long uptimeSecs) {
    super(time, hostname, region, idle, happydevop, uptimeSecs);
  }

  public InfluxDbCpu cloneAt(Instant time) {
    return new InfluxDbCpu(
        time, getHostname(), getRegion(), getIdle(), getHappydevop(), getUptimeSecs());
  }
}
// #define-class
