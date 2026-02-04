package com.flipkart.csv_google_form_app.constants;

import lombok.Getter;

@Getter
public enum FormInputIDs {

//    clone form input ids
//    WSN("entry.1541609902"),
//    BRAND_NAME("entry.567425352"),
//    SELLER_ID("entry.663915340"),
//    VERTICAL("entry.1226136032"),
//    REFINISHING_TASK("entry.897578502"),
//    PV_REMARK("entry.1265244198");

//    central form input ids
    WSN("entry.891680046"),
    BRAND_NAME("entry.1243252732"),
    SELLER_ID("entry.512679889"),
    VERTICAL("entry.1055426944"),
    REFINISHING_TASK("entry.1979312034"),
    PV_REMARK("entry.480183700");

    private final String inputID;
    
    FormInputIDs(String value) {
        this.inputID = value;
    }

}
