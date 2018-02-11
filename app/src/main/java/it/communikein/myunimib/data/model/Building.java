package it.communikein.myunimib.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

@Entity(tableName = "buildings")
public class Building {

    @PrimaryKey @NonNull
    private String name;
    private String address;
    private String description;
    private double latitude;
    private double longitude;

    public Building(String name, String address, String description,
                    double latitude, double longitude) {
        this(name, address, description, new LatLng(latitude, longitude));
    }

    @Ignore
    public Building(String name, String address, String description, LatLng coordinates) {
        setName(name);
        setAddress(address);
        setDescription(description);
        setLatitude(coordinates.latitude);
        setLongitude(coordinates.longitude);
    }

    @Ignore
    public Building(String name, String address, LatLng coordinates) {
        setName(name);
        setAddress(address);
        setDescription("");
        setLatitude(coordinates.latitude);
        setLongitude(coordinates.longitude);
    }


    @NonNull public String getName() {
        return name;
    }

    private void setName(@NonNull String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    private void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    @Ignore
    public LatLng getCoordinates() {
        return new LatLng(getLatitude(), getLongitude());
    }

    @Ignore
    public void setCoordinates(LatLng coordinates) {
        setLatitude(coordinates.latitude);
        setLongitude(coordinates.longitude);
    }

    public double getLatitude() {
        return this.latitude;
    }

    private void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    private void setLongitude(double longitude) {
        this.longitude = longitude;
    }


    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof Building)) return false;

        Building building = (Building) obj;
        return building.getName().equals(this.getName());
    }

    public boolean displayEquals(Object obj) {
        Building building = (Building) obj;
        return building.getName().equals(this.getName()) &&
                building.getAddress().equals(this.getAddress()) &&
                building.getLatitude() == this.getLatitude() &&
                building.getLongitude() == this.getLongitude();
    }

}
