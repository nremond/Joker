package cl.own.usi.tests.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;

import cl.own.usi.thrift.WorkerRPC.Client;


public class WorkerFacadeTester {

	@Test
	public void test() {

		TTransport transport;
		try {
			transport = new TSocket("localhost", 7911);
			TProtocol protocol = new TBinaryProtocol(transport);
			Client client = new Client(protocol);
			transport.open();
			String userId = client.loginUser("email", "password");
			System.out.println("UserId from logged user : " + userId);
			transport.close();
		} catch (TTransportException e) {
			e.printStackTrace();
		} catch (TException e) {
			e.printStackTrace();
		}

	}
}
