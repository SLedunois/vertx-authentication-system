package fr.sledunois.vertx.auth.handler;

import fr.sledunois.vertx.auth.Route;
import fr.sledunois.vertx.auth.bean.AuthCookie;
import fr.sledunois.vertx.auth.bean.RedisSession;
import io.vertx.core.MultiMap;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.web.sstore.SessionStore;

import java.util.UUID;

public class AuthFormLoginHandler implements FormLoginHandler {
  private static final Logger log = LoggerFactory.getLogger(AuthFormLoginHandler.class);

  private SessionStore sessionStore;
  private AuthProvider authProvider;
  private String usernameParam;
  private String passwordParam;
  private String returnURLParam;
  private String directLoggedInOKURL;

  public AuthFormLoginHandler setSessionStore(SessionStore sessionStore) {
    this.sessionStore = sessionStore;
    return this;
  }

  public AuthFormLoginHandler setAuthProvider(AuthProvider authProvider) {
    this.authProvider = authProvider;
    return this;
  }

  @Override
  public AuthFormLoginHandler setUsernameParam(String usernameParam) {
    this.usernameParam = usernameParam;
    return this;
  }

  @Override
  public AuthFormLoginHandler setPasswordParam(String passwordParam) {
    this.passwordParam = passwordParam;
    return this;
  }

  @Override
  public AuthFormLoginHandler setReturnURLParam(String returnURLParam) {
    throw new UnsupportedOperationException();
  }

  @Override
  public AuthFormLoginHandler setDirectLoggedInOKURL(String directLoggedInOKURL) {
    this.directLoggedInOKURL = directLoggedInOKURL;
    return this;
  }

  @Override
  public void handle(RoutingContext rc) {
    //TODO Check if user already have session. If session is valid, doRedirect to index page
    HttpServerRequest req = rc.request();
    if (req.method() != HttpMethod.POST) {
      rc.fail(405); // Must be a POST
    } else {
      if (!req.isExpectMultipart()) {
        throw new IllegalStateException("HttpServerRequest should have setExpectMultipart set to true, but it is currently set to false.");
      }
      MultiMap params = req.formAttributes();
      String username = params.get(usernameParam);
      String password = params.get(passwordParam);
      if (username == null || password == null) {
        log.warn("No username or password provided in form - did you forget to include a BodyHandler?");
        rc.fail(400);
      } else {
        JsonObject authInfo = new JsonObject().put("username", username).put("password", password);
        authProvider.authenticate(authInfo, res -> {
          if (res.succeeded()) {
            User user = res.result();
            rc.setUser(user);

            Session session = new RedisSession(UUID.randomUUID().toString(), user.principal().getMap());
            sessionStore.put(session, evt -> {
              if (evt.failed()) doRedirect(req.response(), Route.LOGIN.path());
              else {
                rc.response().addCookie(Cookie.cookie(AuthCookie.name, session.id()));
                doRedirect(req.response(), directLoggedInOKURL != null ? directLoggedInOKURL : Route.INDEX.path());
              }
            });
          } else {
            //TODO Redirect to another page with 403 state
            rc.fail(403);  // Failed login
          }
        });
      }
    }
  }

  private void doRedirect(HttpServerResponse response, String url) {
    response.putHeader("Location", url).setStatusCode(302).end();
  }
}
