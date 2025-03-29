package org.maplibre.navigation.android.example

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.graphhopper.GraphHopper
import com.graphhopper.ResponsePath
import com.graphhopper.util.shapes.GHPoint
import com.graphhopper.config.Profile
import org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.style.layers.PropertyFactory
import java.util.Locale


class MockNavigationActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private var mapLibreMap: MapLibreMap? = null
    private var hopper: GraphHopper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mock_navigation)

        // MapView 초기화
        mapView = findViewById<MapView>(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        // Intent로 데이터 수신
        val intent = intent
        val start = intent.getStringExtra("start")
        val destination = intent.getStringExtra("destination")
        val distance = intent.getStringExtra("distance")!!.toDouble()

        // GraphHopper 초기화 (OSM 파일 로드)
        initGraphHopper()

        // 경로 탐색 및 지도 업데이트
        mapView.getMapAsync(OnMapReadyCallback { map: MapLibreMap? ->
            mapLibreMap = map
            mapLibreMap!!.setStyle("https://demotiles.maplibre.org/style.json") { style: Style? ->
                if (hopper != null) {
                    val startPoint = parsePoint(start)
                    val endPoint = parsePoint(destination)
                    val path =
                        RoutingHelper.routingWithDesiredDistance(
                            hopper,
                            distance,
                            startPoint,
                            endPoint
                        )

                    if (path != null) {
                        updateMapWithRoute(path)
                    } else {
                        showErrorToast("경로를 찾을 수 없습니다.")
                    }
                }
            }
        })
    }

    private fun initGraphHopper() {
        Thread {
            hopper = GraphHopper()
                .setOSMFile("korea.osm.pbf") // OSM 파일 경로 설정
                .setGraphHopperLocation("$filesDir/graphhopper")
                .setProfiles(Profile("foot").setVehicle("foot"))
                .importOrLoad()
        }.start()
    }

    private fun parsePoint(input: String?): GHPoint {
        val parts = input!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val lat = parts[0].trim { it <= ' ' }.toDouble()
        val lon = parts[1].trim { it <= ' ' }.toDouble()
        return GHPoint(lat, lon)
    }

    private fun updateMapWithRoute(path: ResponsePath) {
        val routePoints: MutableList<LatLng> = ArrayList()
        for (point in path.points) {
            routePoints.add(LatLng(point.getLat(), point.getLon()))
        }

        val routeSource = GeoJsonSource("route-source", createGeoJson(routePoints))
        mapLibreMap!!.style!!.addSource(routeSource)

        val routeLayer = LineLayer("route-layer", "route-source").apply {
            setProperties(
                PropertyFactory.lineColor(Color.BLUE),
                PropertyFactory.lineWidth(5f)
            )
        }

        mapLibreMap!!.style!!.addLayer(routeLayer)

        // 카메라 이동
        val firstPoint = routePoints[0]
        mapLibreMap!!.animateCamera(newLatLngZoom(firstPoint, 14.0))
    }

    private fun createGeoJson(points: List<LatLng>): String {
        val geoJsonBuilder =
            StringBuilder("{\"type\":\"FeatureCollection\",\"features\":[{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[")

        for (i in points.indices) {
            val point = points[i]
            geoJsonBuilder.append(
                String.format(
                    Locale.US,
                    "[%f,%f]",
                    point.longitude,
                    point.latitude
                )
            )
            if (i < points.size - 1) geoJsonBuilder.append(",")
        }

        geoJsonBuilder.append("]}}]}")

        return geoJsonBuilder.toString()
    }

    private fun showErrorToast(message: String) {
        runOnUiThread {
            Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView!!.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView!!.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView!!.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView!!.onDestroy()
    }
}

