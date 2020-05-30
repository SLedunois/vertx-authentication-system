package fr.sledunois.vertx.authentication.pg;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

public class Pg {

  private PgPool client;

  private static class PgHolder {
    private static final Pg instance = new Pg();
  }

  public void init(Vertx vertx, String host, Integer port, String database, String user, String password, Integer poolSize) {
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setHost(host)
      .setPort(port)
      .setDatabase(database)
      .setUser(user)
      .setPassword(password);
    PoolOptions poolOptions = new PoolOptions().setMaxSize(poolSize);
    client = PgPool.pool(vertx, connectOptions, poolOptions);
  }

  public static Pg getInstance() {
    return PgHolder.instance;
  }


}
