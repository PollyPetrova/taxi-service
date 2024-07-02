package org.example.taxiservice.utils;

public class GeoUtils {
    private static final double EARTH_RADIUS = 6371.0;

    public static double[] getBoundingBox(double centerLatitude, double centerLongitude, double radiusKm) {
        double minLat = centerLatitude - Math.toDegrees(radiusKm / GeoUtils.EARTH_RADIUS);
        double maxLat = centerLatitude + Math.toDegrees(radiusKm / GeoUtils.EARTH_RADIUS);
        double minLon = centerLongitude - Math.toDegrees(radiusKm / (GeoUtils.EARTH_RADIUS * Math.cos(Math.toRadians(centerLatitude))));
        double maxLon = centerLongitude + Math.toDegrees(radiusKm / (GeoUtils.EARTH_RADIUS * Math.cos(Math.toRadians(centerLatitude))));

        return new double[]{minLat, maxLat, minLon, maxLon};
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371.0 * c;
    }

}
