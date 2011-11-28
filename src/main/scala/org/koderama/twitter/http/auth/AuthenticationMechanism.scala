package org.koderama.twitter
package http.auth

import com.ning.http.client.AsyncHttpClient
import com.ning.http.util.Base64

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
 * Basic Authentication sends user credentials within the HTTP request headers. It easy to use, but insecure. OAuth is
 * the Twitter preferred method of authentication.
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