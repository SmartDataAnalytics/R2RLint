Prefix xsd:<http://www.w3.org/2001/XMLSchema#>
Prefix foaf:<http://xmlns.com/foaf/0.1/>
Prefix r:<http://example.org/resource/>
Prefix v:<http://example.org/vocab/>

Prefix country:<http://downlode.org/rdf/iso-3166/countries#>


Create View people As
  Construct {
    ?s
      a foaf:Person     ;
      foaf:firstName   ?fn ;
      foaf:givenName   ?ln ;
      v:age         ?a  ;
      foaf:mbox     ?m  ;
      v:country  ?c  .
  }
  With
    ?s  = uri(concat(r:person, ?id))
    ?fn = plainLiteral(?first_name)
    ?ln = plainLiteral(?last_name)
    ?a  = typedLiteral(?age, xsd:integer)
    ?m  = plainLiteral(?mbox)
    ?c  = uri(concat('http://downlode.org/rdf/iso-3166/countries#', ?country))
  From
    person

