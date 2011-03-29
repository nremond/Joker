package cl.own.usi.dao.impl.cassandra;

import org.springframework.stereotype.Component;

import me.prettyprint.cassandra.service.OperationType;
import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import me.prettyprint.hector.api.HConsistencyLevel;

@Component
public class JokerConsistencyPolicy implements ConsistencyLevelPolicy {

	@Override
	public HConsistencyLevel get(OperationType op) {
		return HConsistencyLevel.ONE;
	}

	@Override
	public HConsistencyLevel get(OperationType op, String cfName) {
		return HConsistencyLevel.ONE;
	}

}
