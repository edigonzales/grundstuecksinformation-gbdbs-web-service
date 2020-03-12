package ch.so.agi.gbdbs.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ch.admin.geo.schemas.bj.tgbv.gbbasistypen._2.BBArt;
import ch.admin.geo.schemas.bj.tgbv.gbbasistypen._2.Bodenbedeckung;
import ch.admin.geo.schemas.bj.tgbv.gbbasistypen._2.Extensions;
import ch.admin.geo.schemas.bj.tgbv.gbbasistypen._2.Flurname;
import ch.admin.geo.schemas.bj.tgbv.gbbasistypen._2.GBAmt;
import ch.admin.geo.schemas.bj.tgbv.gbbasistypen._2.GrundstueckType;
import ch.admin.geo.schemas.bj.tgbv.gbbasistypen._2.InhaltGrundstueckType;
import ch.admin.geo.schemas.bj.tgbv.gbbasistypen._2.InhaltLiegenschaftType;
import ch.admin.geo.schemas.bj.tgbv.gbbasistypen._2.LiegenschaftType;
import ch.admin.geo.schemas.bj.tgbv.gbbasistypen._2.SelbstaendigesDauerndesRechtType;
import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.BezugInhalt;
import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.GetParcelsByIdRequestType;
import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.GetParcelsByIdResponse;
import ch.admin.geo.schemas.bj.tgbv.gbdbs._2.GetParcelsByIdResponse.Grundstueck;
import ch.ech.xmlns.ech_0007._6.CantonAbbreviationType;
import ch.ech.xmlns.ech_0007._6.Gemeinde;
import ch.ech.xmlns.ech_0010._6.OrganisationMailAddressInfoType;
import ch.ech.xmlns.ech_0010._6.OrganisationMailAdress;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.dom.DOMResult;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.slf4j.Logger;

@Endpoint
public class WebServiceEndpoint {
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_LIEGENSCHAFT = "dm01vch24lv95dliegenschaften_liegenschaft";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_SELBSTRECHT = "dm01vch24lv95dliegenschaften_selbstrecht";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_BERGWERK = "dm01vch24lv95dliegenschaften_bergwerk";
    private static final String TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_GRUNDSTUECK = "dm01vch24lv95dliegenschaften_grundstueck";
    private static final String TABLE_DM01VCH24LV95DGEMEINDEGRENZEN_GEMEINDE = "dm01vch24lv95dgemeindegrenzen_gemeinde";  
    private static final String TABLE_DM01VCH24LV95DBODENBEDECKUNG_BOFLAECHE  = "dm01vch24lv95dbodenbedeckung_boflaeche"; 
    private static final String TABLE_DM01VCH24LV95NOMENKLATUR_FLURNAME = "dm01vch24lv95dnomenklatur_flurname";
    private static final String TABLE_SO_G_V_0180822GRUNDBUCHKREISE_GRUNDBUCHKREIS = "so_g_v_0180822grundbuchkreise_grundbuchkreis";

    private static final String NAMESPACE_URI = "http://schemas.geo.admin.ch/BJ/TGBV/GBDBS/2.1";
    
    @Autowired
    Jaxb2Marshaller marshaller;

    @Autowired
    JdbcTemplate jdbcTemplate;
    
    @Autowired
    NamedParameterJdbcTemplate jdbcParamTemplate; 

    @Value("${gbdbs.dbschema}")
    private String dbschema;
    
