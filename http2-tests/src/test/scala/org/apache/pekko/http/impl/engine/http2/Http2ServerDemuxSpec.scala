/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
 */

/*
 * Copyright (C) 2021-2022 Lightbend Inc. <https://www.lightbend.com>
 */

package org.apache.pekko.http.impl.engine.http2

import org.apache.pekko
import pekko.http.impl.engine.http2.FrameEvent.{ ParsedHeadersFrame, Setting, SettingsFrame }
import pekko.http.impl.engine.http2.Http2Protocol.SettingIdentifier
import pekko.http.impl.util.PekkoSpecWithMaterializer
import pekko.http.scaladsl.settings.Http2ServerSettings
import pekko.stream.scaladsl.{ BidiFlow, Flow, Keep }
import pekko.stream.testkit.scaladsl.{ TestSink, TestSource }
import pekko.util.{ ByteString, OptionVal }

import scala.collection.immutable.Seq

/**
 * low-level tests testing Http2ServerDemux in isolation
 */
class Http2ServerDemuxSpec extends PekkoSpecWithMaterializer("""
    pekko.http.server.remote-address-header = on
    pekko.http.server.http2.log-frames = on
    pekko.stream.materializer.debug.fuzzing-mode = on
  """) {
  "Http2ServerDemux" should {
    "not pull twice when started with initial RemoteSettings from HTTP/1.1 Upgrade" in {
      val settings = Http2ServerSettings("")
      val initialRemoteSettings = Seq[Setting](
        (SettingIdentifier.SETTINGS_MAX_CONCURRENT_STREAMS, 100),
        (SettingIdentifier.SETTINGS_INITIAL_WINDOW_SIZE, 335),
        (SettingIdentifier.SETTINGS_ENABLE_PUSH, 0))
      val bidi = BidiFlow.fromGraph(new Http2ServerDemux(settings, initialRemoteSettings, upgraded = true))

      val ((substreamProducer, (frameConsumer, frameProducer)), substreamConsumer) = TestSource.probe[Http2SubStream]
        .viaMat(bidi.joinMat(Flow.fromSinkAndSourceMat(TestSink.probe[FrameEvent], TestSource.probe[FrameEvent])(
          Keep.both))(Keep.right))(Keep.both)
        .toMat(TestSink.probe[Http2SubStream])(Keep.both)
        .run()

      frameConsumer.request(1000)
      substreamProducer.ensureSubscription()
      substreamConsumer.request(1000)
      frameProducer.ensureSubscription()

      // The request is taken from the HTTP/1.1 request that had the Upgrade
      // header and is passed to the handler code 'directly', bypassing the demux stage,
      // so the first thing the demux stage sees of this request is the response:
      val response = ParsedHeadersFrame(streamId = 1, endStream = true, Seq((":status", "200")), None)
      substreamProducer.sendNext(new Http2SubStream(
        response,
        OptionVal.None,
        Left(ByteString.empty),
        Map.empty))

      frameConsumer.expectNext shouldBe an[SettingsFrame]
      // The client could send an 'ack' here, but doesn't need to
      // frameProducer.sendNext(SettingsAckFrame(frame.asInstanceOf[SettingsFrame].settings))

      frameConsumer.expectNext shouldBe response
    }
  }
}
