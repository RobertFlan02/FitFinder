package vinigarstudios.fitfinder.shop;

import android.util.Log;
import java.util.List;
import vinigarstudios.fitfinder.clothes.Clothes;

public class Shop {
    private String image;
    private String shopName;
    private String shopDescription;

    private List<Clothes> clothesList;

    /**
     * Generate a shop with an image, name and description.
     * @param image The image of the shop.
     * @param shopName The shops name.
     * @param description The description of the shop.
     */
    public Shop(String image, String shopName, String description)
    {
        this.image = image;
        this.shopName = shopName;
        this.shopDescription = description;
    }

    public boolean TryAddClothes(Clothes clothes)
    {
        try
        {
         if (!this.clothesList.contains(clothes))
         {
             this.clothesList.add(clothes);
             return true;
         }
         else
         {
             Log.i(shopName + "clothesAlreadyAdded: " + clothes.getName(), "Clothes with name: " + clothes.getName() + " is already added to " + this.shopName + ".");
             return false;
         }
        }
        catch(Exception e)
        {
            Log.e(shopName + " TryAddClothesError","An error occurred with shop: " + this.shopName + ".\n Error: " + e.getMessage());
            return false;
        }
    }

    public boolean TryRemoveClothes(Clothes clothes)
    {
        try
        {
            if (this.clothesList.contains(clothes))
            {
                this.clothesList.remove(clothes);
                return true;
            }
            else
            {
                Log.i(shopName + "clothesNotInShop: " + clothes.getName(), "Clothes with name: " + clothes.getName() + " cannot be removed from " + this.shopName + ". As it is not in the clothesList.");
                return false;
            }
        }
        catch(Exception e)
        {
            Log.e(shopName + " TryRemoveClothesError","An error occurred with shop: " + this.shopName + ".\n Error: " + e.getMessage());
            return false;
        }
    }
    public boolean TryRemoveClothes(Clothes clothes)
    {
        try
        {
            if (this.clothesList.contains(clothes))
            {
                this.clothesList.remove(clothes);
                return true;
            }
            else
            {
                Log.i(shopName + "clothesNotInShop: " + clothes.getName(), "Clothes with name: " + clothes.getName() + " cannot be removed from " + this.shopName + ". As it is not in the clothesList.");
                return false;
            }
        }
        catch(Exception e)
        {
            Log.e(shopName + " TryRemoveClothesError","An error occurred with shop: " + this.shopName + ".\n Error: " + e.getMessage());
            return false;
        }
    }
}
