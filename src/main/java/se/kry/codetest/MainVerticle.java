package se.kry.codetest;

import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.RoutingContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.validator.routines.UrlValidator;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.time.LocalDateTime;
import java.util.HashMap;

public class MainVerticle extends AbstractVerticle {

    private HashMap<String, String> services = new HashMap<>();
    //TODO use this
    private DBConnector connector;
    private BackgroundPoller poller = new BackgroundPoller();

    @Override
    public void start(Future<Void> startFuture) {
        connector = new DBConnector(vertx);
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        // services.put("https://www.kry.se", "UNKNOWN");
        vertx.setPeriodic(1000 * 60, timerId -> poller.pollServices(services));
        setRoutes(router);
        vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(8080, result -> {
                    if (result.succeeded()) {
                        System.out.println("KRY code test service started");
                        startFuture.complete();
                    } else {
                        startFuture.fail(result.cause());
                    }
                });
    }

    private void setRoutes(Router router) {
        router.route("/*").handler(StaticHandler.create());
        router.get("/service").handler(req -> {
            connector.query("SELECT url, status from service").setHandler(done -> {
                if (done.succeeded()) {
                    System.out.println("service fetched");
                    List<JsonObject> jsonServices = new ArrayList();
                    done.result().getResults().forEach(row ->
                            jsonServices.add(new JsonObject()
                                    .put("name", row.getValue(0))
                                    .put("status", row.getValue(1))));

                    req.response()
                            .putHeader("content-type", "application/json")
                            .end(new JsonArray(jsonServices).encode());
                } else {
                    done.cause().printStackTrace();
                }

            });
        });
        router.post("/service").handler(this::saveService);
        router.delete("/service").handler(req -> {
            JsonObject jsonBody = req.getBodyAsJson();
            final String url = jsonBody.getString("url");
            connector.query("DELETE FROM service WHERE url = '" + url + "';").setHandler(done -> {
                if (done.succeeded()) {
                    System.out.println("Service " + url + " was deleted");
                    req.response()
                            .putHeader("content-type", "text/plain")
                            .end("OK");
                } else {
                    req.response()
                            .putHeader("content-type", "text/plain")
                            .end("FAIL");
                    done.cause().printStackTrace();

                }
            });

        });
    }

    private void saveService(RoutingContext req) {
        JsonObject jsonBody = req.getBodyAsJson();
        String url = jsonBody.getString("url");

        if (!(new UrlValidator().isValid(url))) {
            req.response()
                    .putHeader("content-type", "text/plain")
                    .end("FAIL");
        } else {
            String status = "Added";
            connector.query("INSERT OR IGNORE INTO service(url, status, last_updated) VALUES('" + url + "','" + status + "','" + LocalDateTime.now() + "')")
                    .setHandler(done -> {
                        if (done.succeeded()) {
                            System.out.println("service added");
                            req.response()
                                    .putHeader("content-type", "text/plain")
                                    .end("OK");
                        } else {
                            done.cause().printStackTrace();
                            req.response()
                                    .putHeader("content-type", "text/plain")
                                    .end("FAIL");
                        }
                    });
        }
    }

}


