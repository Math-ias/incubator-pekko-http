/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) 2017-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.http.scaladsl.settings

import scala.annotation.nowarn
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class SettingsEqualitySpec extends AnyWordSpec with Matchers {

  val config = ConfigFactory.load.resolve

  "equality" should {
    "hold for ConnectionPoolSettings" in {
      val s1 = ConnectionPoolSettings(config)
      val s2 = ConnectionPoolSettings(config)

      s1 shouldBe s2
      s1.toString should startWith("ConnectionPoolSettings(")
    }

    "hold for ParserSettings.forServer" in {
      val s1 = ParserSettings(config)
      val s2 = ParserSettings(config)

      s1 shouldBe s2
      s1.toString should startWith("ParserSettings(")
    }: @nowarn("msg=apply in object ParserSettings is deprecated")

    "hold for ClientConnectionSettings" in {
      val s1 = ClientConnectionSettings(config)
      val s2 = ClientConnectionSettings(config)

      s1 shouldBe s2
      s1.toString should startWith("ClientConnectionSettings(")
    }

    "hold for ServerSettings" in {
      val s1 = ServerSettings(config)
      val s2 = ServerSettings(config)

      s1 shouldBe s2
      s1.toString should startWith("ServerSettings(")
    }
  }

}
