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

package org.apache.pekko.stream.connectors.elasticsearch.impl

import org.apache.pekko.annotation.InternalApi
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.Uri.Path
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.unmarshalling.Unmarshal
import org.apache.pekko.stream.connectors.elasticsearch._
import org.apache.pekko.stream.stage._
import org.apache.pekko.stream._
import org.apache.pekko.stream.connectors.elasticsearch

import scala.collection.immutable
import scala.concurrent.{ ExecutionContext, Future }

/**
 * INTERNAL API.
 *
 * Updates Elasticsearch without any built-in retry logic.
 */
@InternalApi
private[elasticsearch] final class ElasticsearchSimpleFlowStage[T, C](
    elasticsearchParams: ElasticsearchParams,
    settings: WriteSettingsBase[_, _],
    writer: MessageWriter[T])(implicit http: HttpExt, mat: Materializer, ec: ExecutionContext)
    extends GraphStage[
      FlowShape[(immutable.Seq[WriteMessage[T, C]], immutable.Seq[WriteResult[T, C]]), immutable.Seq[WriteResult[T,
          C]]]] {

  private val in =
    Inlet[(immutable.Seq[WriteMessage[T, C]], immutable.Seq[WriteResult[T, C]])]("messagesAndResultPassthrough")
  private val out = Outlet[immutable.Seq[WriteResult[T, C]]]("result")
  override val shape = FlowShape(in, out)

  private val restApi: RestBulkApi[T, C] = settings.apiVersion match {
    case ApiVersion.V5 =>
      new RestBulkApiV5[T, C](elasticsearchParams.indexName,
        elasticsearchParams.typeName.get,
        settings.versionType,
        settings.allowExplicitIndex,
        writer)
    case ApiVersion.V7 =>
      new RestBulkApiV7[T, C](elasticsearchParams.indexName, settings.versionType, settings.allowExplicitIndex, writer)

    case elasticsearch.OpensearchApiVersion.V1 =>
      new RestBulkApiV7[T, C](elasticsearchParams.indexName, settings.versionType, settings.allowExplicitIndex, writer)

    case other => throw new IllegalArgumentException(s"API version $other is not supported")
  }

  private val baseUri = Uri(settings.connection.baseUrl)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new StageLogic()

  private class StageLogic extends GraphStageLogic(shape) with InHandler with OutHandler with StageLogging {

    private var inflight = false

    private val failureHandler =
      getAsyncCallback[(immutable.Seq[WriteResult[T, C]], Throwable)](handleFailure)
    private val responseHandler =
      getAsyncCallback[(immutable.Seq[WriteMessage[T, C]], immutable.Seq[WriteResult[T, C]], String)](handleResponse)

    setHandlers(in, out, this)

    override def onPull(): Unit = tryPull()

    override def onPush(): Unit = {
      val endpoint = if (settings.allowExplicitIndex) "/_bulk" else s"/${elasticsearchParams.indexName}/_bulk"
      val (messages, resultsPassthrough) = grab(in)
      inflight = true
      val json: String = restApi.toJson(messages)

      log.debug("Posting data to Elasticsearch: {}", json)

      if (json.nonEmpty) {
        val uri = baseUri.withPath(Path(endpoint))
        val request = HttpRequest(HttpMethods.POST)
          .withUri(uri)
          .withEntity(HttpEntity(NDJsonProtocol.`application/x-ndjson`, json))
          .withHeaders(settings.connection.headers)

        ElasticsearchApi
          .executeRequest(
            request,
            connectionSettings = settings.connection)
          .map {
            case HttpResponse(StatusCodes.OK, _, responseEntity, _) =>
              Unmarshal(responseEntity)
                .to[String]
                .map(json => responseHandler.invoke((messages, resultsPassthrough, json)))
            case response: HttpResponse =>
              Unmarshal(response.entity).to[String].map { body =>
                failureHandler.invoke(
                  (resultsPassthrough,
                    new RuntimeException(s"Request failed for POST $uri, got ${response.status} with body: $body")))
              }
          }
          .recoverWith {
            case cause: Throwable =>
              failureHandler.invoke((Nil, new RuntimeException(s"Request failed for POST $uri", cause)))
              Future.failed(cause)
          }
      } else {
        // if all NOPs, pretend an empty response:
        handleResponse((messages, resultsPassthrough, """{"took":0, "errors": false, "items":[]}"""))
      }
    }

    private def handleFailure(
        args: (immutable.Seq[WriteResult[T, C]], Throwable)): Unit = {
      inflight = false
      val (resultsPassthrough, exception) = args

      log.error(s"Received error from elastic after having already processed {} documents. Error: {}",
        resultsPassthrough.size,
        exception)
      failStage(exception)
    }

    private def handleResponse(
        args: (immutable.Seq[WriteMessage[T, C]], immutable.Seq[WriteResult[T, C]], String)): Unit = {
      inflight = false
      val (messages, resultsPassthrough, response) = args

      if (log.isDebugEnabled) {
        import spray.json._
        log.debug("response {}", response.parseJson.prettyPrint)
      }
      val messageResults = restApi.toWriteResults(messages, response)

      if (log.isErrorEnabled) {
        messageResults.filterNot(_.success).foreach { failure =>
          if (failure.getError.isPresent) {
            log.error(s"Received error from elastic when attempting to index documents. Error: {}",
              failure.getError.get)
          }
        }
      }

      emit(out, messageResults ++ resultsPassthrough)
      if (isClosed(in)) completeStage()
      else tryPull()
    }

    private def tryPull(): Unit =
      if (!isClosed(in) && !hasBeenPulled(in)) {
        pull(in)
      }

    override def onUpstreamFinish(): Unit =
      if (!inflight) completeStage()
  }
}
