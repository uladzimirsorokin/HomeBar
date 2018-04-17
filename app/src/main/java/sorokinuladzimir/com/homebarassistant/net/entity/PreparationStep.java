package sorokinuladzimir.com.homebarassistant.net.entity;


import com.google.gson.annotations.SerializedName;

/*  {
          "text": "[Добавьте|Action|add] 2 чайные ложки [малиновое варенье|Ingredient|raspberry-jam].",
          "description": "Сладкая масса густой консистенции, сваренная из малины и сахара. Доступно в большинстве продуктовых магазинов.\r\n",
          "imagePath": "tools",
          "imageName": "boston-shaker",
          "action": "add",
          "quantity": 2.0,
          "measurement": "Чайные ложки",
          "ingredient": "Малиновое варенье",
          "ingredientType": "BaseSpirit",
          "container": "boston-shaker",
          "containerType": "Tool",
          "centilitres": 4.0,
          "textPlain": "Добавьте 2 чайные ложки малиновое варенье."
          }
*/
public class PreparationStep {

    @SerializedName("description")
    private String ingredientDescription;

    private String imagePath;

    private String imageName;

    @SerializedName("quantity")
    private float amount;

    @SerializedName("centilitres")
    private float centilitresAmount;

    @SerializedName("measurement")
    private String unit;

    @SerializedName("ingredient")
    private String ingredientName;

    @SerializedName("textPlain")
    private String preparationStepText;

    public String getIngredientDescription() {
        return ingredientDescription;
    }

    public void setIngredientDescription(String ingredientDescription) {
        this.ingredientDescription = ingredientDescription;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getCentilitresAmount() {
        return centilitresAmount;
    }

    public void setCentilitresAmount(float centilitresAmount) {
        this.centilitresAmount = centilitresAmount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getPreparationStepText() {
        return preparationStepText;
    }

    public void setPreparationStepText(String preparationStepText) {
        this.preparationStepText = preparationStepText;
    }
}
