package org.koderama.twitter
package streaming

import http.StreamReconnectionStrategy
import protocol.EntitySerializer
import http.auth.AuthenticationMechanism
import org.specs2.mock.Mockito
import util.Logging
import akka.testkit.TestActorRef
import org.specs2.Specification
import org.mockito.Mockito.verify
import org.specs2.specification.{Fragments, Step}
import com.ning.http.client.{HttpResponseBodyPart, AsyncHandler, AsyncHttpClient}
import model.{GeoPoint, Tweet}
import org.joda.time.DateTime
import akka.event.EventHandler
import akka.actor.{Scheduler, ActorRef, Actor}


trait AkkaSpec extends Specification {
  override def map(fs: => Fragments) = {
    fs ^ Step({
      EventHandler.shutdown()
      Actor.registry.shutdownAll()
      Actor.remote.shutdown()
      Scheduler.shutdown()
    })
  }
}

/**
 * Specs for the [[TwitterStreamingSession]] trait
 *
 * @author alejandro@koderama.com
 */
class TwitterStreamingSessionSpec extends AkkaSpec with Mockito {

  def is =
    "This is a specification to check the general behavior of the TwitterStreamingSession trait" ^
      p ^
      "The TwitterStreamingSession should" ^
      "Execute a GET for a Sample(params) message" ! ctx().e1 ^
      "Execute a POST for a Filter(params) message" ! ctx().e2

  end


  case class ctx() {
    val mocks = new TwitterStreamingSessionMocks {

    }

    val customSessionActorRef = TestActorRef(new TwitterStreamingSessionWithMocks(mocks)).start()
    val customSessionActor = customSessionActorRef.underlyingActor

    val tweet1 = new Tweet("1", null, "100+", new GeoPoint(List(2.3, 4.3)), "Hello there!",
      false, "web", null, null, new DateTime(2011, 11, 26, 12, 0, 0), 123456l, "some_screen_name", "some_user_id",
      false, false, null, null)

    def e1 = {
      val get = mock[AsyncHttpClient#BoundRequestBuilder]
      mocks.client.prepareGet("https://stream.twitter.com/1/statuses/sample.json?") returns get

      customSessionActorRef ? Sample()

      verifyOnEntity(get, tweet1)
    }

    def e2 = {
      val post = mock[AsyncHttpClient#BoundRequestBuilder]
      mocks.client.preparePost("https://stream.twitter.com/1/statuses/filter.json") returns post

      customSessionActorRef ? Filter()

      verifyOnEntity(post, tweet1)
    }


    def verifyOnEntity(request: AsyncHttpClient#BoundRequestBuilder, tweet1: Tweet) = {
      verify(mocks.auth).addAuthInfo(request)
      val handler = capture[AsyncHandler[String]]
      verify(request).execute(captured(handler))

      val response = mock[HttpResponseBodyPart]
      response.getBodyPartBytes returns "{tweet1}".toCharArray.map(_.toByte)
      response.isLast returns false
      mocks.serializer.deserialize("{tweet1}").asInstanceOf[Tweet] returns tweet1

      handler.value.onBodyPartReceived(response)
      verify(mocks.handler).!(EntityReceived(tweet1))
      end
    }

    def end = {
      true must beTrue
    }
  }

}

trait TwitterStreamingSessionMocks extends Mockito {
  val strategy = mock[StreamReconnectionStrategy]
  val serializer = mock[EntitySerializer]
  val auth = mock[AuthenticationMechanism]
  val client = mock[AsyncHttpClient]
  val handler = mock[ActorRef]
}

class TwitterStreamingSessionWithMocks(val mocks: TwitterStreamingSessionMocks) extends TwitterStreamingSession[Tweet] {

  val handler = mocks.handler

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