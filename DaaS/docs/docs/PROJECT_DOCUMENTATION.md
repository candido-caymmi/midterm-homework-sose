# DaaS Professional Documentation

# Ethically-Aware Tourism Recommendation System

## 1. Purpose of the DaaS Component

The Data as a Service component exposes tourism information as reusable REST services. Instead of allowing the client to access the RDF dataset directly, the DaaS provides controlled REST endpoints that internally execute SPARQL queries over the RDF dataset.

The DaaS answers questions such as:

- Which places are accessible?
- Which places are sustainable?
- Which places are located in a specific city?
- Which places belong to a specific tourism category?
- Which places should be recommended based on multiple data conditions?

## 2. Architecture Design

```text
+-----------------------------+
| Client / Browser / Postman  |
+-------------+---------------+
              |
              | HTTP GET request
              v
+-----------------------------+
| Apache Tomcat               |
+-------------+---------------+
              |
              | /rest/* servlet mapping
              v
+-----------------------------+
| Apache CXF                  |
+-------------+---------------+
              |
              | JAX-RS routing
              v
+-----------------------------+
| TourismController           |
+-------------+---------------+
              |
              | Java method call
              v
+-----------------------------+
| TourismServiceImpl          |
+-------------+---------------+
              |
              | Loads model and delegates query
              v
+-----------------------------+
| SPARQLExecutor              |
+-------------+---------------+
              |
              | SPARQL over Jena model
              v
+-----------------------------+
| tourism.rdf                 |
+-------------+---------------+
              |
              | RDF query results
              v
+-----------------------------+
| JSON Response               |
+-----------------------------+
```

## 3. Architectural Responsibilities

| Component | Responsibility |
|---|---|
| Client | Sends REST requests and receives JSON |
| Apache Tomcat | Hosts the web application |
| Apache CXF | Provides the REST/JAX-RS service runtime |
| TourismController | Defines REST endpoints |
| TourismServiceImpl | Coordinates application logic |
| RDFLoader | Loads `tourism.rdf` into an Apache Jena model |
| SPARQLExecutor | Executes SPARQL queries |
| TourismPlace | Represents JSON response data |
| tourism.rdf | Stores tourism RDF data |

## 4. Dataset Description

The dataset is stored in:

```text
src/main/resources/datasets/tourism.rdf
```

The dataset is represented in RDF/XML format. It contains selected tourism attractions represented as RDF resources.

Each attraction has a local URI such as:

```text
http://odws.univaq.it/tourism/1
```

Each local tourism resource is linked to an external Wikidata resource using:

```text
schema:sameAs
```

This follows Linked Data principles and allows the local dataset to reference external semantic resources.

## 5. Main RDF Classes and Relations

| Element | Meaning |
|---|---|
| `schema:TouristAttraction` | Main RDF class for tourism attractions |
| `dbo:name` | Name of the tourism place |
| `dbo:location` | DBpedia resource location |
| `schema:category` | Tourism category |
| `schema:sameAs` | External linked-data reference |
| `tourism:accessible` | Accessibility status |
| `tourism:sustainability` | Sustainability level |
| `tourism:riskLevel` | Risk classification |

## 6. REST API Documentation

Base URL:

```text
http://localhost:8080/tourism-daas/rest
```

| Endpoint | Method | Description |
|---|---|---|
| `/tourism/{id}` | GET | Returns one place by ID |
| `/tourism/accessible` | GET | Returns accessible places |
| `/tourism/sustainable` | GET | Returns sustainable places |
| `/tourism/location/{location}` | GET | Returns places by location |
| `/tourism/category/{category}` | GET | Returns places by category |
| `/tourism/recommended` | GET | Returns recommended places using multiple conditions |

## 7. Important SPARQL Query

The recommendation endpoint combines multiple conditions:

```sparql
FILTER(
    ?accessible = true &&
    ?sustainability = "high" &&
    ?riskLevel = "low"
)
```

This query supports a data-based recommendation decision by selecting places that are accessible, sustainable, and low risk.

## 8. DaaS Quality Evaluation

The DaaS is technically strong because:

1. It separates REST logic from data logic.
2. It uses RDF/XML for semantic data representation.
3. It queries data through SPARQL using Apache Jena.
4. It exposes reusable REST endpoints through Apache CXF.
5. It returns JSON responses suitable for clients and EaaS integration.
6. It includes a recommendation endpoint with multiple SPARQL conditions.

## 9. Limitations

The current DaaS has some limitations:

- The RDF dataset is local and controlled, not dynamically queried from a remote SPARQL endpoint.
- Accessibility, sustainability, and risk values are manually defined for demonstration.
- The recommendation logic is rule-based.
- Ethical evaluation is not yet implemented in the DaaS layer.
- Production-level authentication and authorization are not included.

## 10. Relationship with EaaS

The DaaS provides data and recommendation candidates. The EaaS will evaluate whether the recommendation is ethically acceptable.

DaaS answers:

```text
What can be recommended from the dataset?
```

EaaS answers:

```text
Should this recommendation be accepted, revised, escalated, or rejected?
```
