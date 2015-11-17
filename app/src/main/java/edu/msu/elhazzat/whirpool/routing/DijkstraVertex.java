package edu.msu.elhazzat.whirpool.routing;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Stephanie on 10/13/2015.
 */
public class DijkstraVertex {

    final private String id;
    final private String name;
    public LatLng coords;


    public DijkstraVertex(String id, String name, LatLng coords) {
        this.id = id;
        this.name = name;
        this.coords = coords;
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DijkstraVertex other = (DijkstraVertex) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}
