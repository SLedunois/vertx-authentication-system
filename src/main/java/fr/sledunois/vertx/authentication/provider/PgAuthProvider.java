package fr.sledunois.vertx.authentication.provider;

import fr.sledunois.vertx.authentication.bean.Salt;
import fr.sledunois.vertx.authentication.bean.UserImpl;
import fr.sledunois.vertx.authentication.pg.Pg;
import fr.sledunois.vertx.authentication.pg.PgResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.sqlclient.Tuple;

import java.util.Objects;

public class PgAuthProvider implements AuthProvider {
  @Override
  public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> handler) {
    String username = authInfo.getString("username");
    String password = authInfo.getString("password");
    String salt = new Salt(password).SHA1();
    String query = "SELECT * FROM public.user WHERE username = $1 AND password = $2";
    Pg.getInstance().preparedQuery(query, Tuple.of(username, salt), PgResult.uniqueJsonResult(ar -> {
      if (ar.succeeded() && Objects.nonNull(ar.result())) {
        handler.handle(Future.succeededFuture(new UserImpl(ar.result())));
      } else {
        handler.handle(Future.failedFuture("User not found"));
      }
    }));
  }
}
