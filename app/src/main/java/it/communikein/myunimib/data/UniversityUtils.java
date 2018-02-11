package it.communikein.myunimib.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import it.communikein.myunimib.data.model.Building;


@SuppressWarnings("unused")
@Singleton
public class UniversityUtils {

    private final List<Building> uniBuildings;

    @Inject
    public UniversityUtils() {
        uniBuildings = new ArrayList<>();
        uniBuildings.add(new Building(
                "U1", "Piazza della Scienza 1, Milano",
                new LatLng(45.513277, 9.211733)));
        uniBuildings.add(new Building(
                "U2", "Piazza della Scienza 3, Milano",
                new LatLng(45.513528, 9.210687)));
        uniBuildings.add(new Building(
                "U3", "Piazza della Scienza 2, Milano",
                new LatLng(45.513757, 9.212024)));
        uniBuildings.add(new Building(
                "U4", "Piazza della Scienza 4, Milano",
                new LatLng(45.514029, 9.210919)));
        uniBuildings.add(new Building(
                "U5", "Via Roberto Cozzi 55, Milano",
                new LatLng(45.512114, 9.212650)));
        uniBuildings.add(new Building(
                "U6", "Piazza dell'Ateneo Nuovo 1, Milano",
                new LatLng(45.518246, 9.213916)));
        uniBuildings.add(new Building(
                "U7", "Via Bicocca degli Arcimboldi 8, Milano",
                new LatLng(45.516980, 9.213160)));
        uniBuildings.add(new Building(
                "U8", "Via Cadore 48, Monza",
                new LatLng(45.603866, 9.258856)));
        uniBuildings.add(new Building(
                "U9", "Viale dell'Innovazione 10, Milano",
                new LatLng(45.511139, 9.211187)));
        uniBuildings.add(new Building(
                "U11", "Viale dell'Innovazione 4, Milano",
                new LatLng(45.510003, 9.210804)));
        uniBuildings.add(new Building(
                "U12", "Via Vizzola 5, Milano",
                new LatLng(45.515941, 9.212617)));
        uniBuildings.add(new Building(
                "U14", "Viale Sarca 336, Milano",
                new LatLng(45.523720, 9.219518)));
        uniBuildings.add(new Building(
                "U16", "Via Thomas Mann 8, Milano",
                new LatLng(45.524392, 9.209521)));
        uniBuildings.add(new Building(
                "U17", "Piazzetta Difesa per le donne, Milano",
                new LatLng(45.516871, 9.212853)));
        uniBuildings.add(new Building(
                "U24", "Viale Sarca 336, Milano",
                new LatLng(45.523543, 9.220714)));
        uniBuildings.add(new Building(
                "U26", "Via Thomas Mann 8, Milano",
                new LatLng(45.525375, 9.209559)));
        uniBuildings.add(new Building(
                "U36", "Viale Sarca 232, Milano",
                new LatLng(45.522483, 9.215099)));
    }

    public List<Building> getBuildings() {
        return uniBuildings;
    }

    public Building getBuilding(String name){
        for (Building b : uniBuildings)
            if (b.getName().equals(name))
                return b;

        return null;
    }

}
