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

package org.apache.pekko.stream.connectors.google.firebase.fcm.v1.models

sealed trait NotificationTarget

/**
 * Token model.
 * @see https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages
 */
final case class Token(token: String) extends NotificationTarget

/**
 * Topic model.
 * @see https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages
 */
final case class Topic(topic: String) extends NotificationTarget

/**
 * Condition model.
 * @see https://firebase.google.com/docs/reference/fcm/rest/v1/projects.messages
 */
final case class Condition(conditionText: String) extends NotificationTarget

object Condition {
  sealed trait ConditionBuilder {
    def &&(condition: ConditionBuilder): And = And(this, condition)
    def ||(condition: ConditionBuilder): Or = Or(this, condition)
    def unary_! : Not = Not(this)
    def toConditionText: String
  }
  final case class Topic(topic: String) extends ConditionBuilder {
    def toConditionText: String = s"'$topic' in topics"
  }
  final case class And(condition1: ConditionBuilder, condition2: ConditionBuilder) extends ConditionBuilder {
    def toConditionText: String = s"(${condition1.toConditionText} && ${condition2.toConditionText})"
  }
  final case class Or(condition1: ConditionBuilder, condition2: ConditionBuilder) extends ConditionBuilder {
    def toConditionText: String = s"(${condition1.toConditionText} || ${condition2.toConditionText})"
  }
  final case class Not(condition: ConditionBuilder) extends ConditionBuilder {
    def toConditionText: String = s"!(${condition.toConditionText})"
  }

  def apply(builder: ConditionBuilder): Condition =
    Condition(builder.toConditionText)
}
