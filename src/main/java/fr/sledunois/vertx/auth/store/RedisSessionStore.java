package fr.sledunois.vertx.auth.store;

import fr.sledunois.vertx.auth.bean.RedisSession;
import fr.sledunois.vertx.redis.Redis;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.redis.client.Response;

import java.util.Arrays;
import java.util.Collections;

public class RedisSessionStore implements SessionStore {
  private String SESSION_KEY = "session:%s";
  private PRNG random;
  private Vertx vertx;
  private boolean closed;

  @Override

  public SessionStore init(Vertx vertx, JsonObject options) {
    this.random = new PRNG(vertx);
    this.vertx = vertx;
    return this;
  }

  @Override
  public long retryTimeout() {
    return 0;
  }

  @Override
  public Session createSession(long timeout) {
    return new RedisSession(random, timeout, DEFAULT_SESSIONID_LENGTH);
  }

  @Override
  public Session createSession(long timeout, int length) {
    return new RedisSession(random, timeout, length);
  }

  @Override
  public void get(String cookieValue, Handler<AsyncResult<Session>> handler) {
    Redis.client().get(String.format(SESSION_KEY, cookieValue), ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
        throw new RuntimeException(ar.cause());
      } else {
        Response response = ar.result();
        if (response == null) {
          handler.handle(Future.failedFuture("Session is null"));
        } else {
          JsonObject value = new JsonObject(response.toString());
          handler.handle(Future.succeededFuture(new RedisSession(cookieValue, value.getMap())));
        }
      }
    });
  }

  @Override
  public void delete(String id, Handler<AsyncResult<Void>> handler) {
    String key = String.format(SESSION_KEY, id);
    Redis.client().del(Collections.singletonList(key), ar -> {
      if (ar.failed()) handler.handle(Future.failedFuture(ar.cause()));
      else handler.handle(Future.succeededFuture());
    });
  }

  @Override
  public void put(Session session, Handler<AsyncResult<Void>> handler) {
    RedisSession redisSession = (RedisSession) session;
    String key = String.format(SESSION_KEY, redisSession.id());
    Redis.client().set(Arrays.asList(key, redisSession.toJSON().encode()), ar -> {
      if (ar.failed()) handler.handle(Future.failedFuture(ar.cause()));
      else handler.handle(Future.succeededFuture());
    });
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> resultHandler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> resultHandler) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    closed = true;
  }
}
