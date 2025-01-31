/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) 2018-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.http.impl.model

import org.apache.pekko
import pekko.annotation.InternalApi
import pekko.http.scaladsl.model.Uri
import pekko.http.scaladsl.model.Uri.Host
import java.nio.charset.Charset

/**
 * INTERNAL API.
 */
@InternalApi
private[http] abstract class UriJavaAccessor

/**
 * INTERNAL API.
 */
@InternalApi
private[http] object UriJavaAccessor {
  import collection.JavaConverters._

  def hostApply(string: String): Host = Uri.Host(string)
  def hostApply(string: String, mode: Uri.ParsingMode): Host = Uri.Host(string, mode = mode)
  def hostApply(string: String, charset: Charset): Host = Uri.Host(string, charset = charset)
  def emptyHost: Uri.Host = Uri.Host.Empty

  def queryApply(params: Array[pekko.japi.Pair[String, String]]): Uri.Query = Uri.Query(params.map(_.toScala): _*)
  def queryApply(params: java.util.Map[String, String]): Uri.Query = Uri.Query(params.asScala.toSeq: _*)
  def queryApply(string: String, mode: Uri.ParsingMode): Uri.Query = Uri.Query(string, mode = mode)
  def queryApply(string: String, charset: Charset): Uri.Query = Uri.Query(string, charset = charset)
  def emptyQuery: Uri.Query = Uri.Query.Empty

  def pmStrict: Uri.ParsingMode = Uri.ParsingMode.Strict
  def pmRelaxed: Uri.ParsingMode = Uri.ParsingMode.Relaxed
}
