package com.example.Demo.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@Configuration
public class ElasticClientConfig  {

        @Bean
        public RestClient restClient() {
            return RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
        }

        @Bean
        public ElasticsearchClient elasticsearchClient(RestClient restClient) {
            return new ElasticsearchClient(
                    new RestClientTransport(
                            restClient,
                            new JacksonJsonpMapper()
                    )
            );
        }

        @Bean
        public ElasticsearchOperations elasticsearchTemplate(ElasticsearchClient elasticsearchClient) {
            return new ElasticsearchTemplate(elasticsearchClient);
        }
        //elasticsearchtemplate is an implementation of the elasticsearchoperations interface using the transport
    // client
}


