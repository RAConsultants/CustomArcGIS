package com.stratagis.geoevent.adapter.sgcustom;

import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.spatial.Spatial;

public class StrataGisGPRMCMessageTranslator extends StrataGisMessageTranslator {
	public StrataGisGPRMCMessageTranslator(Spatial spatial) {
		super(spatial);
	}

	@Override
	public void translate(GeoEvent geoEvent, String[] data)
			throws ValidationException {
		int i = 1;
		try {
			if (data[1].length() > 0 && data[2].length() > 0
					&& data[3].length() > 0 && data[4].length() > 0
					&& data[5].length() > 0 && data[6].length() > 0
					&& data[7].length() > 0 && data[8].length() > 0
					&& data[9].length() > 0) {
				geoEvent.setField(i++, toTime(data[1], data[9]));
				geoEvent.setField(
						i++,
						toPoint(data[3], data[5], "N".equals(data[4]),
								"E".equals(data[6])));
				geoEvent.setField(i++, data[2]);
				geoEvent.setField(i++, convertToDouble(data[7]) * 1.15078);
				geoEvent.setField(i++, convertToDouble(data[8]));
				geoEvent.setField(i++, convertToDouble(data[10]));
				geoEvent.setField(i++, data[11].split("\\*")[0]);
			}
		} catch (Exception e) {
			throw new ValidationException(" Error on Translate [i:" + i
					+ "] [ERROR:" + e.getMessage() + "]");
		}
	}

	@Override
	protected void validate(String[] data) throws ValidationException {
		if (data == null || data.length > 13 || data[1].length() == 0
				|| data[2].length() == 0 || data[3].length() == 0
				|| data[4].length() == 0 || data[5].length() == 0
				|| data[6].length() == 0 || data[7].length() == 0
				|| data[8].length() == 0 || data[9].length() == 0)
			throw new ValidationException("GPRMC message data is invalid."
					+ data.length);
	}
}
