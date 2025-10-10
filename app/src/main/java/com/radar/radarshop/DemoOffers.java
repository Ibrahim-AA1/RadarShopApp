package com.radar.radarshop;

import java.util.ArrayList;
import java.util.List;

public class DemoOffers {
    public static List<offer> list() {
        List<offer> list = new ArrayList<>();
        list.add(new offer("o1","Build your first mech keyboard","Starter kits from $79 • 15% off", R.drawable.product_placeholder));
        list.add(new offer("o2","PBT Keycaps – Fresh Drops","From $24 • 10% off", R.drawable.product_placeholder));
        list.add(new offer("o3","Linear Switches (35pc)","From $16 • 20% off", R.drawable.product_placeholder));
        list.add(new offer("o4","Desk Mats & Wrist Rests","From $12 • New arrivals", R.drawable.product_placeholder));
        list.add(new offer("o5","Coiled USB-C Cables","From $18 • Mix & Match", R.drawable.product_placeholder));
        return list;
    }
}
