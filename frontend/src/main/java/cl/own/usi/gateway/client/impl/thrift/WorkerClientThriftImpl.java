package cl.own.usi.gateway.client.impl.thrift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.gateway.client.WorkerClient;
import cl.own.usi.model.Question;
import cl.own.usi.service.GameService;
import cl.own.usi.thrift.WorkerRPC.Client;

@Component
public class WorkerClientThriftImpl implements WorkerClient, InitializingBean {
	
	@Autowired
	GameService gameService;
	
	int port = 7911;
	String host = "localhost";
	
	private int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	private String getHost() {
		return host;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	@Override
	public UserAndScore validateUserAndInsertQuestionRequest(String userId,
			int questionNumber) {
		
		try {
			cl.own.usi.thrift.UserAndScore userAndScore = getClient().validateUserAndInsertQuestionRequest(userId, questionNumber);
			return map(userAndScore);
		} catch (TException e) {
			
			// TODO : select another node
			return null;
		}
	}

	@Override
	public UserAndScoreAndAnswer validateUserAndInsertQuestionResponseAndUpdateScore(
			String userId, int questionNumber, Integer answer) {
		
		try {
			answer = gameService.validateAnswer(questionNumber, answer);
			boolean answerCorrect = gameService.isAnswerCorrect(questionNumber, answer);
			Question question = gameService.getQuestion(questionNumber);
			cl.own.usi.thrift.UserAndScore userAndScore = getClient().validateUserAndInsertQuestionResponseAndUpdateScore(userId, questionNumber, question.getValue(), answer, answerCorrect);
			return map(userAndScore, answerCorrect);
		} catch (TException e) {

			// TODO : select another node
			return null;
		}
	}

	@Override
	public UserAndScore validateUserAndGetScore(String userId) {
		
		try {
			cl.own.usi.thrift.UserAndScore userAndScore = getClient().validateUserAndGetScore(userId);
			return map(userAndScore);
		} catch (TException e) {
			
			// TODO : select another node
			return null;
		}
	}

	@Override
	public String loginUser(String email, String password) {
		
		try {
			return getClient().loginUser(email, password);
		} catch (TException e) {
			
			// TODO : select another node
			return null;
		}
	}

	@Override
	public boolean insertUser(String email, String password, String firstname,
			String lastname) {
		
		try {
			return getClient().insertUser(email, password, firstname, lastname);
		} catch (TException e) {
			
			// TODO : select another node
			return false;
		}
	}

	@Override
	public void flushUsers() {
		try {
			getClient().flushUsers();
		} catch (TException e) {
			
			// TODO : select another node
		}
	}

	@Override
	public List<UserInfoAndScore> getTop100() {
		
		try {
			List<cl.own.usi.thrift.UserInfoAndScore> users = getClient().getTop100();
			
			List<UserInfoAndScore> retUsers = new ArrayList<UserInfoAndScore>(users.size());
			for (cl.own.usi.thrift.UserInfoAndScore user : users) {
				retUsers.add(map(user));
			}
			return retUsers;
		} catch (TException e) {
			
			// TODO : select another node
			return Collections.emptyList();
		}
	}

	@Override
	public List<UserInfoAndScore> get50Before(String userId) {
		
		try {
			List<cl.own.usi.thrift.UserInfoAndScore> users = getClient().get50Before(userId);
			
			List<UserInfoAndScore> retUsers = new ArrayList<UserInfoAndScore>(users.size());
			for (cl.own.usi.thrift.UserInfoAndScore user : users) {
				retUsers.add(map(user));
			}
			return retUsers;
		} catch (TException e) {
			
			// TODO : select another node
			return Collections.emptyList();
		}
	}

	@Override
	public List<UserInfoAndScore> get50After(String userId) {
		
		try {
			List<cl.own.usi.thrift.UserInfoAndScore> users = getClient().get50After(userId);
			
			List<UserInfoAndScore> retUsers = new ArrayList<UserInfoAndScore>(users.size());
			for (cl.own.usi.thrift.UserInfoAndScore user : users) {
				retUsers.add(map(user));
			}
			return retUsers;
		} catch (TException e) {
			
			// TODO : select another node
			return Collections.emptyList();
		}
		
	}
	
	private UserAndScore map(cl.own.usi.thrift.UserAndScore userAndScore) {
		if (userAndScore == null) {
			return null;
		} else {
			UserAndScore newUserAndScore = new UserAndScore();
			newUserAndScore.score = userAndScore.score;
			newUserAndScore.userId = userAndScore.userId;
			return newUserAndScore;
		}
	}
	
	private UserAndScoreAndAnswer map(cl.own.usi.thrift.UserAndScore userAndScore, boolean answer) {
		if (userAndScore == null) {
			return null;
		} else {
			UserAndScoreAndAnswer newUserAndScoreAndAnswer = new UserAndScoreAndAnswer();
			newUserAndScoreAndAnswer.score = userAndScore.score;
			newUserAndScoreAndAnswer.userId = userAndScore.userId;
			newUserAndScoreAndAnswer.answer = answer;
			return newUserAndScoreAndAnswer;
		}
	}

	private UserInfoAndScore map(cl.own.usi.thrift.UserInfoAndScore userInfoAndScore) {
		if (userInfoAndScore == null) {
			return null;
		} else {
			UserInfoAndScore newUserInfoAndScore = new UserInfoAndScore();
			newUserInfoAndScore.email = userInfoAndScore.email;
			newUserInfoAndScore.firstname = userInfoAndScore.firstname;
			newUserInfoAndScore.lastname = userInfoAndScore.lastname;
			newUserInfoAndScore.score = userInfoAndScore.score;
			newUserInfoAndScore.userId = userInfoAndScore.userId;
			return newUserInfoAndScore;
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	ThreadLocal<Client> threadLocalClient = new ThreadLocal<Client>();
	
	private Client getClient() {
		
		Client client = threadLocalClient.get();
		if (client == null) {
			TTransport transport;
			try {
				transport = new TSocket(getHost(), getPort());
				TProtocol protocol = new TBinaryProtocol(transport);
				client = new Client(protocol);
				transport.open();
				return client;
			} catch (TTransportException e) {
				e.printStackTrace();
			}
			threadLocalClient.set(client);	
		}
		return client;
		
	}
}
