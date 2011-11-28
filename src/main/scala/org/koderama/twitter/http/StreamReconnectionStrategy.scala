package org.koderama.twitter.http

import com.ning.http.client.{AsyncHttpClient, AsyncHandler}
import org.koderama.twitter.util.Logging
import java.io.IOException


/**
 * Defines a connection strategy to be used when there is a problem with the connection.
 *
 * @author alejandro@koderama.com
 */
trait StreamReconnectionStrategy {

  /**
   * Performs the required reconnect actions whenever there was a major error on the current connection.
   *
   * @param handler
   *            the handler in charge or process the response stream.
   * @param request
   *            the builder of the previously sent request.
   */
  def reconnectOnException(handler: AsyncHandler[String], request: AsyncHttpClient#BoundRequestBuilder)

  /**
   * Performs the required reconnect actions whenever the server responded with a error status.
   *
   * @param handler
   *            the handler in charge or process the response stream.
   * @param request
   *            the builder of the previously sent request.
   */
  def reconnectOnHttpError(handler: AsyncHandler[String], request: AsyncHttpClient#BoundRequestBuilder)
}


/**
 * Defines the configuration timeouts for reconnections
 *
 * @author alejandro@koderama.com
 */
trait ReconnectionTimeouts {

  /**
   * Default time to wait when a HTTP error occurred. Default value = 4000 milliseconds
   */
  def defaultHttpTimeout = 4000l

  /**
   * The time to add on the wait period after each HTTP error. Default value = 4000 milliseconds
   */
  def stepHttpTimeout = 4000l

  /**
   * The max time to wait with HTTP errors. Default value = 24000 milliseconds
   */
  def maxHttpTimeout = 24000l

  /**
   * Default time to wait when a network problem occurred. Default value = 500 milliseconds
   */
  def defaultNetworkTimeout = 500l

  /**
   * The time to add on the wait period after each network problem. Default value = 500 milliseconds
   */
  def stepNetworkTimeout = 500l

  /**
   * The max time to wait in the case of network problems. Default value = 200000 milliseconds
   */
  def maxNetworkTimeout = 200000l
}

/**
 * Implements the [[StreamReconnectionStrategy]] interface following the guidelines described at the Twitter site:
 *
 * When a network error (TCP/IP level) is encountered, back off linearly. Perhaps start at 250 milliseconds and cap at
 * 16 seconds. Network layer problems are generally transitory and tend to clear quickly.
 *
 * When a HTTP error (> 200) is returned, back off exponentially. Perhaps start with a 10 second wait, double on each
 * subsequent failure, and finally cap the wait at 240 seconds. Consider sending an alert to a human operator after
 * multiple HTTP errors, as there is probably a client configuration issue that is unlikely to be resolved without human
 * intervention. There's not much point in polling any faster in the face of HTTP error codes and your client may find
 * itself rate limited. Clients that reconnect immediately after an HTTP error or do not otherwise back-off
 * exponentially, will be automatically rate limited and risk long-term blacklisting...
 *
 * @see https://dev.twitter.com/docs/streaming-api/concepts#connecting
 *
 * @author alejandro@koderama.com
 */
trait BackOffStreamReconnectionStrategy extends StreamReconnectionStrategy with ReconnectionTimeouts with Logging {

  private var actualHttpTimeout = defaultHttpTimeout
  private var actualNetworkTimeout = defaultNetworkTimeout

  override def reconnectOnException(handler: AsyncHandler[String], request: AsyncHttpClient#BoundRequestBuilder) {
    info("Trying to reconnect...")
    sleepMilliseconds(actualNetworkTimeout)

    actualNetworkTimeout += stepNetworkTimeout
    if (actualNetworkTimeout > maxNetworkTimeout) {
      actualNetworkTimeout = maxNetworkTimeout
    }

    try {
      request.execute(handler)
    } catch {
      case e: IOException => error("Error while trying to reconnect", e)
    }
  }

  override def reconnectOnHttpError(handler: AsyncHandler[String], request: AsyncHttpClient#BoundRequestBuilder) {
    info("Trying to reconnect...")
    sleepMilliseconds(actualHttpTimeout)

    actualHttpTimeout += stepHttpTimeout
    if (actualHttpTimeout > maxHttpTimeout) {
      actualHttpTimeout = maxHttpTimeout
    }
    try {
      request.execute(handler)
    } catch {
      case e: IOException => error("Error while trying to reconnect", e)
    }
  }

  private def sleepMilliseconds(timeout: Long) {
    try {
      Thread.sleep(timeout)
    } catch {
      case ex: InterruptedException => debug("Got interrupted")
    }
  }

}