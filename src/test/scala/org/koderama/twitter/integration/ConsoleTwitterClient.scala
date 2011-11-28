package org.koderama.twitter.integration

import org.koderama.twitter.streaming.DefaultTwitterStreamingSession
import akka.actor.Actor
import org.koderama.twitter.util.Logging
import org.koderama.twitter.{ErrorCodeOnProcessing, ExceptionOnProcessing, EntityReceived, Filter}


/**
 * Simple integration test that will print incoming tweets to the console.
 *
 * @author alejandro@koderama.com
 */
object ConsoleTwitterClient extends App {

  val s = Actor.actorOf(new ConsoleTwitterSession).start()
  s ! Filter()

  Thread.sleep(20000)

  s.stop()

}

class ConsoleTwitterSession extends DefaultTwitterStreamingSession {
  def username = "someusername"

  def password = "somepass"

  val defaultTracks = Set("akka", "scala")

  def handler = this.self.spawnLink[PrinterActor]
}

class PrinterActor extends Actor with Logging {
  def receive = {
    case EntityReceived(tweet) => info("Tweet: %s \n".format(tweet))
    case ExceptionOnProcessing(throwable) => info("Exception on stream: %s \n".format(throwable))
    case ErrorCodeOnProcessing(code) => info("Error code recieved: %s \n".format(code))
  }
}