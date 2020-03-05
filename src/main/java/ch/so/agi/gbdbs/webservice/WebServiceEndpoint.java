package ch.so.agi.gbdbs.webservice;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.GetParcelsByIdRequestType;
import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.GetParcelsByIdResponse;
import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.ObjectFactory;

@Endpoint
public class WebServiceEndpoint {

    private static final String NAMESPACE_URI = "http://schemas.geo.admin.ch/BJ/TGBV/GBDBS/2.1";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetParcelsByIdRequest")
    @ResponsePayload
    public GetParcelsByIdResponse getParcelsById(@RequestPayload GetParcelsByIdRequestType request) {

//        String outputString = "Hello " + request.getTransactionId() + "!";
//        String outputString = "Hello " + request.getTransactionId() + "!";
        String outputString = "Hello " + request.getIds() + "!";
        System.out.println(outputString);

        ObjectFactory factory = new ObjectFactory();
        GetParcelsByIdResponse response = factory.createGetParcelsByIdResponse();
        System.out.println(response.toString());
        
//        response.getGrundstuecks().add(e)
//        response.setResult(outputString);

        return response;
    }

}
