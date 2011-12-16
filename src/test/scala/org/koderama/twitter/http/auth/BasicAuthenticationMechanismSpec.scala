package org.koderama.twitter
package http.auth

import org.specs2.Specification
import org.koderama.twitter.http.auth.BasicAuthenticationMechanism._
import com.ning.http.client.AsyncHttpClient
import org.specs2.mock.Mockito
import org.mockito.Matchers
import org.mockito.Mockito.verify

/**
 * Specs for the [[BasicAuthenticationMechanism]] trait
 *
 * @author alejandro@koderama.com
 */
class BasicAuthenticationMechanismSpec extends Specification with Mockito {

  lazy val auth = new BasicAuthenticationMechanism {
    def username = "alejandro"

    def password = "mypass"
  }

  val encodedLogon = "YWxlamFuZHJvOm15cGFzcw=="

  val requestBuilder = mock[AsyncHttpClient#BoundRequestBuilder]

  def is =
    "This is a specification to check the general behavior of the BasicAuthenticationMechanism class" ^
      p ^
      "The BasicAuthenticationMechanism should" ^
      "Encode username: alejandro:mypass:" ! e1

  end

  def e1 = {
    val encodedLoginCaptor = capture[String]

    auth.addAuthInfo(requestBuilder)

    verify(requestBuilder).addHeader(Matchers.eq(HeaderAuthorization), captured(encodedLoginCaptor))
    encodedLoginCaptor.value must equalTo(HeaderBasic + encodedLogon)
  }
}