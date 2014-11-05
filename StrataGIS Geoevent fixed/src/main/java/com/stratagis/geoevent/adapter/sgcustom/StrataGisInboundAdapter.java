package com.stratagis.geoevent.adapter.sgcustom;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.adapter.AdapterDefinition;
import com.esri.ges.adapter.InboundAdapterBase;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.spatial.Spatial;

public class StrataGisInboundAdapter extends InboundAdapterBase {
	private static final Log LOG = LogFactory.getLog(StrataGisInboundAdapter.class);

	StringBuilder nameBuffer = new StringBuilder();

	public StrataGisInboundAdapter(AdapterDefinition definition) throws ComponentException {
		super(definition);
	}

	private class GeoEventProducer implements Runnable {
		private String channelId;
		private List<byte[]> messages;

		public String deviceName;
		public String inputstr;

		public GeoEventProducer(String channelId, List<byte[]> messages, String deviceName, String input) {
			this.channelId = channelId;
			this.messages = messages;
			this.deviceName = deviceName;
			this.inputstr = input;
		}

		@Override
		public void run() {
			while (!messages.isEmpty()) {
				String[] data = new String(messages.remove(0)).split(",");
				if (data.length > 0) {
					int eventNumber = 0;
					try {
						eventNumber = inputEventCode(inputstr);
					} catch (Exception e) {
						LOG.error("Exception while getting event number " + e.getMessage());
					}
					String gedName = "SGCustom";
					// if (eventNumber == 31)
					// gedName = "SGCustomA";
					int lineNumber = 58;
					try {
						StrataGisMessageTranslator translator = new StrataGisGPRMCMessageTranslator(spatial);
						translator.validate(data);
						GeoEvent geoEvent = geoEventCreator.create(((AdapterDefinition) definition)
								.getGeoEventDefinition(gedName).getGuid());
						geoEvent.setField(0, channelId);
						geoEvent.setField(8, deviceName);
						geoEvent.setField(9, eventNumber);
						geoEvent.setField(34, inputstr);

						int asterix = inputstr.indexOf('*');
						lineNumber = 73;
						if (eventNumber == 30) {
							try {
								ArrayList<String> obdInfo = new ArrayList<String>();
								obdInfo = getODBInfo(inputstr);
								if (obdInfo.size() == 1)
									// GPS Odometer
									geoEvent.setField(10, Double.parseDouble(obdInfo.get(0)) * 0.000621371);
								else if (obdInfo.size() >= 7) {
									// GPS Odometer
									geoEvent.setField(10, Double.parseDouble(obdInfo.get(0)) * 0.000621371);
									// VIN
									geoEvent.setField(11, (obdInfo.get(1)));
									// OBD Protocol
									geoEvent.setField(12, (obdInfo.get(2)));
									// FW Version
									geoEvent.setField(13, (obdInfo.get(3)));
									// RSSI (received signal strength indicator)
									geoEvent.setField(14, (obdInfo.get(4)));
									// OBDII Malfunction Indicator Light (MIL) Count
									geoEvent.setField(15, (obdInfo.get(5)));
									// OBDII Malfunction Indicator Light (MIL) Errors
									geoEvent.setField(16, (obdInfo.get(6)));
									// OBDII Trip Odometer Data
									geoEvent.setField(17, Double.parseDouble(obdInfo.get(7)) * 0.000621371);
								}
							} catch (Exception e) {
								LOG.error("Exception while Parsing OBD Data : eventNumber" + eventNumber + " asterix"
										+ asterix + " device:" + deviceName + " error:" + e.getMessage()
										+ " inputString:" + inputstr);
							}

						} else if (eventNumber == 31) {
							lineNumber = 101;
							ArrayList<String> accelInfo = new ArrayList<String>();
							try {
								String tempString = "";
								accelInfo = getAccelerationData(inputstr);
								lineNumber = 104;
								if (accelInfo != null && accelInfo.size() >= 9) {
									lineNumber = 106;
									// Speed in MPH
									if (accelInfo.get(0) != "00000000")
										geoEvent.setField(18, (int) (Integer.parseInt(accelInfo.get(0), 16) * 0.621371));
									else
										geoEvent.setField(18, (int) 0);
									lineNumber = 109;
									// RPMs
									geoEvent.setField(19, (int) (Integer.parseInt(accelInfo.get(1), 16) / 4));
									lineNumber = 112;
									// Coolant Temp in F
									geoEvent.setField(20, (int) (Integer.parseInt(accelInfo.get(2), 16) - 40) * (9 / 5)
											+ 32);
									lineNumber = 115;
									// GPS Validity
									geoEvent.setField(21, (int) (Integer.parseInt(accelInfo.get(3), 16)));
									lineNumber = 118;
									// Idle Time
									geoEvent.setField(22, (int) (Integer.parseInt(accelInfo.get(4), 16)));
									lineNumber = 121;
									// User Var
									geoEvent.setField(23, accelInfo.get(5));
									lineNumber = 124;
									// AccelerationX
									geoEvent.setField(24, Double.parseDouble(accelInfo.get(6)));
									lineNumber = 127;
									// AccelerationY
									geoEvent.setField(25, Double.parseDouble(accelInfo.get(7)));
									lineNumber = 130;
									// AccelerationZ
									geoEvent.setField(26, Double.parseDouble(accelInfo.get(8)));
									lineNumber = 133;

									if (accelInfo.size() >= 15) {
										// ExcAccelTime
										tempString = accelInfo.get(9);
										if (tempString.length() >= 6) {
											tempString = tempString.substring(0, 2) + ":" + tempString.substring(2, 4)
													+ ":" + tempString.substring(4);
										}
										geoEvent.setField(27, tempString);
										lineNumber = 142;

										// ExcAccelLength
										tempString = accelInfo.get(10);
										if (tempString.contains(",")) {
											tempString = tempString.replace(",", "");
										}
										geoEvent.setField(28, Double.parseDouble(tempString));
										lineNumber = 150;
										// ExcAcceleration
										geoEvent.setField(29, Double.parseDouble(accelInfo.get(11)));
										lineNumber = 153;

										// ExcDecelTime
										tempString = accelInfo.get(12);
										if (tempString.length() >= 6) {
											tempString = tempString.substring(0, 2) + ":" + tempString.substring(2, 4)
													+ ":" + tempString.substring(4);
										}
										geoEvent.setField(30, tempString);
										lineNumber = 162;

										// ExcDecelLength
										tempString = accelInfo.get(13);
										if (tempString.contains(",")) {
											tempString = tempString.replace(",", "");
										}
										geoEvent.setField(31, Double.parseDouble(tempString));
										lineNumber = 170;
										// ExcDeceleration
										geoEvent.setField(32, Double.parseDouble(accelInfo.get(14)));
										lineNumber = 173;
									}
								}
							} catch (Exception e) {
								LOG.error("Exception while Parsing Acceleration Data : [line number:" + lineNumber
										+ "] [AccelInfo Size:" + accelInfo.size() + "] [eventNumber:" + eventNumber
										+ "] [device:" + deviceName + "] [error:" + e.getMessage() + "] [inputString:"
										+ inputstr + "]");
							}
						} else if (eventNumber == 32) {
							lineNumber = 188;
							ArrayList<String> accelInfo = new ArrayList<String>();
							try {
								String tempString = "";
								accelInfo = getAccelerationData(inputstr);
								lineNumber = 193;
								if (accelInfo != null && accelInfo.size() >= 3) {
									lineNumber = 195;
									// Digital input
									if (accelInfo.get(0) != "00000000")
										geoEvent.setField(33, (int) (Integer.parseInt(accelInfo.get(0), 16)));
									else
										geoEvent.setField(33, (int) 0);
									
									lineNumber = 202;
									// Idle Time
									if (accelInfo.get(1) != "00000000")
										geoEvent.setField(22, (int) (Integer.parseInt(accelInfo.get(1), 16)));
									else
										geoEvent.setField(22, (int) 0);
																		
									lineNumber = 209;
									// GPS Odometer
									if (accelInfo.get(2) != "00000000")
										geoEvent.setField(10, (int) (Integer.parseInt(accelInfo.get(2), 16) * 0.000621371));
									else
										geoEvent.setField(10, (int) 0);
									
									
								}
							} catch (Exception e) {
								LOG.error("Exception while Parsing Acceleration Data : [line number:" + lineNumber
										+ "] [AccelInfo Size:" + accelInfo.size() + "] [eventNumber:" + eventNumber
										+ "] [device:" + deviceName + "] [error:" + e.getMessage() + "] [inputString:"
										+ inputstr + "]");
							}
						}
						lineNumber = 225;
						translator.translate(geoEvent, data);
						lineNumber = 227;
						geoEventListener.receive(geoEvent);
					} catch (Exception e) {
						LOG.error("Exception while translating a NMEA message :  [eventNumber:" + eventNumber
								+ "] |  [device:" + deviceName + "] |  [line:" + lineNumber + "] | [NMEA Data Length:"
								+ data.length + "] | [ERROR:" + e.getMessage() + "] | [Input String:" + inputstr + "]");
					}

				}
			}
		}
	}

