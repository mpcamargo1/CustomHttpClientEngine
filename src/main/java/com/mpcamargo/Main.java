package com.mpcamargo;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;

import javax.ws.rs.client.ClientBuilder;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        CustomHttpClient httpClient = new CustomHttpClient();

        ResteasyClient client = (ResteasyClient) ((ResteasyClientBuilder) ClientBuilder.newBuilder())
                .httpEngine(httpClient)
                .build();

        // Definir o URL de destino para a requisição
        String url = "http://localhost:3000/";  // Substitua pela URL de destino

        // Criar um WebTarget (representação da URL do serviço)
        Resource target = client.target(url).proxy(Resource.class);

        Example example = new Example();
        example.setBar(1);

        Example example1 = new Example();

        // Fazer uma requisição GET
        //example1 = target.getUsers(example);
        target.sendUsers(example);
        System.out.println(example1);

        client.close();
    }
}