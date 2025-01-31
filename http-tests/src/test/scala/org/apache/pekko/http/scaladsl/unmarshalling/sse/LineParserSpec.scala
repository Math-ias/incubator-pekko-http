/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) 2009-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.http
package scaladsl
package unmarshalling
package sse

import org.apache.pekko
import pekko.stream.scaladsl.{ Sink, Source }
import pekko.util.ByteString
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

final class LineParserSpec extends AsyncWordSpec with Matchers with BaseUnmarshallingSpec {

  "A LineParser" should {

    "parse lines terminated with either CR, LF or CRLF" in {
      Source
        .single(ByteString("line1\nline2\rline3\r\nline4\nline5\rline6\r\n\n"))
        .via(new LineParser(1048576))
        .runWith(Sink.seq)
        .map(_ shouldBe Vector("line1", "line2", "line3", "line4", "line5", "line6", ""))
    }

    "ignore a trailing non-terminated line" in {
      Source
        .single(ByteString("line1\nline2\rline3\r\nline4\nline5\rline6\r\n\nincomplete"))
        .via(new LineParser(1048576))
        .runWith(Sink.seq)
        .map(_ shouldBe Vector("line1", "line2", "line3", "line4", "line5", "line6", ""))
    }

    "ignore a trailing non-terminated line when parsing byte by byte" in {
      Source(ByteString("line1\nline2\rline3\r\nline4\nline5\rline6\r\n\nincomplete").map(ByteString(_)))
        .via(new LineParser(1048576))
        .runWith(Sink.seq)
        .map(_ shouldBe Vector("line1", "line2", "line3", "line4", "line5", "line6", ""))
    }

    "parse a line splitted into many chunks" in {
      Source(('a'.to('z') :+ '\n').map(ByteString(_)))
        .via(new LineParser(1048576))
        .runWith(Sink.seq)
        .map(_ shouldBe Vector('a'.to('z').mkString))
    }

    "parse lines with terminations between chunks" in {
      Source(("after" :: "\n" :: "\n" :: "before" :: "\r" :: "middle" :: "\n" :: Nil).map(ByteString(_)))
        .via(new LineParser(1048576))
        .runWith(Sink.seq)
        .map(_ shouldBe Vector("after", "", "before", "middle"))
    }
  }
}
