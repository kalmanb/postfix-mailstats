package com.kalmanb.mailstats.agent

import java.io.File
import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListenerAdapter
import akka.actor.ActorRef
import java.util.concurrent.atomic.AtomicBoolean

/**
 * We use start delay so that we're just getting the changes to the file, not
 * content that is already there
 */
class LogReader(file: File, processor: ActorRef, intervalMillis: Int = 1000, startDelay: Int = 5000) {
  val listener = new LogListener(processor)
  val tailer = Tailer.create(file, listener, intervalMillis)
  Thread sleep startDelay
  listener.running = true

  def stop = tailer.stop

  class LogListener(processor: ActorRef) extends TailerListenerAdapter {
    @volatile var running = false
    override def handle(line: String): Unit = {
      if (running)
        processor ! line
    }
  }
}

