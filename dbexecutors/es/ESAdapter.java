package dbexecutors.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.ini4j.Ini;

import java.io.File;
import java.io.IOException;

public abstract class ESAdapter {
    static String basicURL;
    static String apiKey;

    static Ini config;

    static RestClient restClient;
    static ElasticsearchTransport transport;
    static ElasticsearchClient esClient;

    static {
        try {
            config = new Ini(new File("./settings.ini"));
        } catch (IOException e) {
            System.out.println("Invalid settings file");
            e.printStackTrace( );
            System.exit(1);
        }
    }

    static {
        Boolean secure = config.get("elastic", "secure", Boolean.class);

        basicURL = config.get("elastic", "url", String.class);
        basicURL = secure ? basicURL.replace("http://", "https://") : basicURL;

        apiKey = config.get("elastic", "key", String.class);

        try {
            restClient = RestClient
                    .builder(HttpHost.create(basicURL))
                    .setDefaultHeaders(new Header[]{
                            new BasicHeader("Authorization", STR."ApiKey \{apiKey}")
                    })
                    .build( );

            transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper( ));

            esClient = new ElasticsearchClient(transport);
        } catch (Exception e) {
            System.out.println("ElasticSearch connection error");
            e.printStackTrace( );
            System.exit(1);
        }
    }
}
