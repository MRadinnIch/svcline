package com.routler;

import com.svcline.prodline.ProductionLine;

public class RContext {
    private ProductionLine productionLine;

    public RContext(ProductionLine productionLine) {
        this.productionLine = productionLine;
    }

    public ProductionLine getProductionLine() {
        return productionLine;
    }

    public void setProductionLine(ProductionLine productionLine) {
        this.productionLine = productionLine;
    }
}
