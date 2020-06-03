package fr.sledunois.vertx.auth.handler;

import fr.sledunois.vertx.auth.bean.AuthCookie;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.sstore.SessionStore;

public class AuthSessionHandler implements Handler<RoutingContext> {
  private SessionStore sessionStore;
  private String redirectUri;

  @Override
  public void handle(RoutingContext rc) {
    Cookie cookie = rc.getCookie(AuthCookie.name);
    if (cookie == null) {
      if (redirectUri != null) {
        doRedirect(rc);
      } else {
        rc.response().setStatusCode(401).end();
      }
      return;
    }

    sessionStore.get(cookie.getValue(), ar -> {
      if (ar.failed()) {
        //Invalid cookie or expired cookie
        rc.removeCookie(AuthCookie.name, true);
        doRedirect(rc);
      } else {
        rc.next();
      }
    });
  }

  private void doRedirect(RoutingContext rc) {
    rc.response().setStatusCode(302).putHeader("Location", redirectUri).end();
  }

  public AuthSessionHandler setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
    return this;
  }

  public AuthSessionHandler setSessionStore(SessionStore sessionStore) {
    this.sessionStore = sessionStore;
    return this;
  }
}
