package org.koderama.twitter.integration

import org.koderama.twitter.util.Logging
import akka.event.EventHandler
import akka.actor.{Scheduler, Actor}
import org.koderama.twitter.streaming.{OAuthTwitterStreamingSession}
import org.koderama.twitter._


/**
 * Simple integration test that will print incoming tweets to the console.
 *
 * @author alejandro@koderama.com
 */
object ConsoleTwitterClient extends App {

  try {
    val s = Actor.actorOf[ConsoleTwitterSession].start()
    s ! Sample()

    Thread.sleep(1500000)
  } finally {
    EventHandler.shutdown()
    Actor.registry.shutdownAll()
    Actor.remote.shutdown()
    Scheduler.shutdown()
  }
}

class ConsoleTwitterSession extends OAuthTwitterStreamingSession {

  val apiKey = "someKey"

  val apiSecret = "someSecret"

  val defaultTracks = Set("akka", "scala")

  val handler = this.self.spawnLink[PrinterActor]

  def verificationCode(authUrl: String) = {
    println("Go to the url %s and enter the verification code".format(authUrl))
    Console.readLine()
  }
}

class PrinterActor extends Actor with Logging {
  def receive = {
    case EntityReceived(tweet) => info("Tweet: %s \n".format(tweet))
    case ExceptionOnProcessing(throwable) => info("Exception on stream: %s \n".format(throwable))
    case ErrorCodeOnProcessing(code) => info("Error code recieved: %s \n".format(code))
  }
}