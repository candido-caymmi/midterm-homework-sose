# SPARQL Query Documentation

## Common Prefixes

```sparql
PREFIX tourism: <http://odws.univaq.it/tourism/>
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbr: <http://dbpedia.org/resource/>
PREFIX schema: <https://schema.org/>
PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
```

## Query by ID

```sparql
SELECT ?name ?location ?category ?sustainability ?accessible ?riskLevel ?sameAs
WHERE {
    tourism:1 rdf:type schema:TouristAttraction ;
              dbo:name ?name ;
              dbo:location ?location ;
              schema:category ?category ;
              schema:sameAs ?sameAs ;
              tourism:accessible ?accessible ;
              tourism:sustainability ?sustainability ;
              tourism:riskLevel ?riskLevel .
}
```

## Accessible Places Query

```sparql
SELECT ?name ?location ?category ?sustainability ?accessible ?riskLevel ?sameAs
WHERE {
    ?place rdf:type schema:TouristAttraction ;
           dbo:name ?name ;
           dbo:location ?location ;
           schema:category ?category ;
           schema:sameAs ?sameAs ;
           tourism:accessible ?accessible ;
           tourism:sustainability ?sustainability ;
           tourism:riskLevel ?riskLevel .

    FILTER(?accessible = true)
}
ORDER BY ?name
```

## Sustainable Places Query

```sparql
SELECT ?name ?location ?category ?sustainability ?accessible ?riskLevel ?sameAs
WHERE {
    ?place rdf:type schema:TouristAttraction ;
           dbo:name ?name ;
           dbo:location ?location ;
           schema:category ?category ;
           schema:sameAs ?sameAs ;
           tourism:accessible ?accessible ;
           tourism:sustainability ?sustainability ;
           tourism:riskLevel ?riskLevel .

    FILTER(?sustainability = "high")
}
ORDER BY ?name
```

## Location Query

```sparql
SELECT ?name ?location ?category ?sustainability ?accessible ?riskLevel ?sameAs
WHERE {
    ?place rdf:type schema:TouristAttraction ;
           dbo:name ?name ;
           dbo:location ?location ;
           schema:category ?category ;
           schema:sameAs ?sameAs ;
           tourism:accessible ?accessible ;
           tourism:sustainability ?sustainability ;
           tourism:riskLevel ?riskLevel .

    FILTER(?location = dbr:Rome)
}
ORDER BY ?name
```

## Category Query

```sparql
SELECT ?name ?location ?category ?sustainability ?accessible ?riskLevel ?sameAs
WHERE {
    ?place rdf:type schema:TouristAttraction ;
           dbo:name ?name ;
           dbo:location ?location ;
           schema:category ?category ;
           schema:sameAs ?sameAs ;
           tourism:accessible ?accessible ;
           tourism:sustainability ?sustainability ;
           tourism:riskLevel ?riskLevel .

    FILTER(?category = "Historical Site")
}
ORDER BY ?name
```

## Recommended Places Query

```sparql
SELECT ?name ?location ?category ?sustainability ?accessible ?riskLevel ?sameAs
WHERE {
    ?place rdf:type schema:TouristAttraction ;
           dbo:name ?name ;
           dbo:location ?location ;
           schema:category ?category ;
           schema:sameAs ?sameAs ;
           tourism:accessible ?accessible ;
           tourism:sustainability ?sustainability ;
           tourism:riskLevel ?riskLevel .

    FILTER(
        ?accessible = true &&
        ?sustainability = "high" &&
        ?riskLevel = "low"
    )
}
ORDER BY ?name
```

This is the most important DaaS query because it combines multiple conditions and supports recommendation logic.
