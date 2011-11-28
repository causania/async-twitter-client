package org.koderama.twitter.http

import org.specs2.Specification
import org.specs2.mock.Mockito
import com.ning.http.client.{AsyncHandler, AsyncHttpClient}

/**
 * Specs for the [[BackOffStreamReconnectionStrategy]] trait
 *
 * @author alejandro@koderama.com
 */
class BackOffStreamReconnectionStrategySpec extends Specification with Mockito {

  val strategy = new BackOffStreamReconnectionStrategy {

  }

  val handler = mock[AsyncHandler[String]]

  def is =
    "This is a specification to check the general behavior of the BackOffStreamReconnectionStrategy trait" ^
      p ^
      "The BackOffStreamReconnectionStrategy should" ^
      "Wait some time when there was a network problem and retry" ! e1 ^
      "Wait some time when there was a http problem and retry" ! e2

  end

  def e1 = {
    val request = mock[AsyncHttpClient#BoundRequestBuilder]
    val initialTime = System.nanoTime()
    strategy.reconnectOnException(handler, request)
    val reconnect1 = System.nanoTime()
    strategy.reconnectOnException(handler, request)
    val reconnect2 = System.nanoTime()

    (reconnect1 - initialTime < reconnect2 - reconnect1 must beTrue) and (there was two(request).execute(handler))
  }

  def e2 = {
    val request = mock[AsyncHttpClient#BoundRequestBuilder]
    val initialTime = System.nanoTime()
    strategy.reconnectOnHttpError(handler, request)
    val reconnect1 = System.nanoTime()
    strategy.reconnectOnHttpError(handler, request)
    val reconnect2 = System.nanoTime()

    (reconnect1 - initialTime < reconnect2 - reconnect1 must beTrue) and (there was two(request).execute(handler))
  }
}