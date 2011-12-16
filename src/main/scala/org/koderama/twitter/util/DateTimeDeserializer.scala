package org.koderama.twitter.util

import org.joda.time.format.DateTimeFormat
import java.util.Locale
import org.joda.time.DateTime
import org.codehaus.jackson.map.{DeserializationContext, JsonDeserializer}
import org.codehaus.jackson.JsonParser

/**
 * Deserialize [[org.joda.time.DateTime]] objects from a Json representation.
 * This class is intended only to be used for the
 * Twitter APi since it uses a specific date-time format: EEE MMM dd HH:mm:ss Z yyyy.
 *
 * If there is an [[java.lang.IllegalArgumentException]] while parsing the data, null is retrieved.
 *
 * Instances of this class are immutable and thread safe.
 *
 * @author alejandro@koderama.com
 */
class DateTimeDeserializer extends JsonDeserializer[DateTime] with Logging {

  import DateTimeDeserializer._

  override def deserialize(parser: JsonParser, ctx: DeserializationContext): DateTime = {
    var dt: DateTime = null
    try {
      dt = DateTimeFormatter.parseDateTime(parser.getText)
    } catch {
      case e: IllegalArgumentException => error("Date-time date couldn't be parsed: " + parser.getText, e)
    }

    dt
  }
}

object DateTimeDeserializer {
  val DateTimePattern = "EEE MMM dd HH:mm:ss Z yyyy"

  val DateTimeFormatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy").withLocale(Locale.ENGLISH)
}