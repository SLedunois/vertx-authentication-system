package fr.sledunois.vertx.util;

import fr.sledunois.vertx.auth.handler.AuthSessionHandler;
import fr.sledunois.vertx.auth.provider.PgAuthProvider;
import fr.sledunois.vertx.auth.store.RedisSessionStore;
import fr.sledunois.vertx.pg.Pg;
import fr.sledunois.vertx.redis.Redis;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.sstore.SessionStore;

public class HttpVerticle extends AbstractVerticle {
  /*
     TODO
     - Vertx session handler is not useful. Implements custom session handler based on redis
   */
  protected HttpServer server;
  protected AuthProvider authProvider;
  protected SessionStore sessionStore;
  protected Router router;
  protected Handler<RoutingContext> sessionHandler;

  @Override
  public void start(Promise<Void> startPromise) {
    Pg.getInstance().init(vertx, "localdev", 5432, "authentication", "web-education", "We_1234", 5);
    Redis.getInstance().init(vertx, "redis://localdev:6379");
  }

  protected HttpServer createHttpServer(Integer port) {
    sessionStore = new RedisSessionStore()
      .init(vertx, new JsonObject());
    authProvider = new PgAuthProvider().setStore(sessionStore);
    sessionHandler = new AuthSessionHandler()
      .setRedirectUri("/sign-in")
      .setSessionStore(sessionStore);
    router = Router.router(vertx);
    router.route().handler(BodyHandler.create(false));

    return vertx.createHttpServer()
      .requestHandler(router)
      .listen(port, ar -> {
        if (ar.succeeded()) System.out.println(String.format("Verticle listening on port %d", port));
        else throw new RuntimeException(ar.cause());
      });
  }
}
