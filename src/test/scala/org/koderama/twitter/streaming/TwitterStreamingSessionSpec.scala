package org.koderama.twitter
package streaming

import http.StreamReconnectionStrategy
import protocol.EntitySerializer
import http.auth.AuthenticationMechanism
import akka.actor.Actor
import model.Tweet
import com.ning.http.client.{AsyncHandler, AsyncHttpClient}
import org.specs2.mock.Mockito
import util.Logging
import akka.testkit.TestActorRef
import org.specs2.Specification

/**
 * Specs for the [[TwitterStreamingSession]] trait
 *
 * @author alejandro@koderama.com
 */
class TwitterStreamingSessionSpec extends Specification with Mockito {

  val mocks = new TwitterStreamingSessionMocks {

  }

  val customSessionActorRef = TestActorRef(new TwitterStreamingSessionWithMocks(mocks)).start()
  val customSessionActor = customSessionActorRef.underlyingActor

  def is =
    "This is a specification to check the general behavior of the TwitterStreamingSession trait" ^
      p ^
      "The TwitterStreamingSession should" ^
      "Execute a GET for a Sample(params) message" ! e1 ^
      "Execute a POST for a Filter(params) message" ! e2

  end

  def e1 = {
    val get = mock[AsyncHttpClient#BoundRequestBuilder]
    mocks.client.prepareGet("https://stream.twitter.com/1/statuses/sample.json") returns get

    customSessionActorRef ? Sample()

    there was one(mocks.client.preparePost("https://stream.twitter.com/1/statuses/sample.json"))
  }

  def e2 = {
    val post = mock[AsyncHttpClient#BoundRequestBuilder]
    mocks.client.preparePost("https://stream.twitter.com/1/statuses/filter.json") returns post

    customSessionActorRef ? Filter()

    there was one(mocks.client.preparePost("https://stream.twitter.com/1/statuses/filter.json"))
  }


  trait TwitterStreamingSessionMocks {
    val strategy = mock[StreamReconnectionStrategy]
    val serializer = mock[EntitySerializer]
    val auth = mock[AuthenticationMechanism]
    val client = mock[AsyncHttpClient]
  }


  class TwitterStreamingSessionWithMocks(val mocks: TwitterStreamingSessionMocks) extends TwitterStreamingSession[Tweet] {

    val handler = TestActorRef(new PrintActor)

    protected override def createHttpClient() = mocks.client

    val defaultTracks = Set("akka", "scala")

    def addAuthInfo(requestBuilder: AsyncHttpClient#BoundRequestBuilder) {
      mocks.auth.addAuthInfo(requestBuilder)
    }

    def reconnectOnException(handler: AsyncHandler[String], request: AsyncHttpClient#BoundRequestBuilder) {
      mocks.strategy.reconnectOnException(handler, request)
    }

    def reconnectOnHttpError(handler: AsyncHandler[String], request: AsyncHttpClient#BoundRequestBuilder) {
      mocks.strategy.reconnectOnHttpError(handler, request)
    }

    def deserialize[T: ClassManifest](content: String): T = mocks.serializer.deserialize(content)
  }

  class PrintActor extends Actor with Logging {
    def receive = {
      case msg => {
        info("Recieved: " + msg)
      }
    }
  }


}