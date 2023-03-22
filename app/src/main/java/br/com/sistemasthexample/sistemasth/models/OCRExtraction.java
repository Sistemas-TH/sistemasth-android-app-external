package br.com.sistemasthexample.sistemasth.models;

import java.io.Serializable;

public class OCRExtraction implements Serializable {

    private static final long serialVersionUID = -6870955087899849044L;

    private String name;
    private String birthdate;
    private String mothersName;
    private String documentId;
    private String federalRevenueNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getMothersName() {
        return mothersName;
    }

    public void setMothersName(String mothersName) {
        this.mothersName = mothersName;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getFederalRevenueNumber() {
        return federalRevenueNumber;
    }

    public void setFederalRevenueNumber(String federalRevenueNumber) {
        this.federalRevenueNumber = federalRevenueNumber;
    }
}
