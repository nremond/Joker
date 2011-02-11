package cl.own.usi.gateway.netty;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.model.Question;
import cl.own.usi.model.User;
import cl.own.usi.service.ExecutorUtil;
import cl.own.usi.service.GameService;
import cl.own.usi.service.ScoreService;
import cl.own.usi.service.UserService;

@Component
public class RequestHandler extends SimpleChannelUpstreamHandler {

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		
		e.getCause().printStackTrace();

		super.exceptionCaught(ctx, e);
	}

	protected static final String URI_API = "/api";
	protected static final int URI_API_LENGTH = URI_API.length();

	protected static final String URI_QUESTION = "/question/";
	protected static final int URI_QUESTION_LENGTH = URI_QUESTION.length();
	protected static final String URI_ANSWER = "/answer/";
	protected static final int URI_ANSWER_LENGTH = URI_ANSWER.length();

	protected static final String URI_GAME = "/game";
	protected static final String URI_LOGIN = "/login";
	protected static final String URI_RANKING = "/ranking";
	protected static final String URI_USER = "/user";
	protected static final String URI_LOGOUT= "/logout";
	
	protected static final String COOKIE_AUTH_NAME = "session_key";
	
	@Autowired
	GameService gameService;

	@Autowired
	UserService userService;
	
	@Autowired
	ScoreService scoreService;

	@Autowired
	ExecutorUtil executorUtil;
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {

		HttpRequest request = (HttpRequest) e.getMessage();

		String URI = request.getUri();

		if (URI.startsWith(URI_API)) {

			URI = URI.substring(URI_API_LENGTH);

			if (URI.startsWith(URI_QUESTION)) {
				
				if (request.getMethod() == HttpMethod.GET) {
					String userId = getCookie(request, COOKIE_AUTH_NAME);
					
					if (userId == null) {
						writeResponse(e, UNAUTHORIZED);
					} else {
						try {
							int questionNumber = Integer.parseInt(URI
									.substring(URI_QUESTION_LENGTH));
							
							if (gameService.getCurrentQuestion().getNumber() != questionNumber) {
								writeResponse(e, BAD_REQUEST);
							} else {
							
								User user = userService.getUserFromUserId(userId);
								
								if (user != null && userService.isQuestionAllowed(user, questionNumber)) {
									
									gameService.userEnter();
									
									
									System.out.println("Get Question " + questionNumber + " for user " + userId);
									
									Question question = gameService.getCurrentQuestion();
									
									StringBuilder sb = new StringBuilder("{");
									sb.append("\"question\":\"").append(question.getLabel()).append("\"");
									int i = 0;
									for (String answer : question.getChoices()) {
										i++;
										sb.append(",\"answer_").append(i).append("\":\"").append(answer).append("\"");
									}
									
									executorUtil.getExecutorService().execute(new QuestionWorker(user.getScore(), e, sb.toString()));
									
								} else {
									
									writeResponse(e, BAD_REQUEST);
									
								}
								
							}
							
						} catch (NumberFormatException exception) {
							writeResponse(e, BAD_REQUEST);
						}
					}
				} else {
					writeResponse(e, NOT_IMPLEMENTED);
				}
			} else if (URI.startsWith(URI_ANSWER)) {
				String userId = getCookie(request, COOKIE_AUTH_NAME);
				
				if (userId == null) {
					writeResponse(e, UNAUTHORIZED);
				} else {
					try {
						int questionNumber = Integer.parseInt(URI
								.substring(URI_ANSWER_LENGTH));

						if (gameService.getCurrentQuestion().getNumber() != questionNumber) {
							writeResponse(e, BAD_REQUEST);
						} else {
							
							User user = userService.getUserFromUserId(userId);
							
							if (user != null && userService.isQuestionAllowed(user, questionNumber)) {
							
								System.out.println("Answer Question " + questionNumber + " for user " + userId);
		
								JSONObject object = (JSONObject) JSONValue.parse(request
										.getContent().toString(CharsetUtil.UTF_8));
								
								Question question = gameService.getCurrentQuestion();
								long deltaTimeToAnswer = System.nanoTime() - gameService.getStartOfCurrentQuestion();
								
								boolean answerCorrect = userService.insertAnswer(user, ((Long)object.get("answer")).intValue());
								
								int newScore = scoreService.updateScore(user, deltaTimeToAnswer, answerCorrect);
								
								StringBuilder sb = new StringBuilder("{ \"are_u_ok\" : ");
								if (answerCorrect) {
									sb.append("true");
								} else {
									sb.append("false");
								}
								sb.append(", \"good_answer\" : \"" + question.getChoices().get(question.getCorrectChoice()) + "\", \"score\" : " + newScore + "}");
								
								writeStringBuilder(sb, e, CREATED);
							
							} else {

								writeResponse(e, BAD_REQUEST);
								
							}
						}
					} catch (NumberFormatException exception) {
						writeResponse(e, BAD_REQUEST);
					}
				}
			} else if (URI.startsWith(URI_RANKING)) {

				String userId = getCookie(request, COOKIE_AUTH_NAME);
				
				if (userId == null) {
					writeResponse(e, UNAUTHORIZED);
				} else {
					
					User user = userService.getUserFromUserId(userId);
					
					if (user != null) {
						
						StringBuilder sb = new StringBuilder("{");
						
						sb.append(" \"my_score\" : ").append(user.getScore()).append(", ");
						
						sb.append(" \"top_scores\" : { ");
						List<User> topUsers = scoreService.getTop100();
						appendUsersScores(topUsers, sb);
						sb.append(" }, ");
						
						sb.append(" \"before_me\" : { ");
						List<User> beforeScores = scoreService.get50Before(user);
						appendUsersScores(beforeScores, sb);
						sb.append(" }, ");
						
						sb.append(" \"after_me\" : { ");
						List<User> afterScores = scoreService.get50After(user);
						appendUsersScores(afterScores, sb);
						sb.append(" } ");
						
						sb.append(" } ");
						
						writeStringBuilder(sb, e, OK);
						
					} else {
						
						writeResponse(e, BAD_REQUEST);
						
					}
					
				}
				
			} else if (URI.startsWith(URI_LOGIN)) {

				if (request.getMethod() == HttpMethod.POST) {

					JSONObject object = (JSONObject) JSONValue.parse(request
							.getContent().toString(CharsetUtil.UTF_8));

					String userId = userService.login(
							(String) object.get("mail"),
							(String) object.get("password"));

					if (userId != null) {
						HttpResponse response = new DefaultHttpResponse(
								HTTP_1_0, CREATED);
						setCookie(response, COOKIE_AUTH_NAME, userId);
						ChannelFuture future = e.getChannel().write(response);
						future.addListener(ChannelFutureListener.CLOSE);
					} else {
						writeResponse(e, BAD_REQUEST);
					}

				} else {
					writeResponse(e, NOT_IMPLEMENTED);
				}
			} else if (URI.startsWith(URI_USER)) {

				if (request.getMethod() == HttpMethod.POST) {

					JSONObject object = (JSONObject) JSONValue.parse(request
							.getContent().toString(CharsetUtil.UTF_8));
					boolean inserted = userService.insertUser(
							(String) object.get("mail"),
							(String) object.get("password"),
							(String) object.get("firstname"),
							(String) object.get("lastname"));

					if (inserted) {
						writeResponse(e, CREATED);
					} else {
						writeResponse(e, BAD_REQUEST);
					}
				} else {
					writeResponse(e, NOT_IMPLEMENTED);
				}
			} else if (URI.startsWith(URI_GAME)) {

				if (request.getMethod() == HttpMethod.POST) {
					
					JSONObject object = (JSONObject) JSONValue.parse(request
							.getContent().toString(CharsetUtil.UTF_8));
					
					JSONArray jsonQuestions = (JSONArray)object.get("questions");
					JSONObject parameters = (JSONObject)object.get("parameters");
					
					List<Map<String, Map<String, Boolean>>> questions = new ArrayList<Map<String, Map<String, Boolean>>>();
					
					for (Object o : jsonQuestions) {
						Map<String, Map<String, Boolean>> question1 = new HashMap<String, Map<String, Boolean>>();
						Map<String, Boolean> answer1 = new LinkedHashMap<String, Boolean>();
						JSONObject jsonObject = (JSONObject)o;
						int goodChoice = ((Long)jsonObject.get("goodchoice")).intValue();
						JSONArray choices = (JSONArray)jsonObject.get("choices");
						int i = 1;
						for (Object o2 : choices) {
							String choice = (String)o2;
							answer1.put(choice, i == goodChoice ? Boolean.TRUE : Boolean.FALSE);
							i++;
						}
						question1.put((String)jsonObject.get("label"), answer1);
						questions.add(question1);
					}
					
					gameService.insertGame(((Long)parameters.get("nbusersthreshold")).intValue(), 
							((Long)parameters.get("questiontimeframe")).intValue(), 
							((Long)parameters.get("longpollingduration")).intValue(), questions);
					
					if ((Boolean)parameters.get("flushusertable")) {
						userService.flushUsers();
					}
					
					writeResponse(e, CREATED);
					
				} else {
					writeResponse(e, NOT_IMPLEMENTED);
				}
				
			} else {
				
				writeResponse(e, NOT_FOUND);
				
			}
			
		} else {

			writeResponse(e, NOT_FOUND);

		}

	}

	private void writeResponse(MessageEvent e, HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(HTTP_1_0, status);
		ChannelFuture future = e.getChannel().write(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}

	private void setCookie(HttpResponse response, String name, String value) {
		CookieEncoder cookieEncoder = new CookieEncoder(true);
		cookieEncoder.addCookie(name, value);
		response.addHeader(SET_COOKIE, cookieEncoder.encode());
	}

	private CookieDecoder cookieDecoder = new CookieDecoder();

	private String getCookie(HttpRequest request, String name) {

		String cookieString = request.getHeader(COOKIE);
		if (cookieString != null) {

			Set<Cookie> cookies = cookieDecoder.decode(cookieString);

			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	private class QuestionWorker implements Runnable {

		final int score;
		final MessageEvent e;
		final String questionFirstPart;
		
		public QuestionWorker(int score, MessageEvent e, String questionFirstPart) {
			this.score = score;
			this.e = e;
			this.questionFirstPart = questionFirstPart;
		}
		
		public void run() {
			
			StringBuilder sb = new StringBuilder(questionFirstPart);
			
			try {
				if (gameService.waitOtherUsers()) {
				
					sb.append(",\"score\":").append(score);
					sb.append("}");
					
					writeStringBuilder(sb, e, OK);
				
				} else {
					
					// time to wait is elapsed, return 400.
					writeResponse(e, BAD_REQUEST);
					
				}
			} catch (InterruptedException ie) {
				
				writeResponse(e, BAD_REQUEST);
				
			}
		}
	}
	
	private void appendUsersScores(List<User> users, StringBuilder sb) {
		StringBuilder topScoresMail = new StringBuilder("\"mail\" : [ ");
		StringBuilder topScoresScores = new StringBuilder("\"scores\" : [ ");
		StringBuilder topScoresFirstName = new StringBuilder("\"firstname\" : [ ");
		StringBuilder topScoresLastname = new StringBuilder("\"lastname\" : [ ");
		boolean first = true;
		for (User topUser : users) {
			if (!first) {topScoresMail.append(",");} 
			topScoresMail.append("\"").append(topUser.getEmail()).append("\"");
			if (!first) {topScoresScores.append(",");} 
			topScoresScores.append(topUser.getScore());
			if (!first) {topScoresFirstName.append(",");} 
			topScoresFirstName.append("\"").append(topUser.getFirstname()).append("\"");
			if (!first) {topScoresLastname.append(",");} 
			topScoresLastname.append("\"").append(topUser.getLastname()).append("\"");
			first = false;
		}
		sb.append(topScoresMail).append(" ] , ");
		sb.append(topScoresScores).append(" ] , ");
		sb.append(topScoresFirstName).append(" ] , ");
		sb.append(topScoresLastname).append(" ] ");
	}
	
	private void writeStringBuilder(StringBuilder sb, MessageEvent e, HttpResponseStatus status) {
		
		ChannelBuffer buf = ChannelBuffers.copiedBuffer(sb.toString(), CharsetUtil.UTF_8);
		
		HttpResponse response = new DefaultHttpResponse(HTTP_1_0, status);
		response.setContent(buf);
		
		ChannelFuture future = e.getChannel().write(response);
		future.addListener(ChannelFutureListener.CLOSE);
		
	}
}
