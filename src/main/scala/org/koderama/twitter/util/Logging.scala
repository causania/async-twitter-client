package org.koderama.twitter
package util

import org.slf4j.LoggerFactory


/**
 * Convenient wrapper trait to be used on classes that need logging.
 * It's not required to use the classic pattern: if(isDebugEnabled)->debug("msg") to avoid string concatenation.
 * Just call the target logging method like debug("message:" + "details").
 * The parameter will only be evaluated if it's required.
 *
 * This class use slf4j in order to get the actual logger.
 *
 * @see org.slf4j.LoggerFactory
 *
 * @author alejandro@koderama.com
 */
trait Logging {

  //TODO: check if it's a Actor and use LoggingAdapter

  /**
   * The Logger instance. Created when we need it.
   */
  private lazy val logger = LoggerFactory.getLogger(getClass)

  /**
   * Log a debug level message.
   *
   * @param msg Message to log, only evaluated if debug logging is enabled.
   * @param t Throwable, optional.
   */
  protected def debug(msg: => Any, t: => Throwable = null) {
    if (logger.isDebugEnabled) {
      if (t != null) {
        logger.debug(msg.toString, t)
      } else {
        logger.debug(msg.toString)
      }
    }
  }

  /**
   * Log a info level message.
   *
   * @param msg Message to log, only evaluated if info logging is enabled.
   * @param t Throwable, optional.
   */
  protected def info(msg: => Any, t: => Throwable = null) {
    if (logger.isInfoEnabled) {
      if (t != null) {
        logger.info(msg.toString, t)
      } else {
        logger.info(msg.toString)
      }
    }
  }

  /**
   * Log a warn level message.
   *
   * @param msg Message to log, only evaluated if warn logging is enabled.
   * @param t Throwable, optional.
   */
  protected def warn(msg: => Any, t: => Throwable = null) {
    if (logger.isWarnEnabled) {
      if (t != null) {
        logger.warn(msg.toString, t)
      } else {
        logger.warn(msg.toString)
      }
    }
  }

  /**
   * Log a error level message.
   *
   * @param msg Message to log, only evaluated if error logging is enabled.
   * @param t Throwable, optional.
   */
  protected def error(msg: => Any, t: => Throwable = null) {
    if (logger.isErrorEnabled) {
      if (t != null) {
        logger.error(msg.toString, t)
      } else {
        logger.error(msg.toString)
      }
    }
  }
}