package org.koderama.twitter

/**
 * <p>
 * Represents the parameters to be used for the different requests. Notice that each request might require different
 * parameters.
 * </p>
 * <p>
 * Instances of this class are immutable and thread safe.
 * </p>
 *
 * @author alejandro@koderama.com
 */
case class RequestParams(count: Option[Int] = None,
                         delimited: Option[Int] = None,
                         track: Option[Set[String]] = None,
                         follow: Option[Set[String]] = None,
                         locations: Option[Set[Double]] = None) {

  def toMap: Map[String, String] = {
    Map(mapSafely("count", count),
      mapSafely("delimited", delimited),
      mapSafely("follow", follow),
      mapSafely("locations", locations),
      mapSafely("track", track))
  }

  private def mapSafely(key: String, v: Option[Traversable[_]]): (String, String) = {
    v match {
      case None => "" -> ""
      case Some(col) => key -> col.map(_.toString).reduceLeft(_ + "," + _)
    }
  }

  private def mapSafely(key: String, v: Any): (String, String) = {
    v match {
      case None => "" -> ""
      case _ => key -> v.toString
    }
  }

  override def toString = toMap.toString()
}
