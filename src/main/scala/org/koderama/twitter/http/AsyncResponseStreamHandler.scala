package org.koderama.twitter
package http

import util.Logging
import com.ning.http.client.AsyncHandler.STATE
import com.ning.http.client.{HttpResponseStatus, HttpResponseHeaders, HttpResponseBodyPart, AsyncHandler}
import protocol.{EntitySerializer, TwitterProtocolException}

/**
 * An asynchronous handler which take cares of the different events within the request.
 *
 * Callback methods are invoked in the following order: 1 - [[#onStatusReceived(HttpResponseStatus)]] 2 -
 * [[#onHeadersReceived(HttpResponseHeaders)]] 3 - [[#onBodyPartReceived(HttpResponseBodyPart)]] (could be
 * invoked several times) 4 - [[#onCompleted()]] (once the response has been fully read).
 *
 * Interrupting the process of the asynchronous response can be achieved by returning a [[AsyncHandler.STATE#ABORT]]
 * at any moment during the processing of the asynchronous response.
 *
 * This handler will not store the partial responses (mainly to avoid OOM). Then [[#onCompleted()]] returns an empty string.
 *
 * Concrete implementations must provide a: [[ResponseEventHandler[T]]] and and [[EntitySerializer]] by using mixins
 *
 * @author alejandro@koderama.com
 */
trait AsyncResponseStreamHandler[T] extends AsyncHandler[String] with ResponseEventHandler[T] with EntitySerializer with Logging {

  import ResponseControlCharacter._
  import HttpResponseCode._

  override def onBodyPartReceived(content: HttpResponseBodyPart): STATE = {
    debug(new String(content.getBodyPartBytes))

    if (content.isLast) {
      content.markUnderlyingConnectionAsClosed()
    }

    val bodyResponse = new String(content.getBodyPartBytes)
    val escapedResponse = removeControlChars(bodyResponse)

    if (escapedResponse.length() > 0) {
      try {
        val entity: T = deserialize(escapedResponse)
        debug("Entity recieved: " + entity)

        onEntity(entity)
      } catch {
        case e: TwitterProtocolException => warn("Can't deserialize entity from " + escapedResponse, e)
      }
    }

    STATE.CONTINUE
  }

  override def onThrowable(throwable: Throwable) {
    debug("Error in the request process: " + throwable);
    onError(throwable);
  }

  override def onCompleted() = {
    ""
  }

  override def onHeadersReceived(headers: HttpResponseHeaders): STATE = {
    debug("Headers received " + headers.toString)
    STATE.CONTINUE
  }

  override def onStatusReceived(responseStatus: HttpResponseStatus): STATE = {
    debug("Status received %s ,code %s".format(responseStatus.getStatusText, responseStatus.getStatusCode))

    if (responseStatus.getStatusCode > HttpOk.id) {
      error("Http error code: %s text %s ".format(responseStatus.getStatusCode, responseStatus.getStatusText))

      responseStatus.getStatusCode match {
        case code if (code == HttpAuthorizationError.id) => error("The provided username and/or password are wrong")
        case code if (code == HttpTooManyLogins.id) => error("Too many logins. Retry later on")
        case _ => onErrorCode(responseStatus.getStatusCode)
      }

      STATE.ABORT
    } else {
      STATE.CONTINUE
    }
  }

  private def removeControlChars(bodyResponse: String): String = {
    if (bodyResponse.startsWith(NewLine) || bodyResponse.startsWith(Return)
      || bodyResponse.startsWith(Tab)) {
      debug("Some control characters were received")
      val modifiedResponse = new StringBuilder(bodyResponse)

      while (modifiedResponse.startsWith(NewLine) || modifiedResponse.startsWith(Return)
        || modifiedResponse.startsWith(Tab)) {
        modifiedResponse.replace(0, 1, "")
      }

      modifiedResponse.toString()
    } else {
      bodyResponse
    }
  }

}

/**
 * Represents the actions to be taken when something happen in the stream.
 *
 * @author alejandro@koderama.com
 */
trait ResponseEventHandler[T] {

  /**
   * Whenever an entity was read from the response.
   *
   * @param entity the incoming entity
   */
  def onEntity(entity: T)

  /**
   * Whenever there was a general error on the response stream.
   *
   * @param throwable the related error.
   */
  def onError(throwable: Throwable)

  /**
   * Whenever there was an Http error code.
   *
   * @param code the Http code.
   */
  def onErrorCode(code: Int)
}


object ResponseControlCharacter {
  val NewLine = "\n"
  val Tab = "\t"
  val Return = "\r"
}

object HttpResponseCode extends Enumeration {
  val HttpOk = Value(200)
  val HttpAuthorizationError = Value(401)
  val HttpTooManyLogins = Value(420)
}