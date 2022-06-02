package amata1219.like.handler;

import amata1219.like.define.HologramData;

import java.util.ArrayList;
import java.util.List;

public class HologramDataHandler {

    private List<HologramData> hologramDataList;

    public HologramDataHandler() {
        this.hologramDataList = new ArrayList<>();
    }

    public HologramDataHandler(List<HologramData> hologramDataList) {
        this.hologramDataList = hologramDataList;
    }

    public HologramData getHologramData(String name) {
        for (HologramData hologramData : hologramDataList) {
            if (hologramData.getName().equalsIgnoreCase(name)) return hologramData;
        }
        return null;
    }

    public List<HologramData> getHologramDataList() {
        return hologramDataList;
    }

}
