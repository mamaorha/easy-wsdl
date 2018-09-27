package co.il.nmh.easy.wsdl;

import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.axis.Handler;
import org.apache.axis.SimpleChain;
import org.apache.axis.SimpleTargetedChain;
import org.apache.axis.client.AxisClient;
import org.apache.axis.client.Service;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.transport.http.HTTPSender;
import org.apache.axis.transport.http.HTTPTransport;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;

import co.il.nmh.easy.wsdl.core.AxisTrafficLogger;
import co.il.nmh.easy.wsdl.core.SoapTrafficLogger;
import co.il.nmh.easy.wsdl.listeners.IWSDLTrafficListener;

/**
 * @author Maor Hamami
 */

public class EasyWSDL
{
	private static final String STDOUT = "<stdout>";

	public static <T> T createCXF(Class<T> wsEndpointClass, String wsdlLocation, String wsQNameURI, String wsQNameLP, String endpointURL)
	{
		return createCXF(wsEndpointClass, wsdlLocation, wsQNameURI, wsQNameLP, endpointURL, false, null);
	}

	@SuppressWarnings("rawtypes")
	public static <T> T createCXF(Class<T> wsEndpointClass, String wsdlLocation, String wsQNameURI, String wsQNameLP, String endpointURL, boolean logEnabled, IWSDLTrafficListener wsdlTrafficListener)
	{
		URL wsdlUrl = EasyWSDL.class.getResource(wsdlLocation);
		QName qName = new QName(wsQNameURI, wsQNameLP);

		javax.xml.ws.Service wsService = javax.xml.ws.Service.create(wsdlUrl, qName);

		Object port = wsService.getPort(wsEndpointClass);

		BindingProvider bindingProvider = (BindingProvider) port;
		bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);

		if (logEnabled)
		{
			try
			{
				Client cxfClient = ClientProxy.getClient(port);

				LoggingOutInterceptor xOutLogger = new LoggingOutInterceptor();
				xOutLogger.setOutputLocation(STDOUT);

				LoggingInInterceptor xInLogger = new LoggingInInterceptor();
				xInLogger.setOutputLocation(STDOUT);

				cxfClient.getInInterceptors().add(xInLogger);
				cxfClient.getOutInterceptors().add(xOutLogger);
			}

			catch (Exception e)
			{
				List<javax.xml.ws.handler.Handler> handlerList = bindingProvider.getBinding().getHandlerChain();
				handlerList.add(new SoapTrafficLogger(wsdlTrafficListener));

				bindingProvider.getBinding().setHandlerChain(handlerList);
			}
		}

		return wsEndpointClass.cast(port);
	}

	public static void injectAxisLogger(Service service, IWSDLTrafficListener wsdlTrafficListener)
	{
		SimpleChain reqHandler = new SimpleChain();
		reqHandler.addHandler(new AxisTrafficLogger(wsdlTrafficListener));

		SimpleChain respHandler = new SimpleChain();
		respHandler.addHandler(new AxisTrafficLogger(wsdlTrafficListener));

		Handler pivot = new HTTPSender();
		Handler transport = new SimpleTargetedChain(reqHandler, pivot, respHandler);

		SimpleProvider clientConfig = new SimpleProvider();
		clientConfig.deployTransport(HTTPTransport.DEFAULT_TRANSPORT_NAME, transport);

		service.setEngineConfiguration(clientConfig);
		service.setEngine(new AxisClient(clientConfig));
	}
}
