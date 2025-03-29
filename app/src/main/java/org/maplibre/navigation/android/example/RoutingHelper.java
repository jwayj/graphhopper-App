package org.maplibre.navigation.android.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import android.os.AsyncTask;

import com.graphhopper.ResponsePath;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.LMProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.json.Statement;
import static com.graphhopper.json.Statement.If;
import static com.graphhopper.json.Statement.Op.LIMIT;
import static com.graphhopper.json.Statement.Op.MULTIPLY;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.Snap;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.DistanceCalcEarth;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import com.graphhopper.util.Translation;
import com.graphhopper.util.details.PathDetail;
import com.graphhopper.util.shapes.GHPoint;

public class RoutingHelper {

    public static ResponsePath routingWithDesiredDistance(GraphHopper hopper, double desiredDistance, GHPoint start, GHPoint end) {
        double tolerance = 100; // 100미터 오차 허용
        double searchRadius = desiredDistance * 0.75; // 원하는 거리의 75%로 검색 반경 설정

        ResponsePath bestPath = null;
        double closestDifference = Double.MAX_VALUE;

        LocationIndex locationIndex = hopper.getLocationIndex();
        List<GHPoint> nearbyPoints = new ArrayList<>();

        DistanceCalc distCalc = DistanceCalcEarth.DIST_EARTH;

        // 여러 개의 가까운 지점을 찾기
        for (int i = 0; i < 10000; i++) { // 포인트 생성 수 증가
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * searchRadius;
            double lat = start.lat + (distance / 111000) * Math.cos(angle);
            double lon = start.lon + (distance / (111000 * Math.cos(Math.toRadians(start.lat)))) * Math.sin(angle);
            Snap qr = locationIndex.findClosest(lat, lon, EdgeFilter.ALL_EDGES);
            if (qr.isValid()) {
                GHPoint nearbyPoint = qr.getSnappedPoint();
                nearbyPoints.add(nearbyPoint);
            }
        }

        // 랜덤으로 지점 선택
        Collections.shuffle(nearbyPoints);

        // 각 랜덤 지점에 대해 경로 찾기
        for (GHPoint intermediatePoint : nearbyPoints) {
            ResponsePath path1 = findPath(hopper, start, intermediatePoint, "foot");
            ResponsePath path2 = findPath(hopper, intermediatePoint, end, "foot");


            if (path1 != null && path2 != null) {
                double totalDistance = path1.getDistance() + path2.getDistance();
                double difference = Math.abs(totalDistance - desiredDistance);

                if (difference < closestDifference) {
                    closestDifference = difference;
                    bestPath = combinePaths(path1, path2);

                    if (difference <= tolerance) {
                        return bestPath; // 충분히 가까운 경로를 찾았으면 즉시 반환
                    }
                }
            }
        }

        if (bestPath == null) {
            System.out.println("원하는 거리의 경로를 찾을 수 없습니다. 직접 연결 경로를 반환합니다.");
            return findPath(hopper, start, end, "foot");
        }

        System.out.println("가장 가까운 경로를 찾았습니다. 차이: " + closestDifference + " 미터");
        return bestPath;
    }

    private static ResponsePath findPath(GraphHopper hopper, GHPoint start, GHPoint end, String profile) {
        GHRequest req = new GHRequest(start, end)
                .setAlgorithm(Parameters.Algorithms.ASTAR_BI)
                .setProfile(profile);
        GHResponse rsp = hopper.route(req);
        if (rsp.hasErrors()) {
            return null;
        }
        return rsp.getBest();
    }
    public static ResponsePath combinePaths(ResponsePath path1, ResponsePath path2) {
        ResponsePath combined = new ResponsePath();
        PointList combinedPoints = new PointList();

        for (int i = 0; i < path1.getPoints().size(); i++) {
            combinedPoints.add(
                    path1.getPoints().getLat(i),
                    path1.getPoints().getLon(i)
            );
        }
        for (int i = 0; i < path2.getPoints().size(); i++) {
            combinedPoints.add(
                    path2.getPoints().getLat(i),
                    path2.getPoints().getLon(i)
            );
        }
        combined.setPoints(combinedPoints);
        combined.setDistance(path1.getDistance() + path2.getDistance());
        combined.setTime(path1.getTime() + path2.getTime());
        return combined;
    }

//    // AsyncTask 활용
//    private class RouteTask extends AsyncTask<Void, Void, ResponsePath> {
//        protected ResponsePath doInBackground(Void... params) {
//            return routingWithDesiredDistance(...);
//        }
//
//        protected void onPostExecute(ResponsePath result) {
//            // UI 업데이트
//        }
//    }
    // 좌표 유효성 검사
    private boolean isValidPoint(GHPoint point) {
        return point.lat >= -90 && point.lat <= 90
                && point.lon >= -180 && point.lon <= 180;
    }
}
