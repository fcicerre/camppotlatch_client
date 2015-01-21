package org.coursera.camppotlatch.client.commons;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Fabio on 07/11/2014.
 */
public class DateUtils {
    private SimpleDateFormat dateFormater = null;
    private SimpleDateFormat yyyymmddFormater = null;

    public DateUtils() {
        dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        dateFormater.setTimeZone(TimeZone.getTimeZone("UTC"));

        yyyymmddFormater = new SimpleDateFormat("yyyy/MM/dd");
    }

    public String convertToISO8601DateFormat(Date date) {
        // Example: 2014-10-27T09:44:55Z
        return dateFormater.format(date);
    }

    public Date parseISO8601DateFormat(String text) throws ParseException {
        return dateFormater.parse(text);
    }

    public Date convertToISO8601Date(Date date) throws ParseException {
        return parseISO8601DateFormat(convertToISO8601DateFormat(date));
    }

    public String printYYYYMMDDFormat(Date date) {
        return yyyymmddFormater.format(date);
    }
}
