package ru.nsu.ccfit.sukhinsky

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*
import kotlinx.serialization.SerialName

@Serializable
data class PlaceDescription(
    val bbox: Bbox = Bbox(0.0,0.0,0.0,0.0),
    val image: String = "",
    val info: Info = Info(),
    val kinds: String = "",
    val name: String = "",
    val osm: String = "",
    val otm: String = "",
    val point: Point = Point(),
    val rate: String = "",
    val sources: Sources = Sources(LinkedList<String>(),""),
    val wikidata: String = "",
    val dist: Double = 0.0,
    val wikipedia: String = "",
    val xid: String = ""
)

@Serializable
data class Bbox(
    val lat_max: Double = 0.0,
    val lat_min: Double = 0.0,
    val lon_max: Double = 0.0,
    val lon_min: Double = 0.0
)

@Serializable
data class Info(
    val descr: String = "",
    val image: String = "",
    val img_height: Int = 0,
    val img_width: Int = 0,
    val src: String = "",
    val src_id: Int = 0
)

@Serializable
data class Sources(
    val attributes: List<String> = LinkedList<String>(),
    val geometry: String = ""
)

@Serializable
data class InterestingPlace(
    @SerialName("dist")
    val dist: Double = 0.0,
    @SerialName("kinds")
    val kinds: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("osm")
    val osm: String = "",
    @SerialName("point")
    val point: Point = Point(),
    @SerialName("rate")
    val rate: Int = 0,
    @SerialName("wikidata")
    val wikidata: String = "",
    @SerialName("xid")
    val xid: String = ""
)

@Serializable
data class Point(
    @SerialName("lat")
    val lat: Double = 0.0,
    @SerialName("lng")
    val lng: Double = 0.0
)

@Serializable
data class Weather(
    val base: String = "",
    val clouds: Clouds = Clouds(0),
    val cod: Int = 0,
    val coord: Coord = Coord(0.0, 0.0),
    val dt: Int = 0,
    val id: Int = 0,
    val main: Main = Main(0.0, 0, 0, 0, 0, 0.0, 0.0, 0.0),
    val name: String = "",
    val rain: Rain = Rain(0.0, 0.0),
    val sys: Sys = Sys("", 0, 0, 0, 0, ""),
    val timezone: Int = 0,
    val visibility: Int = 0,
    val weather: List<WeatherX> = LinkedList<WeatherX>(),
    val wind: Wind = Wind(0, 0.0, 0.0),
    val snow: Snow = Snow(0.0, 0.0)
)

@Serializable
data class Clouds(
    val all: Int = 0
)

