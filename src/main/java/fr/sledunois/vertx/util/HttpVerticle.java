package fr.sledunois.vertx.util;

import fr.sledunois.vertx.auth.provider.PgAuthProvider;
import fr.sledunois.vertx.pg.Pg;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

public class HttpVerticle extends AbstractVerticle {
  protected HttpServer server;
  protected AuthProvider authProvider = new PgAuthProvider();
  protected SessionStore sessionStore;
  protected Router router;
  protected SessionHandler sessionHandler;
  protected AuthHandler redirectAuthHandler;

  @Override
  public void start(Promise<Void> startPromise) {
    Pg.getInstance().init(vertx, "localdev", 5432, "authentication", "web-education", "We_1234", 5);
  }

  protected HttpServer createHttpServer(Integer port) {
    redirectAuthHandler = RedirectAuthHandler.create(authProvider, "/sign-in");
    sessionStore = LocalSessionStore.create(vertx);
    sessionHandler = SessionHandler.create(sessionStore)
      .setSessionCookieName("X-Session-Id");
    router = Router.router(vertx);
    router.route().handler(BodyHandler.create(false));

    return vertx.createHttpServer()
      .requestHandler(router)
      .listen(port);
  }
}
