package observers;

import types.World;

public interface PopulationObserver {

    void populationChange(PopulationObservable o, World world, int change);

}
