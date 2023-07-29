import java.awt.Image;
import java.sql.Connection;

public interface MainInterface {
    Connection dbConnect();
    void init();
    void close();
    void DisplayUpdatedMealInfoTbl();
    void getValue();
    void getValueImage();
    void signOut();
    void displayMealInfo();
    void searchByFoodName();
    void searchByFoodGroup();
    void searchByTimeOfTheDay();
    void searchByID();
    void searchByDrinks();
    void searchByDate();
    void searchFilterByEverything();
    void clearInfo();
    void addDiary();
    void updateDiary();
    void deleteDiary();
    void filterByFoodGroup();
    void imageSave();
    void imageUpload();
    Image scaledImage(byte[] img, int w, int h) ;
}
