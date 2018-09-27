package co.il.nmh.easy.wsdl.listeners;

/**
 * @author Maor Hamami
 */

public interface IWSDLTrafficListener
{
	void traffic(String log);

	void error(String log, Exception e);
}
