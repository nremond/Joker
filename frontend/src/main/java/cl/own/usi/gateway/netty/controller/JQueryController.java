package cl.own.usi.gateway.netty.controller;

import static cl.own.usi.gateway.netty.ResponseHelper.writeResponse;
import static cl.own.usi.gateway.netty.ResponseHelper.writeStringToReponse;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_IMPLEMENTED;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Controller that send the JQuery lib
 *
 * @author nicolas
 */
@Component
public class JQueryController extends AbstractController {

	public static final String URI_PLAY = "/play/";

	@Value(value = "classpath:js/jquery.min.js")
	private Resource jqueryJs;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();

		if (request.getMethod() == HttpMethod.GET) {

			BufferedReader in = null;
			StringBuffer buff = new StringBuffer();
			try {
				final InputStream inputStream = jqueryJs.getInputStream();

				in = new BufferedReader(new InputStreamReader(inputStream));

				String str;
				while ((str = in.readLine()) != null) {
					buff.append(str);
					buff.append("\n");
				}

			} catch (IOException exp) {
				getLogger().error("The html template couldn't be found at {}",
						jqueryJs);
			} finally {
				if (in != null) {
					in.close();
				}
			}

			writeStringToReponse(buff.toString(), e, CREATED);
		} else {
			writeResponse(e, NOT_IMPLEMENTED);
			getLogger().info("Wrong method.");
		}

	}
}
