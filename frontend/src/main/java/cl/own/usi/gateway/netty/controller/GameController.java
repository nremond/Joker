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
			
			JSONArray jsonQuestions = (JSONArray) object
					.get("questions");
			JSONObject parameters = (JSONObject) object
					.get("parameters");

			List<Map<String, Map<String, Boolean>>> questions = new ArrayList<Map<String, Map<String, Boolean>>>();

			for (Object o : jsonQuestions) {
				Map<String, Map<String, Boolean>> question1 = new HashMap<String, Map<String, Boolean>>();
				Map<String, Boolean> answer1 = new LinkedHashMap<String, Boolean>();
				JSONObject jsonObject = (JSONObject) o;
				int goodChoice = ((Long) jsonObject.get("goodchoice"))
						.intValue();
				JSONArray choices = (JSONArray) jsonObject
						.get("choices");
				int i = 1;
				for (Object o2 : choices) {
					String choice = (String) o2;
					answer1.put(choice, i == goodChoice ? Boolean.TRUE
							: Boolean.FALSE);
					i++;
				}
				question1
						.put((String) jsonObject.get("label"), answer1);
				questions.add(question1);
			}

			gameService.insertGame(((Long) parameters
					.get("nbusersthreshold")).intValue(),
					((Long) parameters.get("questiontimeframe"))
							.intValue(), ((Long) parameters
							.get("longpollingduration")).intValue(),
					questions);

			if ((Boolean) parameters.get("flushusertable")) {
				workerClient.flushUsers();
			}

			writeResponse(e, CREATED);

		} else {
			writeResponse(e, NOT_IMPLEMENTED);
		}
		
	}
	
}
