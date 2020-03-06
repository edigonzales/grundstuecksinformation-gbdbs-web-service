package ch.so.agi.gbdbs.webservice;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.BezugInhalt;
import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.GetParcelsByIdRequestType;
import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.GetParcelsByIdResponse;
import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.ObjectFactory;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;

@Endpoint
public class WebServiceEndpoint {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private static final String NAMESPACE_URI = "http://schemas.geo.admin.ch/BJ/TGBV/GBDBS/2.1";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetParcelsByIdRequest")
    @ResponsePayload
    public GetParcelsByIdResponse getParcelsById(@RequestPayload GetParcelsByIdRequestType request) {
        ObjectFactory factory = new ObjectFactory();

        // TEST
        // Nicht klar wie das mit den Exceptions geht, eventuell: 
        // https://docs.oracle.com/cd/E24329_01/web.1211/e24965/faults.htm#WSADV639
        // https://docs.spring.io/spring-ws/docs/current/reference/#_code_soapfaultannotationexceptionresolver_code
        // Steckt die GBDBS Exception im detail-Element?
        if (!request.getBezugInhalt().equals(BezugInhalt.INDEX)) {
            logger.info("falscher BezugInhalt");
            ch.admin.geo.schemas.bj.tgbv.gbdbs._2.Exception exception = factory.createException();
            exception.setMessage("falscher BezugInhalt");
        }
        
        GetParcelsByIdResponse response = factory.createGetParcelsByIdResponse();
        
        for (String id : request.getIds()) {
            logger.info(id.substring(0, 14));
            
            ch.admin.geo.schemas.bj.tgbv.gbdbs._2.GetParcelsByIdResponse.Grundstueck grundstueck = factory.createGetParcelsByIdResponseGrundstueck();
            
            // FIXME: use real date from database
            try {
                logger.info("timezone id {}", TimeZone.getDefault().getID());
                XMLGregorianCalendar today = null;
                GregorianCalendar gdate = new GregorianCalendar();
                gdate.setTime(new java.util.Date());
                today = DatatypeFactory.newInstance().newXMLGregorianCalendar(gdate);
                response.setStandDerDaten(today);
            } catch (DatatypeConfigurationException e) {
                throw new IllegalStateException(e);
            }
            response.getGrundstuecks().add(grundstueck);    
       }

        return response;
    }

}
