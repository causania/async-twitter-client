package org.koderama.twitter
package http.auth

import com.ning.http.client.AsyncHttpClient
import com.ning.http.util.Base64
import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.{TwitterApi}
import org.scribe.model.{OAuthRequest, Verb, Verifier}
import scala.collection.JavaConversions._

/**
 * Defines an interface to be used as part of the request creation process.
 *
 * @see https://dev.twitter.com/docs/streaming-api/concepts#authentication
 *
 * @author alejandro@koderama.com
 */
trait AuthenticationMechanism {

  /**
   * Add the required credentials to the request. How this information is hooked within the request depends on the
   * implementation.
   *
   * @param requestBuilder
   *            builder the request builder where the authorization information will be added.
   */
  def addAuthInfo(requestBuilder: AsyncHttpClient#BoundRequestBuilder)
}

/**
 * Basic Authentication sends user credentials within the HTTP request headers.
 * It easy to use, but insecure. OAuth is the Twitter preferred method of authentication.
 *
 * This mechanism will be deprecated at some point by Twitter.
 *
 * @author alejandro@koderama.com
 */
trait BasicAuthenticationMechanism extends AuthenticationMechanism {
  require(username != null)
  require(password != null)

  import BasicAuthenticationMechanism._

  def username: String

  def password: String

  override def addAuthInfo(requestBuilder: AsyncHttpClient#BoundRequestBuilder) {
    requestBuilder.addHeader(HeaderAuthorization, HeaderBasic + encodedLogon())
  }

  private def encodedLogon(): String = {
    val logon = username + ":" + password
    Base64.encode(logon.getBytes)
  }
}

object BasicAuthenticationMechanism {
  val HeaderAuthorization = "Authorization"
  val HeaderBasic = "Basic "
}

/**
 * Use OAuth workflow as authentication method.
 * Notice that this authentication mechanism requires user interaction.
 *
 * @author alejandro@koderama.com
 */
trait OAuthAuthenticationMechanism extends AuthenticationMechanism {

  protected lazy val service = {
    new ServiceBuilder().provider(classOf[TwitterApi]).apiKey(apiKey)
      .apiSecret(apiSecret).build()
  }

  protected lazy val accessToken = {
    val requestToken = service.getRequestToken
    val verifier = new Verifier(verificationCode(service.getAuthorizationUrl(requestToken)))

    service.getAccessToken(requestToken, verifier)
  }

  override def addAuthInfo(requestBuilder: AsyncHttpClient#BoundRequestBuilder) {
    val partial = requestBuilder.build()
    val verb = Verb.valueOf(partial.getMethod)
    val internal = new OAuthRequest(verb, partial.getUrl)
    service.signRequest(accessToken, internal)

    internal.getHeaders.foreach(kv => requestBuilder.setHeader(kv._1, kv._2))
    if (verb != Verb.GET) {
      val body = internal.getBodyContents
      requestBuilder.setHeader("Content-Type", "application/x-www-form-urlencoded")
      requestBuilder.setHeader("Content-Type", body.size.toString)
      requestBuilder.setBody(body)
    }
    requestBuilder.setUrl(internal.getUrl)
  }

  def apiKey: String

  def apiSecret: String

  /**
   * Gets the verification code returned on the OAuth process when the user accept the terms.
   *
   * @param authUrl the url representing the Twitter page where the user must accept the terms.
   */
  def verificationCode(authUrl: String): String
}

object OAuthAuthenticationMechanism {
  val HeaderConsumerKey = "oauth_consumer_key"
  val HeaderNonce = "oauth_nonce"
  val HeaderSignatureMethod = "oauth_signature_method"
  val HeaderToken = "oauth_token"
  val HeaderTimestamp = "oauth_timestamp"
  val HeaderVersion = "oauth_version"
  val HeaderSignature = "oauth_signature"
}