	@Override
	public GeoEvent adapt(ByteBuffer buffer, String channelId) {
		// We don't need to implement anything in here because this method will
		// never get called. It would normally be called
		// by the base class's receive() method. However, we are overriding that
		// method, and our new implementation does not call
		// the adapter's adapt() method.
		return null;
	}

	@Override
	public void receive(ByteBuffer buffer, String channelId) {
		ByteBuffer buffer2 = buffer.duplicate();
		ByteBuffer buffer3 = buffer.duplicate();
		new Thread(new GeoEventProducer(channelId, index(buffer), deviceNameFind(buffer2), inputToString(buffer3)))
				.start();
	}

	private static List<byte[]> index(ByteBuffer in) {

		List<byte[]> messages = new ArrayList<byte[]>();
		for (int i = -1; in.hasRemaining();) {
			byte b = in.get();
			if (b == ((byte) '$')) // bom
			{
				i = in.position();
				in.mark();
			} else if (b == ((byte) '\r') || b == ((byte) '\n')) // eom
			{
				if (i != -1) {
					byte[] message = new byte[in.position() - 1 - i];
					System.arraycopy(in.array(), i, message, 0, message.length);
					messages.add(message);
				}
				i = -1;
				in.mark();
			} else if (messages.isEmpty() && i == -1)
				in.mark();
		}
		return messages;
	}

