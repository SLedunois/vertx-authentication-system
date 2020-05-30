package fr.sledunois.vertx.authentication.provider;

import fr.sledunois.vertx.authentication.bean.UserImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

public class PgAuthProvider implements AuthProvider {
  @Override
  public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> handler) {
    User user = new UserImpl(new JsonObject().put("username", "simon.ledunois"));
    handler.handle(Future.succeededFuture(user));
  }
}
