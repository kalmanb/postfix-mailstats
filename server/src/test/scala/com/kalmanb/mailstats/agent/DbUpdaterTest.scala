package com.kalmanb.mailstats.agent

import com.kalmanb.test.TestSpec
import akka.actor._
import akka.testkit._
import com.datastax.driver.core._
import scala.collection.JavaConversions._

class DbUpdaterTest extends TestSpec {
  implicit val system = ActorSystem("test")

  describe("db updater") {
    it("should send sent data to cassandra") {
      val updater = TestActorRef[DbUpdater](Props(new DbUpdater()))

      truncate("SentFrom")

      updater.underlyingActor.updateSent("domain", "server", "queue", 1)
      updater.underlyingActor.updateSent("domain", "server", "queue", 2)

      val results = getSent("domain", "server", "queue")
      results.size should be(2)
      results(0).getInt("sent") should be(1)
      results(1).getInt("sent") should be(2)

      updater.stop
    }
  }

  def getSent(fromDomain: String, server: String, queue: String): List[Row] = {
    val session = getSession

    val statement = new BoundStatement(session.prepare("""
      select * from mailstats.SentFrom 
      where  
        fromDomain = ?
        and server = ?
        and queue = ? 
      """))
    statement.bind(fromDomain, server, queue)
    val results = session.execute(statement).all

    session.shutdown
    results.toList
  }

  def truncate(table: String) = {
    val session = getSession
    session.execute(s"truncate mailstats.$table")
    session.shutdown
  }
  def getSession = {
    lazy val cluster = Cluster.builder.addContactPoint("localhost").build
     cluster.connect
  }
}

