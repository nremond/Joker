package cl.own.usi.gateway.netty;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.COOKIE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SET_COOKIE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.json.LoginRequest;
import cl.own.usi.json.UserRequest;
import cl.own.usi.model.Question;
import cl.own.usi.model.User;
import cl.own.usi.service.GameService;
import cl.own.usi.service.ScoreService;
import cl.own.usi.service.UserService;

@Component
public class RequestHandler extends SimpleChannelUpstreamHandler {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {

		logger.error("Exception thrown", e.getCause());

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
	protected static final String URI_LOGOUT = "/logout";

	protected static final String COOKIE_AUTH_NAME = "session_key";

	private final ObjectMapper jsonObjectMapper = new ObjectMapper();

	@Autowired
	private GameService gameService;

	@Autowired
	private UserService userService;

	@Autowired
	private ScoreService scoreService;

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
						logger.info("User not authorized");
					} else {
						try {
							int questionNumber = Integer.parseInt(URI
									.substring(URI_QUESTION_LENGTH));

							User user = userService.getUserFromUserId(userId);

							if (user == null
									|| !gameService.validateQuestionToRequest(questionNumber)) {
								writeResponse(e, BAD_REQUEST);
								logger.info("Invalid question number " + questionNumber);
							} else {

								userService.insertRequest(user, questionNumber);

								logger.debug("Get Question "
										+ questionNumber + " for user "
										+ userId);

								Question question = gameService
										.getQuestion(questionNumber);

								StringBuilder sb = new StringBuilder("{");
								sb.append("\"question\":\"")
										.append(question.getLabel())
										.append("\"");
								int i = 0;
								for (String answer : question.getChoices()) {
									i++;
									sb.append(",\"answer_").append(i)
											.append("\":\"").append(answer)
											.append("\"");
								}

								gameService.scheduleQuestionReply(
										new QuestionWorker(questionNumber, user
												.getScore(), e, sb.toString()));

							}

						} catch (NumberFormatException exception) {
							writeResponse(e, BAD_REQUEST);
							logger.warn("NumberFormatException", exception);
						}
					}
				} else {
					writeResponse(e, NOT_IMPLEMENTED);
					logger.info("Wrong method");
				}
			} else if (URI.startsWith(URI_ANSWER)) {
				String userId = getCookie(request, COOKIE_AUTH_NAME);

				if (userId == null) {
					writeResponse(e, UNAUTHORIZED);
					logger.info("User not authorized");
				} else {
					try {
						int questionNumber = Integer.parseInt(URI
								.substring(URI_ANSWER_LENGTH));

						User user = userService.getUserFromUserId(userId);

						if (user == null
								|| !gameService.validateQuestionToAnswer(questionNumber)) {
							writeResponse(e, BAD_REQUEST);
							logger.info("Invalid question number" + questionNumber);
						} else {

							logger.debug("Answer Question "
									+ questionNumber + " for user " + userId);

							gameService.userAnswer(questionNumber);
							
							JSONObject object = (JSONObject) JSONValue
									.parse(request.getContent().toString(
											CharsetUtil.UTF_8));

							Question question = gameService
									.getQuestion(questionNumber);

							boolean answerCorrect = userService.insertAnswer(
									user, questionNumber,
									((Long) object.get("answer")).intValue());

							int newScore = scoreService.updateScore(question,
									user, answerCorrect);

							StringBuilder sb = new StringBuilder(
									"{ \"are_u_ok\" : ");
							if (answerCorrect) {
								sb.append("true");
							} else {
								sb.append("false");
							}
							sb.append(", \"good_answer\" : \""
									+ question.getChoices().get(
											question.getCorrectChoice())
									+ "\", \"score\" : " + newScore + "}");

							writeStringBuilder(sb, e, CREATED);

						}
					} catch (NumberFormatException exception) {
						writeResponse(e, BAD_REQUEST);
						logger.warn("NumberFormatException", exception);
					}
				}
			} else if (URI.startsWith(URI_RANKING)) {

				String userId = getCookie(request, COOKIE_AUTH_NAME);

				if (userId == null) {
					writeResponse(e, UNAUTHORIZED);
					logger.info("User not authorized");
				} else {

					User user = userService.getUserFromUserId(userId);

					if (user != null) {

						StringBuilder sb = new StringBuilder("{");

						sb.append(" \"my_score\" : ").append(user.getScore())
								.append(", ");

						sb.append(" \"top_scores\" : { ");
						List<User> topUsers = scoreService.getTop100();
						appendUsersScores(topUsers, sb);
						sb.append(" }, ");

						sb.append(" \"before_me\" : { ");
						List<User> beforeScores = scoreService
								.get50Before(user);
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

					final LoginRequest loginRequest = jsonObjectMapper
							.readValue(
									request.getContent().toString(
											CharsetUtil.UTF_8),
									LoginRequest.class);

					String userId = userService.login(loginRequest.getMail(),
							loginRequest.getPassword());

					if (userId != null) {
						
						gameService.enterGame(userId);
						
						HttpResponse response = new DefaultHttpResponse(
								HTTP_1_0, CREATED);
						setCookie(response, COOKIE_AUTH_NAME, userId);
						ChannelFuture future = e.getChannel().write(response);
						future.addListener(ChannelFutureListener.CLOSE);
					} else {
						writeResponse(e, BAD_REQUEST);
						logger.warn("Wrong method");
					}

				} else {
					writeResponse(e, NOT_IMPLEMENTED);
					logger.warn("Not implemented");
				}
			} else if (URI.startsWith(URI_USER)) {

				if (request.getMethod() == HttpMethod.POST) {

					final UserRequest userRequest = jsonObjectMapper.readValue(
							request.getContent().toString(CharsetUtil.UTF_8),
							UserRequest.class);

					boolean inserted = userService.insertUser(
							userRequest.getMail(), userRequest.getPassword(),
							userRequest.getFirstName(),
							userRequest.getLastName());

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

	public class QuestionWorker implements Runnable {

		final int questionNumber;
		final int score;
		final MessageEvent e;
		final String questionFirstPart;

		public QuestionWorker(int questionNumber, int score, MessageEvent e,
				String questionFirstPart) {
			this.questionNumber = questionNumber;
			this.score = score;
			this.e = e;
			this.questionFirstPart = questionFirstPart;
		}

		public void run() {

			StringBuilder sb = new StringBuilder(questionFirstPart);

			try {
				if (gameService.waitOtherUsers(questionNumber)) {

					sb.append(",\"score\":").append(score);
					sb.append("}");

					writeStringBuilder(sb, e, OK);

				} else {

					// time to wait is elapsed, return 400.
					writeResponse(e, BAD_REQUEST);

					logger.warn("Fail to wait on other users for question " + questionNumber + ", maybe long polling timeout");
				}
			} catch (InterruptedException ie) {

				writeResponse(e, BAD_REQUEST);
				logger.warn("Interrupted", ie);
				
			}
		}
		
		public int getQuestionNumber() {
			return questionNumber;
		}
	}

	private void appendUsersScores(List<User> users, StringBuilder sb) {
		StringBuilder topScoresMail = new StringBuilder("\"mail\" : [ ");
		StringBuilder topScoresScores = new StringBuilder("\"scores\" : [ ");
		StringBuilder topScoresFirstName = new StringBuilder(
				"\"firstname\" : [ ");
		StringBuilder topScoresLastname = new StringBuilder("\"lastname\" : [ ");
		boolean first = true;
		for (User topUser : users) {
			if (!first) {
				topScoresMail.append(",");
			}
			topScoresMail.append("\"").append(topUser.getEmail()).append("\"");
			if (!first) {
				topScoresScores.append(",");
			}
			topScoresScores.append(topUser.getScore());
			if (!first) {
				topScoresFirstName.append(",");
			}
			topScoresFirstName.append("\"").append(topUser.getFirstname())
					.append("\"");
			if (!first) {
				topScoresLastname.append(",");
			}
			topScoresLastname.append("\"").append(topUser.getLastname())
					.append("\"");
			first = false;
		}
		sb.append(topScoresMail).append(" ] , ");
		sb.append(topScoresScores).append(" ] , ");
		sb.append(topScoresFirstName).append(" ] , ");
		sb.append(topScoresLastname).append(" ] ");
	}

	private void writeStringBuilder(StringBuilder sb, MessageEvent e,
			HttpResponseStatus status) {

		ChannelBuffer buf = ChannelBuffers.copiedBuffer(sb.toString(),
				CharsetUtil.UTF_8);

		HttpResponse response = new DefaultHttpResponse(HTTP_1_0, status);
		response.setContent(buf);

		ChannelFuture future = e.getChannel().write(response);
		future.addListener(ChannelFutureListener.CLOSE);

	}
}
