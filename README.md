# grundstuecksinformation-gbdbs-web-service
gbdbs-web-service


```
http://localhost:8080/ws/gbdbs.wsdl
```

Use data from file:
```
ACTION="http://schemas.geo.admin.ch/BJ/TGBV/GBDBSSvc/2.1/GetParcelsById"
URL="http://localhost:8080/ws"
curl -H "Content-Type: text/xml; charset=utf-8" -H "SOAPAction:$ACTION" -d@request.xml $URL > response.xml && xmllint --format response.xml
```

Use inline XML data:
```
curl <<-EOF -fsSL -H "content-type: text/xml" -d @- http://localhost:8080/ws > response.xml && xmllint --format response.xml

<?xml version="1.0"?>
  <soapenv:Envelope xmlns:ns="http://schemas.geo.admin.ch/BJ/TGBV/GBDBS/2.1" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
  <soapenv:Header/>
  <soapenv:Body>
    <ns:GetParcelsByIdRequest>
      <ns:version>2.1</ns:version>
      <ns:transactionId>RAUM-14922-1</ns:transactionId>
      <ns:BezugInhalt>IndexMitEigentum</ns:BezugInhalt>
      <ns:includeHistory>true</ns:includeHistory>
      <ns:Id>CH240632707339::::</ns:Id>
    </ns:GetParcelsByIdRequest>
  </soapenv:Body>
</soapenv:Envelope>

EOF
```


## TODO:
- Fix schemaLocation in `http://localhost:8080/ws/gbdbs.wsdl`. Notfalls auf dem Filesystem rumschieben bis es passt.
- Exception handling: Stimmt die eingeschlagene Richtung?



