package org.koderama.twitter
package protocol

import org.codehaus.jackson.map.ObjectMapper
import java.io.IOException
import org.joda.time.DateTime
import util.DateTimeDeserializer
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.codehaus.jackson.map.module.SimpleModule
import org.codehaus.jackson.{Version, JsonParseException}

/**
 * Defines a generic marshalling interface to be used by different kind of protocols (Json, Xml, etc).
 * 
 * @author alejandro@koderama.com
 */
trait EntitySerializer {

  /**
   * Receives a String, in a given format (XML, JSON, etc) and constructs an object for the target class.
   *
   * @param content the response in a String format.
   * @tparam T the class of the object to deserialize.
   * @return an object that resulted from the deserialization process.
   *
   * @throws TwitterProtocolException
   *             if there is a problem during the deserialization
   */
  def deserialize[T : ClassManifest](content: String): T
}

/**
 * Whenever there is a problem with the API protocol. E.g.: can't deserialize a message
 *
 * @author alejandro@koderama.com
 */
case class TwitterProtocolException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)

/**
 * Implementation of the [[org.koderama.twitter.protocol.EntitySerializer]] interface to be used for Json protocols.
 *
 * Instances of this class are immutable and thread safe.
 *
 * @author alejandro@koderama.com
 */
trait JsonEntitySerializer extends EntitySerializer {

  import JsonEntitySerializer._

  val mapper = new ObjectMapper()
  configure(mapper)
  mapper.registerModule(DefaultScalaModule)

  override def deserialize[T : ClassManifest](content: String): T = {
    try {
      mapper.readValue(content, implicitly[ClassManifest[T]].erasure.asInstanceOf[Class[T]])
    } catch {
      case e: JsonParseException => throw createException(content, e)
      case e: IOException => throw createException(content, e)
    }
  }

  protected def createException(content: String, e: Exception): TwitterProtocolException = {
    new TwitterProtocolException("Content %s is invalid".format(content), e);
  }

}

object JsonEntitySerializer {
  
  protected def configure(mapper: ObjectMapper) {
    val simpleModule = new SimpleModule("DateTimeDeserializer", new Version(1, 0, 0, null))
    simpleModule.addDeserializer(classOf[DateTime], new DateTimeDeserializer())
    mapper.registerModule(simpleModule)

    mapper.getSerializationConfig.withSerializationInclusion(Inclusion.NON_NULL)

  }
}
