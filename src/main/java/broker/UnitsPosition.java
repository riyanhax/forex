package broker;

class UnitsPosition implements Position {

    private final Instrument instrument;
    private final int units;

    public UnitsPosition(Instrument instrument, int units) {
        this.instrument = instrument;
        this.units = units;
    }

    @Override
    public Instrument getInstrument() {
        return instrument;
    }

    @Override
    public int getShares() {
        return units;
    }
}
