package ch.so.agi.gbdbs.webservice;

import java.util.Locale;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.AbstractEndpointExceptionResolver;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;

import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.ObjectFactory;

@Component
public class DetailSoapFaultDefinitionExceptionResolver extends AbstractEndpointExceptionResolver {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private static final ObjectFactory FACTORY = new ObjectFactory();

    @Autowired
    Jaxb2Marshaller marshaller;

//    @Override
//    protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
//        logger.warn("Exception processed ", ex);
//        if (ex instanceof Exception) {
//            
//            /*
//            ServiceStatus status = ((ServiceFaultException) ex).getServiceStatus();
//            SoapFaultDetail detail = fault.addFaultDetail();
//            detail.addFaultDetailElement(CODE).addText(status.getStatusCode());
//            detail.addFaultDetailElement(MESSAGE).addText(status.getMessage());
//            */
//            
//            ObjectFactory factory = new ObjectFactory();
//            ch.admin.geo.schemas.bj.tgbv.gbdbs._2.Exception exception = factory.createException();
//            exception.setMessage("falscher BezugInhalt");
//            
//            QName MESSAGE = new QName("message");
//
//            SoapFaultDetail faultDetail = fault.addFaultDetail();
//            Result result = faultDetail.getResult();
//            
//            result.
//            
//            
//            faultDetail.addFaultDetailElement(MESSAGE).addText("gagag");
//            
//            
//        }
//    }

    @Override
    protected boolean resolveExceptionInternal(MessageContext messageContext, Object endpoint, Exception ex) {
        if (ex instanceof Exception) { 
            

            final SoapMessage response = (SoapMessage) messageContext.getResponse();
            final SoapBody soapBody = response.getSoapBody();

            final SoapFault soapFault = soapBody.addClientOrSenderFault(
                    "fubar",
                    Locale.ENGLISH);
            

            final SoapFaultDetail faultDetail = soapFault.addFaultDetail();
            final Result result = faultDetail.getResult();
            
            ch.admin.geo.schemas.bj.tgbv.gbdbs._2.Exception exception = FACTORY.createException();
            exception.setCode("other");
            exception.setNativeCode("S340");
            exception.setMessage("falscher BezugInhalt");
            exception.setTechnicalDetails(ex.getMessage());
            exception.setTransactionId("transactionId");
            
            marshaller.marshal(exception, result);
            return true;

//            try {
//            } catch (final JAXBException e) {
//                // Mention what went wrong, but don't fallback or something. Spring will take care of this.
//                logger.error("Could not marshall Exception type", e);
//            }
//

        }
        return false; // Exception not handled.
    }
}