    ch.admin.geo.schemas.bj.tgbv.gbdbs._2.ObjectFactory gbdbsFactory = new ch.admin.geo.schemas.bj.tgbv.gbdbs._2.ObjectFactory();
    ch.admin.geo.schemas.bj.tgbv.gbbasistypen._2.ObjectFactory gbbasistypenFactory = new ch.admin.geo.schemas.bj.tgbv.gbbasistypen._2.ObjectFactory();

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetParcelsByIdRequest")
    @ResponsePayload
    public GetParcelsByIdResponse getParcelsById(@RequestPayload GetParcelsByIdRequestType request) throws Exception {

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
        
        GetParcelsByIdResponse response = gbdbsFactory.createGetParcelsByIdResponse();
        
        for (String id : request.getIds()) {
            // Support only egrid at the moment.
            String egrid;
            try {
                egrid = id.split(":")[0].substring(0, 14);
            } catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
                throw new IllegalArgumentException("egrid was not found in request id: " + id);
            }
            
            SimpleGrundstueck parcel = getSimpleGrundstueck(egrid);
            GrundstueckType grundstueckType = null;
            JAXBElement<? extends GrundstueckType> grundstueckTypeElement = null;

            if (parcel.getArt().equalsIgnoreCase("Liegenschaft")) {
                grundstueckType = gbbasistypenFactory.createLiegenschaftType();
                               
                InhaltLiegenschaftType inhaltType = gbbasistypenFactory.createInhaltLiegenschaftType();
                inhaltType.setFlaechenmass(new BigDecimal(parcel.getFlaechenmass()));
                
                setBodenbedeckung((LiegenschaftType) grundstueckType, parcel.getGeometrie());

                JAXBElement<InhaltLiegenschaftType> inhaltLiegenschaftTypeElement = gbbasistypenFactory.createInhaltLiegenschaft(inhaltType);
                grundstueckType.getInhaltGrundstuecks().add(inhaltLiegenschaftTypeElement);
                
                grundstueckTypeElement = gbbasistypenFactory.createLiegenschaft((LiegenschaftType) grundstueckType);
            } else if (parcel.getArt().contains("SelbstRecht")) {
                //<GewoehnlichesSDR>
                
                // DauerndesRechtArt:
                //value="Baurecht"/>
                //value="Quellenrecht"/>
                //value="Konzession"/>
                //value="weitere"/>

            } else if (parcel.getArt().equalsIgnoreCase("Bergwerk")) {
                // BergwerkType?
            }
            
            // common attributes for types
            grundstueckType.setId(UUID.randomUUID().toString());
            // Serialisierte Form der GrundstueckNummer. EGRID:Nummer:NummerZusatz:SubKreis:Los
            grundstueckType.setNummer(egrid+":"+parcel.getNummer()+"::"+String.valueOf(Optional.ofNullable(parcel.getGbSubKreisNummer()).orElse(""))+":");
            grundstueckType.setGrundbuchname(parcel.getGbSubKreis());

            Gemeinde gemeinde = new Gemeinde();
            gemeinde.setMunicipalityId(parcel.getBfsnr());
            gemeinde.setMunicipalityName(parcel.getGemeinde());
            gemeinde.setCantonAbbreviation(CantonAbbreviationType.fromValue(parcel.getKanton()));
            grundstueckType.setGemeinde(gemeinde);
            
            setFlurnamen(grundstueckType, parcel.getGeometrie());
            
            setGebaeude(grundstueckType, parcel.getGeometrie());
            
            // In eigenes extensions.xsd auslagern. (?)
            /*
            OrganisationMailAdress nfgeometerAddress = new OrganisationMailAdress();
            OrganisationMailAddressInfoType nfgeometerAddressInfoType = new OrganisationMailAddressInfoType();
            nfgeometerAddressInfoType.setOrganisationName("Firmenname");
            nfgeometerAddress.setOrganisation(nfgeometerAddressInfoType);
            
            DOMResult res = new DOMResult();
            marshaller.marshal(nfgeometerAddress, res);
            Document doc = (Document) res.getNode();
            
            Extensions extensions = new Extensions();
            extensions.getAnies().add(doc.getDocumentElement());
            grundstueckType.setExtensions(extensions);
            */
            
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
            
            Grundstueck grundstueck = gbdbsFactory.createGetParcelsByIdResponseGrundstueck();
            
//            SelbstaendigesDauerndesRechtType sdr = new SelbstaendigesDauerndesRechtType();
//            sdr.setGrundbuchname("grundbuchname");
//            sdr.setNummer("fooo");
                        
//            if (grundstueckType instanceof LiegenschaftType) {
//                logger.info("fubar");
//            }

            grundstueck.setGrundstueck(grundstueckTypeElement);
            response.getGrundstuecks().add(grundstueck);    
       }
        return response;
    }
    
    // TODO
    private void setNfGeometerAddress() {
        
    }
    
    // TODO
    private void setGrundbuchamtAddress() {
        
    }
    
    private void setGebaeude(GrundstueckType grundstueckType, Geometry geometry) {
        WKBWriter geomEncoder = new WKBWriter(2, ByteOrderValues.BIG_ENDIAN);
        byte wkbGeometry[] = geomEncoder.write(geometry);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("geom", wkbGeometry);

    }
    
    private void setFlurnamen(GrundstueckType grundstueckType, Geometry geometry) {
        WKBWriter geomEncoder = new WKBWriter(2, ByteOrderValues.BIG_ENDIAN);
        byte wkbGeometry[] = geomEncoder.write(geometry);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("geom", wkbGeometry);
        
        List<Flurname> flurnameList = jdbcParamTemplate.query("SELECT aname as flurname \n" + 
                "FROM (SELECT (ST_Dump(ST_CollectionExtract(ST_Intersection(ST_GeomFromWKB(:geom,2056), f.geometrie), 3))).geom AS geom, f.aname FROM "+getSchema()+"."+TABLE_DM01VCH24LV95NOMENKLATUR_FLURNAME+" AS f WHERE ST_Intersects(ST_GeomFromWKB(:geom,2056), f.geometrie)) AS foo \n" + 
                "WHERE ST_IsValid(geom) IS TRUE AND geom IS NOT NULL GROUP BY aname", parameters, new RowMapper<Flurname>() {

            @Override
            public Flurname mapRow(ResultSet rs, int rowNum) throws SQLException {
                String name = rs.getString("flurname");
                
                Flurname flurname = gbbasistypenFactory.createFlurname();
                flurname.setName(name);
                
                return flurname;
            } 
        });

        grundstueckType.getFlurnames().addAll(flurnameList);
    }
    
    private void setBodenbedeckung(LiegenschaftType liegenschaftType, Geometry geometry) {
        WKBWriter geomEncoder = new WKBWriter(2, ByteOrderValues.BIG_ENDIAN);
        byte wkbGeometry[] = geomEncoder.write(geometry);
        
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("geom", wkbGeometry);

        List<Bodenbedeckung> bbList = jdbcParamTemplate.query("SELECT ST_Area(ST_Union(geom)) AS flaechenmass, art \n" + 
                "FROM (SELECT (ST_Dump(ST_CollectionExtract(ST_Intersection(ST_GeomFromWKB(:geom,2056), b.geometrie), 3))).geom AS geom, b.art FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DBODENBEDECKUNG_BOFLAECHE+" AS b WHERE ST_Intersects(ST_GeomFromWKB(:geom,2056), b.geometrie)) AS foo \n" + 
                "WHERE ST_IsValid(geom) IS TRUE AND geom IS NOT NULL GROUP BY art", parameters, new RowMapper<Bodenbedeckung>() {

            @Override
            public Bodenbedeckung mapRow(ResultSet rs, int rowNum) throws SQLException {
                double flaechenmass = rs.getDouble("flaechenmass");
                String art = rs.getString("art");
                
                Bodenbedeckung bb = gbbasistypenFactory.createBodenbedeckung();
                bb.setArt(BBArt.fromValue(art));
                bb.setArtBezeichnung(art);
                bb.setFlaechenmass(new BigDecimal(Math.round(10000*flaechenmass)).movePointLeft(4));
//                bb.setFlaechenmass(new BigDecimal(flaechenmass));
                
                return bb;
            } 
        });
        
        liegenschaftType.getBodenbedeckungs().addAll(bbList);
    }

    private SimpleGrundstueck getSimpleGrundstueck(String egrid) {
        // CH955832730623 = Liegenschaft
        // CH707406053288 = SelbstRecht.Baurecht
        // CH527732831247 = SelbstRecht.Quellenrecht
        // CH367883126943 = SelbstRecht.Konzessionsrecht
        // CH327840831216 = SelbstRecht.weitere
        // CH487706867746 = Bergwerk
        // CH310663327779 = mehrere Flurnamen
        // CH493273420604 = Grenchen GB-Nr. 4000

        // Mehr oder weniger copy/paste from oereb-web-service.
        // Ist ziemlich smart gemacht, da es z.B. auch "Multipolygon"-Liegenschaften
        // berücksichtigt, die es bei uns (SO) nicht gibt.
        // Und hier die Geometrie zu holen und mit dieser weiterzuarbeiten, ist 
        // auch eine gute Lösung. Sonst müsste man immer wieder eine Subquery o.ä.
        // machen.
        
        PrecisionModel precisionModel = new PrecisionModel(1000.0);
        GeometryFactory geomFactory = new GeometryFactory(precisionModel);
        
        List<SimpleGrundstueck> gslist = jdbcTemplate.query(
                "SELECT ST_AsBinary(l.geometrie) as l_geometrie,ST_AsBinary(s.geometrie) as s_geometrie,ST_AsBinary(b.geometrie) as b_geometrie,nummer,nbident,art,gesamteflaechenmass,l.flaechenmass as l_flaechenmass,s.flaechenmass as s_flaechenmass,b.flaechenmass as b_flaechenmass FROM "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_GRUNDSTUECK+" g"
                        +" LEFT JOIN "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_LIEGENSCHAFT+" l ON g.t_id=l.liegenschaft_von "
                        +" LEFT JOIN "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_SELBSTRECHT+" s ON g.t_id=s.selbstrecht_von"
                        +" LEFT JOIN "+getSchema()+"."+TABLE_DM01VCH24LV95DLIEGENSCHAFTEN_BERGWERK+" b ON g.t_id=b.bergwerk_von"
                        +" WHERE g.egris_egrid=?", new RowMapper<SimpleGrundstueck>() {
                    WKBReader decoder=new WKBReader(geomFactory);
                    
                    @Override
                    public SimpleGrundstueck mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Geometry polygon=null;
                        byte l_geometrie[]=rs.getBytes("l_geometrie");
                        byte s_geometrie[]=rs.getBytes("s_geometrie");
                        byte b_geometrie[]=rs.getBytes("b_geometrie");
                        try {
                            if(l_geometrie!=null) {
                                polygon=decoder.read(l_geometrie);
                            }else if(s_geometrie!=null) {
                                polygon=decoder.read(s_geometrie);
                            }else if(b_geometrie!=null) {
                                polygon=decoder.read(b_geometrie);
                            }else {
                                throw new IllegalStateException("no geometrie");
                            }
                            if(polygon==null || polygon.isEmpty()) {
                                return null;
                            }
                        } catch (ParseException e) {
                            throw new IllegalStateException(e);
                        }
                        SimpleGrundstueck ret=new SimpleGrundstueck();
                        ret.setGeometrie(polygon);
                        ret.setEgrid(egrid);
                        ret.setNbident(rs.getString("nbident"));
                        ret.setNummer(rs.getString("nummer"));
                        ret.setArt(rs.getString("art"));
                        int f = rs.getInt("gesamteflaechenmass");
                        if(rs.wasNull()) {
                            if (l_geometrie!=null) {
                                f=rs.getInt("l_flaechenmass");
                            } else if(s_geometrie!=null) {
                                f=rs.getInt("s_flaechenmass");
                            } else if(b_geometrie!=null) {
                                f=rs.getInt("b_flaechenmass");
                            } else {
                                throw new IllegalStateException("no geometrie");
                            }
                        }
                        ret.setFlaechenmass(f);
                        ret.setKanton(ret.getNbident().substring(0,2).toUpperCase());
                        return ret;
                    }
                }, egrid);
        
        if(gslist==null || gslist.isEmpty()) {
            return null;
        }
        Polygon polygons[] = new Polygon[gslist.size()];
        int i=0;
        for (SimpleGrundstueck gs : gslist) {
            polygons[i++] = (Polygon)gs.getGeometrie();
        }
        Geometry multiPolygon=geomFactory.createMultiPolygon(polygons);
        SimpleGrundstueck gs = gslist.get(0);
        gs.setGeometrie(multiPolygon);

        // Grundbuchkreis
        try {
            Map<String,Object> gbKreis = jdbcTemplate.queryForMap(
                    "SELECT gb.aname,gb.grundbuchkreis_bfsnr,gb.bfsnr,gem.aname AS gemeindename FROM "+getSchema()+"."+TABLE_SO_G_V_0180822GRUNDBUCHKREISE_GRUNDBUCHKREIS+" AS gb" +
                    " LEFT JOIN "+getSchema()+"."+TABLE_DM01VCH24LV95DGEMEINDEGRENZEN_GEMEINDE+" AS gem ON gem.bfsnr = gb.bfsnr" +
                    " WHERE nbident=?", gs.getNbident());
            gs.setGbSubKreis((String) gbKreis.get("aname"));
            gs.setGbSubKreisNummer(String.valueOf(gbKreis.get("grundbuchkreis_bfsnr")));
            gs.setBfsnr((Integer) gbKreis.get("bfsnr"));
            gs.setGemeinde((String) gbKreis.get("gemeindename"));
        } catch(EmptyResultDataAccessException ex) {
            logger.warn("no gbkreis for nbident {}",gs.getNbident());
        }
        return gs;
    }
 
    private String getSchema() {
        return dbschema!=null?dbschema:"xgbdbs";
    }
}