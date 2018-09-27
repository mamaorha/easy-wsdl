package co.il.nmh.easy.wsdl.core;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.axis.utils.StringUtils;

import co.il.nmh.easy.wsdl.listeners.IWSDLTrafficListener;

/**
 * @author Maor Hamami
 */

public class SoapTrafficLogger implements SOAPHandler<SOAPMessageContext>
{
	private IWSDLTrafficListener wsdlTrafficListener;

	public SoapTrafficLogger(IWSDLTrafficListener wsdlTrafficListener)
	{
		this.wsdlTrafficListener = wsdlTrafficListener;
	}

	@Override
	public void close(MessageContext context)
	{
	}

	@Override
	public boolean handleFault(SOAPMessageContext context)
	{
		logMessage(context);
		return false;
	}

	@Override
	public boolean handleMessage(SOAPMessageContext context)
	{
		logMessage(context);
		return true;
	}

	private void logMessage(SOAPMessageContext context)
	{
		try
		{
			Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
			Object packet = null;
			String packetData = "";
			String endpoint = "";

			Field packetField = context.getClass().getSuperclass().getDeclaredField("packet");
			packetField.setAccessible(true);

			packet = packetField.get(context);

			endpoint = (String) packet.getClass().getMethod("getEndPointAddressString").invoke(packet);
			packetData = packet.toString();

			int start = packetData.indexOf("<?xml");

			if (start > -1)
			{
				packetData = packetData.substring(start);
			}

			String correlationID = null;
			String direction = "";

			if (outbound)
			{
				correlationID = "easy-wsdl-" + UUID.randomUUID().toString();
				direction = "Sending";

				context.put("INBOUND_ID", correlationID);
			}

			else
			{
				correlationID = (String) context.get("INBOUND_ID");
				direction = "Receieved";
			}

			if (StringUtils.isEmpty(correlationID))
			{
				correlationID = "unknown";
			}

			if (null != wsdlTrafficListener)
			{
				String log = String.format("%s SOAP message: endpoint [%s], id [%s], data [%s]", direction, endpoint, correlationID, packetData);
				wsdlTrafficListener.traffic(log);
			}
		}

		catch (Exception e)
		{
			if (null != wsdlTrafficListener)
			{
				String log = String.format("SoapTrafficLogger failed with error %s", e.getMessage());
				wsdlTrafficListener.error(log, e);
			}
		}
	}

	@Override
	public Set<QName> getHeaders()
	{
		return null;
	}

}
