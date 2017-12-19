package it.communikein.myunimib.utilities;

import it.communikein.myunimib.data.type.Building;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;


@SuppressWarnings("unused")
public class UniversityUtils {

    public static HashMap<String, Building> uniBuildings;

    public static HashMap<String, Building> createBuildings(){
        if (uniBuildings == null || uniBuildings.size() == 0){
            uniBuildings = new HashMap<>();
            uniBuildings.put("U1", new Building("U1", new LatLng(45.513277, 9.211733)));
            uniBuildings.put("U2", new Building("U2", new LatLng(45.513528, 9.210687)));
            uniBuildings.put("U3", new Building("U3", new LatLng(45.513757, 9.212024)));
            uniBuildings.put("U4", new Building("U4", new LatLng(45.514029, 9.210919)));
            uniBuildings.put("U5", new Building("U5", new LatLng(45.512114, 9.212650)));
            uniBuildings.put("U6", new Building("U6", new LatLng(45.518246, 9.213916)));
            uniBuildings.put("U7", new Building("U7", new LatLng(45.516980, 9.213160)));
            uniBuildings.put("U8", new Building("U8", new LatLng(45.603866, 9.258856)));
            uniBuildings.put("U9", new Building("U9", new LatLng(45.511139, 9.211187)));
            uniBuildings.put("U11", new Building("U11", new LatLng(45.510003, 9.210804)));
            uniBuildings.put("U12", new Building("U12", new LatLng(45.515941, 9.212617)));
            uniBuildings.put("U14", new Building("U14", new LatLng(45.523720, 9.219518)));
            uniBuildings.put("U16", new Building("U16", new LatLng(45.524392, 9.209521)));
            uniBuildings.put("U17", new Building("U17", new LatLng(45.516871, 9.212853)));
            uniBuildings.put("U24", new Building("U24", new LatLng(45.523543, 9.220714)));
            uniBuildings.put("U26", new Building("U26", new LatLng(45.525375, 9.209559)));
            uniBuildings.put("U36", new Building("U36", new LatLng(45.522483, 9.215099)));
        }

        return uniBuildings;
    }

    public static boolean setupBuildings(){
        if (uniBuildings != null) {
            for (Building b : uniBuildings.values())
                b.setup();

            return true;
        }

        return false;
    }

    public static LatLng getLatLongBuilding(String buildingName){
        if (uniBuildings != null && uniBuildings.containsKey(buildingName))
            return uniBuildings.get(buildingName).getCoordinates();

        return new LatLng(0,0);
    }

}
