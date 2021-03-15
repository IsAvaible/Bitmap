import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

public class Overview {

    /**
     * Short overview of the different functions in the Bitmap class <br><br>
     * Will create this image: <br>
     * <img src="../doc-files/default.png" width="250" height="250" alt="/doc-files/default.png">
     */
    public static void example(){
        // Setting up a new bitmap and specifying width and height | The full filepath will be visible in the terminal
        Bitmap bitmap = new Bitmap(800, 800);
        // Saving the colors subclass as a own var to make it easier accessible
        Bitmap.Colors colors = bitmap.colors;
        // Filling the whole window with a single color_provider
        bitmap.fillWin(colors.white());
        // Filling a Area with a single color_provider
        bitmap.fillArea(100, 100, 700, 700, colors.white());
        // Creating a custom color_provider
        Bitmap.Color my_color = bitmap.new Color(30, 20, 100, 0.7);

        // Creating a gradient (long way). From and to can be freely selected, but from must be smaller than to. This can be used to select only parts of a gradient, or to limit the gradient to a specified area.
        Bitmap.Pattern my_gradient = bitmap.new Pattern(colors.green(), colors.blue(), "gradient",false, 100, 700);
        // Above way to create a gradient is pretty long, but there is also the option to use a fast_pattern instead
        // First we select the type of the pattern, V indicates that gradient is vertical, the last values are used to declare the width of the Gradient (can also be set to auto)
        String fast_pattern = "gradientV=100-700";
        // By using this simple fast_pattern we can now declare a new pattern with colors.merge() or bitmap.new Pattern()
        Bitmap.Pattern my_faster_gradient = colors.merge(colors.green(), colors.blue(), fast_pattern);
        // Adding a border with a specified thickness and the gradient as color_provider
        bitmap.border(100, 100, 700, 700, 4, my_faster_gradient);
        // Note: Borders will always be drawn outside the specified area, which means that you need to consider the remaining space when adding a outline

        // Patterns can also be stacked. In this example we merge a green-blue gradient texture with a white colored vertical stripe texture
        bitmap.fillArea(
                200, 200, 600, 600,
                colors.merge(colors.white() , colors.merge(colors.green(), colors.blue(), "gradientV=auto"), "stripesV")
        );

        // Different predefined shapes are available through the shapes subclass
        // Here, a circle with a radius of 30 at the center of the screen is created
        bitmap.shapes.circle(bitmap.canvas_width /2, bitmap.canvas_height /2, 30,
                // Body color_provider | It's also possible to diffuse between to gradients
                colors.merge(colors.merge(colors.green(),  colors.blue(), "gradientV=auto"), colors.merge(colors.green(),  colors.white(), "gradientV=auto"), "gradientV=auto"),
                // Outline color_provider | The alpha can only darken the color_provider. To brighten a color_provider one can use colors.mix with a bright color_provider like white
                bitmap.new Outline(1, colors.merge(colors.mix(colors.green(), colors.white(), 0.5), colors.white(), "gradientH=370-430")));
        // Adding a cross
        bitmap.shapes.cross(400, 400, 100, 6,
                colors.merge(
                        colors.black(),
                        colors.merge(colors.blue(), colors.white(), "gradientV=350-450"),
                        "checkerboard>" // The ">" is used to shift the checkerboard texture by one unit to the right
                ),  // Remember: Patterns can be endlessly stacked
                bitmap.new Outline(3)
        );

        // A custom function can give the option to create a custom pattern or represent a graph like
        // f(x) = (x - 400)^2 + 100 (Normal Parabola shifted in x- and y-axis.)
        // arg[0] = x, arg[1] = y, arg[2] = from, arg[3] = to
        // The Predicate is used to determine which slot of the pattern should be executed. When the return is true the first slot will be executed, else the second slot.
        Predicate<int[]> normal_parabola = arg -> {
            // If using only the normal_parabola_function and checking if its equal to y, the normal parabola will be
            // a dotted line (doc-files/dotted-parabola.png ). Therefore, we calculate the last and the
            // next y, to color_provider each pixel "between" the two.
            Function<Integer, Integer> normal_parabola_function = shift -> (int) ((Math.pow((arg[0]+shift - arg[2]), 2) * 0.1 + arg[3]) );
            //|........Function declaration.....................||.param.||........................(x - a)^2...........||comp. 0.1||..+c.||
            int this_y = normal_parabola_function.apply(0);
            int next_y = normal_parabola_function.apply(1);
            int last_y = normal_parabola_function.apply(-1);

            // 1. Check if the calculated y value is equal to our current y value (Try commenting out the second part and the line will be dotted)
            // 2. Check if the current y is between the next and the last y (positive values || negative values)
            return this_y == arg[1] || arg[1] > last_y && arg[1] < next_y || arg[1] < last_y && arg[1] > next_y;
        };

        Bitmap.Pattern normal_parabola_pattern = bitmap.new Pattern(colors.white(), colors.black(), normal_parabola);
        normal_parabola_pattern.from = 400; // This will be our shift on the x-axis in this example
        normal_parabola_pattern.to = 200; // And we'll use this as the shift on the y-axis
        // Uncomment below to show parabola (doc-files/parabola.png)
        // bitmap.fillArea(100, 100, 700, 700, normal_parabola_pattern);

        // Render must be called by the user to prevent unnecessary code executions, it's essentially saving the object to the file
        bitmap.render("bitmap.ppm");

        // To view .ppm files without compression and instantly refresh them on change, I recommend https://imageglass.org/
    }

    public static void how_to_create_patterns() {
        // Because I started this project with no concept whatsoever, my
        // initial approach of using overloads to create patterns has
        // become unmaintainable.
        // I now added a class PatternBuilder to manage optional parameters
        // more easily. In the following I will show all ways to create a pattern
        // up to this day.

        // The standard steps to initialize the new canvas
        Bitmap bitmap = new Bitmap(800, 800);
        Bitmap.Colors colors = bitmap.colors;
        // Storage for our example
        Bitmap.Pattern example_pattern;

        // First way: colors.merge()
        // This is essentially just a wrapper for fast patterns.
        // Whilst this is the fastest approach, it's not as maintainable,
        // because fast patterns aren't as read intuitive
        example_pattern = colors.merge(colors.blue(), colors.white(), "customV=auto");

        // Second (new) way: PatternBuilders
        example_pattern = bitmap.patternBuilders.gradient(colors.blue(), colors.white()).withVertical().build();

        // Third way: Interacting directly with the Pattern class (overload hell incoming)
        example_pattern = bitmap.new Pattern(colors.blue(), colors.white(),"gradient", false, 0, 0, true); // (Disgusting and not understandable without a good IDE)
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
                            String.format("gradient%s=auto", hor ? 'H' : 'V'))
            );
        }
        render_delayed(bitmap);

        bitmap.fillArea(1, 1, 800, 800, colors.merge(colors.merge(colors.blue(), colors.white(), "gradientH=1-800"), colors.black(), "superhugegrid"));
        render_delayed(bitmap);
        // Combining patterns can produce cool results
        bitmap.fillArea(100, 100, 700, 700, colors.merge(colors.merge(colors.white(), colors.black(), "checkerboard"), colors.black(), "hugegrid"));
        render_delayed(bitmap);
        // Cells like pattern
        bitmap.shapes.circle(400, 400, 200, colors.merge(colors.merge(colors.white(), colors.light_blue(), "flowergrid"), colors.black(), "biggrid"), colors.white());
        render_delayed(bitmap);
        // A Custom function
        Predicate<int[]> custom_fun = arr -> arr[0] * arr[1] % 16 == 0; // Body Color
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
