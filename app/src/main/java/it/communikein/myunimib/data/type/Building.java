package it.communikein.myunimib.data.type;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


@SuppressWarnings("unused")
public class Building {
    private String name;
    private LatLng coordinates;
    private MarkerOptions marker;

    public Building(String name, LatLng coordinates) {
        this.name = name;
        this.coordinates = coordinates;
    }

    public void setup(){
        marker = new MarkerOptions().title(name.toUpperCase())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .position(coordinates);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public MarkerOptions getMarker(){
        return marker;
    }

}