package ch.so.agi.gbdbs.webservice;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
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

    @Bean
    public Jaxb2Marshaller createMarshaller() {
        Jaxb2Marshaller marshaller=new Jaxb2Marshaller();
        //marshaller.setClassesToBeBound(ch.admin.geo.schemas.bj.tgbv.gbdbs._2.Exception.class,ch.ech.xmlns.ech_0010._6.OrganisationMailAdress.class);
        marshaller.setClassesToBeBound(ch.admin.geo.schemas.bj.tgbv.gbdbs._2.Exception.class, ch.ech.xmlns.ech_0010._6.OrganisationMailAddressType.class);
        marshaller.setSupportJaxbElementClass(true);
        marshaller.setLazyInit(true);
        return marshaller;
    }
}
