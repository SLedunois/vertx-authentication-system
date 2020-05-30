package fr.sledunois.vertx.authentication;

import fr.sledunois.vertx.authentication.pg.Pg;
import fr.sledunois.vertx.authentication.provider.PgAuthProvider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

public class HttpVerticle extends AbstractVerticle {
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

    return vertx.createHttpServer()
      .requestHandler(router)
      .listen(port);
  }
}
