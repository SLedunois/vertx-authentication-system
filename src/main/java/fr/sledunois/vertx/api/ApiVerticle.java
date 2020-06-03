package fr.sledunois.vertx.api;

import fr.sledunois.vertx.auth.handler.AuthSessionHandler;
import fr.sledunois.vertx.util.HttpVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ApiVerticle extends HttpVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    super.start(startPromise);
    server = createHttpServer(3001);

    router.get("/books").handler(new AuthSessionHandler()).handler(this::getBooks);
    startPromise.complete();
  }

  private void getBooks(RoutingContext rc) {
    JsonObject book = new JsonObject()
      .put("title", "Harry Potter and the philosopher's stone")
      .put("author", "J.K. Rowling");


    rc.response().putHeader("content-type", "application/json");
    rc.response().setStatusCode(200);
    rc.response().end(new JsonArray().add(book).encode());
  }
}
