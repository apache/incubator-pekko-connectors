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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetOptions;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.client.java.kv.PersistTo;
import com.couchbase.client.java.kv.ReplicateTo;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.stream.Materializer;
import org.apache.pekko.stream.connectors.couchbase.CouchbaseDeleteResult;
import org.apache.pekko.stream.connectors.couchbase.CouchbaseSessionRegistry;
import org.apache.pekko.stream.connectors.couchbase.CouchbaseSessionSettings;
import org.apache.pekko.stream.connectors.couchbase.CouchbaseWriteFailure;
import org.apache.pekko.stream.connectors.couchbase.CouchbaseWriteResult;
import org.apache.pekko.stream.connectors.couchbase.CouchbaseWriteSettings;
import org.apache.pekko.stream.connectors.couchbase.javadsl.CouchbaseFlow;
import org.apache.pekko.stream.connectors.couchbase.javadsl.CouchbaseSession;
import org.apache.pekko.stream.connectors.couchbase.javadsl.CouchbaseSource;
import org.apache.pekko.stream.connectors.couchbase.testing.CouchbaseSupportClass;
import org.apache.pekko.stream.connectors.couchbase.testing.StringDocument;
import org.apache.pekko.stream.connectors.couchbase.testing.TestObject;
import org.apache.pekko.stream.connectors.testkit.javadsl.LogCapturingJunit4;
import org.apache.pekko.stream.javadsl.Sink;
import org.apache.pekko.stream.javadsl.Source;
import org.apache.pekko.stream.testkit.javadsl.StreamTestKit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import scala.concurrent.duration.FiniteDuration;

public class CouchbaseExamplesTest {

  @Rule
  public final LogCapturingJunit4 logCapturing = new LogCapturingJunit4();

  private static final CouchbaseSupportClass support = new CouchbaseSupportClass();
  private static final CouchbaseSessionSettings sessionSettings = support.sessionSettings();
  private static final String bucketName = support.bucketName();
  private static final String queryBucketName = support.queryBucketName();
  private static ActorSystem actorSystem;
  private static TestObject sampleData;
  private static List<TestObject> sampleSequence;

  @BeforeClass
  public static void beforeAll() {
    support.beforeAll();
    actorSystem = support.actorSystem();
    sampleData = support.sampleData();
    sampleSequence = support.sampleJavaList();
  }

  @AfterClass
  public static void afterAll() {
    support.afterAll();
  }

  @After
  public void checkForStageLeaks() {
    StreamTestKit.assertAllStagesStopped(Materializer.matFromSystem(actorSystem));
  }

  @Test
  public void registry() throws Exception {
    // #registry

    CouchbaseSessionRegistry registry = CouchbaseSessionRegistry.get(actorSystem);

    // If connecting to more than one Couchbase cluster, the environment should be shared
    ClusterEnvironment environment = ClusterEnvironment.builder().build();
    actorSystem.registerOnTermination(() -> environment.shutdown());

    CouchbaseSessionSettings sessionSettings =
        CouchbaseSessionSettings.create(actorSystem).withEnvironment(environment);
    CompletionStage<CouchbaseSession> sessionCompletionStage =
        registry.getSessionFor(sessionSettings);
    // #registry
    CouchbaseSession session =
        sessionCompletionStage.toCompletableFuture().get(3, TimeUnit.SECONDS);
    assertNotNull(session);
  }

  @Test
  public void session() throws Exception {
    // #session

    Executor executor = Executors.newSingleThreadExecutor();
    CouchbaseSessionSettings sessionSettings = CouchbaseSessionSettings.create(actorSystem);
    CompletionStage<CouchbaseSession> sessionCompletionStage = CouchbaseSession.create(sessionSettings, executor);
    actorSystem.registerOnTermination(() -> sessionCompletionStage.thenAccept(CouchbaseSession::close));
    sessionCompletionStage.thenAccept(
        session -> {
          String id = "myId";
          CompletableFuture<GetResult> documentCompletionStage = session.collection(bucketName).get(id, GetOptions.getOptions());
          documentCompletionStage.whenComplete((result, exception) -> {
            if (exception != null) {
              System.out.println("Document " + id + " wasn't found");
            } else {
              System.out.println(result.contentAsObject());
            }
          });
        });
    // #session
    Thread.sleep(1000); // wait future
  }

