package com.flipkart.csv_google_form_app.constants;

import lombok.Getter;

@Getter
public enum FormInputIDs {
    RC_NAME("entry.1392263175"),
    WSN("entry.1966213075"),
    PV_REASON("entry.350524584"),
    CONSIGNMENT_NO("entry.831106542"),
    CASPER("entry.730315831"),
    PV_REMARK("entry.1781837085");
    
    private final String inputID;
    
    FormInputIDs(String value) {
        this.inputID = value;
    }

}
