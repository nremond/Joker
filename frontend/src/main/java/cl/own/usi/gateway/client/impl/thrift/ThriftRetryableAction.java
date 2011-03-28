package cl.own.usi.gateway.client.impl.thrift;

import org.apache.thrift.TException;

import cl.own.usi.thrift.WorkerRPC.Client;

public interface ThriftRetryableAction<T> {

	T doAction(Client client) throws TException;
}
