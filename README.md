# grundstuecksinformation-gbdbs-web-service
gbdbs-web-service


```
http://localhost:8080/ws/gbdbs.wsdl
```


```
ACTION="http://schemas.geo.admin.ch/BJ/TGBV/GBDBSSvc/2.1/GetParcelsById"
URL="http://localhost:8080/ws"
curl -H "Content-Type: text/xml; charset=utf-8" -H "SOAPAction:$ACTION" -d@request.xml $URL > response.xml && xmllint --format response.xml
```

## TODO:
- Fix schemaLocation in `http://localhost:8080/ws/gbdbs.wsdl`. Notfalls auf dem Filesystem rumschieben bis es passt.



