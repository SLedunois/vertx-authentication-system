package fr.sledunois.vertx;

import fr.sledunois.vertx.auth.AuthVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.deployVerticle(AuthVerticle.class.getName(), ar -> {
      if (ar.succeeded()) startPromise.complete();
      else System.exit(1);
    });
  }

}