@Serializable
data class Coord(
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

@Serializable
data class Snow(
    val `1h`: Double = 0.0,
    val `3h`: Double = 0.0
)

@Serializable
data class Main(
    val feels_like: Double = 0.0,
    val grnd_level: Int = 0,
    val humidity: Int = 0,
    val pressure: Int = 0,
    val sea_level: Int = 0,
    val temp: Double = 0.0,
    val temp_max: Double = 0.0,
    val temp_min: Double = 0.0
)

@Serializable
data class Rain(
    val `1h`: Double = 0.0,
    val `3h`: Double = 0.0
)

@Serializable
data class Sys(
    val country: String = "",
    val id: Int = 0,
    val sunrise: Int = 0,
    val sunset: Int = 0,
    val type: Int = 0,
    val message: String = ""
)

@Serializable
data class WeatherX(
    val description: String = "",
    val icon: String = "",
    val id: Int = 0,
    val main: String = ""
)

@Serializable
data class Wind(
    val deg: Int = 0,
    val gust: Double = 0.0,
    val speed: Double = 0.0
)

@Serializable
data class Hit(
    val city: String = "",
    val country: String = "",
    val countrycode: String = "",
    val extent: List<Double> = LinkedList<Double>(),
    val house_number: String = "",
    val housenumber: String = "",
    val name: String = "",
    val osm_id: Long = 0,
    val osm_key: String = "",
    val osm_type: String = "",
    val osm_value: String = "",
    val point: Point = Point(0.0, 0.0),
    val postcode: String = "",
    val state: String = "",
    val street: String = ""
)

@Serializable
data class Geopoints(
    val hits: List<Hit> = LinkedList<Hit>(),
    val locale: String = ""
)

suspend fun main() {
    val reader = Scanner(System.`in`)

    val geopositionRequest: Deferred<Geopoints>
    val weatherRequest: Deferred<Weather>
    val interestingPlacesRequest: Deferred<List<InterestingPlace>>
    var descriptionPlacesRequest : List<Pair<InterestingPlace, Deferred<PlaceDescription>>>

    print("Enter the name of the place: ")
    val enteredPlaceName:String = reader.nextLine()

    val client = HttpClient(CIO)    {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }
    }

    runBlocking {
        geopositionRequest = async {
            client.get("https://graphhopper.com/api/1/geocode") {
                url {
                    parameters.append("key", "0eeaba33-9fb5-4860-bfd5-6af4c6676038")
                    parameters.append("q", enteredPlaceName)
                    parameters.append("locale", "ru")
                    parameters.append("limit", "20")
                }
            }.body()
        }
    }

    val variantsOfEnteredPlace : Geopoints = geopositionRequest.await()
    variantsOfEnteredPlace.hits.forEachIndexed { ind: Int, elem ->
        println("$ind: ${elem.name}, ${elem.country}, ${elem.state}, ${elem.city}, ${elem.street}")
    }

    print("Choose one variant: ")
    val userPlaceChoiceInd:Int = reader.nextInt()
    val userPlaceChoice:Hit = variantsOfEnteredPlace.hits[userPlaceChoiceInd]

    println("${userPlaceChoice.name}, ${userPlaceChoice.country}, ${userPlaceChoice.state}, " +
            "${userPlaceChoice.city}, ${userPlaceChoice.street}")

    runBlocking {
        weatherRequest = async {
            client.get("http://api.openweathermap.org/data/2.5/weather") {
                url {
                    parameters.append("appid", "92a7ee2fa20f5ddcf18beb4d1d6bf118")
                    parameters.append("units", "metric")
                    parameters.append("lang", "ru")
                    parameters.append("lat", userPlaceChoice.point.lat.toString())
                    parameters.append("lon", userPlaceChoice.point.lng.toString())
                }
            }.body()
        }

        interestingPlacesRequest = async {
            client.get("https://api.opentripmap.com/0.1/ru/places/radius") {
                url {
                    parameters.append("apikey", "5ae2e3f221c38a28845f05b61d8383bf6da06b5a9369d71ec66251b9")
                    parameters.append("radius", "2000")
                    parameters.append("lat", userPlaceChoice.point.lat.toString())
                    parameters.append("lon", userPlaceChoice.point.lng.toString())
                    parameters.append("format", "json")
                }
            }.body()
        }

        descriptionPlacesRequest = select {
            interestingPlacesRequest.onAwait { places ->
                var placeDescription: Deferred<PlaceDescription>
                val placeDescriptionList: LinkedList<Pair<InterestingPlace, Deferred<PlaceDescription>>> = LinkedList()
                places.forEach { place: InterestingPlace ->
                    placeDescription = async {
                        client.get("https://api.opentripmap.com/0.1/ru/places/xid/" + place.xid) {
                            url {
                                parameters.append("apikey", "5ae2e3f221c38a28845f05b61d8383bf6da06b5a9369d71ec66251b9")
                            }
                        }.body()
                    }
                    placeDescriptionList.addLast(Pair(place, placeDescription))
                }
                return@onAwait placeDescriptionList
            }
        }
    }


    val weather : Weather = weatherRequest.await()
    println("\n ///////////////////// WEATHER ///////////////////")
    println("Temperature: ${weather.main.temp}°C")
    println("Feels like: ${weather.main.feels_like}°C")
    print("State: ")
    weather.weather.forEach {
        println("${it.description}; ")
    }
    println("Pressure: ${weather.main.pressure * 0.75} mmHg")
    println("Humidity: ${weather.main.humidity}%")

    println("\n /////////////////////// INTERESTING PLACES NEARBY ////////////////////")

    runBlocking {
        descriptionPlacesRequest.forEach { pair ->
            launch {
                val place = pair.first
                val placeLine = place.name + ": " + place.kinds
                val descr = pair.second.await()
                val descrLine = descr.info.descr
                var outLine = placeLine
                if (descrLine.isNotEmpty()) {
                    outLine += ". Descr: $descrLine"
                }
                println(outLine)
            }
        }
    }

    client.close()
}