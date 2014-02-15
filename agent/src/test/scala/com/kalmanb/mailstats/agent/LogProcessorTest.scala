package com.kalmanb.mailstats.agent

import com.kalmanb.test.TestSpec
import akka.testkit._
import akka.actor._
import scala.concurrent.duration._
import scala.collection.mutable

class LogProcessorTest extends TestSpec {
  implicit val system = ActorSystem("test")

  describe("processor") {
    it("should find sent items") {
      /**
       * Full postfix log including an internal redirect to another postfix
       * priority queue listening on port 1025
       */
      val fullExample =
        """Feb 15 05:36:29 mail2 postfix/smtpd[18217]: connect from some.host[10.10.10.10]
Feb 15 05:36:29 mail2 postfix/smtpd[18217]: DDEB3E922BA: client=some.host[10.10.10.10]
Feb 15 05:36:29 mail2 postfix/cleanup[18219]: DDEB3E922BA: message-id=<1535816242.669430.1392442590079.JavaMail.atm@some.host>
Feb 15 05:36:29 mail2 postfix/qmgr[4535]: DDEB3E922BA: from=<from@domain.com>, size=8992, nrcpt=1 (queue active)
Feb 15 05:36:29 mail2 postfix-priority/smtpd[18220]: connect from localhost[127.0.0.1]
Feb 15 05:36:29 mail2 postfix-priority/smtpd[18220]: E98C9E922BB: client=localhost[127.0.0.1]
Feb 15 05:36:29 mail2 postfix-priority/cleanup[18222]: E98C9E922BB: message-id=<1535816242.669430.1392442590079.JavaMail.atm@some.host>
Feb 15 05:36:29 mail2 postfix-priority/qmgr[4561]: E98C9E922BB: from=<from@domian.com>, size=9217, nrcpt=1 (queue active)
Feb 15 05:36:29 mail2 postfix/smtp[17954]: DDEB3E922BA: to=<to@destination.com>, relay=127.0.0.1[127.0.0.1]:1025, delay=0.06, delays=0.02/0/0.02/0.02, dsn=2.0.0, status=sent (250 2.0.0 Ok: queued as E98C9E922BB)
Feb 15 05:36:29 mail2 postfix/qmgr[4535]: DDEB3E922BA: removed
Feb 15 05:36:29 mail2 postfix-priority/smtpd[18220]: disconnect from localhost[127.0.0.1]
Feb 15 05:36:31 mail2 postfix-priority/smtp[17977]: E98C9E922BB: to=<to@destination.com>, relay=aspmx.l.google.com[74.125.142.27]:25, delay=1.9, delays=0.01/0/0.24/1.6, dsn=2.0.0, status=sent (250 2.0.0 OK 1392442591 bu8si9870026icb.45 - gsmtp)
Feb 15 05:36:31 mail2 postfix-priority/qmgr[4561]: E98C9E922BB: removed
Feb 15 05:36:34 mail2 postfix/smtpd[18217]: disconnect from some.host[10.10.10.10]
"""
      val processor = getProcessor()
      fullExample.lines.foreach(line ⇒ processor ! line)

      processor.underlyingActor.totals.contains("postfix") should be(false)
      processor.underlyingActor.totals("postfix-priority").sentToDomain("destination.com") should be(1)
      processor.stop
    }

    it("should parse into LogEvent") {
      val line = "Feb 15 05:36:29 mail2 postfix/smtpd[18217]: connect from ..."
      val expected = LogEvent("postfix", "smtpd", "connect from ...")
      val processor = getProcessor().underlyingActor
      processor.parse(line) should be(expected)
      processor.postStop
    }
  }

  describe("push stats to persistence actor") {
    val sentLine = "Feb 15 05:36:29 mail2 postfix/smtp[18217]: to=<to@destination.com> status=sent ..."
    it("should send after timelimit and reset totals") {
      val dbUpdater = TestProbe()
      val processor = getProcessor(dbUpdater.ref)
      (1 to 10) map (_ ⇒ processor ! sentLine)
      Thread sleep 50

      processor.underlyingActor.totals("postfix").sentToDomain("destination.com") should be(10)

      // Wait for db update
      Thread sleep 100
      processor.underlyingActor.totals.contains("postfix") should be(false)

      val expected = SummaryData("localhost", "postfix", mutable.Map.empty, mutable.Map("destination.com" -> 10))
      dbUpdater.expectMsg(expected)
      processor.stop
    }
  }

  def getProcessor(updater: ActorRef = TestProbe().ref) = TestActorRef[LogProcessor](Props(new LogProcessor("localhost", updater, 100 millis)))
}
