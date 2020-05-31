package fr.sledunois.vertx.authentication;

import fr.sledunois.vertx.authentication.bean.Salt;
import fr.sledunois.vertx.authentication.pg.Pg;
import fr.sledunois.vertx.authentication.pg.PgResult;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.sqlclient.Tuple;

public class MainVerticle extends HttpVerticle {

  HttpServer server;

  @Override
  public void start(Promise<Void> startPromise) {
    super.start(startPromise);
    server = createHttpServer(3000);

    FormLoginHandler formLoginHandler = FormLoginHandler.create(authProvider)
      .setPasswordParam("password")
      .setUsernameParam("username")
      .setDirectLoggedInOKURL("/");

    SessionHandler sessionHandler = SessionHandler.create(sessionStore)
      .setSessionCookieName("X-Session-Id");

    AuthHandler redirectAuthHandler = RedirectAuthHandler.create(authProvider, "/sign-in");

    router.route().handler(BodyHandler.create(false));
    router.get("/sign-in").handler(this::signIn);
    router.get("/sign-up").handler(this::signUp);
    router.get("/sign-out").handler(this::signOut);
    router.post("/register").handler(this::register);

    router.route().handler(sessionHandler);
    router.post("/login").handler(formLoginHandler);
    router.get("/").handler(redirectAuthHandler).handler(this::indexPage);
  }

  private void register(RoutingContext rc) {
    MultiMap attributes = rc.request().formAttributes();
    if (!attributes.contains("username") || !attributes.contains("password")) {
      rc.response().setStatusCode(400).end();
      return;
    }
    String username = attributes.get("username");
    String password = attributes.get("password");
    String salt = new Salt(password).SHA1();
    String query = "INSERT INTO public.user(username, password) VALUES ($1, $2) RETURNING *;";
    Pg.getInstance().preparedQuery(query, Tuple.of(username, salt),
      PgResult.uniqueJsonResult(ar -> rc.response().setStatusCode(302).putHeader("location", ar.succeeded() ? "/" : "/sign-in").end()));
  }

  private void signUp(RoutingContext rc) {
    rc.response().sendFile("static/sign-up.html");
  }

  private void signOut(RoutingContext rc) {
    String sessionId = rc.request().getCookie("X-Session-Id").getValue();
    sessionStore.delete(sessionId, ar -> rc.response().putHeader("location", ar.succeeded() ? "/sign-in" : "/").setStatusCode(302).end());
  }

  private void indexPage(RoutingContext rc) {
    rc.response().sendFile("static/index.html");
  }

  private void signIn(RoutingContext rc) {
    rc.response().sendFile("static/sign-in.html");
  }

}
