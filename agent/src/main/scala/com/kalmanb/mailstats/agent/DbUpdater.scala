package com.kalmanb.mailstats.agent

import akka.actor._
import com.datastax.driver.core._

class DbUpdater extends Actor {
  lazy val cluster = Cluster.builder.addContactPoint("localhost").build

  def receive = {                          
    case s: SummaryData ⇒ s.sentFromDomain foreach { i =>
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

