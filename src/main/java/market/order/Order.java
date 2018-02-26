package market.order;

public interface Order<INSTRUMENT> {

    INSTRUMENT getInstrument();

    int getUnits();
}
