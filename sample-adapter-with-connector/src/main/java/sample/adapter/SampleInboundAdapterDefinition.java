package sample.adapter;

import java.util.ArrayList;
import java.util.List;

import com.esri.ges.adapter.AdapterDefinitionBase;
import com.esri.ges.adapter.AdapterType;
import com.esri.ges.connector.Connector;
import com.esri.ges.connector.Connector.ConnectorType;
import com.esri.ges.connector.ConnectorProperty;
import com.esri.ges.connector.ConnectorProperty.Source;
import com.esri.ges.core.Uri;

public class SampleInboundAdapterDefinition extends AdapterDefinitionBase
{
	private ArrayList<Connector> connectors = new ArrayList<Connector>();

	public SampleInboundAdapterDefinition()
	{
		super(AdapterType.INBOUND);

		Uri adapterUri = new Uri();
		adapterUri.setDomain(getDomain());
		adapterUri.setName(getName());
		adapterUri.setVersion(getVersion());

		// When defining a connector, you have to specify which Transport you Adapter is going to be paired with.  In this case,
		// the adapter is going to be paired with the TCP transport that comes with GeoEvent Processor.
		Uri transportUri = new Uri();
		transportUri.setDomain("com.esri.ges.transport.inbound");
		transportUri.setName("TCP");
		transportUri.setVersion(getVersion());

		Connector newConnector = new Connector("my-connector", "custom-input", ConnectorType.inbound, adapterUri, transportUri, "Sample Connector", "This connector was installed by one of the code samples in the SDK");
		// Specifying properties in the connector is optional.  It allows you to override the label and description for a property when viewed by the user.
		// Here, we are changing the default value of the Transport's "port" to 6010.
		ConnectorProperty portProperty = new ConnectorProperty( Source.transport, "port", "6010", "Communication Port (do not change)");
		// You can also control the visibility of the property by putting it in the "shown" section, the "Advanced" section, or the "Hidden" section.
		newConnector.addShownProperty(portProperty);

		ConnectorProperty handshakeProperty = new ConnectorProperty( Source.transport, "handshake", "hello", "Handshake text");
		// By putting a property in the "Hidden" section, it cannot be modified by the user, so the default value is always used.
		newConnector.addHiddenProperty(handshakeProperty);

		connectors.add(newConnector);
	}

	@Override
	public List<Connector> getConnectors()
	{
		return connectors;
	}

	@Override
	public String getName()
	{
		return "SampleAdapterWithConnector";
	}

	@Override
	public String getLabel()
	{
		return "Sample Adapter with Connector";
	}

	@Override
	public String getDomain()
	{
		return "sample.adapter";
	}

	@Override
	public String getDescription()
	{
		return "This is a sample adapter that demonstrates how to make the Adapter auto-install a connector so that users immediately see an entry in the 'Add Input' page.";
	}

	@Override
	public String getContactInfo()
	{
		return "yourname@yourcompany.com";
	}

	@Override
	public String getVersion()
	{
		return "10.2.1";
	}
}