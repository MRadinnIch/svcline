package com.svcline.models;

import com.google.cloud.firestore.Firestore;
import com.svcline.prodline.ProductionLine;

public class Context {
    private ProductionLine productionLine;

    public Context(ProductionLine productionLine) {
        this.productionLine = productionLine;
    }

    public ProductionLine getProductionLine() {
        return productionLine;
    }

    public void setProductionLine(ProductionLine productionLine) {
        this.productionLine = productionLine;
    }
}
