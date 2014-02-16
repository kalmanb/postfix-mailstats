package com.kalmanb.mailstats.agent

import akka.actor._
import com.datastax.driver.core._
import scala.collection.JavaConversions._

class DbUpdater extends Actor {
  val nodes = context.system.settings.config.("mailstats.agent.cassandra-nodes")
  lazy val cluster = {
    val builder = Cluster.builder
    nodes foreach { node ⇒ builder.addContactPoint(node) }
    builder.build
  }

  def receive = {
    case s: SummaryData ⇒ s.sentFromDomain foreach { i ⇒
      updateSent(i._1, s.server, s.queue, i._2)
    }
  }

  def updateSent(fromDomain: String, server: String, queue: String, sent: Integer) = {
    val session = cluster.connect

    val statement = new BoundStatement(session.prepare("""
      INSERT INTO mailstats.SentFrom 
      (fromDomain, server, queue, time, sent) 
      values (?,?,?,dateOf(NOW()),?)
      """))
    statement.bind(fromDomain, server, queue, sent)
    try {
      session.execute(statement)
    } catch { case _: Exception ⇒ println("oh no") }

    session.shutdown
  }

  override def postStop() = cluster.shutdown
}

