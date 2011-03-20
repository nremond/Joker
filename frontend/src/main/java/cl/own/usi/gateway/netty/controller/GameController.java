package cl.own.usi.gateway.netty.controller;

import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.util.CharsetUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cl.own.usi.gateway.client.WorkerClient;
import cl.own.usi.model.Game;
import cl.own.usi.service.GameService;

/**
 * Controller that create the {@link Game}
 * 
 * @author bperroud
 * @author nicolas
 *
 */
@Component
public class GameController extends AbstractController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GameService gameService;
	
	@Autowired
	private WorkerClient workerClient;
	
	private String authentificationKey;
	
	@Value(value = "${frontend.authentificationkey:1234}")
	public void setAuthentificationKey(String authentificationKey) {
		this.authentificationKey = authentificationKey;
	}
	
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
	throws Exception {
		
		HttpRequest request = (HttpRequest) e.getMessage();
		
		if (request.getMethod() == HttpMethod.POST) {

			JSONObject object = (JSONObject) JSONValue.parse(request
					.getContent().toString(CharsetUtil.UTF_8));

			if (object == null) {
				logger.error("No content or bad content");
				writeResponse(e, BAD_REQUEST);
				return;
			}
			
			String authenticationKey = (String) object
				.get("authentication_key");
			
			if (!this.authentificationKey.equals(authenticationKey)) {
				logger.error("Bad authentificationKey");
				writeResponse(e, BAD_REQUEST);
				return;
			}
			
			JSONObject allParameters = (JSONObject) object
				.get("parameters");
			
			if (allParameters == null) {
				logger.error("No content or bad content for allParameters");
				writeResponse(e, BAD_REQUEST);
				return;
			}
			
			JSONArray jsonQuestions = (JSONArray) allParameters
					.get("questions");
			if (jsonQuestions == null) {
				logger.error("No content or bad content for questions");
				writeResponse(e, BAD_REQUEST);
				return;
			}
			
			JSONObject parameters = (JSONObject) allParameters
					.get("parameters");
			if (parameters == null) {
				logger.error("No content or bad content for parameters");
				writeResponse(e, BAD_REQUEST);
				return;
			}
			
			List<Map<String, Map<String, Boolean>>> questions = new ArrayList<Map<String, Map<String, Boolean>>>();

			for (Object o : jsonQuestions) {
				Map<String, Map<String, Boolean>> question1 = new HashMap<String, Map<String, Boolean>>();
				Map<String, Boolean> answer1 = new LinkedHashMap<String, Boolean>();
				JSONObject jsonObject = (JSONObject) o;
				if (jsonObject == null) {
					logger.error("No content or bad content on question");
					writeResponse(e, BAD_REQUEST);
					return;
				}
				Long goodChoiceL = (Long) jsonObject.get("goodchoice");
				
				int goodChoice;
				if (goodChoiceL == null || goodChoiceL <= 0 || goodChoiceL > 4) {
					logger.error("No content or bad content for good choice");
					writeResponse(e, BAD_REQUEST);
					return;
				} else {
						goodChoice = goodChoiceL.intValue();
				}
				
				JSONArray choices = (JSONArray) jsonObject
						.get("choices");
				if (choices == null) {
					logger.error("No content or bad content for choices");
					writeResponse(e, BAD_REQUEST);
					return;
				}
				
				int i = 1;
				for (Object o2 : choices) {
					String choice = (String) o2;
					answer1.put(choice, i == goodChoice ? Boolean.TRUE
							: Boolean.FALSE);
					i++;
				}
				if (i != 5) {
					logger.error("Not the good quantity of choices");
					writeResponse(e, BAD_REQUEST);
					return;
				}
				
				question1
						.put((String) jsonObject.get("label"), answer1);
				questions.add(question1);
			}
			
			Long usersLimitL = (Long) parameters.get("nbusersthreshold");
			int usersLimit;
			if (usersLimitL == null) {
				logger.error("No content or bad content for attribute nbusersthresholdt");
				writeResponse(e, BAD_REQUEST);
				return;
			} else {
				usersLimit = usersLimitL.intValue();
			}
			
			Long questionTimeLimitL = (Long) parameters.get("questiontimeframe");
			int questionTimeLimit;
			if (questionTimeLimitL == null) {
				logger.error("No content or bad content for attribute questiontimeframe");
				writeResponse(e, BAD_REQUEST);
				return;
			} else {
				questionTimeLimit = questionTimeLimitL.intValue();
			}
			Long pollingTimeLimitL = (Long) parameters.get("logintimeout");
			int pollingTimeLimit;
			if (pollingTimeLimitL == null) {
				logger.error("No content or bad content for attribute logintimeout");
				writeResponse(e, BAD_REQUEST);
				return;
			} else {
				pollingTimeLimit = pollingTimeLimitL.intValue();
			}
			Long synchroTimeLimitL = (Long) parameters.get("synchrotime");
			int synchroTimeLimit;
			if (synchroTimeLimitL == null) {
				logger.error("No content or bad content for attribute synchrotime");
				writeResponse(e, BAD_REQUEST);
				return;
			} else {
				synchroTimeLimit = synchroTimeLimitL.intValue();
			}
			Long numberOfQuestionL = (Long) parameters.get("nbquestions");
			int numberOfQuestion;
			if (numberOfQuestionL == null || numberOfQuestionL <= 0L || numberOfQuestionL > 20L) {
				logger.error("No content or bad content for attribute numberOfQuestion");
				writeResponse(e, BAD_REQUEST);
				return;
			} else {
				numberOfQuestion = numberOfQuestionL.intValue();
			}
			
			gameService.insertGame(usersLimit, questionTimeLimit, pollingTimeLimit, 
					synchroTimeLimit, numberOfQuestion, questions);

			Boolean flushusertable = (Boolean) parameters.get("flushusertable");
			if (flushusertable != null && flushusertable) {
				workerClient.flushUsers();
			}

			writeResponse(e, CREATED);

		} else {
			writeResponse(e, NOT_IMPLEMENTED);
		}
		
	}
	
}
