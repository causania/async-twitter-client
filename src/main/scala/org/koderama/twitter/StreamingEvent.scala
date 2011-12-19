package org.koderama.twitter

sealed trait StreamingEvent

/**
 * Returns public statuses that match one or more filter predicates. At least one predicate parameter, follow,
 * locations, or track must be specified. Multiple parameters may be specified which allows most clients to use a
 * single connection to the Streaming API. Placing long parameters in the URL may cause the request to be rejected
 * for excessive URL length. Use a POST request header parameter to avoid long URLs.
 * <p>
 * The default access level allows up to 400 track keywords, 5,000 follow userids and 25 0.1-360 degree location
 * boxes.
 * </p>
 *
 * @see https://dev.twitter.com/docs/streaming-api/methods
 *
 * @param params
 *            possible parameters: count, delimited, follow, locations and track
 */
case class Filter(params: RequestParams = new RequestParams) extends StreamingEvent

/**
 * Returns a random sample of all public statuses. The default access level, ‘Spritzer’ provides a small proportion
 * of the Firehose, very roughly, 1% of all public statuses. The “Gardenhose” access level provides a proportion
 * more suitable for data mining and research applications that desire a larger proportion to be statistically
 * significant sample. Currently Gardenhose returns, very roughly, 10% of all public statuses. Note that these
 * proportions are subject to unannounced adjustment as traffic volume varies.
 *
 * @see https://dev.twitter.com/docs/streaming-api/methods
 *
 * @param params
 *            possible parameters: count and delimiter
 */
case class Sample(params: RequestParams = new RequestParams) extends StreamingEvent

/**
 * Represents when an entity is read in the stream
 */
case class EntityReceived[T](entity: T)extends StreamingEvent

/**
 * Represents when there was an error in the stream
 */
case class ExceptionOnProcessing(throwable: Throwable)extends StreamingEvent

/**
 * Represents when there was an error code in the stream
 */
case class ErrorCodeOnProcessing(code: Int)extends StreamingEvent