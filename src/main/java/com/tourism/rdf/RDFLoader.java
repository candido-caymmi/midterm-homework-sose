package com.tourism.rdf;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.InputStream;

public class RDFLoader {

    public Model loadModel() {

        Model model = ModelFactory.createDefaultModel();

        InputStream inputStream =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream("datasets/tourism.rdf");

        if (inputStream == null) {
            throw new RuntimeException("RDF dataset not found: datasets/tourism.rdf");
        }

        model.read(inputStream, null, "RDF/XML");

        return model;
    }
}