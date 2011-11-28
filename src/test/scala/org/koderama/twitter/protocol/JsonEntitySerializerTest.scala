package org.koderama.twitter
package protocol

import org.specs2.Specification
import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.chrono.ISOChronology
import model.Tweet


/**
 * Specs for the [[JsonEntitySerializer]] trait
 *
 * @author alejandro@koderama.com
 */
class JsonEntitySerializerTest extends Specification {

  val ser = new JsonEntitySerializer {

  }
  val created_at = new DateTime(2011, 11, 26, 15, 47, 24, ISOChronology.getInstanceUTC).toDateTime(DateTimeZone.getDefault)
  val user_created_at = new DateTime(2010, 9, 19, 2, 24, 35, ISOChronology.getInstanceUTC).toDateTime(DateTimeZone.getDefault)

  def is =
    "This is a specification to check the general behavior of the JsonEntitySerializer class" ^
      p ^
      "The JsonEntitySerializer should" ^
      "Deserialize a Tweet object from a twitter response and have correct values" ! e1 ^
      "With id 192413253" ! e2 ^
      "With coordinates null" ! e3 ^
      "With retweet_count 0" ! e4 ^
      "With text \"@Hanna__M estrela, s2\"" ! e5 ^
      "With created_at %s".format(created_at) ! e6 ^
      "With in_reply_to_status_id 140455562998579200" ! e7 ^
      "With source web" ! e8 ^
      "User screen_name EmersonMeister" ! eu1 ^
      "User created_at %s".format(user_created_at) ! eu2 ^
      "User friends_count 65" ! eu3 ^
      "User lang pt" ! eu4

  end

  lazy val tweet: Tweet = ser.deserialize[Tweet](json)

  def e1 = tweet must not(beNull)

  def e2 = tweet.id must equalTo("140456629408763904")

  def e3 = tweet.coordinates must beNull

  def e4 = tweet.retweet_count must equalTo("0")

  def e5 = tweet.text must equalTo("@Hanna__M estrela, s2")

  def e6 = tweet.created_at must equalTo(created_at)

  def e7 = tweet.in_reply_to_status_id must equalTo(140455562998579200l)

  def e8 = tweet.source must equalTo("web")

  //def e9 = tweet.coordinates must equalTo("web")

  // User
  def eu1 = tweet.user.screen_name must equalTo("EmersonMeister")

  def eu2 = tweet.user.created_at must equalTo(user_created_at)

  def eu3 = tweet.user.friends_count must equalTo(65l)

  def eu4 = tweet.user.lang must equalTo("pt")


  // Json example from twitter response
  val json = "{\"text\":\"@Hanna__M estrela, s2\",\"in_reply_to_status_id\":140455562998579200,\"favorited\":false,\"" +
    "retweet_count\":0,\"in_reply_to_screen_name\":\"Hanna__M\",\"truncated\":false,\"" +
    "created_at\":\"Sat Nov 26 15:47:24 +0000 2011\",\"entities\":{\"urls\":[],\"user_mentions\":" +
    "[{\"indices\":[0,9],\"screen_name\":\"Hanna__M\",\"name\":\"Hanna Beatriz\",\"id_str\":\"421857860\"," +
    "\"id\":421857860}],\"hashtags\":[]},\"source\":\"web\",\"place\":null,\"retweeted\":false," +
    "\"in_reply_to_status_id_str\":\"140455562998579200\",\"geo\":null,\"in_reply_to_user_id_str\":\"421857860\"," +
    "\"id_str\":\"140456629408763904\",\"contributors\":null,\"user\":{\"favourites_count\":0,\"profile_background_color\":\"131516\"," +
    "\"notifications\":null,\"profile_background_tile\":true,\"profile_sidebar_fill_color\":\"030303\",\"is_translator\":false," +
    "\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_background_images\\/369738872\\/SDC11403.JPG\"," +
    "\"created_at\":\"Sun Sep 19 02:24:35 +0000 2010\",\"friends_count\":65,\"description\":\"\",\"listed_count\":0," +
    "\"contributors_enabled\":false,\"lang\":\"pt\",\"geo_enabled\":false,\"profile_sidebar_border_color\":\"ffffff\"," +
    "\"show_all_inline_media\":false,\"profile_use_background_image\":true,\"url\":null,\"default_profile\":false," +
    "\"following\":null,\"profile_text_color\":\"333333\",\"protected\":false,\"screen_name\":\"EmersonMeister\"," +
    "\"default_profile_image\":false,\"statuses_count\":781,\"profile_background_image_url\":" +
    "\"http:\\/\\/a2.twimg.com\\/profile_background_images\\/369738872\\/SDC11403.JPG\",\"time_zone\":\"Brasilia\"," +
    "\"followers_count\":206,\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1606541063\\/IMG0091A_normal.jpg\"," +
    "\"profile_image_url\":\"http:\\/\\/a3.twimg.com\\/profile_images\\/1606541063\\/IMG0091A_normal.jpg\"," +
    "\"name\":\"Emerson Silva\",\"id_str\":\"192413253\",\"follow_request_sent\":null,\"verified\":false,\"profile_link_color\":\"0096fa\"," +
    "\"location\":\"\",\"id\":192413253,\"utc_offset\":-10800},\"id\":140456629408763904,\"coordinates\":null,\"in_reply_to_user_id\":421857860}"


}