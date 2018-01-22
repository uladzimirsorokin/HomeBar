
package sorokinuladzimir.com.homebarassistant.net;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import sorokinuladzimir.com.homebarassistant.Constants;


public interface AbsolutDrinksApi {

   String ROOT = Constants.Uri.ABSOLUT_DRINKS_ROOT;

   @GET("/drinks/{conditions}")
   Call<AbsolutDrinksResult> getAllMatchedDrinks(
           @Path("conditions") String conditions
   );

}