  @Test
  public void statement() throws Exception {
    support.upsertSampleData(queryBucketName);
    // #statement

    CompletionStage<List<JsonObject>> listCompletionStage = CouchbaseSource.fromQueryJson(
            sessionSettings, "select * from " + queryBucketName + " limit 10")
        .runWith(Sink.head(), actorSystem);
    // #statement
    List<JsonObject> jsonObjects =
        listCompletionStage.toCompletableFuture().get(3, TimeUnit.SECONDS);
    assertEquals(4, jsonObjects.size());
  }


  @Test
  public void settings() {
    // #write-settings
    CouchbaseWriteSettings writeSettings =
        CouchbaseWriteSettings.create()
            .withParallelism(3)
            .withPersistTo(PersistTo.FOUR)
            .withReplicateTo(ReplicateTo.THREE)
            .withTimeout(Duration.ofSeconds(5));
    // #write-settings

    assertEquals(writeSettings.timeout(), FiniteDuration.apply(5, TimeUnit.SECONDS));
  }

  @Test
  public void fromId() throws Exception {
    support.upsertSampleData(queryBucketName);
    // #fromId
    List<String> ids = Arrays.asList("First", "Second", "Third", "Fourth");

    CompletionStage<List<GetResult>> result =
        Source.from(ids)
            .via(CouchbaseFlow.fromId(sessionSettings, queryBucketName))
            .runWith(Sink.seq(), actorSystem);
    // #fromId

    List<GetResult> jsonObjects = result.toCompletableFuture().get(3, TimeUnit.SECONDS);
    assertEquals(4, jsonObjects.size());
  }

  @Test
  public void upsert() throws Exception {

    TestObject obj = new TestObject("First", "First");

    CouchbaseWriteSettings writeSettings = CouchbaseWriteSettings.create();

    // #upsert
    CompletionStage<MutationResult> jsonDocumentUpsert =
        Source.single(obj)
            .map(support::toJsonDocument)
            .via(CouchbaseFlow.upsert(sessionSettings, writeSettings, bucketName, s -> s.getString("id")))
            .runWith(Sink.head(), actorSystem);
    // #upsert
    MutationResult mutationResult = jsonDocumentUpsert.toCompletableFuture().get(3, TimeUnit.SECONDS);
  }

  @Test
  public void upsertDoc() throws Exception {
    CouchbaseWriteSettings writeSettings = CouchbaseWriteSettings.create();

    // #upsertDoc
    CompletionStage<MutationResult> stringDocumentUpsert =
        Source.single(sampleData)
            .map(support::toStringDocument)
            .via(CouchbaseFlow.upsert(sessionSettings, writeSettings, bucketName, StringDocument::id))
            .runWith(Sink.head(), actorSystem);
    // #upsertDoc

    MutationResult mutationResult = stringDocumentUpsert.toCompletableFuture().get(3, TimeUnit.SECONDS);
  }

  @Test
  public void upsertDocWithResult() throws Exception {
    CouchbaseWriteSettings writeSettings = CouchbaseWriteSettings.create();

    // #upsertDocWithResult
    CompletionStage<List<CouchbaseWriteResult<StringDocument>>> upsertResults =
        Source.from(sampleSequence)
            .map(support::toStringDocument)
            .via(CouchbaseFlow.upsertWithResult(sessionSettings, writeSettings, bucketName, StringDocument::id))
            .runWith(Sink.seq(), actorSystem);

    List<CouchbaseWriteResult<StringDocument>> writeResults =
        upsertResults.toCompletableFuture().get(3, TimeUnit.SECONDS);
    List<CouchbaseWriteFailure<StringDocument>> failedDocs =
        writeResults.stream()
            .filter(CouchbaseWriteResult::isFailure)
            .map(res -> (CouchbaseWriteFailure<StringDocument>) res)
            .collect(Collectors.toList());
    // #upsertDocWithResult

    assertThat(writeResults.size(), is(sampleSequence.size()));
    assertTrue("unexpected failed writes", failedDocs.isEmpty());
  }

  @Test
  public void replace() throws Exception {

    support.upsertSampleData(bucketName);

    TestObject obj = new TestObject("First", "FirstReplace");

    CouchbaseWriteSettings writeSettings = CouchbaseWriteSettings.create();

    // #replace
    CompletionStage<MutationResult> jsonDocumentReplace =
        Source.single(obj)
            .map(support::toJsonDocument)
            .via(CouchbaseFlow.replace(sessionSettings, writeSettings, bucketName, s -> s.getString("id")))
            .runWith(Sink.head(), actorSystem);
    // #replace

    jsonDocumentReplace.toCompletableFuture().get(3, TimeUnit.SECONDS);
  }

