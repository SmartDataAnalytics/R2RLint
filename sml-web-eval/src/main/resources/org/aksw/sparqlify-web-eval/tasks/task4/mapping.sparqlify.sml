Prefix xsd:<http://www.w3.org/2001/XMLSchema#>
Prefix foaf:<http://xmlns.com/foaf/0.1/>
Prefix r:<http://example.org/resource/>
Prefix v:<http://example.org/vocab/>

Prefix country:<http://downlode.org/rdf/iso-3166/countries#>


Create View product As
  Construct {
    ?s
      a ex:Person ;
      ex:firstName ?fn .
  }
  With
    ?s  = uri(concat(r:person, ?id))
    ?fn = plainLiteral(?first_name)
  From
    person