	private static String deviceNameFind(ByteBuffer in) {
		try {
			// int first = -1;
			// int second = -1;
			String input = "";
			while (in.hasRemaining()) {
				char b = (char) in.get();
				input += b;
				in.mark();
			}
			int i1 = input.indexOf("#");
			int i2 = input.indexOf("#", i1 + 1);

			if (i1 >= 0 && i2 >= 0 && i1 != i2)
				return input.substring(i1 + 1, i2);
			else
				return "NotFound" + " > " + input;
		} catch (Exception e) {
			LOG.error("Exception while finding device name : " + e.toString());
			return "NotFound Error";
		}
	}

	private static String inputToString(ByteBuffer in) {
		try {
			// int first = -1;
			// int second = -1;
			String input = "";
			while (in.hasRemaining()) {
				char b = (char) in.get();
				int asc = b;

				if (asc >= 32 && asc <= 126)
					input += b;
				in.mark();
			}
			return input;
		} catch (Exception e) {
			LOG.error("Exception while congerting input to string : " + e.toString());
			return "NotFound Error";
		}
	}

	private static int inputEventCode(String input) {
		try {
			int eventNumber = 0;
			// String[] data = input.trim().split("[ ]+");
			int i1 = input.indexOf("#");
			int i2 = input.indexOf("#", i1 + 1);
			int i3 = input.indexOf("#", i2 + 1);
			int i4 = input.indexOf(" ", i2 + 1);
			String middle = input.substring(i2 + 6, i2 + 18);
			eventNumber = Integer.parseInt(middle.trim());
			return eventNumber;
		} catch (Exception e) {
			LOG.error("Exception while finding event number : " + e.toString());
			return 0;
		}
	}

