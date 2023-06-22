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

package org.apache.pekko.stream.connectors.googlecloud.bigquery.scaladsl.spray

import spray.json.{ deserializationError, DeserializationException, JsArray, JsValue, ProductFormats, StandardFormats }

/**
 * Provides the helpers for constructing custom BigQueryJsonFormat implementations for types implementing the Product trait
 * (especially case classes)
 */
trait BigQueryProductFormats extends BigQueryProductFormatsInstances { this: ProductFormats with StandardFormats =>

  protected def fromBigQueryField[T](value: JsValue, fieldName: String, index: Int)(
      implicit reader: BigQueryJsonReader[T]): T =
    value match {
      case x: JsArray =>
        try reader.read(x.elements(index).asJsObject.fields("v"))
        catch {
          case e: IndexOutOfBoundsException =>
            deserializationError("Object is missing required member '" + fieldName + "'", e, fieldName :: Nil)
          case DeserializationException(msg, cause, fieldNames) =>
            deserializationError(msg, cause, fieldName :: fieldNames)
        }
      case _ => deserializationError("Object expected in field '" + fieldName + "'", fieldNames = fieldName :: Nil)
    }

}
