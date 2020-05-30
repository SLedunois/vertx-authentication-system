package fr.sledunois.vertx.authentication.bean;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

public class UserImpl implements User {
  private String username;

  public UserImpl(JsonObject object) {
    this.username = object.getString("username", "");
  }

  @Override
  public User isAuthorized(String authority, Handler<AsyncResult<Boolean>> resultHandler) {
    return this;
  }

  @Override
  public User clearCache() {
    return this;
  }

  @Override
  public JsonObject principal() {
    return new JsonObject().put("username", username);
  }

  @Override
  public void setAuthProvider(AuthProvider authProvider) {
    throw new UnsupportedOperationException("setAuthProvider not implemented");
  }
}
