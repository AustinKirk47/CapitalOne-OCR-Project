package com.capitalone.creditocr.model.ingest;

public class LetterData {

    //Letter text
    private String text;
    //Date of the letter
    private String letterDate = "";
    //First line of address (Ex: 1234 Main St.)
    private String streetAddress = "";
    //Customer's 17-digit Capital One account number
    private String acctNum = "";
    //Social Security Number (Ex: 123-45-6789)
    private String SSN = "";

    public LetterData(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLetterDate() {
        return letterDate;
    }

    public void setLetterDate(String letterDate) {
        this.letterDate = letterDate;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getAcctNum() {
        return acctNum;
    }

    public void setAcctNum(String acctNum) {
        this.acctNum = acctNum;
    }

    public String getSSN() {
        return SSN;
    }

    public void setSSN(String SSN) {
        this.SSN = SSN;
    }
}
