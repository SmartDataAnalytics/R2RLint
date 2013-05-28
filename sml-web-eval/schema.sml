Prefix r: <http://example.org/resource/>
Prefix o: <http://example.org/ontology/>

Create View Item As
  Construct {
    ?s
      a o:Item ;
      rdfs:label ?l .
  }
  With
    ?s = uri(r:item, ?id)
    ?l = plainLiteral(?text, 'en')
  From
    item


Create View Options As
  Construct {
    ?s
      a o:Item .
  }
  With
    ?s = uri(r:item, )
  From
    item

