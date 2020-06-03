package fr.sledunois.vertx.auth;

import fr.sledunois.vertx.auth.bean.AuthCookie;
import fr.sledunois.vertx.auth.bean.Salt;
import fr.sledunois.vertx.auth.form.Field;
import fr.sledunois.vertx.auth.handler.AuthFormLoginHandler;
import fr.sledunois.vertx.pg.Pg;
import fr.sledunois.vertx.pg.PgResult;
import fr.sledunois.vertx.util.HttpVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Tuple;

public class AuthVerticle extends HttpVerticle {

  /*
    TODO
     - Create authentication service
     - Use vertx gen to generate proxy class
     - Use service discovery to expose authentication class
     - Update PgProvider to call authentication microservice
   */

  @Override
  public void start(Promise<Void> startPromise) {
    super.start(startPromise);
    server = createHttpServer(3000);

    AuthFormLoginHandler formLoginHandler = new AuthFormLoginHandler()
      .setPasswordParam(Field.password.name())
      .setUsernameParam(Field.username.name())
      .setDirectLoggedInOKURL(Route.INDEX.path())
      .setSessionStore(sessionStore)
      .setAuthProvider(authProvider);

    router.get(Route.SIGN_IN.path()).handler(this::signIn);
    router.get(Route.SIGN_UP.path()).handler(this::signUp);
    router.get(Route.SIGN_OUT.path()).handler(this::signOut);
    router.post(Route.REGISTER.path()).handler(this::register);
    router.post(Route.LOGIN.path()).handler(formLoginHandler);

    router.route().handler(sessionHandler);
    router.get(Route.INDEX.path()).handler(this::indexPage);
    startPromise.complete();
  }


  private void register(RoutingContext rc) {
    MultiMap attributes = rc.request().formAttributes();
    if (!attributes.contains("username") || !attributes.contains("password")) {
      rc.response().setStatusCode(400).end();
      return;
    }
    String username = attributes.get(Field.username.name());
    String password = attributes.get(Field.password.name());
    String salt = new Salt(password).SHA1();
    String query = "INSERT INTO public.user(username, password) VALUES ($1, $2) RETURNING *;";
    Pg.getInstance().preparedQuery(query, Tuple.of(username, salt),
      PgResult.uniqueJsonResult(ar -> rc.response().setStatusCode(302).putHeader("Location", ar.succeeded() ? Route.INDEX.path() : Route.SIGN_IN.path()).end()));
  }

  private void signUp(RoutingContext rc) {
    rc.response().sendFile("static/auth/sign-up.html");
  }

  private void signOut(RoutingContext rc) {
    String sessionId = rc.request().getCookie(AuthCookie.name).getValue();
    sessionStore.delete(sessionId, ar -> {
      if (ar.failed()) {
        rc.response().putHeader("Location", Route.INDEX.path()).setStatusCode(302).end();
      } else {
        rc.removeCookie(AuthCookie.name, true);
        rc.response().putHeader("Location", Route.SIGN_IN.path()).setStatusCode(302).end();
      }
    });
  }

  private void indexPage(RoutingContext rc) {
    rc.response().sendFile("static/index.html");
  }

  private void signIn(RoutingContext rc) {
    rc.response().sendFile("static/auth/sign-in.html");
  }
}
