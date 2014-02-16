package com.kalmanb.mailstats.agent

import org.scalatest.FunSpec
import java.io.File
import java.io.FileWriter
import akka.actor._
import akka.testkit._
import scala.concurrent.duration._

class LogReaderTest extends FunSpec {
  implicit val system = ActorSystem("test")

  describe("log reader") {
    it("should read file but only from new content") {
      val file = new File("test.log")
      val writer = new FileWriter(file)
      writer.write("start\n")
      writer.flush

      val processor = TestProbe()
      val reader = new LogReader(file, processor.ref, 100, 100)
      writer.write("first\n")
      writer.write("second\n")
      writer.close

      // "start" should not be included 
      processor.expectMsg(100 millis, "first")
      processor.expectMsg("second")

      reader.stop
      file.delete
    }
  }
}
