package com.stratagis.geoevent.adapter.sgcustom;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.spatial.Point;
import com.esri.ges.spatial.Spatial;

public abstract class StrataGisMessageTranslator {
	private Spatial spatial;

	public StrataGisMessageTranslator(Spatial spatial) {
		this.spatial = spatial;
	}

	protected abstract void translate(GeoEvent geoEvent, String[] data)
			throws FieldException, ValidationException;

	protected abstract void validate(String[] data) throws ValidationException;

	protected Date toTime(String time, String date) {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		if (time != null && time.length() >= 6) {
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, 2)));
			c.set(Calendar.MINUTE, Integer.parseInt(time.substring(2, 4)));
			c.set(Calendar.SECOND, Integer.parseInt(time.substring(4, 6)));
		}
		if (date != null && date.length() >= 6) {
			c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(0, 2)));
			c.set(Calendar.MONTH, Integer.parseInt(date.substring(2, 4)) - 1);
			c.set(Calendar.YEAR, 2000 + Integer.parseInt(date.substring(4, 6)));
		}
		return c.getTime();
	}

	protected Point toPoint(String latitude, String longitude, boolean isNorth,
			boolean isEast) {
		if (latitude != null && longitude != null && latitude.length() > 0 && longitude.length() > 0) {
			double lat_d = convertToDouble(latitude.substring(0, 2), 0d);
			double lat_m = convertToDouble(
					latitude.substring(2, latitude.length()), 0d);
			double lat = lat_d + (lat_m / 60.0);
			double lon_d = convertToDouble(longitude.substring(0, 3), 0d);
			double lon_m = convertToDouble(
					longitude.substring(3, longitude.length()), 0d);
			double lon = lon_d + (lon_m / 60.0);
			lat = isNorth ? lat : -lat;
			lon = isEast ? lon : -lon;
			return spatial.createPoint(lon, lat, 4326);
		} else
			return spatial.createPoint(0, 0, 4326);

	}

	public boolean isEmpty(String s) {
		return (s == null || s.length() == 0);
	}

	public Double convertToDouble(String s, Double defaultValue) {
		if (!isEmpty(s)) {
			try {
				return Double.parseDouble(s.replaceAll(",", "."));
			} catch (Exception e) {
				;
			}
		}
		return defaultValue;
	}

	public Double convertToDouble(Object value) {
		return (value != null) ? (value instanceof Double) ? (Double) value
				: convertToDouble(value.toString(), null) : null;
	}

	public Short convertToShort(Object value) {
		if (value != null) {
			if (value instanceof Short)
				return (Short) value;
			else {
				Double doubleValue = convertToDouble(value);
				if (doubleValue != null)
					return ((Long) Math.round(doubleValue)).shortValue();
			}
		}
		return null;
	}
}
