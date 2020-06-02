package fr.sledunois.vertx.util;

import fr.sledunois.vertx.auth.provider.PgAuthProvider;
import fr.sledunois.vertx.pg.Pg;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

public class HttpVerticle extends AbstractVerticle {
  protected HttpServer server;
  protected AuthProvider authProvider = new PgAuthProvider();
  protected SessionStore sessionStore;
  protected Router router;

  @Override
  public void start(Promise<Void> startPromise) {
    Pg.getInstance().init(vertx, "localdev", 5432, "authentication", "web-education", "We_1234", 5);
  }

  protected HttpServer createHttpServer(Integer port) {
    sessionStore = LocalSessionStore.create(vertx);
    router = Router.router(vertx);
    router.route().handler(BodyHandler.create(false));

    return vertx.createHttpServer()
      .requestHandler(router)
      .listen(port);
  }
}
