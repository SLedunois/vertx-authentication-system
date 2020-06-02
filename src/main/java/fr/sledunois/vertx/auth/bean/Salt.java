package fr.sledunois.vertx.auth.bean;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

public class Salt {
  private String value;

  public Salt(String value) {
    this.value = value;
  }

  public String SHA1() {
    try {
      byte[] bytes = MessageDigest.getInstance("SHA-1").digest(this.value.getBytes());
      Formatter formatter = new Formatter();
      for (byte b : bytes) {
        formatter.format("%02x", b);
      }

      return formatter.toString();
    } catch (NoSuchAlgorithmException e) {
      return "";
    }
  }
}
