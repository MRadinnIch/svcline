package com.svcline.models.clocker;

import com.svcline.models.clocker.db.DbClockerFacade;
import com.svcline.prodline.ProductionLine;

import java.util.concurrent.ExecutionException;

public class ClockerService {
    private ProductionLine productionLine;

    public ClockerService() {
    }

    public ClockerService(ProductionLine productionLine) {
        this.productionLine = productionLine;
    }

    public void setTime(String itemId, String stationId, Operation operation) throws ExecutionException, InterruptedException {
        DbClockerFacade dbClockerFacade = new DbClockerFacade(this.productionLine.getFirestore(), this.productionLine.getProps().isLiveEnv());

        Clocker clocker = dbClockerFacade.getFor(itemId);
        if (clocker == null) {
            clocker = new Clocker(itemId, this.productionLine.getProductLineConfiguration(),
                                          this.productionLine.getProps().isTimekeeping());
        }
        clocker.addTime(stationId, operation);

        dbClockerFacade.set(clocker);
    }
}
