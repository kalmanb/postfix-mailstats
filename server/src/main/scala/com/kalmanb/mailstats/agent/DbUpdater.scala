package com.kalmanb.mailstats.agent

import akka.actor._
import com.datastax.driver.core._
import scala.collection.JavaConversions._

class DbUpdater extends Actor {
  lazy val cluster = Cluster.builder.addContactPoint("localhost").build

  def receive = {
    case s: SummaryData ⇒
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
  
  def testGet(fromDomain:String, server:String, queue:String):List[Row] = {
    val session = cluster.connect

    val statement = new BoundStatement(session.prepare("""
      select * from mailstats.SentFrom 
      where  
        fromDomain = ?
        and server = ?
        and queue = ? 
      """))
    statement.bind(fromDomain, server, queue )
    val results = session.execute(statement).all

    session.shutdown
    results.toList
  }

  override def postStop() = cluster.shutdown
}

