# Ethically-Aware Tourism Recommendation System

## Data as a Service Component

This project implements the Data as a Service component of an ethically-aware tourism recommendation system. The DaaS exposes tourism data through reusable REST APIs. The tourism data is represented as an RDF/XML dataset and queried using SPARQL through Apache Jena. The REST API is implemented using Apache CXF and deployed on Apache Tomcat.

## Current DaaS Standard

The DaaS component is implemented using a service-oriented architecture with clear separation between REST access, service logic, RDF loading, SPARQL querying, and data representation.

## Technology Stack

| Layer | Technology |
|---|---|
| REST framework | Apache CXF |
| RDF framework | Apache Jena |
| Query language | SPARQL |
| Dataset format | RDF/XML |
| Output format | JSON |
| Server | Apache Tomcat |
| Build tool | Maven |
| Language | Java |

## Architecture

```text
Client / Browser / Postman
        ↓
Apache Tomcat
        ↓
Apache CXF REST Servlet
        ↓
TourismController
        ↓
TourismServiceImpl
        ↓
SPARQLExecutor
        ↓
Apache Jena Model
        ↓
tourism.rdf
        ↓
JSON Response
```

## Base URL

```text
http://localhost:8080/tourism-daas/rest
```

## Available Endpoints

| No. | Endpoint | Description |
|---|---|---|
| 1 | `GET /tourism/{id}` | Returns one tourism place by ID |
| 2 | `GET /tourism/accessible` | Returns accessible tourism places |
| 3 | `GET /tourism/sustainable` | Returns highly sustainable tourism places |
| 4 | `GET /tourism/location/{location}` | Returns places by location |
| 5 | `GET /tourism/category/{category}` | Returns places by category |
| 6 | `GET /tourism/recommended` | Returns recommended places using multiple SPARQL conditions |

## Example URLs

```text
http://localhost:8080/tourism-daas/rest/tourism/1
http://localhost:8080/tourism-daas/rest/tourism/accessible
http://localhost:8080/tourism-daas/rest/tourism/sustainable
http://localhost:8080/tourism-daas/rest/tourism/location/Rome
http://localhost:8080/tourism-daas/rest/tourism/category/Historical%20Site
http://localhost:8080/tourism-daas/rest/tourism/recommended
```

## Build and Run

### Prerequisites

Install:

- JDK
- Maven
- Apache Tomcat
- Apache CXF
- Eclipse IDE for Enterprise Java Developers

### Build

```bash
mvn clean package
```

### Run

Deploy the generated WAR file to Tomcat or run the project from Eclipse using:

```text
Run As → Run on Server
```

Then open:

```text
http://localhost:8080/tourism-daas/
```

## DaaS Completion Notes

This DaaS satisfies the required technical aspects: RDF dataset, SPARQL queries, REST endpoints, JSON output, at least five endpoints, and one multi-condition recommendation query.

The next project phase is Ethics as a Service, where the recommendations produced by DaaS will be evaluated using explicit policies, risk assessment, explanation, and audit traces.
