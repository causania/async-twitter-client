package org.koderama.twitter
package model

import org.joda.time.DateTime
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility
import org.codehaus.jackson.annotate.{JsonAutoDetect, JsonIgnoreProperties}
import util.DateTimeDeserializer
import org.codehaus.jackson.map.annotate.JsonDeserialize

//TODO: see https://github.com/FasterXML/jackson-module-scala/issues/10

/**
 * Demarcation trait for all the system entities.
 * 
 * @author alejandro@koderama.com
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
sealed trait BaseEntity

/**
 * Represents a geo location point. The {@link Object#equals(Object)} and {@link Object#hashCode()} are overridden to
 * check whenever 2 points are equals based on the coordinates.
 *
 * This class in immutable and thread safe.
 *
 * @author alejandro@koderama.com
 */
case class GeoPoint(coordinates: List[Double]) extends BaseEntity

/**
 * Represents a Twitter user.
 *
 * Instances of this class are immutable and thread safe.
 *
 * @author alejandro@koderama.com
 */
case class User(id: String,
                time_zone: String,
                screen_name: String,
                notifications: Boolean,
                listed_count: Long,
                location: String,
                statuses_count: Long,
                description: String,
                contributors_enabled: Boolean,
                geo_enabled: Boolean,
                @JsonDeserialize(using = classOf[DateTimeDeserializer]) created_at: DateTime,
                followers_count: Long,
                url: String,
                friends_count: Long,
                lang: String,
                verified: Boolean,
                name: String,
                follow_request_sent: Boolean,
                following: Boolean,
                favourites_count: Long,
                utc_offset: Long) extends BaseEntity

/**
 * Represents a deletion request from twitter. Clients should discard deleted statuses immediately.Status deletion
 * messages may arrive before the status. Even in this scenario, the late arriving status should be deleted.
 *
 * Instances of this class are immutable and thread safe.
 *
 * @author alejandro@koderama.com
 */
case class TweetDelete(status: TweetDeleteStatus) extends BaseEntity

/**
 * Represents a deletion request status from twitter.
 *
 * Instances of this class are immutable and thread safe.
 *
 * @author alejandro@koderama.com
 */
case class TweetDeleteStatus(id: String, user_id: String) extends BaseEntity

/**
 * Class representing a tweet from the Twitter streaming API.
 *
 * Instances of this class are immutable and thread safe.
 *
 * @author alejandro@koderama.com
 */
case class Tweet(id: String,
                 coordinates: GeoPoint,
                 retweet_count: String,
                 geo: GeoPoint,
                 text: String,
                 favorited: Boolean,
                 source: String,
                 rel: String,
                 contributors: String,
                 @JsonDeserialize(using = classOf[DateTimeDeserializer]) created_at: DateTime,
                 in_reply_to_status_id: Long,
                 in_reply_to_screen_name: String,
                 in_reply_to_user_id: String,
                 truncated: Boolean,
                 retweeted: Boolean,
                 user: User,
                 delete: TweetDelete) extends BaseEntity