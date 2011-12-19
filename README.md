Asynchronous Twitter client intended to be used with the Twitter Streaming API.
Build on top of Ning/Netty/Akka provided a fully async end to end communication.

Each "Streaming Session" consist on a long running Http connection and a group
of actors who take care of the different stages.

Twitter sends the information by chunks. Then, each part will represent an individual message to be processed.

Usage:

```scala

object ConsoleTwitterClient extends App {

  val system = ActorSystem("ConsoleTwitterClient")

  try {
    val s = system.actorOf[ConsoleTwitterSession]
    s ! Sample()

    Thread.sleep(50000) // Wait some time to get some tweets
  } finally {
    system.stop()
  }


class ConsoleTwitterSession extends DefaultTwitterStreamingSession {
  def username = "someusername"

  def password = "somepass"

  val defaultTracks = Set("akka", "scala")

  def handler = this.context.actorOf[PrinterActor]
}

class PrinterActor extends Actor with Logging {
  def receive = {
    case EntityReceived(tweet) => info("Tweet: %s \n".format(tweet))
    case ExceptionOnProcessing(throwable) => info("Exception on stream: %s \n".format(throwable))
    case ErrorCodeOnProcessing(code) => info("Error code recieved: %s \n".format(code))
  }
}

```


So, you have to provide the handler (PrinterActor) and the login credentials.



Authentication

The DefaultTwitterStreamingSession use basic authentication. But, you can use the OAuthTwitterStreamingSession trait.


```scala
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
```


OAuthTwitterStreamingSession Looks like

```scala
trait OAuthTwitterStreamingSession
  extends TwitterStreamingSession[Tweet] with BackOffStreamReconnectionStrategy with JsonEntitySerializer with OAuthAuthenticationMechanism
```