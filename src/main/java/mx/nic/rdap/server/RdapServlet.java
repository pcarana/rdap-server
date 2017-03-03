package mx.nic.rdap.server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.PriorityQueue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mx.nic.rdap.db.exception.InvalidValueException;
import mx.nic.rdap.db.exception.NotImplementedException;
import mx.nic.rdap.db.exception.ObjectNotFoundException;
import mx.nic.rdap.db.exception.RdapDataAccessException;
import mx.nic.rdap.server.AcceptHeaderFieldParser.Accept;
import mx.nic.rdap.server.exception.RequestHandleException;
import mx.nic.rdap.server.renderer.DefaultRenderer;

/**
 * Base class of all RDAP servlets.
 */
public abstract class RdapServlet extends HttpServlet {

	/** This is just a warning shutupper. */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		RdapResult result;
		try {
			result = doRdapGet(request);
		} catch (ObjectNotFoundException e) {
			response.sendError(404, e.getMessage());
			return;
		} catch (InvalidValueException e) {
			response.sendError(422, e.getMessage());
			return;
		} catch (NotImplementedException e) {
			response.sendError(501, e.getMessage());
			return;
		} catch (SQLException | IOException | RdapDataAccessException e) {
			response.sendError(500, e.getMessage());
			return;
		} catch (RequestHandleException e) {
			response.sendError(e.getHttpResponseStatusCode(), e.getMessage());
			return;
		}

		Renderer renderer = findRenderer(request);
		response.setCharacterEncoding("UTF-8");
		response.setContentType(renderer.getResponseContentType());
		renderer.render(result, response.getWriter());
	}

	/**
	 * Handles the `request` GET request and builds a response. Think of it as a
	 * {@link HttpServlet#doGet(HttpServletRequest, HttpServletResponse)},
	 * except you don't have to grab a database connection and the response will
	 * be built for you.
	 * 
	 * @param request
	 *            request to the servlet.
	 * @param connection
	 *            Already initialized connection to the database.
	 * @return response to the user.
	 * @throws RequestHandleException
	 *             Errors found handling `request`.
	 */
	protected abstract RdapResult doRdapGet(HttpServletRequest request)
			throws RequestHandleException, IOException, SQLException, RdapDataAccessException;

	/**
	 * Tries hard to find the best suitable renderer for
	 * <code>httpRequest</code>.
	 */
	private Renderer findRenderer(HttpServletRequest httpRequest) {
		Renderer renderer;

		AcceptHeaderFieldParser parser = new AcceptHeaderFieldParser(httpRequest.getHeader("Accept"));
		PriorityQueue<Accept> accepts = parser.getQueue();

		while (!accepts.isEmpty()) {
			renderer = RendererPool.get(accepts.remove().getMediaRange());
			if (renderer != null) {
				return renderer;
			}
		}

		return new DefaultRenderer();
	}

}
