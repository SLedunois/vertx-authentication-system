package fr.sledunois.vertx.authentication;

import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;

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
    router.get("/sign-out").handler(this::signOut);

    router.route().handler(sessionHandler);
    router.post("/login").handler(formLoginHandler);
    router.get("/").handler(redirectAuthHandler).handler(this::indexPage);
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
