package se.kry.codetest.migrate;

import io.vertx.core.Vertx;
import se.kry.codetest.DBConnector;

public class DBMigration {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DBConnector connector = new DBConnector(vertx);
        connector.query("DROP TABLE IF EXISTS service")
                .setHandler(done -> {});
        connector.query("CREATE TABLE service (url VARCHAR(128) NOT NULL, status VARCHAR(128), last_updated TEXT)")
                .setHandler(done -> {
            if (done.succeeded()) {
                System.out.println("completed db migrations");
            } else {
                done.cause().printStackTrace();
            }
            vertx.close(shutdown -> {
                System.exit(0);
            });
        });
    }
}
