package sample.adapter;

import com.esri.ges.adapter.Adapter;
import com.esri.ges.adapter.AdapterServiceBase;
import com.esri.ges.core.component.ComponentException;

public class SampleInboundAdapterService extends AdapterServiceBase
{
	public SampleInboundAdapterService()
	{
	  definition = new SampleInboundAdapterDefinition();
	}
	
	@Override
	public Adapter createAdapter() throws ComponentException
	{
	  return new SampleInboundAdapter(definition);
	}
}