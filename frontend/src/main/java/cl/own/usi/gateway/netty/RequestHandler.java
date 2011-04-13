package cl.own.usi.gateway.netty;

import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static cl.own.usi.gateway.netty.controller.AbstractController.URI_API;
import static cl.own.usi.gateway.netty.controller.AbstractController.URI_API_LENGTH;
import static cl.own.usi.gateway.netty.controller.AnswerController.URI_ANSWER;
import static cl.own.usi.gateway.netty.controller.QuestionController.URI_QUESTION;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cl.own.usi.gateway.netty.controller.AddWorkerNodeController;
import cl.own.usi.gateway.netty.controller.AnswerController;
import cl.own.usi.gateway.netty.controller.AuditController;
import cl.own.usi.gateway.netty.controller.GameController;
import cl.own.usi.gateway.netty.controller.JQueryController;
import cl.own.usi.gateway.netty.controller.LoginController;
import cl.own.usi.gateway.netty.controller.LogoutController;
import cl.own.usi.gateway.netty.controller.PlayController;
import cl.own.usi.gateway.netty.controller.QuestionController;
import cl.own.usi.gateway.netty.controller.RankingController;
import cl.own.usi.gateway.netty.controller.UserController;

/**
 * No inversion of control ... but it may be enough for use.
 *
 * @author bperroud
 * @author nicolas
 */
@Component
public class RequestHandler extends SimpleChannelUpstreamHandler {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {

		logger.error("Exception thrown", e.getCause());
		super.exceptionCaught(ctx, e);
	}

	private static final String URI_GAME = "/game";
	private static final String URI_LOGIN = "/login";
	private static final String URI_RANKING = "/ranking";
	private static final String URI_USER = "/user";
	private static final String URI_LOGOUT = "/logout";
	private static final String URI_ADD_WORKER_NODE = "/join";
	private static final String URI_AUDIT = "/audit";
	private static final String URI_PLAY = "/play";
	private static final String URI_SCORE = "/score";
	private static final String URI_JQUERY = "/lib/jquery.min.js";

	@Autowired
	private QuestionController questionController;

	@Autowired
	private AnswerController answerController;

	@Autowired
	private RankingController rankingController;

	@Autowired
	private GameController gameController;

	@Autowired
	private UserController userController;

	@Autowired
	private LoginController loginController;

	// TODO
	@Autowired
	private LogoutController logoutController;

	@Autowired
	private AuditController auditController;

	@Autowired
	private PlayController playController;

	@Autowired
	private JQueryController jQueryController;

	@Autowired
	private AddWorkerNodeController addWorkerNodeController;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {

		try {
			HttpRequest request = (HttpRequest) e.getMessage();

			String uri = request.getUri();

			if (uri.startsWith(URI_API)) {

				uri = uri.substring(URI_API_LENGTH);

				if (uri.startsWith(URI_QUESTION)) {
					questionController.messageReceived(ctx, e);
				} else if (uri.startsWith(URI_ANSWER)) {
					answerController.messageReceived(ctx, e);
				} else if (uri.startsWith(URI_RANKING)) {
					rankingController.messageReceived(ctx, e);
				} else if (uri.startsWith(URI_LOGIN)) {
					loginController.messageReceived(ctx, e);
				} else if (uri.startsWith(URI_USER)) {
					userController.messageReceived(ctx, e);
				} else if (uri.startsWith(URI_ADD_WORKER_NODE)) {
					addWorkerNodeController.messageReceived(ctx, e);
				} else if (uri.startsWith(URI_GAME)) {
					gameController.messageReceived(ctx, e);
				} else if (uri.startsWith(URI_AUDIT)) {
					auditController.messageReceived(ctx, e);
				} else if (uri.startsWith(URI_PLAY)) {
					playController.messageReceived(ctx, e);
				} else if (uri.startsWith(URI_SCORE)) {
					//TODO playController.messageReceived(ctx, e);
				} else {
					writeResponse(e, NOT_FOUND);
				}
			} else if (uri.startsWith(URI_JQUERY)) {
				jQueryController.messageReceived(ctx, e);
			} else {
				writeResponse(e, NOT_FOUND);
			}
		} catch (Exception ex) {
			logger.warn("Exception thrown", ex);
			writeResponse(e, INTERNAL_SERVER_ERROR);
		}
	}
}
