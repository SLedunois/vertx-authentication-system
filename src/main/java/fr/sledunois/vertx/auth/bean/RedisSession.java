package fr.sledunois.vertx.auth.bean;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.web.sstore.AbstractSession;

import java.util.Map;

public class RedisSession extends AbstractSession {

  public RedisSession(PRNG random, long timeout, int length) {
    super(random, timeout, length);
  }

  public RedisSession(String id, Map<String, Object> data) {
    setId(id);
    setData(data);
  }

  public JsonObject toJSON() {
    return new JsonObject(this.data());
  }

  public String toString() {
    return this.toJSON().encode();
  }
}
