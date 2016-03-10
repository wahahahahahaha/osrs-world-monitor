package observers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import types.World;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class PopulationObservable extends Thread {

    private Map<Integer, World> previousWorlds = new HashMap<>();

    private boolean paused = false;

    private boolean changed = false;
    private Vector<PopulationObserver> obs;

    public PopulationObservable() {
        obs = new Vector<>();
    }

    public synchronized void addObserver(PopulationObserver o) {
        if (o == null)
            throw new NullPointerException();
        if (!obs.contains(o)) {
            obs.addElement(o);
        }
    }

    public synchronized void deleteObserver(PopulationObserver o) {
        obs.removeElement(o);
    }

    public void notifyObservers() {
        notifyObservers(null, 0);
    }

    public void notifyObservers(World world, int change) {

        Object[] arrLocal;

        synchronized (this) {
            if (!changed)
                return;
            arrLocal = obs.toArray();
            clearChanged();
        }

        for (int i = arrLocal.length - 1; i >= 0; i--)
            ((PopulationObserver) arrLocal[i]).populationChange(this, world, change);
    }

    public synchronized void deleteObservers() {
        obs.removeAllElements();
    }

    protected synchronized void setChanged() {
        changed = true;
    }

    protected synchronized void clearChanged() {
        changed = false;
    }

    public synchronized boolean hasChanged() {
        return changed;
    }

    public synchronized int countObservers() {
        return obs.size();
    }

    @Override
    public void run() {
        super.setName("Population Thread");

        previousWorlds = getWorlds();

        while (true) {
            if (paused) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            Map<Integer, World> currentWorlds = getWorlds();
            for (Integer world : currentWorlds.keySet()) {
                if (Math.abs(currentWorlds.get(world).getPlayers() - previousWorlds.get(world).getPlayers()) >= 13) {
                    setChanged();
                    notifyObservers(currentWorlds.get(world), (currentWorlds.get(world).getPlayers() - previousWorlds.get(world).getPlayers()));
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            previousWorlds = currentWorlds;
        }

    }

    private Map<Integer, World> getWorlds() {
        Map<Integer, World> map = new HashMap<>();
        Document document = null;
        try {
            document = Jsoup.connect("http://oldschool.runescape.com/slu").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (document != null) {
            Elements results = document.select("tr.server-list__row");
            for (Element result : results) {
                World w = World.valueOf(result.text());
                map.put(w.getWorldId(), w);
            }
        } else {
            map = previousWorlds;
        }
        return map;
    }

    public void pauseTracking() {
        paused = true;
    }

    public void resumeTracking() {
        paused = false;
        previousWorlds = getWorlds();
    }

}
