package vinigarstudios.fitfinder.clothes;

@Deprecated
public class Clothes {

    private String image;
    private String name;
    private String description;


    /**
     * Generate clothes with an image and name. The description will be a default description.
     *
     * @param image The image of the piece of clothing.
     * @param name The name of the piece of clothing.
     */
    public Clothes(String image, String name) {
        this.image = image;
        this.name = name;
        this.description = "";
    }

    /**
     * Generate clothes with an image, name and description.
     *
     * @param image The image of the piece of clothing.
     * @param name The name of the piece of clothing.
     * @param description The description of the piece of clothing.
     */
    public Clothes(String image, String name, String description) {
        this.image = image;
        this.name = name;
        this.description = description;
    }

    public String getName()
    {
        return this.name;
    }


}
