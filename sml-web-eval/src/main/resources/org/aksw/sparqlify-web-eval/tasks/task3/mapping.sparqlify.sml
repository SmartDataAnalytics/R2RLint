Prefix xsd:<http://www.w3.org/2001/XMLSchema#>
Prefix ex:<http://example.org/vocab/>

Prefix country:<http://downlode.org/rdf/iso-3166/countries#>


Create View links As
  Construct {
      ?s a ex:Link .
      ?s ex:created ?d . 
  }
  With
      ?s = uri(?x)
      ?d = plainLiteral(?date)
  From
    [[SELECT MD5(s || p || o) As x FROM links]]

