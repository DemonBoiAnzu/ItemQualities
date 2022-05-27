package com.abraxas.itemqualities.api;

import com.abraxas.itemqualities.api.quality.ItemQuality;

import java.util.Comparator;

public class ItemQualityComparator implements Comparator<ItemQuality> {
    @Override
    public int compare(ItemQuality o1, ItemQuality o2) {
        return o1.tier - o2.tier;
    }
}
