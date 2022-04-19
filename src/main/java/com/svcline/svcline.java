package com.svcline;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.gson.Gson;
import com.svcline.handlers.ConfigurationHandler;
import com.svcline.handlers.LineHandler;
import com.routler.RContext;
import com.routler.RError;
import com.routler.RResponse;
import com.svcline.models.Props;
import com.svcline.prodline.ProductLineConfiguration;
import com.svcline.prodline.ProductionLine;
import com.routler.Routler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class svcline implements HttpFunction {
    private static ProductionLine productionLine = null;
    private static final Gson gson = new Gson();

    private static final String CONTENT_TYPE = "application/json;charset=utf-8";

    private static final String PATH_CONFIGURATION = "/configurations/{configId}";
    private static final String PATH_PRODUCTION_LINE = "/items/{itemId}";

    // Register our path with handlers
    static {
        Routler.register(PATH_CONFIGURATION, new ConfigurationHandler());
        Routler.register(PATH_PRODUCTION_LINE, new LineHandler());
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        OutputStream os;   // Must use OutputStream for UTF-8
        try {
            // No need to continue execution if we fail to return the response.
            os = response.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        response.setContentType(CONTENT_TYPE);

        // Since we can return the response, now we initiate the system and gracefully exit if it fails.
        try {
            initProductionLine();
        } catch (InstantiationException | IOException e) {
            e.printStackTrace();
            response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
            try {
                os.write(bytes(gson.toJson(new RError("Production line initialization failed. Application will not start."))));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return;
        }

        RContext RContext = new RContext(productionLine);
        // We came so far, now handle the request
        RResponse rResponse = Routler.handle(request, response, RContext);

        // Attempt returning the actual response
        try {
            if (rResponse != null) {
                response.setStatusCode(rResponse.getCode());
                os.write(bytes(rResponse.getJson()));
            } else {
                response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
                os.write(bytes(gson.toJson(new RError("Unexpected error while handling request."))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initProductionLine() throws InstantiationException, IOException {
        if (productionLine == null) {
            productionLine = new ProductionLine();
            Props props = new Props();
            //currentlyLoadedConfiguration = PRODUCT_CONFIGURATION1;

            ProductLineConfiguration productLineConfiguration = new ProductLineConfiguration(productionLine.getFirestore());
            productLineConfiguration.loadFromDb(props.getCurrentlyLoadedConfiguration());

            /*ProductLineConfiguration productLineConfiguration = new  ProductLineConfiguration();
            productLineConfiguration.loadTestConfiguration();
            productLineConfiguration.writeToDb(currentlyLoadedConfiguration);*/

            productionLine.init(productLineConfiguration, props);
        }
    }

    public static void reloadProductionLineConfiguration() {
        ProductLineConfiguration cfg;
        try {
            cfg = new ProductLineConfiguration(productionLine.getFirestore());
            cfg.loadFromDb(productionLine.getProps().getCurrentlyLoadedConfiguration());

            productionLine.init(cfg, productionLine.getProps());
            System.out.println("Production line setup:\n" + gson.toJson(cfg));
        } catch (InstantiationException | IOException e) {
            e.printStackTrace();
        }
    }

    /*private Props loadProperties() throws IOException {
        Props props = new Props();

        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        InputStream resourceStream = loader.getResourceAsStream("application.properties");
        Properties properties = new Properties();
        properties.load(resourceStream);

        props.setButtonColorBg(properties.getProperty("button.color.bg"));
        props.setButtonColorTxt(properties.getProperty("button.color.txt"));
        props.setCurrentlyLoadedConfiguration(properties.getProperty("line.configuration"));
        props.setEnvironment(properties.getProperty("environment"));

        System.out.println("Props: " + props);

        return props;
    }*/

    public static ProductionLine getProductionLine() {
        return productionLine;
    }

    private static byte[] bytes(String str) {
        return str != null ? str.getBytes(StandardCharsets.UTF_8) : "".getBytes(StandardCharsets.UTF_8);
    }
}