package sorokinuladzimir.com.homebarassistant.net;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import sorokinuladzimir.com.homebarassistant.net.entity.Preparation;


public interface AbsolutDrinksApi {

    @GET("/drinks/{conditions}")
    Call<AbsolutDrinksResult> getAllMatchedDrinks(
            @Path("conditions") String conditions,
            @Query("start") int start,
            @Query("pageSize") int pageSize
    );

    @GET("quickSearch/drinks/{searchString}")
    Call<AbsolutDrinksResult> searchDrinks(
            @Path("searchString") String searchString,
            @Query("start") int start,
            @Query("pageSize") int pageSize);

    @GET("/drinks/{drinkId}/howtomix")
    Call<Preparation> getPreparationSteps(@Path("drinkId") String drinkId);

}

