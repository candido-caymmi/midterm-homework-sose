# DaaS REST Endpoint Documentation

## Base URL

```text
http://localhost:8080/tourism-daas/rest
```

## 1. Get Place by ID

```http
GET /tourism/{id}
```

Example:

```text
http://localhost:8080/tourism-daas/rest/tourism/1
```

Returns one tourism place using its local RDF resource ID.

## 2. Get Accessible Places

```http
GET /tourism/accessible
```

Example:

```text
http://localhost:8080/tourism-daas/rest/tourism/accessible
```

Returns all tourism places where `accessible = true`.

## 3. Get Sustainable Places

```http
GET /tourism/sustainable
```

Example:

```text
http://localhost:8080/tourism-daas/rest/tourism/sustainable
```

Returns all tourism places where `sustainability = high`.

## 4. Get Places by Location

```http
GET /tourism/location/{location}
```

Example:

```text
http://localhost:8080/tourism-daas/rest/tourism/location/Rome
```

Returns tourism places located in the selected location.

## 5. Get Places by Category

```http
GET /tourism/category/{category}
```

Example:

```text
http://localhost:8080/tourism-daas/rest/tourism/category/Historical%20Site
```

Returns tourism places belonging to the selected category.

## 6. Get Recommended Places

```http
GET /tourism/recommended
```

Example:

```text
http://localhost:8080/tourism-daas/rest/tourism/recommended
```

Returns tourism places satisfying:

- `accessible = true`
- `sustainability = high`
- `riskLevel = low`

This endpoint demonstrates a multi-condition SPARQL query.
