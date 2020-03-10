package ch.so.agi.gbdbs.webservice;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import ch.admin.geo.schemas.bj.tgbv.gbbasistypen._2.GrundstueckType;
import ch.admin.geo.schemas.bj.tgbv.gbbasistypen._2.LiegenschaftType;
import ch.admin.geo.schemas.bj.tgbv.gbbasistypen._2.SelbstaendigesDauerndesRechtType;
import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.BezugInhalt;
import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.GetParcelsByIdRequestType;
import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.GetParcelsByIdResponse;
import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.ObjectFactory;

import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.slf4j.Logger;

@Endpoint
public class WebServiceEndpoint {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_LIEGENSCHAFT = "dm01vch24lv95dliegenschaften_liegenschaft";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_SELBSTRECHT = "dm01vch24lv95dliegenschaften_selbstrecht";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_BERGWERK = "dm01vch24lv95dliegenschaften_bergwerk";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_GRUNDSTUECK = "dm01vch24lv95dliegenschaften_grundstueck";

    private static final String NAMESPACE_URI = "http://schemas.geo.admin.ch/BJ/TGBV/GBDBS/2.1";
    private static final QName _Liegenschaft_QNAME = new QName("http://schemas.geo.admin.ch/BJ/TGBV/GBBasisTypen/2.1", "Liegenschaft");
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetParcelsByIdRequest")
    @ResponsePayload
    public GetParcelsByIdResponse getParcelsById(@RequestPayload GetParcelsByIdRequestType request) throws Exception {
        ObjectFactory factory = new ObjectFactory();

        // TEST
        // Nicht klar wie das mit den Exceptions geht, eventuell: 
        // https://docs.oracle.com/cd/E24329_01/web.1211/e24965/faults.htm#WSADV639
        // https://docs.spring.io/spring-ws/docs/current/reference/#_code_soapfaultannotationexceptionresolver_code
        // Steckt die GBDBS Exception im detail-Element?
        /*
        if (!request.getBezugInhalt().equals(BezugInhalt.INDEX)) {
            logger.info("falscher BezugInhalt");
            ch.admin.geo.schemas.bj.tgbv.gbdbs._2.Exception exception = factory.createException();
            exception.setMessage("falscher BezugInhalt");
            
            throw new Exception(); //IllegalArgumentException o.ä. -> Das ist momentan mit Exception() ganz doof!!! 
        }
        */
        
        GetParcelsByIdResponse response = factory.createGetParcelsByIdResponse();
        
        for (String id : request.getIds()) {
            logger.info(id.substring(0, 14));
            
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
            
            ch.admin.geo.schemas.bj.tgbv.gbdbs._2.GetParcelsByIdResponse.Grundstueck grundstueck = factory.createGetParcelsByIdResponseGrundstueck();
            
            LiegenschaftType lsType = new LiegenschaftType();
            lsType.setGrundbuchname("Grundbuchname");
            lsType.setId(UUID.randomUUID().toString());
            //ls.setNummer("nummer");
            setNummer(lsType);
            
//            ls.getBodenbedeckungs()
            
            
            SelbstaendigesDauerndesRechtType sdr = new SelbstaendigesDauerndesRechtType();
            sdr.setGrundbuchname("grundbuchname");
            sdr.setNummer("fooo");
            
            
            GrundstueckType grundstueckType = new LiegenschaftType();
            
            if (grundstueckType instanceof LiegenschaftType) {
                logger.info("fubar");
            }
 
            
            
            
            JAXBElement<LiegenschaftType> jaxbLsType = new JAXBElement<LiegenschaftType>(_Liegenschaft_QNAME, LiegenschaftType.class, lsType);
            
            
            
            grundstueck.setGrundstueck(jaxbLsType);
            
            response.getGrundstuecks().add(grundstueck);    
       }

        return response;
    }

    private void setNummer(GrundstueckType grundstueckType) {
        grundstueckType.setNummer("nummer nummer");
    }
    
    /*
     * 
SELECT 
    ST_Area(ST_Union(geom)) AS flaeche, art
FROM 
(
    SELECT 
        (ST_Dump(ST_CollectionExtract(ST_Intersection(l.geometrie, b.geometrie), 3))).geom AS geom, b.art
    FROM 
        live.dm01vch24lv95dliegenschaften_grundstueck AS g 
        LEFT JOIN (SELECT liegenschaft_von AS von, geometrie FROM live.dm01vch24lv95dliegenschaften_liegenschaft
         UNION ALL SELECT selbstrecht_von  AS von, geometrie FROM live.dm01vch24lv95dliegenschaften_selbstrecht
         UNION ALL SELECT bergwerk_von     AS von, geometrie FROM live.dm01vch24lv95dliegenschaften_bergwerk ) AS l ON l.von = g.t_id
        RIGHT JOIN live.dm01vch24lv95dbodenbedeckung_boflaeche AS b ON ST_Intersects(l.geometrie, b.geometrie) 
    WHERE 
        g.egris_egrid = 'CH955832730623'
) AS foo
WHERE 
    ST_IsValid(geom) IS TRUE 
AND 
    geom IS NOT NULL
GROUP BY 
    art
    
}
