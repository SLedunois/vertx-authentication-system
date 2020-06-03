package fr.sledunois.vertx.redis;

import io.vertx.core.Vertx;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;

public class Redis {

  private RedisAPI client;
  private RedisConnection conn;

  private static class RedisHolder {
    private static final Redis instance = new Redis();
  }

  public void init(Vertx vertx, String endpoint) {
    io.vertx.redis.client.Redis.createClient(vertx, endpoint)
      .connect(ar -> {
        if (ar.succeeded()) {
          conn = ar.result();
          client = RedisAPI.api(conn);
        }
      });
  }

  public static Redis getInstance() {
    return Redis.RedisHolder.instance;
  }

  public static RedisAPI client() {
    return RedisHolder.instance.client;
  }

}