  @Test(expected = DocumentNotFoundException.class)
  public void replaceFailsWhenDocumentDoesntExists() throws Throwable {

    support.cleanAllInBucket(bucketName);

    TestObject obj = new TestObject("First", "FirstReplace");

    CouchbaseWriteSettings writeSettings = CouchbaseWriteSettings.create();

    // #replace

    CompletionStage<MutationResult> jsonDocumentReplace = Source.single(obj)
        .map(support::toJsonDocument)
        .via(CouchbaseFlow.replace(sessionSettings, writeSettings, bucketName, s -> s.getString("id")))
        .runWith(Sink.head(), actorSystem);
    // #replace

    try {
      jsonDocumentReplace.toCompletableFuture().get(3, TimeUnit.SECONDS);
    } catch (ExecutionException ex) {
      throw ex.getCause();
    }
  }

  @Test
  public void replaceDoc() throws Exception {

    support.upsertSampleData(bucketName);

    CouchbaseWriteSettings writeSettings = CouchbaseWriteSettings.create();

    TestObject obj = new TestObject("First", "FirstReplace");

    // #replaceDoc
    CompletionStage<MutationResult> stringDocumentReplace =
        Source.single(obj)
            .map(support::toStringDocument)
            .via(CouchbaseFlow.replace(sessionSettings, writeSettings, bucketName, StringDocument::id))
            .runWith(Sink.head(), actorSystem);
    // #replaceDoc

    MutationResult mutationResult = stringDocumentReplace.toCompletableFuture().get(3, TimeUnit.SECONDS);
  }

  @Test
  public void replaceDocWithResult() throws Exception {

    support.upsertSampleData(bucketName);

    CouchbaseWriteSettings writeSettings = CouchbaseWriteSettings.create();

    List<TestObject> list = new ArrayList<TestObject>();
    list.add(new TestObject("First", "FirstReplace"));
    list.add(new TestObject("Second", "SecondReplace"));
    list.add(new TestObject("Third", "ThirdReplace"));
    list.add(new TestObject("NotExisting", "Nothing")); // should fail
    list.add(new TestObject("Fourth", "FourthReplace"));

    // #replaceDocWithResult
    CompletionStage<List<CouchbaseWriteResult<StringDocument>>> replaceResults =
        Source.from(list)
            .map(support::toStringDocument)
            .via(CouchbaseFlow.replaceWithResult(sessionSettings, writeSettings, bucketName, StringDocument::id))
            .runWith(Sink.seq(), actorSystem);

    List<CouchbaseWriteResult<StringDocument>> writeResults =
        replaceResults.toCompletableFuture().get(3, TimeUnit.SECONDS);
    List<CouchbaseWriteFailure<StringDocument>> failedDocs =
        writeResults.stream()
            .filter(CouchbaseWriteResult::isFailure)
            .map(res -> (CouchbaseWriteFailure<StringDocument>) res)
            .collect(Collectors.toList());
    // #replaceDocWithResult

    assertThat(writeResults.size(), is(list.size()));
    assertThat(failedDocs.size(), is(1));
  }

  @Test
  public void delete() throws Exception {
    CouchbaseWriteSettings writeSettings = CouchbaseWriteSettings.create();
    // #delete
    CompletionStage<String> result =
        Source.single(sampleData.id())
            .via(CouchbaseFlow.delete(sessionSettings, writeSettings, bucketName))
            .runWith(Sink.head(), actorSystem);
    // #delete

    String id = result.toCompletableFuture().get(3, TimeUnit.SECONDS);

    assertSame(sampleData.id(), id);
  }

  @Test
  public void deleteWithResult() throws Exception {
    CouchbaseWriteSettings writeSettings = CouchbaseWriteSettings.create();
    // #deleteWithResult
    CompletionStage<CouchbaseDeleteResult> result =
        Source.single("non-existent")
            .via(CouchbaseFlow.deleteWithResult(sessionSettings, writeSettings, bucketName))
            .runWith(Sink.head(), actorSystem);
    // #deleteWithResult
    CouchbaseDeleteResult deleteResult = result.toCompletableFuture().get(3, TimeUnit.SECONDS);
    assertTrue(deleteResult.isFailure());
  }
}
