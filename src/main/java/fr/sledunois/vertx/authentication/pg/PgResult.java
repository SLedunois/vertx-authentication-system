package fr.sledunois.vertx.authentication.pg;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.List;

public class PgResult {
  public static Handler<AsyncResult<RowSet<Row>>> jsonResult(Handler<AsyncResult<JsonArray>> handler) {
    return event -> {
      if (event.failed()) {
        handler.handle(Future.failedFuture(event.cause()));
        return;
      }

      RowSet<Row> rows = event.result();
      JsonArray result = new JsonArray();
      for (Row row : rows) {
        result.add(transformToJsonObject(row, rows.columnsNames()));
      }

      handler.handle(Future.succeededFuture(result));
    };
  }

  public static Handler<AsyncResult<RowSet<Row>>> uniqueJsonResult(Handler<AsyncResult<JsonObject>> handler) {
    return jsonResult(event -> {
      if (event.failed()) {
        handler.handle(Future.failedFuture(event.cause()));
      } else {
        JsonArray result = event.result();
        if (result.isEmpty()) handler.handle(Future.succeededFuture(null));
        else handler.handle(Future.succeededFuture(result.getJsonObject(0)));
      }
    });
  }

  private static JsonObject transformToJsonObject(Row row, List<String> columNames) {
    JsonObject tuple = new JsonObject();
    for (int i = 0; i < columNames.size(); i++) {
      tuple.put(columNames.get(i), row.getValue(i));
    }

    return tuple;
  }

}
