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

package akka.stream.alpakka.geode.impl.stage

import akka.Done
import akka.annotation.InternalApi
import akka.stream.stage._
import akka.stream.{ ActorAttributes, Attributes, Outlet, SourceShape }
import org.apache.geode.cache.client.ClientCache

import scala.concurrent.{ Future, Promise }

@InternalApi
private[geode] class GeodeContinuousSourceStage[V](cache: ClientCache, name: String, sql: String)
    extends GraphStageWithMaterializedValue[SourceShape[V], Future[Done]] {

  override protected def initialAttributes: Attributes =
    super.initialAttributes and Attributes.name("GeodeContinuousSource") and ActorAttributes.IODispatcher

  val out = Outlet[V](s"geode.continuousSource")

  override def shape: SourceShape[V] = SourceShape.of(out)

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[Done]) = {
    val subPromise = Promise[Done]()

    (new GeodeCQueryGraphLogic[V](shape, cache, name, sql) {

        override val onConnect: AsyncCallback[Unit] = getAsyncCallback[Unit] { v =>
          subPromise.success(Done)
        }

        val onElement: AsyncCallback[V] = getAsyncCallback[V] { element =>
          if (isAvailable(out) && incomingQueueIsEmpty) {
            pushElement(out, element)
          } else
            enqueue(element)
          handleTerminaison()
        }

        //
        // This handler, will first forward initial (old) result, then new ones (continuous).
        //
        setHandler(
          out,
          new OutHandler {
            override def onPull() = {
              if (initialResultsIterator.hasNext)
                push(out, initialResultsIterator.next())
              else
                dequeue().foreach { e =>
                  pushElement(out, e)
                }
              handleTerminaison()
            }
          })

      }, subPromise.future)
  }
}
