package org.koderama.twitter
package streaming

import akka.event.EventHandler
import java.io.IOException
import com.ning.http.client.AsyncHttpClient
import akka.config.Supervision.OneForOneStrategy
import akka.actor.{ActorRef, Actor}
import model.Tweet
import http.{BackOffStreamReconnectionStrategy, StreamReconnectionStrategy, AsyncResponseStreamHandler}
import protocol.{JsonEntitySerializer, EntitySerializer}
import http.auth.{BasicAuthenticationMechanism, AuthenticationMechanism}

/**
 * Aggregation trait that defines a default streaming session using [[BackOffStreamReconnectionStrategy]],
 * [[JsonEntitySerializer]] and [[BasicAuthenticationMechanism]]
 *
 * Concrete implementations still need to provide the definition for configuration methods.
 *
 * @author alejandro@koderama.com
 */
trait DefaultTwitterStreamingSession
  extends TwitterStreamingSession[Tweet] with BackOffStreamReconnectionStrategy with JsonEntitySerializer with BasicAuthenticationMechanism

/**
 * Represents a streaming session with the Twitter API. These sessions are expected to be open for a long period.
 *
 * Http connections will persist opened during the session. For more info see [[AsyncResponseStreamHandler]]
 * The creation of the [[AsyncHttpClient]] can be cutomized by overriding the [[#createHttpClient]] method.
 *
 * Concrete implementations need to provide a handler actor to be called when there is something to notify.
 * <p>
 * E.g.: <b>def handler = this.self.spawnLink[MyActorHandler]</>
 * </p>
 *
 * @author alejandro@koderama.com
 */
trait TwitterStreamingSession[T]
  extends Actor with StreamReconnectionStrategy with EntitySerializer with AuthenticationMechanism with TwitterConfiguration {
  self.faultHandler = OneForOneStrategy(List(classOf[Exception]), 5, 5000)

  EventHandler.info(this, "Twitter streaming session is starting up...")

  private val httpClient = createHttpClient()

  /**
   * Creates the [[AsyncHttpClient]] to be used for the underlying Http requests
   */
  protected def createHttpClient() = new AsyncHttpClient()

  /**
   * Defines where the incoming messages should be forwarded
   */
  def handler: ActorRef

  final override def receive: Receive = {
    case msg@Sample(params) => buildRequest(sampleUrl, params.toMap, "GET").execute()
    case msg@Filter(params) => {
      val paramsMap = params.toMap
      if (paramsMap.get("track") == None && defaultTracks != null && !defaultTracks.isEmpty) {
        buildRequest(filterUrl, paramsMap + ("track" -> defaultTracks.reduceLeft(_ + "," + _)), "POST").execute()
      } else {
        buildRequest(filterUrl, paramsMap, "POST").execute()
      }
    }
  }

  private def buildRequest(url: String, params: Map[String, String], method: String): RequestExecutor[T] = {
    if ("POST".equalsIgnoreCase(method)) {
      val request = httpClient.preparePost(url)
      params.foreach(e => request.addParameter(e._1, e._2))
      new RequestExecutor(request, this)
    } else if ("GET".equalsIgnoreCase(method)) {
      new RequestExecutor(httpClient.prepareGet(createGetUrl(url, params)), this)
    } else {
      throw new IllegalArgumentException(String.format("Method %s not supported", method))
    }
  }

  private def createGetUrl(baseUrl: String, params: Map[String, String]): String = {
    val url = new StringBuilder(baseUrl)
    if (params != null && !params.isEmpty) {
      url.append("?")
      params.foreach(e => if (e._1 != null && !e._1.trim.isEmpty) url.append(e._1 + "=" + e._2 + "&"))
    }

    url.toString()
  }

  override def postStop() {
    EventHandler.info(this, "Streaming session is shutting down...")
    httpClient.close()
  }
}

/**
 * Encapsulates the logic to perform a request for a given session.
 *
 * The [[TwitterStreamingSession]] specific implementation is used to perform some stream action, including:
 * <p>
 * 1. Streaming events will be forwarded to the session handler actor.<br/>
 * 2. If there is an error on the stream, the session [[StreamReconnectionStrategy]] will be used to reconnect.<br/>
 * 3. The session serialization configuration will be used to deserialize objects in the stream.<br/>
 * 4. The session [[AuthenticationMechanism]] is used to configure the request.<br/>
 * </p>
 *
 * @author alejandro@koderama.com
 */
class RequestExecutor[T](request: AsyncHttpClient#BoundRequestBuilder, session: TwitterStreamingSession[T])
  extends AsyncResponseStreamHandler[T] {

  def deserialize[T: ClassManifest](content: String): T = {
    session.deserialize[T](content)
  }

  /**
   * Executes the request
   */
  def execute() = {
    try {
      session.addAuthInfo(request)
      request.execute(this)
    } catch {
      case e: IOException => EventHandler.error(
        "IOException while using twitter stream api with method " + request.toString, e)
    }
  }

  final override def onEntity(entity: T) {
    session.handler ! EntityReceived(entity)
  }

  final override def onError(throwable: Throwable) {
    session.reconnectOnException(this, request)
    session.handler ! ExceptionOnProcessing(throwable)
  }

  final override def onErrorCode(code: Int) {
    session.reconnectOnHttpError(this, request)
    session.handler ! ErrorCodeOnProcessing(code)
  }
}

/**
 * Encapsulates all the API configuration
 *
 * @author alejandro@koderama.com
 */
trait TwitterConfiguration {
  val baseUrl = "https://stream.twitter.com/1/"

  val samplePath = "statuses/sample.json"

  val sampleUrl = baseUrl + samplePath

  val filterPath = "statuses/filter.json"

  val filterUrl = baseUrl + filterPath

  def defaultTracks: Set[String]
}