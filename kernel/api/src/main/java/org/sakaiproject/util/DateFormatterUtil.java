/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;

/**
 * Performs date validation respecting i18n.<br>
 * <b>Note:</b> This class does not support "hi_IN", "ja_JP_JP" and "th_TH" locales.
 */
@Slf4j
public final class DateFormatterUtil {

	private static final DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;

	private DateFormatterUtil() {
	}

	/**
	 * Performs date validation checking the ISO_ZONED_DATE_TIME format such as '2017-12-03T10:15:30+01:00[Europe/Paris]'.
	 *
	 * @param date
	 *            The candidate String date.
	 * @return TRUE - Conforms to a valid input date format string.
	 *         FALSE - Does not conform.
	 */
	public static boolean isValidISODate(final String date) {
		try {
		    isoFormatter.parse(date);
		    return true;
		  } catch (Exception e) { 
			  return false;
		  }
	}
	
	/**
	 * Parse the date string input using the ISO_ZONED_DATE_TIME format such as '2017-12-03T10:15:30+01:00[Europe/Paris]'.
	 * 
	 * @param inputDate
	 *            The string that needs to be parsed.
	 * @return The parsed date.
     */
	public static Date parseISODate(final String inputDate) {
		Date convertedDate = null;

		try {
			LocalDateTime ldt = LocalDateTime.parse(inputDate, isoFormatter);
			convertedDate = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		} catch (Exception  e) {
			log.warn("Error parsing the date {} using the ISO_ZONED_DATE_TIME format", inputDate);
		}

		return convertedDate;
	}
	/**
	 * Formats the date input to String using the format given.
	 * 
	 * @param inputDate
	 *            The date that needs to be formatted.
	 * @param format
	 *            The given date-time format.
	 * @param locale
	 *            The given locale.
     */
	public static String format(Date inputDate, String format, Locale locale) {
		if(inputDate == null){
			return null;
		}

		try {
			return new SimpleDateFormat(format, locale)
					.format(inputDate);
		} catch(Exception ex) {
			return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US)
					.format(inputDate)
					.replace(",",""); // FIX JDK8 -> JDK9
		}
	}
	
	/**
	 * Validate whether the date input is valid
	 * @param day
	 * @param month
	 * @param year
	 * @return
	 */
	public static boolean checkDate(int day, int month, int year) {
		try {
			LocalDate.of(year, month, day);
		} catch (DateTimeException e) {
			return false;
		}
		return true;
	}
}
