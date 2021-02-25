import java.util.Random;
import java.util.function.Predicate;

public class Overview {

    public static void example(){
        // Short overview of the different function in the Bitmap class
        // - - -
        // Setting up a new bitmap and specifying width and height | The full filepath will be visible in the terminal
        Bitmap bitmap = new Bitmap(800, 800);
        // Saving the colors subclass as a own var to make it easier accessible
        Bitmap.Colors colors = bitmap.colors;
        // Filling the whole window with a single color
        bitmap.fillWin(colors.white());
        // Filling a Area with a single color
        bitmap.fillArea(100, 100, 700, 700, colors.white()); //colors.merge(colors.green(), colors.blue(), "gradientV=100-700")
        // Creating a custom color
        Bitmap.Color my_color = bitmap.new Color(30, 20, 100, 0.7);

        // Creating a gradient (long way). From and to can be freely selected, but from must be smaller than to. This can be used to select only parts of a gradient, or to limit the gradient to a specified area.
        Bitmap.Pattern my_gradient = bitmap.new Pattern(colors.green(), colors.blue(), "gradient",false, 100, 700);
        // Above way to create a gradient is pretty long, but there is also the option to use a fast_pattern instead
        // First we select the type of the pattern, V indicates that gradient is vertical, the last values are used to declare the width of the Gradient
        String fast_pattern = "gradientV=100-700";
        // By using this simple fast_pattern we can now declare a new pattern with colors.merge()
        Bitmap.Pattern my_faster_gradient = colors.merge(colors.green(), colors.blue(), fast_pattern);
        // Adding a border with a specified thickness and the gradient as color
        bitmap.border(100, 100, 700, 700, 4, my_faster_gradient);
        // Note: Borders will always be drawn outside the specified area, which means that you need to consider the remaining space when adding a outline

        // Patterns can also be stacked. In this example we merge a green-blue gradient texture with a white colored vertical stripe texture
        bitmap.fillArea(200, 200, 600, 600, colors.merge(colors.white() , colors.merge(colors.green(), colors.blue(), "gradientV=200-600"), "stripesV"));

        // Different predefined shapes are available through the shapes subclass
        // Here, a circle with a radius of 30 at the center of the screen is created
        bitmap.shapes.circle(bitmap.canvas_width /2, bitmap.canvas_height /2, 30,
                // It's also possible to diffuse between to gradients | Body color
                colors.merge(colors.merge(colors.green(),  colors.blue(), "gradientV=370-430"), colors.merge(colors.green(),  colors.white(), "gradientV=370-430"), "gradientV=370-430"),
                // The alpha can only darken the color. To brighten a color one can use colors.mix with a bright color like white | Outline color
                colors.merge(colors.mix(colors.green(), colors.white(), 0.5), colors.white(), "gradientH=370-430"));
        // Adding a cross
        bitmap.shapes.cross(400, 400, 100, 6,
                colors.merge(
                        colors.black(),
                        colors.merge(colors.blue(), colors.white(), "gradientV=350-450"),
                        "checkerboard>" // The ">" is used to shift the checkerboard texture by one unit to the right
                ),  // Remember: Patterns can be endlessly stacked
                bitmap.new Outline(3)
        );
        // Render must be called by the user to prevent unnecessary code executions, it's essentially saving the object to the file
        bitmap.render();

        // To view ppm files without compression and instantly refresh them on change, I recommend https://imageglass.org/
    }

    public static void more_examples() {
        Bitmap bitmap = new Bitmap(800, 800);
        Bitmap.Colors colors = bitmap.new Colors();
        // Filling the whole bitmap with one gradient
        // It's also possible to set the gradient to auto.
        bitmap.fillArea(1, 1, 800, 800, colors.merge(colors.green(), colors.blue(), "gradientH=auto")); bitmap.render();
        render_delayed(bitmap);
        // Adding together four different patterns (Scroll to the left and you can notice that blue has the same quantity as white and green added together)
        bitmap.fillArea(1, 1, 800, 800, colors.merge(colors.merge(colors.green(), colors.white(), "stripesV>"), colors.merge(colors.blue(), colors.white(), "gradientV=1-800"), "checkerboard")); bitmap.render();
        render_delayed(bitmap);
        // Many circles
        bitmap.fillArea(2, 2, 799, 799, colors.merge(colors.green(), colors.white(), "gradientV=2-799"));
        Random random = new Random();
        for (int i = 60; i < 760; i+= 2) {
            int x = Math.max(random.nextInt(760), 40); int y = Math.max(random.nextInt(760), 40);
            int radius = 40 - random.nextInt(30);
            boolean hor = x%2 == 0;

            bitmap.shapes.circle(
                    x, y, radius,
                    colors.merge(colors.random(true), colors.random(true),
                            String.format("gradient%s=%s-%s", hor ? 'H' : 'V', 350, 450))
            );
        }
        render_delayed(bitmap);

        bitmap.fillArea(1, 1, 800, 800, colors.merge(colors.merge(colors.blue(), colors.white(), "gradientH=1-800"), colors.black(), "superhugegrid"));
        render_delayed(bitmap);
        // Combining patterns can produce cool results
        bitmap.fillArea(100, 100, 700, 700, colors.merge(colors.merge(colors.white(), colors.black(), "checkerboard"), colors.black(), "hugegrid"));
        render_delayed(bitmap);
        // Cells like pattern
        bitmap.shapes.circle(400, 400, 200, colors.merge(colors.merge(colors.white(), colors.light_blue(), "flowergrid"), colors.black(), "biggrid"),
                colors.white());
        render_delayed(bitmap);
        // A Custom function
        // The Predicate is used to determine which slot of the pattern should be executed. When the return is true the first slot will be executed, else the second slot.
        Predicate<int[]> custom_fun = arr -> arr[0] * arr[1] % 16 == 0;
        bitmap.shapes.cross(400, 400, 200, 10,
                colors.opacity(colors.merge( colors.merge(colors.light_blue(), colors.white(), "gradientV=300-500"), colors.black(), custom_fun)),
                bitmap.new Outline(2, colors.merge(colors.green(), colors.blue(), "gradientV=300-500"))
        );

        // render
        render_delayed(bitmap);
    }

    private static void render_delayed(Bitmap bitmap, int ms) {
        try {
            Thread.sleep(ms);
            bitmap.render();
        } catch (InterruptedException ignored) { }
    }
    private static void render_delayed(Bitmap bitmap) { render_delayed(bitmap, 500); }

}
