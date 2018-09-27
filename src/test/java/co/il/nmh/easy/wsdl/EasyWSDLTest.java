package co.il.nmh.easy.wsdl;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import org.junit.Test;

import com.examples.www.wsdl.HelloService_wsdl.Hello_BindingStub;
import com.examples.www.wsdl.HelloService_wsdl.Hello_ServiceLocator;
import com.student.ChangeStudentDetails;
import com.student.Student;

import co.il.nmh.easy.wsdl.listeners.IWSDLTrafficListener;

/**
 * @author Maor Hamami
 */

public class EasyWSDLTest
{
	@Test
	public void testAxis() throws MalformedURLException, RemoteException
	{
		Hello_ServiceLocator locator = new Hello_ServiceLocator();

		EasyWSDL.injectAxisLogger(locator, new IWSDLTrafficListener()
		{
			@Override
			public void traffic(String log)
			{
				System.out.println(log);
			}

			@Override
			public void error(String log, Exception e)
			{
				e.printStackTrace();
				System.out.println(log);
			}
		});

		try
		{
			Hello_BindingStub hello_BindingStub = new Hello_BindingStub(new URL("http://www.examples.com/SayHello/"), locator);
			hello_BindingStub.sayHello("maor");
		}
		catch (Exception e)
		{
			// expect this to fail since i didnt use real endpoint
		}
	}

	@Test
	public void testCXF()
	{
		ChangeStudentDetails changeStudentDetails = EasyWSDL.createCXF(ChangeStudentDetails.class, "/cxf/ChangeStudent.wsdl", "http://student.com/", "ChangeStudentDetailsImplService", "http://localhost:9090/ChangeStudentDetailsImplPort", true, new IWSDLTrafficListener()
		{
			@Override
			public void traffic(String log)
			{
				System.out.println(log);
			}

			@Override
			public void error(String log, Exception e)
			{
				e.printStackTrace();
				System.out.println(log);
			}
		});

		try
		{
			changeStudentDetails.changeName(new Student());
		}
		catch (Exception e)
		{
			// expect this to fail since i didnt use real endpoint
		}
	}
}