	private static ArrayList<String> getODBInfo(String input) {
		int i1 = input.indexOf('*');
		try {
			ArrayList<String> obdInfo = new ArrayList<String>();

			if (i1 < 0)
				LOG.error("Exception while parsing OBD Data : Cant find *");
			int i2 = input.indexOf(' ', i1);
			if (i1 > 0 && (i1 + 3) < input.length()) {
				String middle = input.substring(i1 + 3);
				if (middle.trim().length() > 0) {
					String[] data = middle.trim().split("[ ]+");

					if (data.length == 1) {// 4100
						// GPS Odometer
						obdInfo.add(data[0]);
					} else if (data.length >= 6) {
						if (data[0].length() < 10) {
							// GPS Odometer
							obdInfo.add(data[0]);
							if (data[1].length() > 10) {
								// VIN
								obdInfo.add(data[1]);
								// OBD Protocol
								obdInfo.add(data[2]);
								// FW Version
								obdInfo.add(data[3]);
								// RSSI (received signal strength indicator)
								obdInfo.add(data[4]);
								// OBDII Malfunction Indicator Light (MIL) Count
								obdInfo.add(data[5]);
								if (data.length >= 8) {
									// OBDII Malfunction Indicator Light (MIL) Errors
									obdInfo.add(data[6]);
									// OBDII Trip Odometer Data
									obdInfo.add(data[7]);
								} else {
									// OBDII Malfunction Indicator Light (MIL) Errors
									obdInfo.add("");
									// OBDII Trip Odometer Data
									obdInfo.add(data[6]);
								}
							}
						} else if (data[0].length() > 10) {
							// GPS Odometer
							obdInfo.add("0");
							// VIN
							obdInfo.add(data[0]);
							// OBD Protocol
							obdInfo.add(data[1]);
							// FW Version
							obdInfo.add(data[2]);
							// RSSI (received signal strength indicator)
							obdInfo.add(data[3]);
							// OBDII Malfunction Indicator Light (MIL) Count
							obdInfo.add(data[4]);

							if (data.length >= 7) {
								// OBDII Malfunction Indicator Light (MIL) Errors
								obdInfo.add(data[5]);
								// OBDII Trip Odometer Data
								obdInfo.add(data[6]);
							} else {
								// OBDII Malfunction Indicator Light (MIL) Errors
								obdInfo.add("");
								// OBDII Trip Odometer Data
								obdInfo.add(data[5]);
							}
						}
					}
				}

			}
			return obdInfo;
		} catch (Exception e) {
			LOG.error("Exception while parsing OBD Data : i1 " + i1 + " " + e.toString());
			return new ArrayList<String>();
		}
	}

	private static ArrayList<String> getAccelerationData(String input) {
		try {
			ArrayList<String> accelInfo = new ArrayList<String>();
			int startIndex = input.indexOf("#");
			// startIndex = input.indexOf("#", startIndex + 1);
			// startIndex = input.indexOf("#", startIndex + 1);
			// startIndex = input.indexOf("#", startIndex + 1);
			int laterIndex = startIndex;
			while (laterIndex >= 0) {
				laterIndex = input.indexOf("#", startIndex + 1);
				if (laterIndex > startIndex)
					startIndex = laterIndex;
			}
			int starIndex = input.indexOf("*", startIndex);
			if (input.substring(startIndex + 1).trim().length() > 0) {
				String[] data = input.substring(startIndex + 1).trim().split("[ ]+");
				// if (laterIndex > 0 && laterIndex > startIndex)
				// data = input.substring(laterIndex + 1).trim().split("[ ]+");

				if (data.length > 10) {
					// Speed
					accelInfo.add(data[0]);
					// RPMs
					accelInfo.add(data[1]);
					// Coolant Temp
					accelInfo.add(data[2]);
					// GPS Validity
					accelInfo.add(data[3]);
					// Idle Time
					accelInfo.add(data[4]);
					// User Var
					accelInfo.add(data[5]);
					// Accel in X
					accelInfo.add(data[6]);
					// Accel in Y
					accelInfo.add(data[7]);
					// Accel in Z
					accelInfo.add(data[8]);

					if (starIndex > 0 && starIndex + 3 < input.length()) {
						String[] excess = input.substring(starIndex + 3).trim().split(",");
						if (excess.length >= 5) {
							// Excess Acceleration Time
							accelInfo.add(excess[0]);
							// Excess Acceleration Length
							accelInfo.add(excess[1]);
							// Excess Acceleration Amount
							accelInfo.add(excess[2].trim().split("[ ]+")[0]);
							// Excess Deceleration Time
							accelInfo.add(excess[2].trim().split("[ ]+")[1]);
							// Excess Deceleration Length
							accelInfo.add(excess[3]);
							// Excess Deceleration Amount
							accelInfo.add(excess[4]);
						}
						else
						{
							LOG.error("Exception while finding acceleration data : "  + " [input:" + input  + " [Star input:" + input.substring(starIndex + 3).trim() + "]");
						}
					}
				} else if (data.length > 3){
					// Speed
					accelInfo.add(data[0]);
					// RPMs
					accelInfo.add(data[1]);
					// Coolant Temp
					accelInfo.add(data[2]);
				}
			}
			return accelInfo;
		} catch (Exception e) {
			LOG.error("Exception while finding acceleration data : " + e.toString() + " [input:" + input + "]");
			return new ArrayList<String>();
		}
	}

	@Override
	public void shutdown() {
		super.shutdown();
	}

	@Override
	public void setSpatial(Spatial spatial) {
		super.setSpatial(spatial);
	}
}
