package com.kalmanb.mailstats.agent

import akka.actor._
import scala.concurrent.duration._
import scala.sys.process._
import java.io.File

object Main extends App {
  val system = ActorSystem("mailstats-agent")
  val config = system.settings.config

  val dbUpdater = system.actorOf(Props(new DbUpdater))
   
  val server = "hostname".!!
  val processor = system.actorOf(Props(new LogProcessor(server, dbUpdater, 30 seconds)))
  val file = new File(config.getString("mailstats.agent.postfix-log-filename"))
  val reader = new LogReader(file, processor)

  println("mailstats agent running")
}
