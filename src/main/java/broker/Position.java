package broker;

public interface Position<I extends Instrument> {

    I getInstrument();

    int getShares();
}
