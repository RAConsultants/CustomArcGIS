package sample.adapter;

import java.nio.ByteBuffer;

import com.esri.ges.adapter.AdapterDefinition;
import com.esri.ges.adapter.InboundAdapterBase;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;

public class SampleInboundAdapter extends InboundAdapterBase
{
	public SampleInboundAdapter(AdapterDefinition definition) throws ComponentException
	{
		super(definition);
	}

	@Override
	public GeoEvent adapt(ByteBuffer buffer, String channelId)
	{
		// Normally you would parse the data here and return a new GeoEvent.
		buffer.position(buffer.limit());
		return null;

	}
}