package `in`.divu.shoppinglistapp

import com.google.android.gms.common.api.internal.ApiKey
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoCodingApiService {

    @GET("maps/api/geocode/json")

    suspend fun getAddressFromCoordinates(
        @Query("latlng") latlng :String,
        @Query("key") apiKey :String
    ):GeoCodingResponse

}