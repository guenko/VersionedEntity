/*
 * Copyright (c) 2013, guenkogit@gmail.com All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the
 * following disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */
package eu.koch.versent.base;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class TimeHelper {

  private static final Logger log = Logger.getLogger(TimeHelper.class);

  public static final String utcId = "UTC";
  public static final TimeZone utcTimeZone = TimeHelper.getTimeZoneChecked(TimeHelper.utcId);

  public static final Date END_OF_TIME;
  protected static final Date MAX_END_OF_TIME_RETRIEVAL;

  // precision the database is able to store time values in milliseconds, minimum 1
  // as of MYSQL it only store the full seconds and does not retrain the millisecond part
  public static long databaseTimePrecision = 1000;

  static {
    Calendar cal = Calendar.getInstance(TimeHelper.utcTimeZone);
    cal.clear();
    cal.set(9999, 11, 31, 0, 0, 0);
    END_OF_TIME = cal.getTime();
    log.info("endOfTime: " + getUtcDateTimeStr(END_OF_TIME));

    MAX_END_OF_TIME_RETRIEVAL = new Date(END_OF_TIME.getTime() - databaseTimePrecision);
  }

  public static TimeZone getTimeZoneChecked(String id) {
    TimeZone tz = TimeZone.getTimeZone(id);
    if (!tz.getID().equals(id)) {
      throw new IllegalArgumentException("illegal timezone id " + id);
    }
    return tz;
  }

  public static Date getNow() {
    return new Date();
  }

  public static String getDateTimeStr(Date date) {
    if (date == null) {
      return "null";
    }
    SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd.HHmmss");
    return sf.format(date);
  }

  public static String getUtcDateTimeStr(Date date) {
    SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd.HHmmss");
    sf.setTimeZone(utcTimeZone);
    return sf.format(date);
  }

  public static String getUtcDateTimeStrMilli(Date date) {
    SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd.HHmmss,SSS");
    sf.setTimeZone(utcTimeZone);
    return sf.format(date);
  }

  public static Date checkForEndOfTime(Date timeToCheck) {
    if (timeToCheck.getTime() >= END_OF_TIME.getTime()) {
      return MAX_END_OF_TIME_RETRIEVAL;
    }
    return timeToCheck;
  }

  public static Date getDatabasePrecision(Date date) {
    return new Date((date.getTime() / databaseTimePrecision) * databaseTimePrecision);
  }

  /**
   * Compares to Date object taking into account the precision of the database, so the two dates are treated
   * as equals as seen by the database
   */
  public static boolean databaseEqual(Date date1, Date date2) {
    return ((date1.getTime() / databaseTimePrecision) * databaseTimePrecision) == ((date2.getTime() / databaseTimePrecision) * databaseTimePrecision);
  }

  public static Date getNowIfNull(Date timeToCheck) {
    if (timeToCheck == null) {
      return getNow();
    }
    return timeToCheck;
  }

  public static Date getDefaultIfNull(Date timeToCheck, Date defaultTime) {
    if (timeToCheck == null) {
      return defaultTime;
    }
    return timeToCheck;
  }

  public static Date contrainUpdateTime(Date timeToCheck, Date referenceTime) {
    if (timeToCheck == null || timeToCheck.before(referenceTime)) {
      timeToCheck = referenceTime;
    }
    if (timeToCheck.after(END_OF_TIME)) {
      timeToCheck = END_OF_TIME;
    }
    return timeToCheck;
  }

  public static boolean isFuture(VersionedEntity entity, Date now) {
    return (now.getTime() < entity.getValidFrom().getTime());
  }

  public static boolean isCurrent(VersionedEntity entity, Date now) {
    return (entity.getValidFrom().getTime() <= now.getTime() && now.getTime() < entity.getValidTo().getTime());
  }
}
