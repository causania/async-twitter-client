package org.koderama.twitter.integration

import org.koderama.twitter.util.Logging
import org.koderama.twitter.streaming.{OAuthTwitterStreamingSession}
import org.koderama.twitter._
import akka.actor._


/**
 * Simple integration test that will print incoming tweets to the console.
 *
 * @author alejandro@koderama.com
 */
object ConsoleTwitterClient extends App {

  // TODO: load config from file. For some reason ConfigFactory.load("application") doesn't work.
  val system = ActorSystem("ConsoleTwitterClient")

  try {
    val s = system.actorOf[ConsoleTwitterSession]
    s ! Sample()

    Thread.sleep(50000) // Wait some time to get some tweets
  } finally {
    system.stop()
  }
}

class ConsoleTwitterSession extends OAuthTwitterStreamingSession {

  val apiKey = "someKey"

  val apiSecret = "someSecret"

  val defaultTracks = Set("akka", "scala")

  val handler = this.context.actorOf[PrinterActor]

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