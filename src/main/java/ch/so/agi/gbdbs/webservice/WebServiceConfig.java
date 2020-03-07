package ch.so.agi.gbdbs.webservice;

import java.util.Properties;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {
    
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        //servlet.setTransformWsdlLocations(true);

        return new ServletRegistrationBean<MessageDispatcherServlet>(servlet, "/ws/*");
    }

    @Bean(name="gbdbs")
    public Wsdl11Definition defaultWsdl11Definition() {
        SimpleWsdl11Definition wsdl11Definition = new SimpleWsdl11Definition();
        wsdl11Definition.setWsdl(new ClassPathResource("/xsd/GBDBS/2.1/gbdbs.wsdl"));

        return wsdl11Definition;
    }

//    @Bean
//    public SoapFaultMappingExceptionResolver exceptionResolver() {
//        SoapFaultMappingExceptionResolver exceptionResolver = new DetailSoapFaultDefinitionExceptionResolver();
//
//        SoapFaultDefinition faultDefinition = new SoapFaultDefinition();
//        faultDefinition.setFaultCode(SoapFaultDefinition.SERVER);
//        exceptionResolver.setDefaultFault(faultDefinition);
//
//        Properties errorMappings = new Properties();
//        errorMappings.setProperty(Exception.class.getName(), SoapFaultDefinition.SERVER.toString());
//        //errorMappings.setProperty(ServiceFaultException.class.getName(), SoapFaultDefinition.SERVER.toString());
//        exceptionResolver.setExceptionMappings(errorMappings);
//        exceptionResolver.setOrder(1); // TODO: needed? What does this actually?
//        return exceptionResolver;
//    }

    @Bean
    public Jaxb2Marshaller createMarshaller() {
        Jaxb2Marshaller marshaller=new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(ch.admin.geo.schemas.bj.tgbv.gbdbs._2.Exception.class);
        //marshaller.setPackagesToScan("ch.ehi.oereb.schemas");
        marshaller.setSupportJaxbElementClass(true);
        marshaller.setLazyInit(true);
        return marshaller;
    }
}
