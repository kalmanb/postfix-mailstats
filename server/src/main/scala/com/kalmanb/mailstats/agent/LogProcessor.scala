package com.kalmanb.mailstats.agent

import akka.actor._
import scala.collection.mutable
import scala.concurrent.duration._

class LogProcessor(
    server: String,
    updater: ActorRef,
    flushInterval: FiniteDuration = 5 seconds) extends Actor {

  var totals = resetTotals

  def resetTotals = mutable.Map.empty[String, SummaryData]

  def receive = {
    case line: String ⇒ process(line)
    case Flush        ⇒ flush
  }

  def process(line: String) {
    val event = parse(line)
    event process match {
      case "smtp" ⇒
        if (event.message.contains("status=sent"))
          if (!event.message.contains("relay=127.0.0.1")) {
            val destination = """.*to=<[^@]+@([^>]+).*""".r
            val destination(destinationDomain) = event.message
            incrementTotals(event.queue, destinationDomain, 1)
          }
      case _ ⇒
    }
  }

  def incrementTotals(queue: String, domain: String, sent: Int) {
    val currentTotals = totals.getOrElse(queue, SummaryData(server, queue, mutable.Map.empty, mutable.Map.empty))
    val currentSentTo = currentTotals.sentToDomain.getOrElse(domain, 0)
    currentTotals.sentToDomain(domain) = currentSentTo + sent
    totals.put(queue, currentTotals)
  }

  def parse(line: String) = {
    val pattern = """.{16}([^ ]+) ([^\/]+)\/([^\[]+)\[[^:]+: (.*)""".r
    val pattern(server, queue, process, message) = line
    LogEvent(queue, process, message)
  }

  def flush = {
    totals.values foreach { updater ! _ }
    totals = resetTotals
  }

  implicit val ec = context.dispatcher
  val scheduled = context.system.scheduler.schedule(flushInterval, flushInterval, self, Flush)

  override def postStop() =
    scheduled.cancel
}
case object Flush

case class SummaryData(
  val server: String,
  val queue: String,
  val sentFromDomain: mutable.Map[String, Int],
  val sentToDomain: mutable.Map[String, Int])

case class LogEvent(
  queue: String,
  process: String,
  message: String)

