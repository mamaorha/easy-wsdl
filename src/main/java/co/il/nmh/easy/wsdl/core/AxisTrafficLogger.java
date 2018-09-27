package co.il.nmh.easy.wsdl.core;

import java.util.UUID;

import javax.xml.rpc.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.utils.StringUtils;

import co.il.nmh.easy.wsdl.listeners.IWSDLTrafficListener;

/**
 * @author Maor Hamami
 */

public class AxisTrafficLogger extends BasicHandler
{
	private static final long serialVersionUID = -8692831241473808734L;

	private IWSDLTrafficListener wsdlTrafficListener;

	public AxisTrafficLogger(IWSDLTrafficListener wsdlTrafficListener)
	{
		this.wsdlTrafficListener = wsdlTrafficListener;
	}

	@Override
	public void invoke(MessageContext messageContext) throws AxisFault
	{
		try
		{
			SOAPMessageContext soapMessageContext = messageContext;
			SOAPMessage soapMessage = soapMessageContext.getMessage();
			SOAPEnvelope envelope = soapMessage.getSOAPPart().getEnvelope();

			String endpoint = (String) soapMessageContext.getProperty("transport.url");
			String correlationID = null;
			String direction = "";

			if (null == messageContext.getResponseMessage())
			{
				correlationID = "easy-wsdl-" + UUID.randomUUID().toString();
				direction = "Sending";

				soapMessageContext.setProperty("INBOUND_ID", correlationID);
			}

			else
			{
				correlationID = (String) soapMessageContext.getProperty("INBOUND_ID");
				direction = "Receieved";
			}

			if (StringUtils.isEmpty(correlationID))
			{
				correlationID = "unknown";
			}

			if (null != wsdlTrafficListener)
			{
				String log = String.format("%s SOAP message: endpoint [%s], id [%s], data [%s]", direction, endpoint, correlationID, envelope.toString());
				wsdlTrafficListener.traffic(log);
			}
		}

		catch (SOAPException e)
		{
			if (null != wsdlTrafficListener)
			{
				String log = String.format("AxisTrafficLogger failed with error %s", e.getMessage());
				wsdlTrafficListener.error(log, e);
			}
		}
	}
}
