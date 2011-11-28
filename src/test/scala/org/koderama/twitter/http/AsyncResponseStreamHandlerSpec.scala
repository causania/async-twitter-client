package org.koderama.twitter
package http

import org.specs2.Specification
import org.specs2.mock.Mockito
import protocol.EntitySerializer
import model.{GeoPoint, Tweet}
import org.joda.time.DateTime
import java.lang.IllegalStateException
import com.ning.http.client.{HttpResponseStatus, HttpResponseBodyPart}
import com.ning.http.client.AsyncHandler.STATE

/**
 * Specs for the [[AsyncResponseStreamHandler]] trait
 *
 * @author alejandro@koderama.com
 */
class AsyncResponseStreamHandlerSpec extends Specification with Mockito {

  def is =
    "This is a specification to check the general behavior of the AsyncResponseStreamHandler trait" ^
      p ^
      "The AsyncResponseStreamHandler should" ^
      "Get an entity when a full response is recieved" ! ctx().e1 ^
      "Call to the upstream handler onError if there is an exception" ! ctx().e2 ^
      "Call to the upstream handler onErrorCode if there is an http problem" ! ctx().e3 ^
      "Does not call to the upstream handler onErrorCode if 200 is recieved" ! ctx().e4 ^
      "Does not call to the upstream handler if just control characters are received" ! ctx().e5

  end

  case class ctx() {

    val serializer = mock[EntitySerializer]
    val evenHandler = mock[ResponseEventHandler[Tweet]]

    // FW not defined methods to mocks
    val responseHandler = new AsyncResponseStreamHandler[Tweet] {

      def deserialize[T: ClassManifest](content: String): T = serializer.deserialize(content)

      def onEntity(entity: Tweet) {
        evenHandler.onEntity(entity)
      }

      def onError(throwable: Throwable) {
        evenHandler.onError(throwable)
      }

      def onErrorCode(code: Int) {
        evenHandler.onErrorCode(code)
      }
    }

    val tweet1 = new Tweet("1", null, "100+", new GeoPoint(List(2.3, 4.3)), "Hello there!",
      false, "web", null, null, new DateTime(2011, 11, 26, 12, 0, 0), 123456l, "some_screen_name", "some_user_id",
      false, false, null, null)

    def e1 = {
      val bodyPart = mock[HttpResponseBodyPart]
      bodyPart.getBodyPartBytes returns "{tweet1}".toCharArray.map(_.toByte)
      serializer.deserialize("{tweet1}").asInstanceOf[Tweet] returns tweet1

      val state = responseHandler.onBodyPartReceived(bodyPart)

      (state must equalTo(STATE.CONTINUE)) and (there was one(evenHandler).onEntity(tweet1))
    }

    def e2 = {
      val throwable = new IllegalStateException()
      responseHandler.onThrowable(throwable)

      there was one(evenHandler).onError(throwable)
    }

    def e3 = {
      val status = mock[HttpResponseStatus]
      status.getStatusCode returns 403
      status.getStatusText returns "Some error"

      val state = responseHandler.onStatusReceived(status)

      (state must equalTo(STATE.ABORT)) and (there was one(evenHandler).onErrorCode(403))
    }

    def e4 = {
      val status = mock[HttpResponseStatus]
      status.getStatusCode returns 200
      status.getStatusText returns "Ok"

      val state = responseHandler.onStatusReceived(status)

      (state must equalTo(STATE.CONTINUE)) and (there was no(evenHandler).onErrorCode(200))
    }

    def e5 = {
      val bodyPart = mock[HttpResponseBodyPart]
      bodyPart.getBodyPartBytes returns "\n\r".toCharArray.map(_.toByte)

      val state = responseHandler.onBodyPartReceived(bodyPart)

      (state must equalTo(STATE.CONTINUE)) and (there was no(evenHandler).onEntity(any))
    }
  }

}